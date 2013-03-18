package models.finance;

import exception.PaymentException;
import helper.Currency;
import models.User;
import models.procure.Deliveryment;
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

    /**
     * 对请款单进行支付操作.
     * 1. 请款单中的币种只能拥有一种
     * 2. 根据前台的 currency 进行判断币种
     * 3. 检查没有批准的, 支付就是对这个请款单中的所有已经批准的进行支付
     *
     * @param unitIds
     * @param currency
     * @param actualPaid
     * @return
     */
    public int paid(Currency currency, Float actualPaid) {
        int paidNum = 0;
        // 1. 首先验证
        for(PaymentUnit unit : this.units) {
            if(unit.remove) continue;
            if(!Arrays.asList(PaymentUnit.S.APPLY, PaymentUnit.S.APPROVAL).contains(unit.state)) {
                Validation.addError("", String.format("%s 状态拒绝 '支付'", unit.state.toString()));
                break;
            }
            if(currency != unit.currency) {
                Validation.addError("", String.format("请款单中拥有不同的币种? 请联系 IT."));
                break;
            }
        }
        if(this.units().size() != this.unitsStateSize(PaymentUnit.S.APPROVAL))
            Validation.addError("", String.format("还有没有审批完成的请款项, 请审批完成后再付款."));
        Validation.current().valid(this);
        // 2. 验证后有错误返回
        if(Validation.hasErrors()) return paidNum;

        // 3. 验证后没错误 更新.
        for(PaymentUnit unit : this.units) {
            if(unit.remove) continue;
            paidNum++;
            unit.state = PaymentUnit.S.PAID;
            unit.save();
        }
        this.currency = currency;
        this.actualPaid = actualPaid;
        this.paymentDate = new Date();
        this.payer = User.current();
        this.state = S.PAID;
        this.save();
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
     * 返回 Payment 没有删除的 PaymentUnit
     *
     * @return
     */
    public List<PaymentUnit> units() {
        List<PaymentUnit> unRemove = new ArrayList<PaymentUnit>();
        for(PaymentUnit unit : this.units) {
            if(unit.remove) continue;
            unRemove.add(unit);
        }
        return unRemove;
    }


    /**
     * 如果拥有 PaymentUnit 那么返回第一个 PaymentUnit 的 Currency 否则是自己默认的
     *
     * @return
     */
    public Currency firstFeeCurrency() {
        for(PaymentUnit fee : this.units) {
            if(fee.remove) continue;
            return fee.currency;
        }
        return this.currency;
    }


    /**
     * 制作一个 Deliveryment 的支付单
     * 1. 时间(24h 之内)
     * 2. 同一个工厂
     * 3. 出于等待支付状态
     * 4. 额度上线 6W 美金
     *
     * @return
     */
    public static Payment makePayment(Deliveryment deliveryment) {
        DateTime now = DateTime.now();
        Payment payment = Payment.find(
                "SELECT p FROM Payment p LEFT JOIN p.units fee WHERE " +
                        "fee.deliveryment.id=? AND p.createdAt>=? AND p.createdAt<=? AND " +
                        "p.state=? ORDER BY p.createdAt DESC",
                deliveryment.id, now.minusHours(24).toDate(), now.toDate(), S.WAITING).first();

        if(payment == null || payment.totalFees()._1 > 60000) {
            payment = new Payment();
            if(deliveryment.cooperator.paymentMethods.size() <= 0)
                throw new PaymentException(
                        String.format("请添加合作伙伴 %s 的支付方式", deliveryment.cooperator.fullName));
            payment.target = deliveryment.cooperator.paymentMethods.get(0);
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
