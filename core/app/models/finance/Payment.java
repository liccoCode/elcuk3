package models.finance;

import exception.PaymentException;
import helper.Currency;
import models.User;
import models.product.Attach;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        WAITING {
            @Override
            public String toString() {
                return "等待支付";
            }
        },
        /**
         * 支付完成
         */
        PAID {
            @Override
            public String toString() {
                return "完成支付";
            }
        },

        CLOSE {
            @Override
            public String toString() {
                return "处理完毕";
            }
        }

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

    public void upload(Attach a) throws IOException {
        // 1. save file
        // 2. fork upload to S3
        //todo: Payments 的上传需要特殊处理. 如何上传到 S3?
        FileUtils.copyFile(a.file, new File(a.location));
        a.save();
    }

    public int approval(List<Long> unitIds) {
        int approvalNum = 0;
        for(PaymentUnit unit : this.units) {
            if(!unitIds.contains(unit.id)) continue;
            if(!Arrays.asList(PaymentUnit.S.APPLY, PaymentUnit.S.DENY).contains(unit.state)) {
                Validation.addError("", String.format("%s 状态拒绝 '批准'", unit.state.toString()));
                continue;
            }
            approvalNum++;
            unit.state = PaymentUnit.S.APPROVAL;
            unit.save();
        }
        return approvalNum;
    }

    public int deny(List<Long> unitIds) {
        int denyNum = 0;
        for(PaymentUnit unit : this.units) {
            if(!unitIds.contains(unit.id)) continue;
            if(!Arrays.asList(PaymentUnit.S.APPLY, PaymentUnit.S.DENY).contains(unit.state)) {
                Validation.addError("", String.format("%s 状态拒绝 '驳回'", unit.state.toString()));
                continue;
            }
            denyNum++;
            unit.state = PaymentUnit.S.DENY;
            unit.save();
        }
        return denyNum;
    }

    public int paid(List<Long> unitIds) {
        int paidNum = 0;
        for(PaymentUnit unit : this.units) {
            if(!unitIds.contains(unit.id)) continue;
            if(!Arrays.asList(PaymentUnit.S.APPLY, PaymentUnit.S.APPROVAL).contains(unit.state)) {
                Validation.addError("", String.format("%s 状态拒绝 '支付'", unit.state.toString()));
                continue;
            }
            paidNum++;
            unit.state = PaymentUnit.S.PAID;
            unit.save();
        }
        return paidNum;
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
            if(unit.remove) continue;
            if(unit.currency == Currency.CNY)
                cny += unit.amount();
            else if(unit.currency == Currency.USD)
                usd += unit.amount();
            else
                throw new PaymentException(PaymentException.INVALID_CURRENCY);
        }
        if(usd == 0) usd = Currency.CNY.toUSD(cny);
        if(cny == 0) cny = Currency.USD.toCNY(usd);
        return new F.T2<Float, Float>(usd, cny);
    }

    /**
     * 计算不同状态的数量
     *
     * @param state
     * @return
     */
    public int unitsStateSize(PaymentUnit.S state) {
        int size = 0;
        for(PaymentUnit unit : this.units) {
            if(!unit.remove && unit.state == state)
                size++;
        }
        return size;
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

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Payment payment = (Payment) o;

        if(!id.equals(payment.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }
}
