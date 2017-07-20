package models.finance;

import helper.Dates;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.libs.F;

import javax.persistence.*;
import java.util.*;

/**
 * 具体的采购的请款单
 * User: wyatt
 * Date: 3/26/13
 * Time: 10:13 AM
 */
@Entity
public class ProcureApply extends Apply {

    @OneToMany(mappedBy = "apply", cascade = CascadeType.PERSIST)
    public List<Deliveryment> deliveryments = new ArrayList<>();

    /**
     * 用来记录 Deliveryment 中指定的 Cooperator. 为方便的冗余数据
     */
    @OneToOne
    public Cooperator cooperator;

    /**
     * 请款单所拥有的支付信息
     */
    @OneToMany(mappedBy = "pApply")
    public List<Payment> payments = new ArrayList<>();

    /**
     * 请款人
     */
    @ManyToOne
    public User applier;

    public boolean confirm = false;

    public enum S {

        /**
         * 默认状态
         */
        OPEN {
            @Override
            public String label() {
                return "入库中";
            }
        },
        /**
         * 关闭，请款金额为0
         */
        CLOSE {
            @Override
            public String label() {
                return "结束";
            }
        };

        public abstract String label();
    }

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) DEFAULT 'OPEN'")
    public S status = S.OPEN;

    /**
     * 已经请款的金额(包含 fixValue)
     *
     * @return
     */
    public float appliedAmount() {
        float appliedAmount = 0;
        for(Deliveryment dmt : this.deliveryments) {
            for(ProcureUnit unit : dmt.units) {
                appliedAmount += unit.appliedAmount();
            }
        }
        return appliedAmount;
    }

    public float totalAmount() {
        float totalAmount = 0;
        for(Deliveryment dmt : this.deliveryments) {
            totalAmount += dmt.units.stream().filter(unit -> unit.type != ProcureUnit.T.StockSplit)
                    .mapToDouble(ProcureUnit::totalAmount).sum();
        }
        return totalAmount;
    }

    public float fixValueAmount() {
        float fixValueAmount = 0;
        for(Deliveryment dmt : this.deliveryments) {
            for(ProcureUnit unit : dmt.units) {
                fixValueAmount += unit.fixValueAmount();
            }
        }
        return fixValueAmount;
    }

    public float leftAmount() {
        return this.totalAmount() - this.appliedAmount();
    }

    /**
     * 返回请款单涉及的 Currency
     *
     * @return
     */
    public helper.Currency currency() {
        helper.Currency currency = null;
        for(Deliveryment dmt : this.deliveryments) {
            for(ProcureUnit unit : dmt.units) {
                if(currency != null) break;
                currency = unit.attrs.currency;
            }
        }
        return currency;
    }

    public String generateSerialNumber(Cooperator cooperator) {
        /**
         * 1. 确定当前的年份
         * 2. 根据年份 + cooperator 确定是今天的第几次请款
         * 3. 生成 PaymentNumber
         */
        this.cooperator = cooperator;
        DateTime now = DateTime.now();
        String year = now.toString("yyyy");
        // 找到 2013-01-01 ~ [2014-01-01 (- 1s)]
        long count = ProcureApply.count("cooperator=? AND createdAt>=? AND createdAt<=?",
                this.cooperator,
                Dates.cn(String.format("%s-01-01", year)).toDate(),
                Dates.cn(String.format("%s-01-01", year)).plusYears(1).minusSeconds(1).toDate());
        // count + 1 为新创建的编号
        return String.format("QK-%s-%03d-%s", this.cooperator.name, count + 1, now.toString("yy"));
    }

    public List<ElcukRecord> records() {
        return ElcukRecord.records(this.id + "", Arrays.asList("procureapply.save", "procureunit.editPaySatus",
                "deliveryment.departApply"), 50);
    }

    /**
     * 向采购请款单中 Append 采购单
     *
     * @param deliverymentIds
     */
    public void appendDelivery(List<String> deliverymentIds) {
        F.T2<List<Deliveryment>, Set<Cooperator>> dmtAndCop = procureAddDeliverymentCheck(
                deliverymentIds);
        if(dmtAndCop._2.iterator().hasNext() && !dmtAndCop._2.iterator().next().equals(this.cooperator))
            Validation.addError("", "合作伙伴不一样, 无法添加");
        if(this.confirm)
            Validation.addError("", "已经确认了, 不允许再向中添加请款");

        if(Validation.hasErrors()) return;

        for(Deliveryment dmt : dmtAndCop._1) {
            dmt.apply = this;
            dmt.save();
        }
        new ERecordBuilder("procureapply.save")
                .msgArgs(StringUtils.join(deliverymentIds, ","), this.id)
                .fid(this.id + "")
                .save();
    }

    private static F.T2<List<Deliveryment>, Set<Cooperator>> procureAddDeliverymentCheck(List<String> deliverymentIds) {
        /**
         * 0. 检查提交的采购单 ID 数量与加载的采购单数量是否一致
         * 1. 检查请款的供应商是否一致.
         */
        List<Deliveryment> deliveryments = Deliveryment.find(JpqlSelect.whereIn("id", deliverymentIds)).fetch();
        if(deliverymentIds.size() != deliveryments.size())
            Validation.addError("", "提交的采购单参数与系统内不符.");
        Set<Cooperator> coopers = new HashSet<>();
        for(Deliveryment dmt : deliveryments) {
            if(dmt.cooperator != null) coopers.add(dmt.cooperator);
        }
        if(coopers.size() > 1)
            Validation.addError("", "请仅对同一个工厂创建请款单.");
        if(coopers.size() < 1)
            Validation.addError("", "请款单至少需要一个拥有供应商的采购单.");
        return new F.T2<>(deliveryments, coopers);
    }

    /**
     * 通过 DeliverymentIds 生成一份采购请款单
     *
     * @param deliverymentIds
     * @return
     */
    public static ProcureApply buildProcureApply(List<String> deliverymentIds) {
        F.T2<List<Deliveryment>, Set<Cooperator>> dmtAndCop = procureAddDeliverymentCheck(deliverymentIds);
        if(Validation.hasErrors()) return null;

        // 生成 ProcureApply
        ProcureApply apply = new ProcureApply();
        apply.serialNumber = apply.generateSerialNumber(dmtAndCop._2.iterator().next());
        apply.createdAt = new Date();
        apply.updateAt = new Date();
        apply.applier = User.current();
        apply.save();
        for(Deliveryment dmt : dmtAndCop._1) {
            dmt.apply = apply;
            dmt.save();
        }
        new ERecordBuilder("procureapply.save")
                .msgArgs(StringUtils.join(deliverymentIds, ","), apply.serialNumber)
                .fid(apply.id)
                .save();
        return apply;
    }

    /**
     * 没有过支付行为请款单
     *
     * @return
     */
    public static List<ProcureApply> unPaidApplies(Long cooperatorId) {
        if(cooperatorId == null) {
            return ProcureApply.find("confirm=false").fetch();
        } else {
            return ProcureApply.find("confirm=false AND cooperator.id=?", cooperatorId).fetch();
        }
    }

    /**
     * 当前时间前6个月的请款单，如果请款总额为0,则status变为close
     */
    public static void initApplyStatus() {
        Date date = DateTime.now().minusMonths(6).toDate();
        List<ProcureApply> applies = ProcureApply.find("createdAt>=?", date).fetch();
        applies.stream().filter(apply -> apply.totalAmount() == 0).forEach(apply -> {
            apply.status = S.CLOSE;
            apply.save();
        });
    }


}
