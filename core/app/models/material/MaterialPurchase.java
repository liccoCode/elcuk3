package models.material;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.PaymentUnit;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 物料采购单
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/5/31
 * Time: AM10:19
 */
@Entity
@DynamicUpdate
public class MaterialPurchase extends GenericModel {

    private static final long serialVersionUID = 6762554005097525886L;

    public MaterialPurchase() {

    }

    @Id
    @Column(length = 30)
    @Expose
    public String id;

    public String name;

    /**
     * 此采购单的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    @Required
    public S state;

    public enum S {
        /**
         * 计划：创建成功后即为“计划”
         */
        PENDING {
            @Override
            public String label() {
                return "计划";
            }
        },
        /**
         * 已确认并已下单：点击确认后变为“已确认并已下单”
         */
        CONFIRM {
            @Override
            public String label() {
                return "确认并已下单";
            }
        },
        /**
         * 取消：取消采购单后变为“取消”
         */
        CANCEL {
            @Override
            public String label() {
                return "取消";
            }
        };

        public abstract String label();
    }

    /**
     * 采购计划
     */
    @OneToMany(mappedBy = "materialPurchase", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public List<MaterialUnit> units = new ArrayList<>();

    @ManyToOne
    public MaterialApply applyPurchase;

    @OneToOne
    public User handler;

    /**
     * 供应商
     * 一个采购单只能拥有一个供应商
     */
    @ManyToOne
    public Cooperator cooperator;

    @Expose
    @Required
    public Date createDate = new Date();

    /**
     * 采购单类型
     */
    @Enumerated(EnumType.STRING)
    public Deliveryment.T deliveryType;

    /**
     * 下单时间(确认时间)
     */
    public Date orderTime;

    @Lob
    public String memo = " ";

    /**
     * 所属公司
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

    /**
     * 财务审核状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    public MaterialPlan.S financeState = MaterialPlan.S.PENDING_REVIEW;

    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = MaterialPurchase.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear()))
                        .toDate(),
                DateTime.parse(
                        String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear()))
                        .toDate()) + "";
        return String.format("WL|%s|%s", dt.toString("yyyyMM"),
                count.length() == 1 ? "0" + count : count);
    }

    /**
     * 获取此采购单的供应商, 如果没有采购货物, 则供应商为空, 否则为第一个采购计划的供应商(因为采购单只允许一个供应商)
     */
    public Cooperator supplier() {
        if(this.units.size() == 0) {
            return null;
        }
        return this.units.get(0).cooperator;
    }


    /**
     * 确认物料下采购单
     */
    public void confirm() {
        if(!Arrays.asList(S.PENDING).contains(this.state)) {
            Validation.addError("", "采购单状态非 " + Deliveryment.S.PENDING.label() + " 不可以确认");
        }
        if(Validation.hasErrors()) {
            return;
        }
        this.orderTime = new Date();
        this.state = S.CONFIRM;
        this.save();
    }

    /**
     * 取消物料采购单
     */
    public void cancel(String msg) {
        // 只允许所有都是 units 都为 采购中 的才能够取消.
        if(this.units.stream().anyMatch(unit -> unit.stage != MaterialUnit.STAGE.DELIVERY)) {
            Validation.addError("", "采购计划必须全部都是采购中的才能取消采购单！");
            return;
        }
        if(Validation.hasErrors()) {
            return;
        }
        this.units.forEach(unit -> unit.toggleAssignTodeliveryment(null, false));
        this.state = MaterialPurchase.S.CANCEL;
        this.save();
        new ElcukRecord(Messages.get("materialpurchases.cancel"),
                Messages.get("materialpurchases.cancel.msg", msg), id).save();

    }


    /**
     * 将 MaterialUnit 从 MaterialPurchase 中解除
     *
     * @param pids
     */
    public List<MaterialUnit> unAssignUnitInMaterialPurchase(List<Long> pids) {
        List<MaterialUnit> materialUnits = MaterialUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(MaterialUnit unit : materialUnits) {
            if(unit.stage != MaterialUnit.STAGE.DELIVERY) {
                Validation.addError("materialPurchase.units.unassign", "%s");
            } else if(this.deliveryType == Deliveryment.T.MANUAL) {
                //手动采购单中的默认的采购计划不允许从采购单中移除
                Validation.addError("", "手动单中默认的采购计划不允许被移除!");
            } else {
                unit.toggleAssignTodeliveryment(null, false);
            }
        }
        if(Validation.hasErrors()) {
            return new ArrayList<>();
        }
        this.units.removeAll(materialUnits);
        this.save();

        new ElcukRecord(Messages.get("materialPurchase.delunit"),
                Messages.get("materialPurchase.delunit.msg", pids, this.id), this.id).save();
        return materialUnits;
    }



    /**
     * 将采购单从其所关联的请款单中剥离开
     */
    public void departFromProcureApply() {
        /**
         * 1. 剥离没有过成功支付的出货单.
         * 2. 剥离后原有的 PaymentUnit 自动 remove 标记.
         */
        if(this.applyPurchase == null){
            Validation.addError("", "采购单没有添加进入请款单, 不需要剥离");
        }
        if(!isProcureApplyDepartable()) {
            Validation.addError("", "当前采购单已经拥有成功支付信息, 无法剥离.");
            return;
        }
        for(MaterialUnit unit : this.units) {
            for(PaymentUnit fee : unit.fees()) {
                fee.materialFeeRemove(String.format(
                        "所属出货单 %s 从原有请款单 %s 中剥离.", this.id, this.applyPurchase.serialNumber));
            }
        }
        new ERecordBuilder("materialpurchases.departapply")
                .msgArgs(this.id, this.applyPurchase.serialNumber)
                .fid(this.applyPurchase.id)
                .save();
        this.applyPurchase = null;
        this.save();
    }

    /**
     * 是否可以和采购请款单分离?
     * (采购单向请款单中添加与剥离, 都需要保证这个采购单没有付款完成的付款单)
     *
     * @return
     */
    public boolean isProcureApplyDepartable() {
        // 这个采购单的采购计划所拥有的 PaymentUnit(支付信息)没有状态为 PAID 的.
        for(MaterialUnit unit : this.units) {
            for(PaymentUnit fee : unit.fees()) {
                if(fee.state == PaymentUnit.S.PAID){
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * material页面获取出货单明细
     * @return
     */
    public List<MaterialUnit> applyUnit() {
        return MaterialUnit.find("materialPurchase.id=? ",this.id).fetch();
    }

    /**
     * 财务审核
     *
     * @param pids
     */
    public static void approve(List<String> pids) {
        List<MaterialPurchase> plans = MaterialPurchase.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(MaterialPurchase plan : plans) {
            if(plan.financeState != MaterialPlan.S.PENDING_REVIEW) {
                Validation.addError("", "采购单 %s 状态非 %s 不可以审核", plan.id, MaterialPlan.S.PENDING_REVIEW.label());
            }
        }
        if(Validation.hasErrors()) return;
        for(MaterialPurchase plan : plans) {
            plan.financeState = MaterialPlan.S.APPROVE;
            plan.save();
        }
    }
}
