package models.material;

import com.google.gson.annotations.Expose;
import helper.Dates;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.Apply;
import models.finance.Payment;
import models.finance.ProcureApply;
import models.procure.Cooperator;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.libs.F;

import javax.persistence.*;
import java.util.*;

/**
 * 物料请款单
 * Created by IntelliJ IDEA.
 * Date: 17/7/4
 * Time: AM11:41
 *
 * @author Even
 */
@Entity
public class MaterialApply extends Apply {


    @OneToMany(mappedBy = "apply", cascade = CascadeType.PERSIST)
    public List<MaterialPlan> materialPlans = new ArrayList<>();

    @OneToMany(mappedBy = "applyPurchase", cascade = CascadeType.PERSIST)
    public List<MaterialPurchase> materialPurchases = new ArrayList<>();

    /**
     * 用来记录 MaterialPlan 中指定的 Cooperator. 为方便的冗余数据
     */
    @OneToOne
    public Cooperator cooperator;

    /**
     * 请款单所拥有的支付信息
     */
    @OneToMany(mappedBy = "mApply")
    public List<Payment> payments = new ArrayList<>();

    /**
     * 请款人
     */
    @ManyToOne
    public User applier;

    public boolean confirm = false;

    /**
     * 状态
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) DEFAULT 'OPEN'")
    public ProcureApply.S status = ProcureApply.S.OPEN;

    /**
     * 物料请款单类型
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    public T type;

    /**
     * 物料请款单类型
     */
    public enum T {
        /**
         * 物料出货单请款单
         */
        PLAN {
            @Override
            public String label() {
                return "物料出货单请款单";
            }
        },
        /**
         * 物料采购单请款单
         */
        PURCHASE {
            @Override
            public String label() {
                return "物料采购单请款单";
            }
        };

        public abstract String label();
    }


    @Override
    public String generateSerialNumber(Cooperator cooper) {
        /**
         * 1. 确定当前的年份
         * 2. 根据年份 + cooperator 确定是今天的第几次请款
         * 3. 生成 PaymentNumber
         */
        this.cooperator = cooper;
        DateTime now = DateTime.now();
        String year = now.toString("yyyy");
        // 找到 2013-01-01 ~ [2014-01-01 (- 1s)]
        long count = MaterialApply.count("cooperator=? AND createdAt>=? AND createdAt<=?",
                this.cooperator,
                Dates.cn(String.format("%s-01-01", year)).toDate(),
                Dates.cn(String.format("%s-01-01", year)).plusYears(1).minusSeconds(1).toDate());
        // count + 1 为新创建的编号
        return String.format("WL-%s-%03d-%s", this.cooperator.name, count + 1, now.toString("yy"));
    }


    /**
     * 查询没有过支付行为请款单
     *
     * @return
     */
    public static List<MaterialApply> unPaidApplies(Long cooperatorId , T type) {
        if(cooperatorId == null) {
            return MaterialApply.find("confirm=false AND type = ? " , type).fetch();
        } else {
            return MaterialApply.find("confirm=false AND type = ? AND cooperator.id=?", type , cooperatorId).fetch();
        }
    }

    /**
     * 通过 materialPlanIds 生成一份采购请款单
     *
     * @param materialPlanIds
     * @return
     */
    public static MaterialApply buildMaterialApply(List<String> materialPlanIds) {
        F.T2<List<MaterialPlan>, Set<Cooperator>> dmtAndCop = materialApplyCheck(materialPlanIds);
        if(Validation.hasErrors()) {
            return null;
        }

        // 生成 ProcureApply
        MaterialApply apply = new MaterialApply();
        apply.serialNumber = apply.generateSerialNumber(dmtAndCop._2.iterator().next());
        apply.createdAt = new Date();
        apply.updateAt = new Date();
        apply.applier = User.current();
        apply.type = T.PLAN;
        apply.save();
        for(MaterialPlan dmt : dmtAndCop._1) {
            dmt.apply = apply;
            dmt.save();
        }
        new ERecordBuilder("materialapply.save")
                .msgArgs(StringUtils.join(materialPlanIds, ","), apply.serialNumber)
                .fid(apply.id)
                .save();
        return apply;
    }

    public static MaterialApply buildMaterialApplyPurchase(List<String> materialPlanIds) {
        F.T2<List<MaterialPurchase>, Set<Cooperator>> dmtAndCop = materialApplyCheckPurchase(materialPlanIds);
        if(Validation.hasErrors()) {
            return null;
        }

        // 生成 ProcureApply
        MaterialApply apply = new MaterialApply();
        apply.serialNumber = apply.generateSerialNumber(dmtAndCop._2.iterator().next());
        apply.createdAt = new Date();
        apply.updateAt = new Date();
        apply.applier = User.current();
        apply.type = T.PURCHASE;
        apply.save();
        for(MaterialPurchase dmt : dmtAndCop._1) {
            dmt.applyPurchase = apply;
            dmt.save();
        }
        new ERecordBuilder("materialapply.save")
                .msgArgs(StringUtils.join(materialPlanIds, ","), apply.serialNumber)
                .fid(apply.id)
                .save();
        return apply;
    }

    /**
     * 向采购请款单中 Append 采购单
     *
     * @param materialPlanIds
     */
    public void appendMaterialApply(List<String> materialPlanIds) {
        F.T2<List<MaterialPlan>, Set<Cooperator>> dmtAndCop = materialApplyCheck(
                materialPlanIds);
        if(dmtAndCop._2.iterator().hasNext() && !dmtAndCop._2.iterator().next().equals(this.cooperator)) {
            Validation.addError("", "合作伙伴不一样, 无法添加");
        }
        if(this.confirm) {
            Validation.addError("", "已经确认了, 不允许再向中添加请款");
        }

        if(Validation.hasErrors()) {
            return;
        }

        for(MaterialPlan dmt : dmtAndCop._1) {
            dmt.apply = this;
            dmt.save();
        }
        new ERecordBuilder("materialapply.save")
                .msgArgs(StringUtils.join(materialPlanIds, ","), this.serialNumber)
                .fid(this.id + "")
                .save();
    }

    public void appendMaterialApplyPurchase(List<String> materialPlanIds) {
        F.T2<List<MaterialPurchase>, Set<Cooperator>> dmtAndCop = materialApplyCheckPurchase(materialPlanIds);
        if(dmtAndCop._2.iterator().hasNext() && !dmtAndCop._2.iterator().next().equals(this.cooperator)) {
            Validation.addError("", "合作伙伴不一样, 无法添加");
        }
        if(this.confirm) {
            Validation.addError("", "已经确认了, 不允许再向中添加请款");
        }

        if(Validation.hasErrors()) {
            return;
        }

        for(MaterialPurchase dmt : dmtAndCop._1) {
            dmt.applyPurchase = this;
            dmt.save();
        }
        new ERecordBuilder("materialapply.save")
                .msgArgs(StringUtils.join(materialPlanIds, ","), this.serialNumber)
                .fid(this.id + "")
                .save();
    }

    /**
     * 校验采购单
     *
     * @param materialPurchaseIds
     * @return
     */
    private static F.T2<List<MaterialPurchase>, Set<Cooperator>> materialApplyCheckPurchase(
            List<String> materialPurchaseIds) {
        /**
         * 0. 检查提交的采购单 ID 数量与加载的出货单数量是否一致
         * 1. 检查请款的供应商是否一致.
         */
        List<MaterialPurchase> materialPurchases = MaterialPurchase.find(JpqlSelect.whereIn("id", materialPurchaseIds))
                .fetch();
        if(materialPurchases.size() != materialPurchases.size()) {
            Validation.addError("", "提交的采购单参数与系统内不符.");
        }
        Set<Cooperator> coopers = new HashSet<>();
        for(MaterialPurchase dmt : materialPurchases) {
            if(dmt.cooperator != null) {
                coopers.add(dmt.cooperator);
            }
        }
        if(coopers.size() > 1) {
            Validation.addError("", "请仅对同一个工厂创建请款单.");
        }
        if(coopers.size() < 1) {
            Validation.addError("", "请款单至少需要一个拥有供应商的出货单.");
        }
        return new F.T2<>(materialPurchases, coopers);
    }


    /**
     * 校验出货单
     *
     * @param materialPlanIds
     * @return
     */
    private static F.T2<List<MaterialPlan>, Set<Cooperator>> materialApplyCheck(List<String> materialPlanIds) {
        /**
         * 0. 检查提交的出货单 ID 数量与加载的出货单数量是否一致
         * 1. 检查请款的供应商是否一致.
         */
        List<MaterialPlan> materialPlans = MaterialPlan.find(JpqlSelect.whereIn("id", materialPlanIds)).fetch();
        if(materialPlans.size() != materialPlans.size()) {
            Validation.addError("", "提交的出货单参数与系统内不符.");
        }
        Set<Cooperator> coopers = new HashSet<>();
        for(MaterialPlan dmt : materialPlans) {
            if(dmt.cooperator != null) {
                coopers.add(dmt.cooperator);
            }
        }
        if(coopers.size() > 1) {
            Validation.addError("", "请仅对同一个工厂创建请款单.");
        }
        if(coopers.size() < 1) {
            Validation.addError("", "请款单至少需要一个拥有供应商的出货单.");
        }
        return new F.T2<>(materialPlans, coopers);
    }


    /**
     * 返回请款单涉及的 Currency
     *
     * @return
     */
    public helper.Currency currencyPurchase() {
        helper.Currency currency = null;
        for(MaterialPurchase dmt : this.materialPurchases) {
            for(MaterialUnit unit : dmt.units) {
                if(currency != null) {
                    break;
                }
                currency = unit.getCurrency();
            }
        }
        return currency;
    }

    /**
     * 返回请款单涉及的 Currency
     *
     * @return
     */
    public helper.Currency currency() {
        helper.Currency currency = null;
        for(MaterialPlan dmt : this.materialPlans) {
            for(MaterialPlanUnit unit : dmt.units) {
                if(currency != null) {
                    break;
                }
                currency = unit.getCurrency();
            }
        }
        return currency;
    }

    /**
     * 已经请款的金额(包含 fixValue)
     *
     * @return
     */
    public float appliedAmountPurchase() {
        float appliedAmount = 0;
        for(MaterialPurchase dmt : this.materialPurchases) {
            for(MaterialUnit unit : dmt.units) {
                appliedAmount += unit.appliedAmount();
            }
        }
        return appliedAmount;
    }

    /**
     * 已经请款的金额(包含 fixValue)
     *
     * @return
     */
    public float appliedAmount() {
        float appliedAmount = 0;
        if(this.type == T.PLAN) {
            for(MaterialPlan dmt : this.materialPlans) {
                for(MaterialPlanUnit unit : dmt.units) {
                    appliedAmount += unit.appliedAmount();
                }
            }
        } else {
            for(MaterialPurchase dmt : this.materialPurchases) {
                for(MaterialUnit unit : dmt.units) {
                    appliedAmount += unit.appliedAmount();
                }
            }
        }
        return appliedAmount;
    }

    public float totalAmountPurchase() {
        float totalAmount = 0;
        for(MaterialPurchase dmt : this.materialPurchases) {
            for(MaterialUnit unit : dmt.units) {
                totalAmount += unit.totalAmount();
            }
        }
        return totalAmount;
    }

    public float totalAmount() {
        float totalAmount = 0;
        for(MaterialPlan dmt : this.materialPlans) {
            for(MaterialPlanUnit unit : dmt.units) {
                totalAmount += unit.totalAmount();
            }
        }
        return totalAmount;
    }

    public float fixValueAmountPurchase() {
        float fixValueAmount = 0;
        for(MaterialPurchase dmt : this.materialPurchases) {
            for(MaterialUnit unit : dmt.units) {
                fixValueAmount += unit.fixValueAmount();
            }
        }
        return fixValueAmount;
    }

    public float fixValueAmount() {
        float fixValueAmount = 0;
        for(MaterialPlan dmt : this.materialPlans) {
            for(MaterialPlanUnit unit : dmt.units) {
                fixValueAmount += unit.fixValueAmount();
            }
        }
        return fixValueAmount;
    }

    public float leftAmountPurchase() {
        return this.totalAmountPurchase() - this.appliedAmountPurchase();
    }

    public float leftAmount() {
        return this.totalAmount() - this.appliedAmount();
    }


    public List<ElcukRecord> records() {
        return ElcukRecord.records(this.id + "", Arrays.asList("materialapply.save", "materialapply.confirm",
                "materialplans.departapply"), 50);
    }

    public void updateAt(long applyId) {
        MaterialApply materialApply = MaterialApply.findById(applyId);
        materialApply.updateAt = new Date();
        materialApply.save();
    }
}
