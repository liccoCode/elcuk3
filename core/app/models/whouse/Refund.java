package models.whouse;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by licco on 2016/11/25.
 */
@Entity
@DynamicUpdate
public class Refund extends GenericModel {

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
                return "收货退货";
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
     * 项目名称
     */
    @Required
    public String projectName;

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
    public Date refundDate;

    /**
     * 仓库交接人
     */
    @OneToOne
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
        this.projectName = unit.projectName;
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
            unit.refund = this;
            unit.save();
        }
    }

    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = Refund.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate()) +
                "";
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
                    if(u.qty == unit.attrs.qty) {
                        unit.stage = ProcureUnit.STAGE.DELIVERY;
                    }
                }
                unit.save();
                createStockRecord(u);
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
                Validation.addError("", "退货单【" + refund.id + "】状态为【" + refund.status.label() + "】" +
                        "，请选择状态为【已创建】的托货单！");
            }
            for(RefundUnit u : refund.unitList) {
                ProcureUnit unit = u.unit;
                if(unit.outbound != null) {
                    Validation.addError("", "退货单【" + refund.id + "】下的采购计划【" + unit.id + "】在出库单" +
                            "【" + unit.outbound.id + "】中，请先处理！");
                }
            }
        }
    }

    public static void createStockRecord(RefundUnit unit) {
        StockRecord record = new StockRecord();
        record.whouse = unit.unit.whouse;
        record.unit = unit.unit;
        record.qty = unit.qty;
        record.type = StockRecord.T.Refund;
        record.recordId = unit.id;
        record.save();
    }

    /**
     * 采购计划对应的退货单是不是全部已退货
     *
     * @param id
     * @return
     */
    public static String isAllReufund(Long id) {
        List<Refund> refunds = Refund.find("SELECT DISTINCT r FROM Refund r LEFT JOIN r.unitList u " +
                "WHERE r.status <> ? AND u.unit.id = ? ", S.Refund, id).fetch();
        if(refunds.size() > 0) {
            return refunds.get(0).id;
        } else {
            return "";
        }
    }

}