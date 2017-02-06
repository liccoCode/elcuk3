package models.whouse;

import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.Reflects;
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

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    /**
     * 名称
     */
    @Required
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
                return "已创建";
            }
        },

        Handing {
            @Override
            public String label() {
                return "处理中";
            }
        },

        End {
            @Override
            public String label() {
                return "已结束";
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
     * 创建时间
     */
    @Required
    public Date createDate;

    /**
     * 收货时间
     */
    @Expose
    public Date receiveDate;

    /**
     * 收货人
     */
    @OneToOne
    public User receiver;


    /**
     * 项目名称
     */
    @Required
    public String projectName;

    /**
     * 备注
     */
    public String memo;

    /**
     * 是否B2B
     */
    @Transient
    public boolean isb2b = false;

    @Transient
    public List<String> qcDtos = new ArrayList<>();

    @Transient
    public List<String> inboundDtos = new ArrayList<>();

    public void create(List<InboundUnit> units) {
        units.stream().filter(Objects::nonNull).forEach(u -> {
            u.inbound = this;
            u.unit = ProcureUnit.findById(u.unitId);
            u.planQty = this.type == T.Purchase ? u.unit.attrs.planQty : u.unit.availableQty;
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
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate()) +
                "";
        return String.format("SR|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public void confirmReceive(List<InboundUnit> units) {
        units.stream().filter(unit -> Inbound.cap(unit.id)).forEach(unit -> {
            InboundUnit u = InboundUnit.findById(unit.id);
            if(u.status.equals(InboundUnit.S.Create)) {
                u.status = InboundUnit.S.Receive;
                u.result = InboundUnit.R.UnCheck;
                if(u.unit.selling != null && Whouse.autoMatching(u) != null) {
                    u.target = Whouse.autoMatching(u);
                }
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
                punit.save();
            }
        });
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
            if(u.status == InboundUnit.S.Receive && u.result == InboundUnit.R.Unqualified) {
                u.status = InboundUnit.S.Abort;
                u.qcUser = Login.current();
                u.qcDate = new Date();
                u.save();
                if(this.type == T.Purchase) {
                    ProcureUnit punit = u.unit;
                    punit.attrs.qty = (punit.attrs.qty == null ? 0 : punit.attrs.qty) - u.qty;
                    if(punit.attrs.qty == 0) {
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
                punit.outQty = punit.availableQty;
                punit.mainBoxInfo = u.mainBoxInfo;
                punit.lastBoxInfo = u.lastBoxInfo;
                punit.currWhouse = u.target;
                punit.currWhouse = u.target;
                punit.save();
                this.createStockRecord(u);
            }
        }
    }

    private void createStockRecord(InboundUnit unit) {
        StockRecord record = new StockRecord();
        record.whouse = unit.target;
        record.unit = unit.unit;
        record.qty = unit.inboundQty;
        record.type = StockRecord.T.Inbound;
        record.recordId = unit.id;
        record.save();
    }

    public void checkIsFinish() {
        int count = InboundUnit.find("inbound.id = ? and status NOT IN (?,?)", this.id,
                InboundUnit.S.Inbound, InboundUnit.S.Abort).fetch().size();
        if(count == 0) {
            List<InboundUnit> units = this.units;
            List<InboundUnit> return_units = new ArrayList<>();
            List<InboundUnit> tail_units = new ArrayList<>();
            for(InboundUnit iunit : units) {
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
                refund.projectName = this.projectName;
                refund.createRefundByInbound(return_units);
                Refund.confirmRefund(Arrays.asList(refund));
            }
            /**创建尾货单**/
            if(tail_units.size() > 0) {
                this.createTailInbound(tail_units);
            }
        }
    }

    private void createTailInbound(List<InboundUnit> tail_units) {
        Inbound inbound = new Inbound();
        inbound.id = this.id();
        inbound.name = this.name + "--尾货单";
        inbound.type = this.type;
        inbound.cooperator = this.cooperator;
        inbound.plan = this.plan;
        inbound.status = S.Create;
        inbound.createDate = new Date();
        inbound.receiver = this.receiver;
        inbound.projectName = this.projectName;
        inbound.save();
        tail_units.forEach(i -> {
            InboundUnit u = new InboundUnit();
            u.status = InboundUnit.S.Create;
            u.inbound = inbound;
            u.planQty = i.planQty - i.qty;
            u.qty = u.planQty;
            u.unit = i.unit;
            u.mainBoxInfo = i.mainBoxInfo;
            u.lastBoxInfo = i.lastBoxInfo;
            u.save();
        });
    }

    public void saveAndLog(Inbound in) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "name", in.name));
        logs.addAll(Reflects.logFieldFade(this, "receiveDate", in.receiveDate));
        logs.addAll(Reflects.logFieldFade(this, "projectName", in.projectName));
        logs.addAll(Reflects.logFieldFade(this, "memo", in.memo));
        if(logs.size() > 0) {
            new ERecordBuilder("inboundrecord.update").msgArgs(this.id, StringUtils.join(logs, "<br>")).fid(this.id)
                    .save();
        }
    }

}
