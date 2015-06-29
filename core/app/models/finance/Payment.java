package models.finance;

import exception.PaymentException;
import helper.Currency;
import helper.Dates;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.product.Attach;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.i18n.Messages;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
         * 锁定付款单
         */
        LOCKED {
            @Override
            public String label() {
                return "锁定准备支付";
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

        CANCEL {
            @Override
            public String label() {
                return "取消";
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

    /**
     * 与运输请款单相关联
     */
    @ManyToOne
    public TransportApply tApply;

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
     * [p/tApply.serialNumber]-[付款次数]
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
    public Float rate = 6.2f;

    public Date ratePublishDate;

    /**
     * 应该支付的金额
     */
    public Float shouldPaid = 0f;

    /**
     * 最终实际支付
     */
    public Float actualPaid = 0f;

    /**
     * 最后支付的币种
     */
    @Enumerated(EnumType.STRING)
    public Currency actualCurrency;

    /**
     * 付款单的币种
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

    /**
     * 实际支付账户
     */
    public String actualUser = "";

    /**
     * 实际支付账号
     */
    public String actualAccountNumber = "";

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

    public void cancel(String reason) {
        /**
         * 1. 当付款单中没有任何请款项目的时候可以关闭
         */
        if(StringUtils.isBlank(reason))
            Validation.addError("", "必须填写原因才可以关闭");
        if(Validation.hasErrors()) return;

        if(this.units().size() == 0) {
            this.state = S.CANCEL;
            this.save();
        } else {
            throw new FastRuntimeException("什么请款下才可以让一个付款单被取消呢? (功能未完成)");
        }
        new ERecordBuilder("payment.cancel")
                .msgArgs(reason)
                .fid(this.id)
                .save();
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

        // 对申请的部分做批准处理
        List<PaymentUnit> waitForDeals = this.waitForDealPaymentUnit(paymentUnitIds);
        if(waitForDeals.size() != paymentUnitIds.size())
            Validation.addError("", "提交的需要批准的数据与系统不一致!");

        for(PaymentUnit unit : waitForDeals) {
            if(PaymentUnit.S.APPLY != unit.state)
                Validation.addError("", String.format("%s 状态拒绝 '批准'", unit.state.label()));
        }

        if(Validation.hasErrors()) return;
        for(PaymentUnit unit : waitForDeals) {
            unit.state = PaymentUnit.S.APPROVAL;
            unit.save();
            //ex: 批准 1000 个 71SMP5100-BHSPU(#68) 请款, 金额 ¥ 12000.0
            new ERecordBuilder("payment.approval")
                    .msgArgs(unit.procureUnit.qty(),
                            unit.procureUnit.sku,
                            "#" + unit.id,
                            unit.feeType.nickName,
                            unit.currency.symbol() + " " + unit.amount())
                    .fid(this.id)
                    .save();
        }
    }

    private List<PaymentUnit> waitForDealPaymentUnit(List<Long> paymentUnitIds) {
        List<PaymentUnit> paymentUnits = new ArrayList<PaymentUnit>();
        for(Long id : paymentUnitIds) {
            PaymentUnit unit = PaymentUnit.findById(id);
            if(unit.payment.id.equals(this.id))
                paymentUnits.add(unit);
        }
        return paymentUnits;
    }

    /**
     * 锁定此付款单
     */
    public void lockAndUnLock(boolean lock) {
        if(lock) {
            if(this.state != S.WAITING)
                Validation.addError("", "只允许" + S.WAITING.label() + "状态付款单锁定.");
            if(Validation.hasErrors()) return;
            this.state = S.LOCKED;
        } else {
            if(this.state != S.LOCKED)
                Validation.addError("", "只允许" + S.LOCKED.label() + "状态付款单解锁.");
            if(Validation.hasErrors()) return;
            this.state = S.WAITING;
        }
        this.save();
    }

    public void payIt(Long paymentTargetId, Currency currency, Float ratio, Date ratio_publish_date,
                      Float actualPaid) {
        /**
         * 0. 验证
         *  - paymentTargetId 必须是当前 Payment 的 Cooperator 的某一个支付方式
         *  - 必须所有请款项通过审核才允许付款
         *  - 只能是 WAITING 与 LOCKED 状态才可以支付.
         *  - 汇率必须大于 0
         *  - 汇率的日期必须是当天的.
         * 1. 重新去 boc 中国银行获取最新的美元汇率 + 时间
         * 2. 计算支付
         */
        PaymentTarget paymentTarget = PaymentTarget.findById(paymentTargetId);
        if(!this.cooperator.paymentTargetOwner(paymentTarget))
            Validation.addError("", "所提交的支付账号并非当前付款单的供应商的支付账号!");

        for(PaymentUnit unit : this.units()) {
            if(PaymentUnit.S.APPROVAL != unit.state) {
                Validation.addError("", "还有没有审批完成的请款项, 请审批完成后再付款.");
                break;
            }
        }
        if(!Arrays.asList(Currency.CNY, Currency.USD).contains(currency))
            Validation.addError("", "现在只支持美元与人民币两种支付币种");

        if(!Arrays.asList(S.WAITING, S.LOCKED).contains(this.state))
            Validation.addError("", String.format("%s 状态不允许支付.", this.state.label()));

        if(ratio == null || ratio <= 0)
            Validation.addError("", "汇率的值不合法.");

        if(!Dates.date2Date().equals(Dates.date2Date(ratio_publish_date)))
            Validation.addError("", "汇率时间错误, 并非当前支付的汇率时间.");

        if(Validation.hasErrors()) return;

        for(PaymentUnit unit : this.units()) {
            unit.state = PaymentUnit.S.PAID;
            unit.save();
        }

        this.rate = ratio;
        this.ratePublishDate = ratio_publish_date;
        this.paymentDate = new Date();
        // 切换到最后选择的支付账号
        this.target = paymentTarget;
        this.actualCurrency = currency;
        this.actualPaid = actualPaid;
        this.actualUser = target.accountUser;
        this.actualAccountNumber = target.accountNumber;
        this.payer = User.current();
        this.state = S.PAID;
        this.shouldPaid = Webs.scalePointUp(4, this.approvalAmount() * this.rate);
        this.save();
        new ERecordBuilder("payment.payit")
                .msgArgs(this.target.toString(),
                        this.actualCurrency.symbol() + " " + this.actualPaid,
                        this.rate + "", Dates.date2Date(this.ratePublishDate))
                .fid(this.id)
                .save();
    }

    /**
     * 分别计算 USD 与 CNY 的总金额
     *
     * @return _.1: USD; _.2: CNY; _.3: 当前 Currency
     */
    public F.T3<Float, Float, Float> totalFees() {
        // todo: 将付款的金额限制在 USD 与 CNY
        float currenctCurrencyAmount = 0;
        float usd = 0;
        float cny = 0;
        Currency lastCurrency = this.currency;
        for(PaymentUnit unit : this.units()) {
            if(lastCurrency != this.currency)
                throw new FastRuntimeException("付款单中的币种不可能不一样, 数据有错误, 请联系开发人员.");
            currenctCurrencyAmount += unit.amount();
        }
        return new F.T3<Float, Float, Float>(
                currency.toUSD(currenctCurrencyAmount),
                currency.toCNY(currenctCurrencyAmount),
                currenctCurrencyAmount);
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
     * 批准后的总金额
     *
     * @return
     */
    public float approvalAmount() {
        float approvalAmount = 0;
        BigDecimal amount = new BigDecimal(0);
        for(PaymentUnit fee : this.units()) {
            if(PaymentUnit.S.DENY != fee.state)
                amount = amount.add(new BigDecimal(Float.toString(fee.amount())));
        }
        approvalAmount = amount.setScale(2, RoundingMode.HALF_UP).floatValue();
        return approvalAmount;
    }

    public List<User> applyers() {
        Set<User> users = new HashSet<User>();
        for(PaymentUnit unit : this.units()) {
            users.add(unit.payee);
        }
        return new ArrayList<User>(users);
    }


    /**
     * 制作一个 Deliveryment 的支付单;(自己为自己的工厂方法)
     * 0. 同一个请款单
     * 1. 时间(24h 之内)
     * 2. 同一个工厂
     * 3. 处于等待支付状态
     * 4. 额度上线 200w HKB, 折算成(23w USD 与 140W CNY)
     *
     * @return
     */
    public static <T extends Apply> Payment buildPayment(Cooperator cooper, Currency currency, Float amount, T apply) {
        DateTime now = DateTime.now();
        JpqlSelect jpql = new JpqlSelect();
        jpql.from("Payment")
                .where("cooperator=?").param(cooper)
                .where("createdAt>=?").param(now.minusHours(24).toDate())
                .where("createdAt<=?").param(now.toDate())
                .where("state=?").param(S.WAITING)
                .where("currency=?").param(currency);
        if(apply instanceof TransportApply)
            jpql.where("tApply=?").param(apply);
        else if(apply instanceof ProcureApply)
            jpql.where("pApply=?").param(apply);
        jpql.orderBy("createdAt DESC");

        Payment payment = Payment.find(jpql.toString(), jpql.getParams().toArray()).first();

        if(payment == null ||
                payment.totalFees()._1 + currency.toUSD(amount) > 230000 ||
                payment.totalFees()._2 + currency.toCNY(amount) > 1400000) {
            payment = new Payment();
            if(cooper.paymentMethods.size() <= 0)
                throw new PaymentException(Messages.get("paymenttarget.missing", cooper.fullName));
            payment.cooperator = cooper;
            payment.target = cooper.paymentMethods.get(0);
            payment.currency = currency;
            payment.generatePaymentNumber(apply).save();
        }
        return payment;
    }

    public <T extends Apply> Payment generatePaymentNumber(T apply) {
        /**
         * 1. 确定当前的年份
         * 2. 根据年份 + cooperator 确定是今天的第几次请款
         * 3. 生成 PaymentNumber
         */
        // 找到 2013-01-01 ~ [2014-01-01 (- 1s)]
        long count = 0;
        if(apply instanceof TransportApply) {
            count = Payment.count("tApply=?", apply);
            this.tApply = (TransportApply) apply;
            this.paymentNumber = String.format("[%s]-%02d", this.tApply.serialNumber, count + 1);
        } else if(apply instanceof ProcureApply) {
            count = Payment.count("pApply=?", apply);
            this.pApply = (ProcureApply) apply;
            this.paymentNumber = String.format("[%s]-%02d", this.pApply.serialNumber, count + 1);
        }
        return this;
    }

    /**
     * 修改 shouldPaid
     *
     * @param shouldPaid
     */
    public void shouldPaid(Float shouldPaid) {
        if(shouldPaid == null)
            Validation.addError("", "应付金额必须填写");
        if(Validation.hasErrors()) return;
        // 非 WATING 状态不允许修改
        if(this.state != S.WAITING) return;
        this.shouldPaid = shouldPaid;
        this.save();
    }

    public List<ElcukRecord> records() {
        return ElcukRecord.records(this.id + "",
                Arrays.asList("payment.approval", "payment.payit", "payment.uploadDestroy", "payment.cancel"));
    }


    public List<ElcukRecord> includesItemsRecords() {
        List<ElcukRecord> all = this.records();
        for(PaymentUnit fee : this.units) {
            all.addAll(fee.records());
        }
        Collections.sort(all, new Comparator<ElcukRecord>() {
            @Override
            public int compare(ElcukRecord e1, ElcukRecord e2) {
                return (int) (e2.createAt.getTime() - e1.createAt.getTime());
            }
        });
        return all;
    }

    /**
     * TODO 这里的两个 Get 方法一段时候之后可以删除.
     * (所有 Payment 访问一次后)
     *
     * @return
     */
    public String getActualUser() {
        if(this.state == S.PAID && StringUtils.isBlank(this.actualUser)) {
            this.actualUser = this.target.accountUser;
            this.save();
        }
        return this.actualUser;
    }

    public String getActualAccountNumber() {
        if(this.state == S.PAID && StringUtils.isBlank(this.actualAccountNumber)) {
            this.actualAccountNumber = this.target.accountNumber;
            this.save();
        }
        return actualAccountNumber;
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
