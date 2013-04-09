package models.finance;

import exception.PaymentException;
import helper.Currency;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.*;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.i18n.Messages;

import javax.persistence.*;
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
public class PaymentUnit extends Model {
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
                return "已支付";
            }
        };

        public abstract String label();
    }

    public PaymentUnit() {
        this.createdAt = new Date();
        this.state = S.APPLY;
    }

    public PaymentUnit(ProcureUnit procureUnit) {
        this();
        this.procureUnit = procureUnit;
        this.deliveryment = procureUnit.deliveryment;
        this.amount = procureUnit.attrs.price * procureUnit.qty();
        this.currency = procureUnit.attrs.currency;
        this.payment = Payment.buildPayment(this.deliveryment, this.currency);
        this.payee = User.current();
        this.payment.pApply = this.deliveryment.apply;
        this.payment.save();
    }

    @ManyToOne
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
     * 5. 其他
     * 各种不同的关联关系, 由于无法像动态语言那样灵活, 所以将复杂性交给 Hibernate, 手动去选择不同的关联类型
     */

    @ManyToOne
    public ProcureUnit procureUnit;

    @ManyToOne
    public Deliveryment deliveryment;

    @ManyToOne
    public ShipItem shipItem;

    @ManyToOne
    public Shipment shipment;

    public Date createdAt;

    /**
     * 总修正值, 而非单个修正值
     */
    @Required
    public float fixValue = 0;

    public boolean remove = false;

    /**
     * 申请的金额
     */
    public float amount = 0;
    /**
     * 申请的币种
     */
    @Enumerated(EnumType.STRING)
    public Currency currency = Currency.CNY;

    @OneToOne
    public FeeType feeType;

    @Lob
    public String memo;

    @Enumerated(EnumType.STRING)
    public S state = S.APPLY;

    @PrePersist
    public void beforeSave() {
        if(this.feeType == null)
            throw new PaymentException("支付单元必须拥有费用类型.");
        if(this.currency == null)
            throw new PaymentException("支付单元的货币种类不允许为空.");
        this.createdAt = new Date();
    }

    @PreRemove
    public void softDelete() {
        // 防止错误使用 Model.delete 删除
        throw new PaymentException("只允许软删除.");
    }

    public PaymentUnit remove(String reason) {
        if(StringUtils.isBlank(reason))
            Validation.addError("", "必须填写取消的理由.");
        if(this.isApproval())
            Validation.addError("", "请款已经被批准, 不允许删除记录, 请与财务交流调整.");
        if(this.remove)
            Validation.addError("", "已经删除了");

        if(Validation.hasErrors()) return null;

        this.remove = true;
        this.save();
        new ERecordBuilder("paymentunit.destroy")
                .msgArgs(reason)
                .fid(this.procureUnit.id) // 取消的操作, 记录在 ProcureUnit 身上, 因为是对采购计划取消请款
                .save();
        return this;
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
        if(this.remove)
            Validation.addError("", "已经删除了");
        if(!Arrays.asList(S.APPLY, S.APPROVAL, S.DENY).contains(this.state))
            Validation.addError("", this.state.label() + " 状态拒绝 '驳回'");
        if(StringUtils.isBlank(reason))
            Validation.addError("", "驳回的原因必须填写");

        if(Validation.hasErrors()) return;
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
        return this.amount + this.fixValue;
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
        if(this.deliveryment != null)
            return this.deliveryment.id;
        else if(this.shipment != null)
            return this.shipment.id;
        else
            return "无外键(孤儿), 请联系 It";
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
            return Shipment.<Deliveryment>findById(fk).cooperator;
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

    /**
     * 修改修正价格;
     * 1. 如果属于驳回状态, 那么自动变为已申请状态
     * 2. 如果属于已支付状态, 不允许再修改修正价格
     * 3. 不允许修改删除的
     *
     * @param fixValue
     */
    public void fixValue(Float fixValue, String reason) {
        if(this.state == S.PAID)
            Validation.addError("", "请款已经完成支付, 不允许再修改修正价格.");
        if(this.fixValue == fixValue && fixValue != 0)
            Validation.addError("", "修正值没有修改");
        if(StringUtils.isBlank(reason))
            Validation.addError("", "修改修正价, 必须填写原因");
        if(this.remove)
            Validation.addError("", "此请款已经被删除, 无法修改.");

        if(Validation.hasErrors()) return;

        float oldFixValue = this.fixValue;
        this.fixValue = fixValue;
        if(this.state == S.DENY)
            this.state = S.APPLY;
        this.save();
        new ERecordBuilder("paymentunit.fixValue")
                .msgArgs(reason, oldFixValue + "", this.fixValue + "")
                .fid(this.id)
                .save();
    }
}

