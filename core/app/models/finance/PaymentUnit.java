package models.finance;

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
         * 审核中
         */
        REVIEWING {
            @Override
            public String toString() {
                return "审核中";
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

    /**
     * 申请的金额
     */
    public float amount = 0;
    /**
     * 申请的币种
     */
    public Currency currency = Currency.CNY;

    @Lob
    public String memo;

    @Enumerated(EnumType.STRING)
    public S state = S.APPLY;

    @PrePersist
    public void beforeSave() {
        this.createdAt = new Date();
    }
}
