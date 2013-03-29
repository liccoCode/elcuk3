package models.finance;

import exception.PaymentException;
import helper.Currency;
import helper.Dates;
import models.User;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.product.Attach;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.libs.F;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
            public String label() {
                return "等待支付";
            }
        },
        /**
         * 支付完成
         */
        PAID {
            @Override
            public String label() {
                return "完成支付";
            }
        },

        CLOSE {
            @Override
            public String label() {
                return "处理完毕";
            }
        };

        public abstract String label();

    }

    @OneToMany(mappedBy = "payment")
    public List<PaymentUnit> units = new ArrayList<PaymentUnit>();

    /**
     * Payment 所关联的采购请款单;
     * 这里很想做成 Apply 这个父类, 可是 Hibernate 的关系需要有 Model 存在,
     * 而作为 Abstract Class 或者 MappedSuperClass 的 Apply 实际上是不存在的, 所以无法达到这种效果, 只能
     * 退而求其次.
     */
    @ManyToOne
    public ProcureApply pApply;

    @ManyToOne
    public Cooperator cooperator;

    /**
     * 关联谁支付的款项, "谁请款"记载在 Unit 身上
     */
    @ManyToOne
    public User payer;

    public Date createdAt;

    public Date updateAt;

    /**
     * 每一份请款单都会拥有一个唯一的请款单编号. 组成:
     * [Cooperator.name]-[numberOfYear]-[Year(Short)]
     * QW-004-13
     */
    @Required
    public String paymentNumber;

    /**
     * 实际支付时间
     */
    public Date paymentDate;

    /**
     * 支付的时候记录的 USD 现汇买入价
     */
    public Float usdRate = 6.2f;

    public Date usdRatePublishDate;

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
        this.updateAt = new Date();
    }

    @PreUpdate
    public void beforeUpdate() {
        this.updateAt = new Date();
    }

    public void upload(Attach a) throws IOException {
        // 1. save file
        // 2. fork upload to S3
        //todo: Payments 的上传需要特殊处理. 如何上传到 S3?
        FileUtils.copyFile(a.file, new File(a.location));
        a.save();
    }

    /**
     * 从 PaymentUnits 中提取 PaymentTarget, 如果没有 PaymentUnit 那么则返回所有 PaymentTargets
     *
     * @return
     */
    public List<PaymentTarget> paymentTargetFromPaymentUnits() {
        if(this.units().size() > 0) {
            PaymentUnit firstPaymentUnit = this.units().get(0);
            if(firstPaymentUnit.deliveryment != null) {
                return firstPaymentUnit.deliveryment.cooperator.paymentMethods;
            } else {
                //TODO 对 shipment 的判断还需要添加
                return new ArrayList<PaymentTarget>();
            }
        } else {
            return PaymentTarget.findAll();
        }
    }

    /**
     * 对请款单进行支付操作.
     * 1. 请款单中的币种只能拥有一种
     * 2. 检查没有批准的, 支付就是对这个请款单中的所有已经批准的进行支付
     * // 这里的付款已经不用理会币种了, 币种的判断到向 Payment 中添加 PaymentUnit 的入口处判断
     *
     * @param unitIds
     * @param currency
     * @param actualPaid
     * @return
     */
    public int paid(Currency currency, Float actualPaid, Float usRatio, Date ratioPublishTime) {
        int paidNum = 0;
        // 1. 首先验证
        for(PaymentUnit unit : this.units) {
            if(unit.remove) continue;
            if(!Arrays.asList(PaymentUnit.S.APPLY, PaymentUnit.S.APPROVAL).contains(unit.state)) {
                Validation.addError("", String.format("%s 状态拒绝 '支付'", unit.state.label()));
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
        this.usdRate = usRatio;
        this.usdRatePublishDate = ratioPublishTime;
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
        for(PaymentUnit unit : this.units()) {
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
     * 返回和这个请款单有联系的合作者;
     * <p/>
     * ps: 正常情况下每一个 Payment 都应该只有一个合作伙伴, 同时也只有一个币种
     *
     * @return
     */
    public List<Cooperator> cooperators() {
        Set<Cooperator> cooperatorSet = new HashSet<Cooperator>();
        for(PaymentUnit unit : this.units()) {
            if(unit.cooperator() == null) continue;
            cooperatorSet.add(unit.cooperator());
        }
        return new ArrayList<Cooperator>(cooperatorSet);
    }

    /**
     * 对付款单所涉及的某些采购计划进行确认操作
     *
     * @param paymentUnitIds
     */
    public void unitsApproval(List<Long> paymentUnitIds) {
        /**
         * 1. 判断确认是否都是这个付款单的, 如果有其他的报错
         * 2. 判断软删除的, 软删除的不允许处理
         * 3. 判断状态是允许的
         */
        List<Long> existIds = new ArrayList<Long>();
        for(PaymentUnit fee : this.units) {
            existIds.add(fee.id);
        }
        for(Long expectId : paymentUnitIds) {
            if(!existIds.contains(expectId))
                Validation.addError("", expectId + " 不属于付款单 " + this.paymentNumber);
            if(PaymentUnit.<PaymentUnit>findById(expectId).remove)
                Validation.addError("", expectId + " 请款已经软删除");
        }
        for(PaymentUnit unit : this.units) {
            if(!Arrays.asList(PaymentUnit.S.APPLY).contains(unit.state))
                Validation.addError("", String.format("%s 状态拒绝 '批准'", unit.state.label()));
        }

        if(Validation.hasErrors()) return;
        for(PaymentUnit unit : this.units) {
            unit.state = PaymentUnit.S.APPROVAL;
            unit.save();
        }
    }

    /**
     * 批准后的总金额
     *
     * @return
     */
    public float approvalAmount() {
        float approvalAmount = 0;
        for(PaymentUnit fee : this.units()) {
            if(fee.state == PaymentUnit.S.APPLY)
                approvalAmount += fee.amount();
        }
        return approvalAmount;
    }


    /**
     * 制作一个 Deliveryment 的支付单;(自己为自己的工厂方法)
     * 1. 时间(24h 之内)
     * 2. 同一个工厂
     * 3. 处于等待支付状态
     * 4. 额度上线 6W 美金
     *
     * @return
     */
    public static Payment buildPayment(Deliveryment deliveryment, Currency currency) {
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
            payment.cooperator = deliveryment.cooperator;
            payment.target = deliveryment.cooperator.paymentMethods.get(0);
            payment.currency = currency;
            payment.generatePaymentNumber().save();
        }
        return payment;
    }

    /**
     * 计算需要的 PaymentNumber 数据
     *
     * @return
     */
    public Payment generatePaymentNumber() {
        /**
         * 1. 确定当前的年份
         * 2. 根据年份 + cooperator 确定是今天的第几次请款
         * 3. 生成 PaymentNumber
         */
        String year = DateTime.now().toString("yyyy");
        // 找到 2013-01-01 ~ [2014-01-01 (- 1s)]
        long count = Payment.count("cooperator=? AND createdAt>=? AND createdAt<=?",
                this.cooperator,
                Dates.cn(String.format("%s-01-01", year)).toDate(),
                Dates.cn(String.format("%s-01-01", year)).minusSeconds(1).toDate());
        this.paymentNumber = String.format("付款单[%s-%03d-%s]",
                this.cooperator.name, count, DateTime.now().toString("yy"));
        return this;
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
