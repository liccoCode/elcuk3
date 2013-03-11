package models.finance;

import exception.PaymentException;
import helper.Currency;
import models.User;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

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
         * first: deliveryment
         * second: shipment
         * third: shipItem
         */
        if(this.deliveryment != null)
            return this.deliveryment.id;
        else if(this.shipment != null)
            return this.shipment.id;
        else if(this.shipItem != null)
            return this.shipItem.id + "";
        else
            return "无外键(孤儿), 请联系 It";
    }
}
