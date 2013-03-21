package models.procure;

import com.google.gson.annotations.Expose;
import models.finance.Payment;
import models.finance.PaymentTarget;
import models.product.Product;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import play.data.validation.Min;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 所有与公司一起的合作者;
 * (使用 Play 1 内置的 validate annotation 做一个测试 model)
 * User: wyattpan
 * Date: 7/16/12
 * Time: 10:58 AM
 */
@Entity
public class Cooperator extends Model {

    public enum T {
        /**
         * 供应商
         */
        SUPPLIER {
            @Override
            public String to_s() {
                return "供应商";
            }
        },
        /**
         * 货代
         */
        SHIPPER {
            @Override
            public String to_s() {
                return "运输商";
            }
        };

        /**
         * 简单的名称
         *
         * @return
         */
        public abstract String to_s();
    }

    /**
     * 只有 SUPPLIER 会使用到此字段, 表示这个 SUPPLIER 可以生产的 Item.
     */
    @OneToMany(mappedBy = "cooperator", fetch = FetchType.LAZY)
    public List<CooperItem> cooperItems = new ArrayList<CooperItem>();

    /**
     * 向这个供应商交易的采购单.
     */
    @OneToMany(mappedBy = "cooperator", fetch = FetchType.LAZY)
    public List<Deliveryment> deliveryments = new ArrayList<Deliveryment>();

    /**
     * 这个合作伙伴的所有可用支付方式
     */
    @OneToMany(mappedBy = "cooper", fetch = FetchType.LAZY)
    public List<PaymentTarget> paymentMethods = new ArrayList<PaymentTarget>();

    /**
     * 这个合作伙伴的所有支付信息
     */
    @OneToMany(mappedBy = "cooperator", fetch = FetchType.LAZY)
    public List<Payment> payments = new ArrayList<Payment>();

    /**
     * 全称
     */
    @Required
    @Expose
    public String fullName;

    /**
     * 简称
     */
    @Required
    @Unique
    @Expose
    public String name;

    /**
     * 地址
     */
    @Required
    @Expose
    public String address;

    /**
     * 联系人
     */
    @Column(length = 32)
    @Required
    @Expose
    public String contacter;

    /**
     * 主要联系电话
     */
    @Column(length = 32)
    @Required
    @Min(7)
    @Expose
    @Phone
    public String phone;

    /**
     * 固定电话
     */
    @Phone
    @Expose
    public String tel;

    /**
     * 传真
     */
    @Phone
    @Expose
    public String fax;

    /**
     * qq 号码
     */
    @Expose
    public String qq;

    /**
     * 旺旺
     */
    @Expose
    public String wangwang;

    /**
     * 备注信息
     */
    @Lob
    public String memo;

    /**
     * 类别的默认交易条款
     */
    @Column(length = 3000)
    public String tradingTerms = " ";

    /**
     * 是什么类型的供应商
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public T type;


    public Cooperator checkAndUpdate() {
        this.check();
        return this.save();
    }

    public Cooperator checkAndSave() {
        this.check();
        return this.save();
    }

    private void check() {
        // 基础的字段验证利用 Play 的验证方法处理了.
        if(this.type == T.SHIPPER && this.cooperItems.size() > 0)
            throw new FastRuntimeException("货物运输商不允许拥有[可生产的商品项目]");

        this.name = this.name.toUpperCase();
    }

    /**
     * 通过 SKU 在此供应商中获取对应的 CooperItem
     *
     * @param sku
     * @return
     */
    public CooperItem cooperItem(String sku) {
        return CooperItem.find("cooperator.id=? AND sku=?", this.id, sku).first();
    }

    /**
     * 前台使用的 Sku 自动提示, 需要过滤掉已经成为此供应商的 Sku
     *
     * @return
     */
    public List<String> frontSkuAutoPopulate() {
        // 需要一份 Clone, 不能修改缓存中的值
        List<String> allSkus = new ArrayList<String>(Product.skus(false));
        final List<String> existSkus = new ArrayList<String>();
        for(CooperItem itm : this.cooperItems) {
            existSkus.add(itm.sku);
        }

        CollectionUtils.filter(allSkus, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                for(String existSku : existSkus) {
                    if(existSku.equals(o.toString())) return false;
                }
                return true;
            }
        });
        return allSkus;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Cooperator that = (Cooperator) o;

        if(this.id != null ? !this.id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.id != null ? this.id.hashCode() : 0);
        return result;
    }

    /**
     * 返回所有供应商
     */
    public static List<Cooperator> suppliers() {
        return Cooperator.find("type=?", T.SUPPLIER).fetch();
    }

    /**
     * 所有快递商
     *
     * @return
     */
    public static List<Cooperator> shippers() {
        return Cooperator.find("type=?", T.SHIPPER).fetch();
    }

}
