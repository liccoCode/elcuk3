package models.finance;

import exception.PaymentException;
import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.material.MaterialApply;
import models.procure.Cooperator;
import models.product.Attach;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;
import play.i18n.Messages;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 支付单, 真正用于一次的支付操作.
 * User: wyatt
 * Date: 1/24/13
 * Time: 11:35 AM
 */
@Entity
public class Payment extends Model {

    private static final long serialVersionUID = 4422642625850089104L;

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
    public List<PaymentUnit> units = new ArrayList<>();

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

    /**
     * 与物料出货单管理
     */
    @ManyToOne
    public MaterialApply mApply;

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
    public BigDecimal shouldPaid = new BigDecimal(0);

    /**
     * 最终实际支付
     */
    public BigDecimal actualPaid = new BigDecimal(0);

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

    @ManyToOne
    public BatchReviewApply batchReviewApply;

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
        List<Long> existIds = new ArrayList<>();
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
            if(unit.procureUnit != null) {
                new ERecordBuilder("payment.approval")
                        .msgArgs(unit.procureUnit.qty(),
                                unit.procureUnit.sku,
                                "#" + unit.id,
                                unit.feeType.nickName,
                                unit.currency.symbol() + " " + unit.amount())
                        .fid(this.id)
                        .save();
            } else if(unit.materialPlanUnit != null) {
                new ERecordBuilder("payment.approval")
                        .msgArgs(unit.unitQty,
                                unit.materialPlanUnit.material.code,
                                "#" + unit.id,
                                unit.feeType.nickName,
                                unit.currency.symbol() + " " + unit.amount())
                        .fid(this.id)
                        .save();
            }
        }
    }

    private List<PaymentUnit> waitForDealPaymentUnit(List<Long> paymentUnitIds) {
        List<PaymentUnit> paymentUnits = new ArrayList<>();
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

    public void payIt(Long paymentTargetId, Currency currency, Float ratio, Date ratioPublishDate,
                      BigDecimal actualPaid) {
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
        if(!Arrays.asList(Currency.CNY, Currency.USD, Currency.EUR, Currency.GBP, Currency.HKD).contains(currency))
            Validation.addError("", "现在只支持美元与人民币两种支付币种");

        if(!Arrays.asList(S.WAITING, S.LOCKED).contains(this.state))
            Validation.addError("", String.format("%s 状态不允许支付.", this.state.label()));
        if(ratio == null || ratio <= 0)
            Validation.addError("", "汇率的值不合法.");
        if(!Dates.date2Date().equals(Dates.date2Date(ratioPublishDate)))
            Validation.addError("", "汇率时间错误, 并非当前支付的汇率时间.");
        if(Validation.hasErrors()) return;
        for(PaymentUnit unit : this.units()) {
            unit.state = PaymentUnit.S.PAID;
            unit.save();
        }
        this.rate = ratio;
        this.ratePublishDate = ratioPublishDate;
        this.paymentDate = new Date();
        // 切换到最后选择的支付账号
        this.target = paymentTarget;
        this.actualCurrency = currency;
        this.actualPaid = actualPaid;
        this.actualUser = target.accountUser;
        this.actualAccountNumber = target.accountNumber;
        this.payer = User.current();
        this.state = S.PAID;
        this.shouldPaid = new BigDecimal(Webs.scalePointUp(4, Float.parseFloat(this.approvalAmount()) * this.rate));
        this.save();
        //2018-02-05 要求付款单付款操作修改请款单的paymentDate
        if(this.pApply != null) {
            this.pApply.paymentDate = new Date();
            this.pApply.save();
        } else if(this.tApply != null) {
            this.tApply.paymentDate = new Date();
            this.tApply.save();
        } else if(this.mApply != null) {
            this.mApply.paymentDate = new Date();
            this.mApply.save();
        }
        new ERecordBuilder("payment.payit").msgArgs(this.target.toString(),
                this.actualCurrency.symbol() + " " + this.actualPaid, this.rate + "",
                Dates.date2Date(this.ratePublishDate)).fid(this.id).save();
    }

    /**
     * 分别计算 USD 与 CNY 的总金额
     *
     * @return _.1: USD; _.2: CNY; _.3: 当前 Currency
     */
    public F.T3<Float, Float, Float> totalFees() {
        float currentCurrencyAmount = 0;
        Currency lastCurrency = this.currency;
        for(PaymentUnit unit : this.units()) {
            if(lastCurrency != this.currency)
                throw new FastRuntimeException("付款单中的币种不可能不一样, 数据有错误, 请联系开发人员.");
            currentCurrencyAmount += unit.amount();
        }
        return new F.T3<>(currency.toUSD(currentCurrencyAmount), currency.toCNY(currentCurrencyAmount),
                currentCurrencyAmount);
    }

    /**
     * 计算不同状态的数量
     *
     * @param state
     * @return
     */
    public long unitsStateSize(PaymentUnit.S state) {
        return this.units.stream()
                .filter(unit -> !unit.remove && unit.state == state)
                .count();
    }

    /**
     * 返回 Payment 没有删除的 PaymentUnit
     * <p>
     * 数据量比较大的时候用时间换 CPU 和内存: (scroll: https://dzone.com/articles/bulk-fetching-hibernate)
     *
     * @return
     */
    public List<PaymentUnit> units() {
        return this.units.stream()
                .filter(unit -> !unit.remove)
                .collect(Collectors.toList());
    }

    /**
     * 返回和这个请款单有联系的合作者;
     * <p/>
     * ps: 正常情况下每一个 Payment 都应该只有一个合作伙伴, 同时也只有一个币种
     *
     * @return
     */
    public List<Cooperator> cooperators() {
        String sql = "SELECT DISTINCT(p.cooperator_id) AS cooperator_id FROM PaymentUnit p WHERE p.payment_id=?";
        List<Map<String, Object>> rows = DBUtils.rows(sql, this.id);
        List<Long> cooperatorIds = rows.stream().map(row -> row.get("cooperator_id"))
                .filter(Objects::nonNull)
                .map(cooperatorId -> NumberUtils.toLong(cooperatorId.toString()))
                .collect(Collectors.toList());
        if(cooperatorIds != null && cooperatorIds.size() > 0) {
            return Cooperator.find("id IN" + SqlSelect.inlineParam(cooperatorIds)).fetch();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 批准后的总金额
     *
     * @return
     */
    public String approvalAmount() {
        BigDecimal amount = new BigDecimal(0);
        ArrayList<String> shipments = new java.util.ArrayList<>();
        for(PaymentUnit fee : this.units()) {
            if(fee.shipment != null && !shipments.contains(fee.shipment.id)) {
                shipments.add(fee.shipment.id);
            }
        }
        /**
         * 因为运输单的四舍五入的问题，需要先将shipment汇总再四舍五入
         */
        if(shipments.size() > 0) {
            for(String shipmentid : shipments) {
                BigDecimal unitamount = new BigDecimal(0);
                for(PaymentUnit fee : this.units()) {
                    if(Objects.equals(shipmentid, fee.shipment.id)) {
                        if(PaymentUnit.S.DENY != fee.state)
                            unitamount = unitamount.add(fee.decimalamount());
                    }
                }
                amount = amount.add(unitamount.setScale(2, RoundingMode.HALF_UP));
            }
        } else {
            for(PaymentUnit fee : this.units()) {
                if(PaymentUnit.S.DENY != fee.state)
                    amount = amount.add(fee.decimalamount());
            }
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public List<User> applyers() {
        String sql = "SELECT DISTINCT(p.payee_id) AS payee_id FROM PaymentUnit p WHERE p.payment_id=?";
        List<Map<String, Object>> rows = DBUtils.rows(sql, this.id);
        List<Long> payeeIds = rows.stream().map(row -> row.get("payee_id"))
                .filter(Objects::nonNull)
                .map(payeeId -> NumberUtils.toLong(payeeId.toString()))
                .collect(Collectors.toList());
        if(payeeIds != null && payeeIds.size() > 0) {
            return User.find("id IN" + SqlSelect.inlineParam(payeeIds)).fetch();
        } else {
            return Collections.EMPTY_LIST;
        }
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
                .where("createdAt<=?").param(Dates.night(now.toDate()))
                .where("state=?").param(S.WAITING)
                .where("currency=?").param(currency);
        if(apply instanceof TransportApply)
            jpql.where("tApply=?").param(apply);
        else if(apply instanceof ProcureApply)
            jpql.where("pApply=?").param(apply);
        else if(apply instanceof MaterialApply)
            jpql.where("mApply=?").param(apply);

        jpql.orderBy("createdAt DESC");

        Payment payment = Payment.find(jpql.toString(), jpql.getParams().toArray()).first();

        if(payment == null || payment.totalFees()._1 + currency.toUSD(amount) > 230000
                || payment.totalFees()._2 + currency.toCNY(amount) > 1400000) {
            payment = new Payment();
            if(cooper.paymentMethods.size() <= 0)
                throw new PaymentException(Messages.get("paymenttarget.missing", cooper.fullName));
            payment.cooperator = cooper;
            payment.target = cooper.paymentMethods.get(0);
            payment.currency = currency;
            payment.generatePaymentNumber(apply).save();
            Logger.info("新增支付单:" + payment.paymentNumber + " totalUSD:" + payment.totalFees()._1 + currency.toUSD(amount)
                    + "totalCNY:" + payment.totalFees()._2 + currency.toCNY(amount) + "apply:" + apply
                    + "createdAt:>=" + now.minusHours(24).toDate() + "createdAt:<=" + now.toDate());
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
        } else if(apply instanceof MaterialApply) {
            count = Payment.count("mApply=?", apply);
            this.mApply = (MaterialApply) apply;
            this.paymentNumber = String.format("[%s]-%02d", this.mApply.serialNumber, count + 1);
        }
        return this;
    }

    /**
     * 修改 shouldPaid
     *
     * @param shouldPaid
     */
    public void shouldPaid(BigDecimal shouldPaid) {
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
                Arrays.asList("payment.approval", "payment.payit", "payment.uploadDestroy", "payment.cancel"), 50);
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

    public static HighChart queryPerDayAmount() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DATE_FORMAT(p.paymentDate,'%Y-%m-%d') AS per, count(1), sum(u.amount + u.fixValue) as total");
        sql.append(" ,p.currency FROM Payment p LEFT JOIN PaymentUnit u ON p.id = u.payment_id ");
        sql.append(" WHERE p.paymentDate IS NOT NULL ");
        sql.append(" GROUP BY DATE_FORMAT(p.paymentDate,'%Y-%m-%d') ");
        sql.append(" ORDER BY DATE_FORMAT(p.paymentDate,'%Y-%m-%d') DESC");
        HighChart lineChart = new HighChart(Series.LINE);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Series.Line line = new Series.Line("支付单");
        DBUtils.rows(sql.toString()).forEach(row -> {
            try {
                line.add(formatter.parse(row.get("per").toString()),
                        Currency.valueOf(row.get("currency").toString()).toCNY(Float.parseFloat(row.get("total")
                                .toString())));
            } catch(ParseException e) {
                e.printStackTrace();
            }
        });
        lineChart.series(line.sort());
        return lineChart;
    }
}
