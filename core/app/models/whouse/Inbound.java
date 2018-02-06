package models.whouse;

import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.Dates;
import helper.Reflects;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.DeliverPlan;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by licco on 2016/11/2.
 * 收货入库单
 */
@Entity
@DynamicUpdate
public class Inbound extends GenericModel {

    private static final long serialVersionUID = -4192529114985615298L;

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    /**
     * 名称
     */
    public String name;

    /**
     * 收货类型
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public T type;

    public enum T {
        Purchase {
            @Override
            public String label() {
                return "采购入库";
            }
        },
        Machining {
            @Override
            public String label() {
                return "加工入库";
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
     * 出货单
     */
    @ManyToOne
    public DeliverPlan plan;

    /**
     * 入库单元
     */
    @OneToMany(mappedBy = "inbound", cascade = {CascadeType.PERSIST})
    public List<InboundUnit> units = new ArrayList<>();

    /**
     * 状态
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public S status;

    public enum S {
        Create {
            @Override
            public String label() {
                return "待收货";
            }
        },

        Handing {
            @Override
            public String label() {
                return "质检中";
            }
        },

        End {
            @Override
            public String label() {
                return "已入库";
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

    public enum DM {
        Factory {
            @Override
            public String label() {
                return "工厂送货";
            }
        },

        Express {
            @Override
            public String label() {
                return "快递送货";
            }
        };

        public abstract String label();
    }

    /**
     * 交货方式
     */
    @Expose
    @Enumerated(EnumType.STRING)
    public DM deliveryMethod;

    /**
     * 创建时间
     */
    @Required
    public Date createDate;

    /**
     * 收货时间
     */
    @Temporal(TemporalType.DATE)
    public Date receiveDate;

    /**
     * 收货人
     */
    @OneToOne
    public User receiver;

    /**
     * 备注
     */
    public String memo;

    /**
     * 公司名称
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

    /**
     * 是否B2B
     */
    @Transient
    public boolean isb2b = false;

    @Transient
    public List<String> qcDtos = new ArrayList<>();

    @Transient
    public List<String> inboundDtos = new ArrayList<>();

    public void create(List<InboundUnit> units, boolean isTail) {
        units.stream().filter(Objects::nonNull).filter(unit -> validIsNotExist(unit.unitId)).forEach(u -> {
            u.inbound = this;
            u.unit = ProcureUnit.findById(u.unitId);
            this.projectName = User.COR.valueOf(u.unit.projectName);
            if(isTail) {
                u.planQty = u.unit.attrs.planQty - u.unit.attrs.qty;
            } else {
                u.planQty = this.type == T.Purchase ? u.unit.attrs.planQty : u.unit.availableQty;
            }
            if(this.type == T.Machining) {
                u.status = InboundUnit.S.Receive;
                u.result = InboundUnit.R.Qualified;
                u.qualifiedQty = u.qty;
                u.inboundQty = u.qty;
            } else {
                u.status = InboundUnit.S.Create;
            }
            u.save();
        });
        this.save();
    }

    public static boolean validIsNotExist(Long unitId) {
        return InboundUnit.count("unit.id = ? AND inbound.status = ? ", unitId, S.Create) == 0;
    }

    /**
     * index页面时间展示格式化
     */
    public void showTime() {
        Map<String, String> qcMap = new HashMap<>();
        Map<String, String> inboundMap = new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for(InboundUnit unit : this.units) {
            if(unit.qcUser != null) {
                String key = unit.qcUser.username + " / " + formatter.format(unit.qcDate);
                if(!qcMap.containsKey(key)) {
                    qcDtos.add(key);
                    qcMap.put(key, "");
                }
            }
            if(unit.confirmUser != null) {
                String key = unit.confirmUser.username + " / " + formatter.format(unit.inboundDate);
                if(!inboundMap.containsKey(key)) {
                    inboundDtos.add(key);
                    inboundMap.put(key, "");
                }
            }
        }
    }


    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = Inbound.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate())
                + "";
        return String.format("SR|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public void confirmReceive(List<InboundUnit> units) {
        units.stream().filter(unit -> Inbound.cap(unit.id)).forEach(unit -> {
            InboundUnit u = InboundUnit.findById(unit.id);
            if(u.status.equals(InboundUnit.S.Create)) {
                u.status = InboundUnit.S.Receive;
                u.result = InboundUnit.R.UnCheck;
                u.target = Whouse.autoMatching(u);
                u.qualifiedQty = u.qty;
                u.unqualifiedQty = 0;
                u.inboundQty = u.qty;
                u.save();
                ProcureUnit punit = u.unit;
                punit.attrs.qty = (punit.attrs.qty == null ? 0 : punit.attrs.qty) + u.qty;
                if(punit.stage != ProcureUnit.STAGE.IN_STORAGE) {
                    punit.attrs.deliveryDate = new Date();
                    punit.stage = ProcureUnit.STAGE.DONE;
                }
                punit.mainBoxInfo = u.mainBoxInfo;
                punit.lastBoxInfo = u.lastBoxInfo;
                punit.result = InboundUnit.R.UnCheck;
                punit.save();
            }
        });
    }

    public static boolean validTailInbound(InboundUnit dto) {
        InboundUnit unit = InboundUnit.findById(dto.id);
        return Arrays.asList("OUTBOUND", "SHIPPING", "SHIP_OVER", "INBOUND", "CLOSE").contains(unit.unit.stage.name());
    }

    private static boolean cap(Long id) {
        InboundUnit u = InboundUnit.findById(id);
        return u.status.equals(InboundUnit.S.Create);
    }

    public void confirmQC(List<InboundUnit> units) {
        for(InboundUnit unit : units) {
            InboundUnit u = InboundUnit.findById(unit.id);
            if(u.result == InboundUnit.R.Qualified && u.target == null) {
                Validation.addError("", "采购计划【" + u.unit.id + "】目标仓库未填写，请查证");
                return;
            }
            if(u.qualifiedQty + u.unqualifiedQty > u.qty) {
                Validation.addError("", "采购计划【" + u.unit.id + "】入库数大于收货数量，请查证");
                return;
            }
            if(u.status == InboundUnit.S.Receive && u.result == InboundUnit.R.Unqualified) {
                u.status = InboundUnit.S.Abort;
                u.qcUser = Login.current();
                u.qcDate = new Date();
                u.save();
                if(this.type == T.Purchase) {
                    ProcureUnit punit = u.unit;
                    punit.unqualifiedQty += u.unqualifiedQty;
                    punit.currWhouse = u.target;
                    if(punit.inboundQty == 0) {
                        punit.stage = ProcureUnit.STAGE.DELIVERY;
                        punit.result = u.result;
                    }
                    punit.save();
                }
            } else if(u.status == InboundUnit.S.Receive && !(u.result == null || (u.result == InboundUnit.R.Qualified
                    && u.qualifiedQty == 0)) && u.result != InboundUnit.R.UnCheck) {
                u.status = InboundUnit.S.Inbound;
                u.confirmUser = Login.current();
                u.inboundDate = new Date();
                u.inboundQty = u.qualifiedQty;
                u.qcDate = new Date();
                u.qcUser = Login.current();
                u.save();
                ProcureUnit punit = u.unit;
                punit.result = u.result;
                punit.stage = ProcureUnit.STAGE.IN_STORAGE;
                if(this.type == T.Purchase) {
                    punit.inboundQty += u.inboundQty;
                    punit.unqualifiedQty += u.unqualifiedQty;
                    punit.availableQty += u.inboundQty;
                } else {
                    punit.unqualifiedQty += u.unqualifiedQty;
                    punit.availableQty = u.inboundQty;
                }
                punit.mainBoxInfo = u.mainBoxInfo;
                punit.lastBoxInfo = u.lastBoxInfo;
                punit.currWhouse = u.target;
                punit.save();
                this.createStockRecord(u, punit.availableQty);
            }
        }
    }

    public int totalRealQty(Long unitId) {
        List<InboundUnit> inboundUnits = InboundUnit.find("unit.id = ?", unitId).fetch();
        return inboundUnits.stream().filter(unit -> unit.status == InboundUnit.S.Inbound)
                .mapToInt(unit -> unit.qty).sum();
    }

    private void createStockRecord(InboundUnit unit, int currQty) {
        StockRecord record = new StockRecord();
        record.creator = Login.current();
        record.whouse = unit.target;
        record.unit = unit.unit;
        record.qty = unit.inboundQty;
        record.type = StockRecord.T.Inbound;
        record.recordId = unit.id;
        record.currQty = currQty;
        record.save();
    }

    public void checkIsFinish() {
        int count = InboundUnit.find("inbound.id = ? and status NOT IN (?,?)", this.id,
                InboundUnit.S.Inbound, InboundUnit.S.Abort).fetch().size();
        if(count == 0) {
            List<InboundUnit> inbound_units = this.units;
            List<InboundUnit> return_units = new ArrayList<>();
            List<InboundUnit> tail_units = new ArrayList<>();
            for(InboundUnit iunit : inbound_units) {
                if(iunit.result == InboundUnit.R.Qualified && iunit.handType == InboundUnit.H.Delivery) {
                    tail_units.add(iunit);
                }
            }
            this.status = S.End;
            this.save();
            if(return_units.size() > 0) {
                Refund refund = new Refund();
                refund.name = String.format("%s-%s", this.cooperator != null ? this.cooperator.name : "",
                        this.plan != null ? this.plan.id : "");
                refund.cooperator = this.cooperator;
                refund.type = Refund.T.After_Receive;
                refund.createRefundByInbound(return_units);
                Refund.confirmRefund(Arrays.asList(refund));
            }
            /*创建尾货单**/
            if(tail_units.size() > 0) {
                this.createTailInbound(tail_units);
            }
        }
    }

    private void createTailInbound(List<InboundUnit> tailUnits) {
        Inbound inbound = new Inbound();
        inbound.id = id();
        inbound.name = this.name + "--尾货单";
        inbound.type = this.type;
        inbound.cooperator = this.cooperator;
        inbound.plan = this.plan;
        inbound.status = S.Create;
        inbound.createDate = new Date();
        inbound.receiver = this.receiver;
        inbound.projectName = Login.current().projectName;
        inbound.save();
        tailUnits.forEach(i -> {
            InboundUnit u = new InboundUnit();
            u.status = InboundUnit.S.Create;
            u.inbound = inbound;
            u.planQty = i.planQty - i.qty;
            u.qty = u.planQty;
            u.unit = i.unit;
            u.save();
        });
    }

    public static void createTailInboundByUnQualifiedHandle(Refund refund) {
        Inbound inbound = new Inbound();
        User user = Login.current();
        inbound.id = id();
        inbound.type = T.Purchase;
        inbound.name = String.format("%s_%s_%s_%s--退货收货单",
                refund.cooperator.name, inbound.type.label(), Dates.date2Date(), user.username);
        inbound.cooperator = refund.cooperator;
        inbound.status = S.Create;
        inbound.createDate = new Date();
        inbound.receiver = user;
        inbound.projectName = user.projectName;
        inbound.memo = refund.memo;
        inbound.save();
        refund.unitList.forEach(i -> {
            InboundUnit u = new InboundUnit();
            u.status = InboundUnit.S.Create;
            u.inbound = inbound;
            u.planQty = i.planQty;
            u.qty = i.qty;
            u.unit = i.unit;
            u.save();
        });
    }

    public static void updateBoxInfo(List<InboundUnit> units) {
        units.forEach(unit -> {
            InboundUnit old = InboundUnit.findById(unit.id);
            if(old.status.name().equals("Create")) {
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

    public void saveAndLog(Inbound inbound) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "name", inbound.name));
        logs.addAll(Reflects.logFieldFade(this, "receiveDate", inbound.receiveDate));
        logs.addAll(Reflects.logFieldFade(this, "memo", inbound.memo));
        logs.addAll(Reflects.logFieldFade(this, "deliveryMethod", inbound.deliveryMethod));
        if(logs.size() > 0) {
            new ERecordBuilder("inbound.update").msgArgs(this.id, StringUtils.join(logs, "<br>")).fid(this.id)
                    .save();
        }
        this.save();
    }

    /**
     * 转入良品时，系统默认生成一条收货入库记录
     *
     * @param unit
     * @param qty
     * @param memo
     * @param type
     */
    public static void createTransferInbound(ProcureUnit unit, int qty, String memo, Refund.InboundType type) {
        Inbound inbound = new Inbound();
        User user = Login.current();
        inbound.id = id();
        inbound.name = String
                .format("%s_%s_%s_%s", unit.cooperator.name, type.label(), Dates.date2Date(), user.username);
        inbound.type = T.Purchase;
        inbound.cooperator = unit.cooperator;
        inbound.plan = unit.deliverplan;
        inbound.status = S.End;
        inbound.projectName = Login.current().projectName;
        inbound.memo = memo;
        inbound.createDate = new Date();
        inbound.receiveDate = new Date();
        inbound.receiver = user;
        inbound.save();
        InboundUnit inboundUnit = new InboundUnit();
        inboundUnit.inbound = inbound;
        inboundUnit.unit = unit;
        inboundUnit.planQty = qty;
        inboundUnit.qty = qty;
        inboundUnit.status = InboundUnit.S.Inbound;
        inboundUnit.result = InboundUnit.R.Qualified;
        inboundUnit.qualifiedQty = qty;
        inboundUnit.inboundQty = qty;
        inboundUnit.target = unit.currWhouse;
        inboundUnit.qcDate = new Date();
        inboundUnit.qcUser = user;
        inboundUnit.inboundDate = new Date();
        inboundUnit.confirmUser = user;
        inboundUnit.save();
    }

    public void saveLog(String info, String fid) {
        new ElcukRecord("inbound.update", info, fid).save();
    }

}
