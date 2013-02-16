package models.finance;

import exception.PaymentException;
import helper.Currency;
import models.User;
import org.joda.time.DateTime;
import play.db.jpa.Model;
import play.libs.F;

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
    public List<PaymentUnit> units = new ArrayList<PaymentUnit>();

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

    /**
     * 最终实际支付
     */
    public Float actualPaid = 0f;

    /**
     * 最后支付的币种是什么
     */
    @Enumerated(EnumType.STRING)
    public Currency currency = Currency.CNY;

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
     * 分别计算 USD 与 CNY 的总金额
     *
     * @return _.1: USD; _.2: CNY
     */
    public F.T2<Float, Float> totalFees() {
        // todo: 将付款的金额限制在 USD 与 CNY
        float usd = 0;
        float cny = 0;
        for(PaymentUnit unit : this.units) {
            if(unit.currency == Currency.CNY)
                cny += unit.amount();
            else if(unit.currency == Currency.USD)
                usd += unit.amount();
            else
                throw new PaymentException(PaymentException.INVALID_CURRENCY);
        }
        return new F.T2<Float, Float>(usd, cny);
    }

    /**
     * 制作一个 Payment, 如果符合条件则寻找一个, 否则创建新的;
     * 1. 时间(24h 之内)
     * 2. 外键 Id
     *
     * @return
     */
    public static Payment makePayment(String deliverymentId) {
        DateTime now = DateTime.now();
        Payment payment = Payment.find(
                "SELECT p FROM Payment p LEFT JOIN p.units fee WHERE " +
                        "fee.deliveryment.id=? AND p.createdAt>=? AND p.createdAt<=? " +
                        "ORDER BY p.createdAt DESC",
                deliverymentId, now.minusHours(24).toDate(), now.toDate()).first();

        if(payment == null) {
            payment = new Payment();
            payment.save();
        }
        return payment;
    }
}
