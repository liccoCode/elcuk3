package models.finance;

import helper.Dates;
import models.User;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;

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
    public List<Deliveryment> deliveryments = new ArrayList<Deliveryment>();

    /**
     * 用来记录 Deliveryment 中指定的 Cooperator. 为方便的冗余数据
     */
    @OneToOne
    public Cooperator cooperator;

    /**
     * 请款单所拥有的支付信息
     */
    @OneToMany(mappedBy = "pApply")
    public List<Payment> payments = new ArrayList<Payment>();

    /**
     * 请款人
     */
    @ManyToOne
    public User applier;

    /**
     * 已经请款的金额
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
            for(ProcureUnit unit : dmt.units) {
                totalAmount += unit.totalAmount();
            }
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

    public void generateSerialNumber(Cooperator cooperator) {
        /**
         * 1. 确定当前的年份
         * 2. 根据年份 + cooperator 确定是今天的第几次请款
         * 3. 生成 PaymentNumber
         */
        this.cooperator = cooperator;
        String year = DateTime.now().toString("yyyy");
        // 找到 2013-01-01 ~ [2014-01-01 (- 1s)]
        long count = ProcureApply.count("cooperator=? AND createdAt>=? AND createdAt<=?",
                this.cooperator,
                Dates.cn(String.format("%s-01-01", year)).toDate(),
                Dates.cn(String.format("%s-01-01", year)).plusYears(1).minusSeconds(1).toDate());
        this.serialNumber = String
                .format("%s-%03d-%s", this.cooperator.name, count, DateTime.now().toString("yy"));
    }


    /**
     * 通过 DeliverymentIds 生成一份采购请款单
     *
     * @param deliverymentIds
     * @return
     */
    public static ProcureApply buildProcureApply(List<String> deliverymentIds) {
        /**
         * 1. 检查请款的供应商是否一致.
         * 2. 生成请款单编号
         * 3. 生成 ProcureApply
         */

        List<Deliveryment> deliveryments = Deliveryment
                .find(JpqlSelect.whereIn("id", deliverymentIds)).fetch();
        Set<Cooperator> coopers = new HashSet<Cooperator>();
        for(Deliveryment dmt : deliveryments) {
            coopers.add(dmt.cooperator);
        }
        if(coopers.size() > 1)
            Validation.addError("", "请仅对同一个工厂创建请款单.");
        if(coopers.size() < 1)
            Validation.addError("", "请款单至少需要一个拥有供应商的采购单.");
        if(Validation.hasErrors()) return null;
        ProcureApply apply = new ProcureApply();
        apply.generateSerialNumber(coopers.iterator().next());
        apply.createdAt = apply.updateAt = new Date();
        apply.applier = User.current();
        apply.save();
        for(Deliveryment dmt : deliveryments) {
            dmt.apply = apply;
            dmt.save();
        }
        return apply;
    }
}
