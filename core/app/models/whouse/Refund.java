package models.whouse;

import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.Reflects;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by licco on 2016/11/2.
 * 退货单
 */
@Entity
@DynamicUpdate
public class Refund extends GenericModel {

    private static final long serialVersionUID = 1504355529353731906L;

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    /**
     * 退货单元
     */
    @OneToMany(mappedBy = "refund", cascade = {CascadeType.PERSIST})
    public List<RefundUnit> unitList = new ArrayList<>();

    /**
     * 名称
     */
    @Required
    public String name;

    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public S status;

    public enum S {
        Create {
            @Override
            public String label() {
                return "已创建";
            }
        },
        Refund {
            @Override
            public String label() {
                return "已退货";
            }
        },
        Cancel {
            @Override
            public String label() {
                return "已取消";
            }
        };

        public abstract String label();
    }

    /**
     * 收货类型
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    public enum T {
        After_Receive {
            @Override
            public String label() {
                return "不良品退货";
            }
        },
        After_Inbound {
            @Override
            public String label() {
                return "入库后退货";
            }
        };

        public abstract String label();
    }

    /**
     * 供应商
     */
    @Required
    @OneToOne
    public Cooperator cooperator;

    /**
     * 备注
     */
    public String memo;

    /**
     * 创建时间
     */
    @Required
    public Date createDate;

    /**
     * 退货时间
     */
    @Required
    @Temporal(TemporalType.DATE)
    public Date refundDate;

    /**
     * 仓库交接人
     */
    @OneToOne
    @Expose
    public User whouseUser;

    /**
     * 制单人
     */
    @OneToOne
    public User creator;

    /**
     * 物流信息
     */
    public String info;

    /**
     * 是否B2B
     */
    @Transient
    public boolean isb2b = false;


    public enum InboundType {

        Good {
            @Override
            public String label() {
                return "良品入库";
            }
        },
        Rework {
            @Override
            public String label() {
                return "返工入库";
            }
        },
        SpecialMining {
            @Override
            public String label() {
                return "特采入库";
            }
        },
        Exchange {
            @Override
            public String label() {
                return "换货入库";
            }
        };

        public abstract String label();
    }


    public Refund() {
        this.status = S.Create;
        this.creator = Login.current();
        this.createDate = new Date();
        this.refundDate = new Date();
    }

    public Refund(ProcureUnit unit) {
        this.status = S.Create;
        this.creator = Login.current();
        this.createDate = new Date();
        this.refundDate = new Date();
        this.cooperator = unit.cooperator;
        this.type = (unit.stage == ProcureUnit.STAGE.IN_STORAGE ? T.After_Inbound : T.After_Receive);
    }

    public void createRefundByInbound(List<InboundUnit> list) {
        this.id = id();
        this.save();
        for(InboundUnit unit : list) {
            RefundUnit runit = new RefundUnit();
            runit.unit = unit.unit;
            runit.refund = this;
            runit.planQty = unit.qty;
            runit.qty = unit.qty;
            runit.mainBoxInfo = unit.mainBoxInfo;
            runit.lastBoxInfo = unit.lastBoxInfo;
            runit.save();
        }
    }

    public void createRefund(List<RefundUnit> list) {
        this.id = id();
        this.createDate = new Date();
        this.creator = Login.current();
        this.save();
        for(RefundUnit unit : list) {
            if(unit != null) {
                unit.unit = ProcureUnit.findById(unit.unitId);
                unit.refund = this;
                unit.save();
            }
        }
    }

    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = Refund.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate())
                + "";
        return String.format("PTT|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public static void confirmRefund(List<Refund> refunds) {
        for(Refund refund : refunds) {
            refund.status = S.Refund;
            refund.save();
            for(RefundUnit u : refund.unitList) {
                ProcureUnit unit = u.unit;
                if(refund.type == T.After_Inbound) {
                    unit.attrs.qty -= u.qty;
                    unit.inboundQty -= u.qty;
                    unit.availableQty -= u.qty;
                    if(unit.inboundQty == 0) {
                        unit.stage = ProcureUnit.STAGE.DELIVERY;
                    }
                    unit.save();
                    createStockRecord(u, StockRecord.T.Refund, "", unit.availableQty);
                } else {
                    unit.attrs.qty -= u.qty;
                    unit.unqualifiedQty -= u.qty;
                    createStockRecord(u, StockRecord.T.Unqualified_Refund, refund.memo, unit.availableQty);
                    new ERecordBuilder("refund.confirm").msgArgs(u.qty, refund.memo).fid(unit.id).save();
                    unit.save();
                }
            }
            if(refund.type == T.After_Receive) {
                Inbound.createTailInboundByUnQualifiedHandle(refund);
            }
        }
    }

    public static void validConfirmRefund(List<Refund> list) {
        if(list.size() == 0) {
            Validation.addError("", "无效退货单ID");
            return;
        }
        for(Refund refund : list) {
            if(refund.status != S.Create) {
                Validation.addError("", "退货单【" + refund.id + "】状态为【" + refund.status.label() + "】"
                        + "，请选择状态为【已创建】的退货单！");
            }
            for(RefundUnit u : refund.unitList) {
                ProcureUnit unit = u.unit;
                if(refund.type == T.After_Inbound) {
                    if(unit.outbound != null) {
                        Validation.addError("", "退货单【" + refund.id + "】下的采购计划【" + unit.id + "】在出库单"
                                + "【" + unit.outbound.id + "】中，请先处理！");
                    }
                } else {
                    if(u.qty > unit.unqualifiedQty) {
                        Validation.addError("", "退货单【" + refund.id + "】下的采购计划【" + unit.id + "】的退货数量大于不良品数量，请先修改！");
                    }
                }
            }
        }
    }

    public static void createStockRecord(RefundUnit unit, StockRecord.T type, String memo, int currQty) {
        StockRecord record = new StockRecord();
        record.creator = Login.current();
        record.whouse = unit.unit.whouse;
        record.unit = unit.unit;
        record.qty = unit.qty;
        record.type = type;
        record.recordId = unit.id;
        record.currQty = currQty;
        record.memo = memo;
        record.save();
    }

    /**
     * 采购计划对应的退货单是不是全部已退货
     *
     * @param id
     * @return
     */
    public static String isAllReufund(Long id) {
        List<Refund> refunds = Refund.find("SELECT DISTINCT r FROM Refund r LEFT JOIN r.unitList u "
                + "WHERE r.status <> ? AND u.unit.id = ? ", S.Refund, id).fetch();
        if(refunds.size() > 0) {
            return refunds.get(0).id;
        } else {
            return "";
        }
    }

    /**
     * 不良品退货
     *
     * @param units
     * @param memo
     */
    public static void unQualifiedHandle(List<ProcureUnit> units, String memo) {
        Refund refund = new Refund();
        refund.id = id();
        refund.memo = memo;
        refund.whouseUser = Login.current();
        refund.status = S.Create;
        refund.type = T.After_Receive;
        refund.creator = Login.current();
        refund.createDate = new Date();
        refund.refundDate = new Date();
        refund.memo = memo;
        refund.save();
        units.stream().filter(unit -> Optional.ofNullable(unit.id).isPresent()).forEach(unit -> {
            ProcureUnit pro = ProcureUnit.findById(unit.id);
            refund.cooperator = pro.cooperator;
            refund.name = String.format("%s_%s_不良品退货", pro.cooperator.name, LocalDate.now());
            RefundUnit u = new RefundUnit();
            u.unit = unit;
            u.refund = refund;
            u.planQty = unit.attrs.qty;
            u.qty = unit.attrs.qty;
            u.save();
            refund.unitList.add(u);
        });
        refund.save();

    }

    /**
     * 不良品转入
     *
     * @param unitId
     * @param qty
     * @param memo
     */
    public static void transferQty(Long unitId, int qty, String memo, String type) {
        ProcureUnit unit = ProcureUnit.findById(unitId);
        if(unit.stage == ProcureUnit.STAGE.DELIVERY) {
            unit.stage = ProcureUnit.STAGE.IN_STORAGE;
        }
        unit.inboundQty += qty;
        unit.unqualifiedQty -= qty;
        unit.availableQty += qty;
        unit.result = InboundUnit.R.Qualified;
        unit.save();
        Inbound.createTransferInbound(unit, qty, memo, Refund.InboundType.valueOf(type));
        /**异动记录**/
        StockRecord record = new StockRecord();
        if(StringUtils.isNotBlank(type))
            record.inboundType = Refund.InboundType.valueOf(type);
        record.creator = Login.current();
        record.whouse = unit.whouse;
        record.unit = unit;
        record.qty = qty;
        record.type = StockRecord.T.Unqualified_Transfer;
        record.recordId = unit.id;
        record.memo = memo;
        record.currQty = unit.availableQty;
        record.save();
        new ERecordBuilder("refund.transfer").msgArgs(record.qty, record.memo).fid(unitId).save();
    }

    public void saveAndLog(Refund refund) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "name", refund.name));
        logs.addAll(Reflects.logFieldFade(this, "refundDate", refund.refundDate));
        if(refund.whouseUser != null)
            logs.addAll(Reflects.logFieldFade(this, "whouseUser.id", refund.whouseUser.id));
        logs.addAll(Reflects.logFieldFade(this, "memo", refund.memo));
        if(logs.size() > 0) {
            new ERecordBuilder("refund.update").msgArgs(this.id, StringUtils.join(logs, "<br>")).fid(this.id)
                    .save();
        }
        this.save();
    }

    public static void updateBoxInfo(List<RefundUnit> units) {
        units.forEach(unit -> {
            RefundUnit old = RefundUnit.findById(unit.id);
            if(old.refund.status == S.Create) {
                if(unit.mainBox.boxNum * unit.mainBox.num + unit.lastBox.boxNum * unit.lastBox.num > old.planQty) {
                    Validation.addError("", "包装信息超过计划退货数");
                    return;
                } else {
                    old.qty = unit.mainBox.boxNum * unit.mainBox.num + unit.lastBox.boxNum * unit.lastBox.num;
                }
            }
            unit.marshalBoxs(old);
            old.save();
        });
    }

    public void quickAddByEdit(Long unitId) {
        if(RefundUnit.count("refund.id=? AND unit.id=?", this.id, unitId) > 0) {
            Validation.addError("", "采购计划" + unitId + "已经存在当前退货单中");
        }
        if(Validation.hasErrors()) return;
        ProcureUnit unit = ProcureUnit.findById(unitId);
        RefundUnit refundUnit = new RefundUnit();
        refundUnit.refund = this;
        refundUnit.unit = unit;
        refundUnit.planQty = unit.availableQty;
        refundUnit.qty = unit.availableQty;
        refundUnit.save();
    }
}
