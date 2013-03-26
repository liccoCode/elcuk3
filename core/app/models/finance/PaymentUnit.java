package models.finance;

import exception.PaymentException;
import helper.Currency;
import models.ElcukRecord;
import models.User;
import models.procure.*;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.i18n.Messages;

import javax.persistence.*;
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
            public String toString() {
                return "已申请";
            }
        },

        /**
         * 驳回
         */
        DENY {
            @Override
            public String toString() {
                return "驳回";
            }
        },
        /**
         * 批准
         */
        APPROVAL {
            @Override
            public String toString() {
                return "批准";
            }
        },
        /**
         * 已支付(关联的支付单完成支付)
         */
        PAID {
            @Override
            public String toString() {
                return "已支付";
            }
        }
    }

    public PaymentUnit() {
        this.createdAt = new Date();
        this.state = S.APPLY;
    }

    public PaymentUnit(ProcureUnit procureUnit) {
        this();
        this.feeType = FeeType.cashpledge();
        this.procureUnit = procureUnit;
        this.deliveryment = procureUnit.deliveryment;
        this.amount = procureUnit.attrs.price * procureUnit.qty();
        this.currency = procureUnit.attrs.currency;
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

    public PaymentUnit remove() {
        if(this.isApproval())
            throw new PaymentException("请款已经被批准, 请向财务确定删除.");

        this.remove = true;
        return this.save();
    }

    /**
     * 计算 Amount 与 Fix 的合计
     *
     * @return
     */
    public float amount() {
        return this.amount + this.fixValueAmount();
    }

    /**
     * FixValue 计算的总价
     *
     * @return
     */
    public float fixValueAmount() {
        if(this.procureUnit != null)
            return this.fixValue * this.procureUnit.qty();
            //TODO: 还有其他类型的 FixValue 需要补全
        else
            return 0;
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
     * 所有与 PaymentUnit 相关的执行记录
     *
     * @return
     */
    public List<ElcukRecord> records() {
        return ElcukRecord.records(this.id + "", Messages.get("paymentunit.fixValue"));
    }

    /**
     * 修改修正价格;
     * 1. 如果属于驳回状态, 那么自动变为已申请状态
     * 2. 如果属于已支付状态, 不允许再修改修正价格
     *
     * @param fixValue
     */
    public void fixValue(Float fixValue, String reason) {
        if(this.state == S.PAID) {
            Validation.addError("", "请款已经完成支付, 不允许再修改修正价格.");
            return;
        }
        float oldFixValue = this.fixValue;
        this.fixValue = fixValue;
        if(this.state == S.DENY)
            this.state = S.APPLY;
        this.save();
        new ElcukRecord(Messages.get("paymentunit.fixValue"),
                Messages.get("paymentunit.fixValue.msg", User.current().username, reason,
                        oldFixValue, this.fixValue),
                this.id + "").save();
    }
}
