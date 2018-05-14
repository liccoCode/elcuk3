package models.finance;

import exception.PaymentException;
import ext.PaymentHelper;
import helper.Currency;
import helper.Reflects;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.material.MaterialPlan;
import models.material.MaterialPlanUnit;
import models.material.MaterialPurchase;
import models.material.MaterialUnit;
import models.procure.*;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.i18n.Messages;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 支付的最小单元, 用于组成一封支付单
 * User: wyatt
 * Date: 1/24/13
 * Time: 11:34 AM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PaymentUnit extends Model {

    private static final long serialVersionUID = -8173938871382052744L;

    public enum S {
        /**
         * 申请
         */
        APPLY {
            @Override
            public String label() {
                return "已申请";
            }
        },

        /**
         * 驳回
         */
        DENY {
            @Override
            public String label() {
                return "驳回";
            }
        },
        /**
         * 批准
         */
        APPROVAL {
            @Override
            public String label() {
                return "批准";
            }
        },
        /**
         * 已支付(关联的支付单完成支付)
         */
        PAID {
            @Override
            public String label() {
                return "支付完成";
            }
        };

        public abstract String label();
    }

    public PaymentUnit() {
        this.createdAt = new Date();
        this.state = S.APPLY;
    }

    /**
     * 通过 ProcureUnit 生成一个 PaymentUnit. 包括:
     * 1. 采购单
     * 2. 总价(由具体交货数量拿具体交货数量计算, 没有则拿预计数量计算)
     * 3. 支付单
     * 4. 请款人
     * 5. 请款单
     *
     * @param procureUnit
     */
    public PaymentUnit(ProcureUnit procureUnit) {
        this();
        this.procureUnit = procureUnit;
        this.deliveryment = procureUnit.deliveryment;
        this.amount = procureUnit.totalAmount();
        this.currency = procureUnit.attrs.currency;
        this.payment = Payment.buildPayment(procureUnit.cooperator, procureUnit.attrs.currency,
                procureUnit.totalAmount(), procureUnit.deliveryment.apply);
        this.payee = User.current();
        this.payment.save();
    }


    public PaymentUnit(MaterialUnit materialUnit) {
        this();
        this.materialUnit = materialUnit;
        this.materialPurchase = materialUnit.materialPurchase;
        this.amount = materialUnit.totalAmount();
        this.unitQty = materialUnit.qty > 0 ? materialUnit.qty : materialUnit.planQty;
        this.currency = materialUnit.getCurrency();
        this.payment = Payment.buildPayment(materialUnit.materialPurchase.cooperator, materialUnit.getCurrency(),
                materialUnit.totalAmount(), materialUnit.materialPurchase.applyPurchase);
        this.payee = User.current();
        this.payment.save();
    }

    public PaymentUnit(MaterialPlanUnit materialPlanUnit) {
        this();
        this.materialPlanUnit = materialPlanUnit;
        this.materialPlan = materialPlanUnit.materialPlan;

        this.unitQty = materialPlanUnit.receiptQty > 0 ? materialPlanUnit.receiptQty : materialPlanUnit.qty;
        this.currency = materialPlanUnit.getCurrency();
        this.payment = Payment.buildPayment(materialPlanUnit.materialPlan.cooperator, materialPlanUnit.getCurrency(),
                materialPlanUnit.totalAmount(), materialPlanUnit.materialPlan.apply);
        this.payee = User.current();
        this.payment.save();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public Payment payment;

    /**
     * 申请人
     */
    @ManyToOne
    public User payee;

    /**
     * 支付单元拥有
     * 1. 采购单
     * 2. 采购单元
     * 3. 运输单
     * 4. 运输单元
     * 5. 物料出货单
     * 6. 物料出货计划
     * 7. 物料采购单
     * 8. 物料采购单元
     * 各种不同的关联关系, 由于无法像动态语言那样灵活, 所以将复杂性交给 Hibernate, 手动去选择不同的关联类型
     */

    @ManyToOne(fetch = FetchType.LAZY)
    public ProcureUnit procureUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    public Deliveryment deliveryment;

    @ManyToOne(fetch = FetchType.LAZY)
    public ShipItem shipItem;

    @ManyToOne(fetch = FetchType.LAZY)
    public Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    public MaterialPlan materialPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    public MaterialPlanUnit materialPlanUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    public MaterialPurchase materialPurchase;

    @ManyToOne(fetch = FetchType.LAZY)
    public MaterialUnit materialUnit;


    /**
     * 费用关系人
     * <p/>
     * 在物流请款中，可能货物是由其它的运输商进行运输，要进行分别付费。
     * 默认值是当前，运输商
     */
    @OneToOne
    public Cooperator cooperator;

    public Date createdAt;

    /**
     * 总修正值, 而非单个修正值
     */
    @Required
    public float fixValue = 0;

    public boolean remove = false;

    /**
     * 申请的金额 (unitPrice * unitQty)
     */
    public float amount = 0;
    /**
     * 申请的币种
     */
    @Enumerated(EnumType.STRING)
    public Currency currency = Currency.CNY;

    /**
     * 费用单价
     */
    public float unitPrice = 0;

    /**
     * 费用数量
     */
    public float unitQty = 1;

    @OneToOne
    public FeeType feeType;

    @Lob
    public String memo;

    @Enumerated(EnumType.STRING)
    public S state = S.APPLY;

    public enum W {
        WEIGHT {
            @Override
            public String label() {
                return "实重";
            }
        },
        VOLUME {
            @Override
            public String label() {
                return "体积重";
            }
        };

        public abstract String label();
    }

    /**
     * 计费方式
     * <p/>
     * 快递运输方式，记录运费时是针对每个采购计划记录运费的，此字段用来标记当时快递是按照重量还是体积来计费的
     */
    @Enumerated(EnumType.STRING)
    public W chargingWay;

    @PrePersist
    public void beforeSave() {
        if(this.feeType == null) {
            throw new PaymentException("支付单元必须拥有费用类型.");
        }
        if(this.currency == null) {
            throw new PaymentException("支付单元的货币种类不允许为空.");
        }
        this.createdAt = new Date();
    }

    public void basicRemoveValidate(String reason) {
        if(StringUtils.isBlank(reason)) {
            Validation.addError("", "必须填写取消的理由.");
        }
        if(this.isApproval()) {
            Validation.addError("", "请款已经被批准, 不允许删除记录, 请与财务交流调整.");
        }
    }

    public PaymentUnit procureFeeRemove(String reason) {
        basicRemoveValidate(reason);
        if(this.remove) {
            Validation.addError("", "已经删除了");
        }

        if(Validation.hasErrors()) {
            return null;
        }

        this.remove = true;
        this.save();
        new ERecordBuilder("paymentunit.destroy")
                .msgArgs(reason, this.currency, this.amount(), this.feeType.nickName)
                .fid(this.procureUnit.id) // 取消的操作, 记录在 ProcureUnit 身上, 因为是对采购计划取消请款
                .save();
        return this;
    }


    public PaymentUnit materialFeeRemove(String reason) {
        basicRemoveValidate(reason);
        if(this.remove) {
            Validation.addError("", "已经删除了");
        }
        if(Validation.hasErrors()) {
            return null;
        }
        this.remove = true;
        this.save();
        Long fid =  this.materialPlanUnit != null ? this.materialPlanUnit.id : this.materialUnit.id;
        new ERecordBuilder("materialPlanUnit.destroy")
                .msgArgs(reason, this.currency, this.amount(), this.feeType.nickName)
                .fid(fid) // 取消的操作, 记录在 MaterialPlanUnit 身上, 因为是对采购计划取消请款
                .save();
        return this;
    }

    public PaymentUnit transportFeeRemove(String reason) {
        basicRemoveValidate(reason);
        if(Validation.hasErrors()) {
            return this;
        }
        this.shipment.fees.remove(this);
        this.shipment.save();
        String fid;
        if(this.shipment != null) {
            fid = this.shipment.id;
        } else {
            fid = this.shipItem.shipment.id;
        }
        this.clearRecords();
        new ERecordBuilder("paymentunit.destroy")
                .msgArgs(reason, this.currency, this.amount(), this.feeType.nickName)
                .fid(fid)
                .save();
        return this;
    }

    /**
     * 永久删除
     */
    public void permanentRemove() {
        if(Arrays.asList(PaymentUnit.S.APPLY, PaymentUnit.S.DENY).contains(this.state)) {
            this.clearRecords();
            PaymentUnit.delete("id=?", this.id);
        }
    }

    public void transportApply() {
        if(this.cooperator == null) {
            this.cooperator = this.shipment.cooper;
        }
        if(this.shipment.apply == null) {
            Validation.addError("", "没有添加请款单, 无需批准操作.");
        }
        if(this.remove) {
            Validation.addError("", "#" + this.id + " 请款单已经删除了");
        }
        if(this.cooperator.paymentMethods == null || this.cooperator.paymentMethods.size() <= 0) {
            Validation.addError("", "请添加合作伙伴" + this.cooperator.name + "的支付方式信息");
        }
        if(Arrays.asList(S.PAID, S.APPROVAL).contains(this.state)) {
            Validation.addError("", String.format("%s 状态拒绝 '批准'", this.state.label()));
        }
        if(Validation.hasErrors()) {
            return;
        }
        if(this.payment == null) {
            this.payment = Payment.buildPayment(this.cooperator, this.currency, this.amount(), this.shipment.apply)
                    .save();
        }
        this.state = S.APPLY;
        this.save();
        new ERecordBuilder("payment.apply").msgArgs(this.unitQty, this.shipItem == null ? "" : this.shipItem.unit.sku,
                this.id, this.feeType.nickName, this.currency.symbol() + " " + this.amount())
                .fid(this.payment.id).save();
    }

    /**
     * 批准运输单请款项目
     */
    public void transportApprove() {
        /*
         * 1. 判断是否拥有请款单
         * 2. 判断状态是否软删除, 软删除不允许处理
         * 3. 判断状态是否允许
         * 4. 费用关系人是否有支付方式
         */
        if(this.cooperator == null) {
            this.cooperator = this.shipment.cooper;
        }
        if(this.shipment.apply == null) {
            Validation.addError("", "没有添加请款单, 无需批准操作.");
        }
        if(this.payment == null) {
            Validation.addError("", this.feeType.nickName + "没有请款, 不能进行批准操作.");
        }
        if(this.remove) {
            Validation.addError("", "#" + this.id + " 请款单已经删除了");
        }
        if(this.cooperator.paymentMethods == null || this.cooperator.paymentMethods.size() <= 0) {
            Validation.addError("", "请添加合作伙伴" + this.cooperator.name + "的支付方式信息");
        }
        if(Arrays.asList(S.PAID, S.APPROVAL).contains(this.state)) {
            Validation.addError("", String.format("%s 状态拒绝 '批准'", this.state.label()));
        }
        if(Validation.hasErrors()) {
            return;
        }
        if(this.payment == null) {
            this.payment = Payment.buildPayment(this.cooperator, this.currency, this.amount(), this.shipment.apply)
                    .save();
        }
        this.state = S.APPROVAL;
        this.save();
        new ERecordBuilder("payment.approval").msgArgs(this.unitQty, this.shipItem == null ? "" : this.shipItem.unit.sku,
                this.id,
                this.feeType.nickName,
                this.currency.symbol() + " " + this.amount())
                .fid(this.payment.id).save();
    }

    /**
     * 驳回
     *
     * @return
     */
    public void deny(String reason) {
        /**
         * 1. 变更状态允许
         * 2. 原因填写
         * 3. 删除检查
         */
        if(this.remove) {
            Validation.addError("", "已经删除了");
        }
        if(!Arrays.asList(S.APPLY, S.APPROVAL, S.DENY).contains(this.state)) {
            Validation.addError("", this.state.label() + " 状态拒绝 '驳回'");
        }
        if(StringUtils.isBlank(reason)) {
            Validation.addError("", "驳回的原因必须填写");
        }

        if(Validation.hasErrors()) {
            return;
        }
        this.state = S.DENY;
        this.save();
        new ERecordBuilder("paymentunit.deny")
                .msgArgs(reason)
                .fid(this.id)
                .save();
    }

    /**
     * 计算 Amount 与 Fix 的合计
     *
     * @return
     */
    public float amount() {
        return new BigDecimal(String.valueOf(this.amount))
                .add(new BigDecimal(String.valueOf(this.fixValue)))
                .floatValue();
    }

    public BigDecimal decimalamount() {
        return new BigDecimal(String.valueOf(this.amount))
                .add(new BigDecimal(String.valueOf(this.fixValue)));
    }

    /**
     * 计算 Amount 与 Fix 的合计
     * 用于报表统计，主要是为了统一显示美元
     *
     * @return
     */
    public float amountForReport() {
        return Currency.CNY.toUSD(this.amount());
    }

    /**
     * 在 APPROVAL 之前的状态都被认为是没有批准
     *
     * @return
     */
    private boolean isApproval() {
        return this.state == S.APPROVAL || this.state == S.PAID;
    }

    /**
     * 采购单元所关联的外键
     *
     * @return
     */
    public String foreignKey() {
        /**
         * first: deliveryment|procureUnit 在其中
         * second: shipment
         * third: shipItem
         */
        if(this.deliveryment != null) {
            return this.deliveryment.id;
        } else if(this.shipment != null) {
            return this.shipment.id;
        } else {
            return "无外键(孤儿), 请联系 It";
        }
    }

    /**
     * 单个请款项目所链接的合作者
     *
     * @return
     */
    public Cooperator cooperator() {
        String fk = this.foreignKey();
        if(fk.startsWith("DL")) {
            return Deliveryment.<Deliveryment>findById(fk).cooperator;
        } else if(fk.startsWith("SP")) {
            return Shipment.<Shipment>findById(fk).cooper;
        }
        return null;
    }

    /**
     * 调整修正价的日志
     *
     * @return
     */
    public List<ElcukRecord> fixValueRecords() {
        return ElcukRecord.records(this.id + "", Messages.get("paymentunit.fixValue"));
    }

    /**
     * 拒绝操作的日志
     *
     * @return
     */
    public List<ElcukRecord> denyRecords() {
        return ElcukRecord.records(this.id + "", Messages.get("paymentunit.deny"));
    }

    public List<ElcukRecord> updateRecords() {
        return ElcukRecord.records(this.id + "", Messages.get("paymentunit.update"));
    }

    public List<ElcukRecord> records() {
        return ElcukRecord.records(this.id + "",
                Arrays.asList("paymentunit.fixValue", "paymentunit.deny", "paymentunit.update"), 50);
    }

    /**
     * 永久删除 PaymentUnit 的时候, 需要将其关联的 Records 一起删除.
     */
    public void clearRecords() {
        for(String action : Arrays.asList("paymentunit.fixValue", "paymentunit.deny")) {
            ElcukRecord.delete("action=? AND fid=?", Messages.get(action), this.id.toString());
        }
    }

    /**
     * 修改修正价格;
     * 1. 如果属于驳回状态, 那么自动变为已申请状态
     * 2. 如果属于已支付状态, 不允许再修改修正价格
     * 3. 不允许修改删除的
     *
     * @param fixValue
     */
    public void fixValue(Float fixValue, String reason) {
        if(this.state == S.PAID) {
            Validation.addError("", "请款已经完成支付, 不允许再修改修正价格.");
        }
        if(this.fixValue == fixValue && fixValue != 0) {
            Validation.addError("", "修正值没有修改");
        }
        if(StringUtils.isBlank(reason)) {
            Validation.addError("", "修改修正价, 必须填写原因");
        }
        if(this.remove) {
            Validation.addError("", "此请款已经被删除, 无法修改.");
        }

        if(Validation.hasErrors()) {
            return;
        }

        float oldFixValue = this.fixValue;
        this.fixValue = fixValue;
        if(this.state == S.DENY) {
            this.state = S.APPLY;
        }
        this.save();
        new ERecordBuilder("paymentunit.fixValue").msgArgs(reason, oldFixValue, this.fixValue).fid(this.id).save();
    }

    /**
     * 更新 Paymentunit 中的单价与数量
     *
     * @param fee
     */
    public PaymentUnit fixUnitValue(PaymentUnit fee) {
        if(this.state == S.PAID) {
            Validation.addError("", "请款已经完成支付, 不允许再修改修正价格.");
        }
        if(Validation.hasErrors()) {
            return this;
        }
        fee.amount = fee.unitPrice * fee.unitQty;
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "amount", fee.amount));
        logs.addAll(Reflects.logFieldFade(this, "unitPrice", fee.unitPrice));
        logs.addAll(Reflects.logFieldFade(this, "unitQty", fee.unitQty));
        logs.addAll(Reflects.logFieldFade(this, "memo", fee.memo));
        logs.addAll(Reflects.logFieldFade(this, "chargingWay", fee.chargingWay));
        if(this.payment == null) {
            logs.addAll(Reflects.logFieldFade(this, "currency", fee.currency));
        }

        if(logs.size() > 0) {
            new ERecordBuilder("paymentunit.update")
                    .msgArgs(StringUtils.join(logs, ";\r\n"))
                    .fid(this.shipment.id).save();
        }
        return this.save();
    }

    /**
     * 运输费用的均价, 没有运输项目. 统一币种为 CNY 则为单价(unitPrice)
     *
     * @return
     */
    public float averagePrice() {
        return PaymentHelper.averagePrice(this);
    }

    /**
     * 运输费用的均价 报表显示 专用
     *
     * @return
     */
    public float averagePriceForReports() {
        float price = PaymentHelper.averagePrice(this);
        return Currency.CNY.toUSD(price);
    }

    public float currentAvgPrice() {
        return PaymentHelper.currentAvgPrice(this);
    }

    public float currentAvgPriceForReports() {
        float price = PaymentHelper.currentAvgPrice(this);
        return Currency.CNY.toUSD(price);
    }

    public String returnChargingWayValue() {
        return this.chargingWay.label();
    }

}

