package models.material;

import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.Reflects;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 物料出貨計劃
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/12
 * Time: PM6:18
 */
@Entity
@DynamicUpdate
public class MaterialPlanUnit extends Model {

    private static final long serialVersionUID = 4894533191306168541L;
    /**
     * 出货单
     */
    @ManyToOne
    public MaterialPlan materialPlan;

    /**
     * 物料信息
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Material material;

    /**
     * 实际交货数量
     * 目前版本对应 收货数量
     */
    public int qty;

    /**
     * 签收数量
     */
    public int receiptQty;


    /**
     * 物料计划状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public ProcureUnit.STAGE stage;

    /**
     * 操作人员
     */
    @OneToOne
    public User handler;

    /**
     * 创建时间
     */
    @Expose
    @Required
    public Date createDate = new Date();

    @OneToMany(mappedBy = "materialPlanUnit", fetch = FetchType.LAZY)
    public List<PaymentUnit> fees = new ArrayList<>();

    /**
     * 是否需要付款
     */
    public boolean isNeedPay = true;


    /**
     * 将 MaterialPlanUnit 添加到/移出 出库单,状态改变
     *
     * @param materialPlan
     */
    public void toggleAssignToMaterialPlan(MaterialPlan materialPlan, boolean assign) {
        if(assign) {
            this.materialPlan = materialPlan;
            this.stage = ProcureUnit.STAGE.DELIVERY;
        } else {
            this.materialPlan = null;
            this.stage = ProcureUnit.STAGE.PLAN;
        }
    }

    /**
     * 修改对象属性
     *
     * @param value
     */
    public void updateAttr(String value) {
        List<String> logs = new ArrayList<>();
        this.qty = NumberUtils.toInt(value);
        logs.addAll(Reflects.logFieldFade(this, "qty", NumberUtils.toInt(value)));
        new ERecordBuilder("materialPlan.updateAttr").msgArgs(this.id, StringUtils.join(logs, "<br/>"))
                .fid(this.id).save();
        this.save();
    }

    /**
     * 获取 cooperItems 的币种
     *
     * @return
     */
    public Currency getCurrency() {
        List<CooperItem> cooperItems = this.material.cooperItems;
        if(cooperItems != null && cooperItems.get(0) != null) {
            return cooperItems.get(0).currency;
        }
        return null;
    }


    /**
     * 获取 cooperItems  的价格
     *
     * @return
     */
    public Float getPrice() {
        List<CooperItem> cooperItems = this.material.cooperItems;
        if(cooperItems != null && cooperItems.get(0) != null) {
            return cooperItems.get(0).price;
        }
        return null;
    }

    /**
     * 总共需要申请的金额
     *
     * @return
     */
    public float totalAmount() {
        return new BigDecimal(this.getPrice().toString())
                .multiply(new BigDecimal(this.receiptQty > 0 ? this.receiptQty : this.qty))
                .setScale(2, 4)
                .floatValue();
    }

    /**
     * 已经申请的金额
     *
     * @return
     */
    public float appliedAmount() {
        float appliedAmount = 0;
        for(PaymentUnit fee : this.fees()) {
            appliedAmount += fee.amount();
        }
        return appliedAmount;
    }

    /**
     * 当前出货计划所有请款的修正总额
     *
     * @return
     */
    public float fixValueAmount() {
        float fixValueAmount = 0;
        for(PaymentUnit fee : this.fees()) {
            fixValueAmount += fee.fixValue;
        }
        return fixValueAmount;
    }


    public List<PaymentUnit> fees() {
        List<PaymentUnit> paymentUnits = new ArrayList<>();
        for(PaymentUnit fee : this.fees) {
            if(fee.remove) continue;
            paymentUnits.add(fee);
        }
        return paymentUnits;
    }

    /**
     * 申请预付款
     *
     * @return
     */
    public PaymentUnit billingPrePay() {
        this.billingValid();
        if(Validation.hasErrors()) return null;
        if(this.hasPrePay())
            Validation.addError("", "存在重复申请预付款的物料，请查证！");

        PaymentUnit fee = new PaymentUnit(this);
        Cooperator cooperator = this.materialPlan.cooperator;
        if(cooperator.materialFirst > 0) {
            fee.amount = this.totalAmount() * cooperator.materialFirst / 100;
        } else {
            Validation.addError("", "该物料预付款设置的比例为0，请查证！");
        }
        if(Validation.hasErrors()) return null;
        fee.feeType = FeeType.cashpledge();
        fee.save();
        this.materialPlan.apply.updateAt = new Date();
        this.materialPlan.apply.save();
        new ERecordBuilder("materialPlanUnit.prepay")
                .msgArgs(this.id, String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id, ProcureUnit.class).save();
        return fee;
    }

    /**
     * 付款申请
     */
    public PaymentUnit billingTailPay() {
        this.billingValid();
        if(Validation.hasErrors()) return null;
        if(this.hasTailPay())
            Validation.addError("", "存在重复申请尾款的物料，请查证！");
        if(Validation.hasErrors()) return null;
        PaymentUnit fee = new PaymentUnit(this);
        fee.amount = this.totalAmount() - this.appliedAmount();
        fee.feeType = FeeType.procurement();
        fee.save();
        this.materialPlan.apply.updateAt = new Date();
        this.materialPlan.apply.save();
        new ERecordBuilder("materialPlanUnit.prepay")
                .msgArgs(this.id, String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id, ProcureUnit.class).save();
        return fee;
    }

    /**
     * 1. 出货计划所在的出货单单需要拥有一个请款单
     * 2. 出货计划需要已经交货
     */
    private void billingValid() {
        if(this.materialPlan.apply == null)
            Validation.addError("", String.format("采购计划所属的采购单[%s]还没有规划的请款单", this.materialPlan.id));
    }

    /**
     * 是否拥有了预付款
     *
     * @return
     */
    public boolean hasPrePay() {
        return this.fees().stream().anyMatch(fee -> fee.feeType == FeeType.cashpledge());
    }


    /**
     * 是否拥有了尾款
     *
     * @return
     */
    public boolean hasTailPay() {
        return this.fees().stream().anyMatch(fee -> fee.feeType == FeeType.procurement());
    }

    /**
     * 修改付款状态
     */
    public void editPayStatus() {
        this.isNeedPay = !this.isNeedPay;
        this.save();
    }
}
