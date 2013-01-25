package models.finance;

import models.User;
import models.procure.ProcureUnit;
import org.joda.time.DateTime;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 支付单, 真正用于一次的支付操作.
 * User: wyatt
 * Date: 1/24/13
 * Time: 11:35 AM
 */
@Entity
public class Payment extends Model {
    public enum S {
        /**
         * 等待支付
         */
        WAITING,
        /**
         * 支付完成
         */
        PAID
    }

    @OneToMany(mappedBy = "payment")
    public List<PaymentUnit> unit = new ArrayList<PaymentUnit>();

    /**
     * 关联谁支付的款项, "谁请款"记载在 Unit 身上
     */
    @ManyToOne
    public User payer;

    public Date createdAt;

    public Date lastUpdateAt;

    /**
     * 实际支付时间
     */
    public Date paymentDate;

    public Float actualPaid = 0f;

    @Lob
    public String memo;

    @Enumerated(EnumType.STRING)
    public S state = S.WAITING;

    /**
     * 支付账号信息
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    public PaymentTarget target;

    @PrePersist
    public void beforeSave() {
        this.createdAt = new Date();
        this.lastUpdateAt = new Date();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.lastUpdateAt = new Date();
    }

    /**
     * 制作一个 Payment, 如果符合条件则寻找一个, 否则创建新的;
     * 1. 时间(24h 之内)
     * 2. 外键 Id
     *
     * @return
     */
    public static Payment makePayment(ProcureUnit unit) {
        DateTime now = DateTime.now();
        Payment payment = Payment.find(
                "SELECT p FROM Payment p LEFT JOIN p.unit fee WHERE " +
                        "fee.deliveryment.id=? AND p.createdAt>=? AND p.createdAt<=? " +
                        "ORDER BY p.createdAt DESC",
                unit.deliveryment.id, now.minusHours(24).toDate(), now.toDate()).first();

        if(payment == null) {
            payment = new Payment();
            payment.save();
        }
        return payment;
    }
}
