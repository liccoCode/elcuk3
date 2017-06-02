package models.procure;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.User;
import models.finance.Payment;
import models.finance.PaymentTarget;
import models.material.Material;
import models.product.Product;
import models.whouse.Whouse;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.data.validation.*;
import play.db.jpa.Model;

import javax.persistence.*;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 所有与公司一起的合作者;
 * (使用 Play 1 内置的 validate annotation 做一个测试 model)
 * User: wyattpan
 * Date: 7/16/12
 * Time: 10:58 AM
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Cooperator extends Model {
    private static final RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.CHINA);

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
    public List<CooperItem> cooperItems = new ArrayList<>();

    /**
     * 向这个供应商交易的采购单.
     */
    @OneToMany(mappedBy = "cooperator", fetch = FetchType.LAZY)
    public List<Deliveryment> deliveryments = new ArrayList<>();

    /**
     * 这个合作伙伴的所有可用支付方式
     */
    @OneToMany(mappedBy = "cooper", fetch = FetchType.LAZY)
    public List<PaymentTarget> paymentMethods = new ArrayList<>();

    /**
     * 这个合作伙伴的所有支付信息
     */
    @OneToMany(mappedBy = "cooperator", fetch = FetchType.LAZY)
    public List<Payment> payments = new ArrayList<>();


    @OneToMany(mappedBy = "cooper", fetch = FetchType.LAZY)
    public List<Shipment> shipments = new ArrayList<>();

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
    public String memo = " ";

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

    /**
     * 首付款
     */
    public int first = 30;

    /**
     * 中期付款
     */
    public int second = 0;

    /**
     * 尾款
     */
    public int tail = 70;

    public enum L {
        MICRO {
            @Override
            public String label() {
                return "微检";
            }
        },
        MILD {
            @Override
            public String label() {
                return "轻度检";
            }
        },
        MEDIUM {
            @Override
            public String label() {
                return "中度检";
            }
        },
        SEVERR {
            @Override
            public String label() {
                return "重度检";
            }
        };

        public abstract String label();
    }

    /**
     * 质检的级别
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public L qcLevel;

    @Lob
    public String instructions = " ";

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

    @OneToOne
    public User creator;

    public Date createDate;


    /**
     * 运输商的仓库
     */
    @OneToMany
    public List<Whouse> whouses;

    public Cooperator checkAndUpdate() {
        this.check();
        if(Validation.hasErrors()) return null;
        this.projectName = Login.current().projectName;
        this.creator = Login.current();
        this.createDate = new Date();
        return this.save();
    }

    public Cooperator checkAndSave() {
        this.check();
        return this.save();
    }

    private void check() {
        // 基础的字段验证利用 Play 的验证方法处理了.
        if(this.type == T.SHIPPER && this.cooperItems.size() > 0)
            Validation.addError("", "货物运输商不允许拥有[可生产的商品项目]");
        if(this.first + this.second + this.tail != 100) {
            Validation.addError("", "付款方式总和必须等于100%，请重新填写。");
        }
        this.name = this.name.toUpperCase();
    }

    /**
     * 通过 SKU 在此供应商中获取对应的 CooperItem
     *
     * @param sku
     * @return
     */
    public CooperItem cooperItem(String sku) {
        CooperItem item = (CooperItem) CollectionUtils.find(this.cooperItems, o -> {
            CooperItem ci = (CooperItem) o;
            return sku.equalsIgnoreCase(ci.sku);
        });
        if(item != null) return item;
        return null;
    }

    /**
     * 检查一个 PaymentTarget 是否属于这个 Cooperator
     *
     * @param paymentTarget
     * @return
     */
    public boolean paymentTargetOwner(PaymentTarget paymentTarget) {
        for(PaymentTarget target : this.paymentMethods) {
            if(target.equals(paymentTarget))
                return true;
        }
        return false;
    }

    /**
     * 前台使用的 Sku 自动提示, 需要过滤掉已经成为此供应商的 Sku
     *
     * @return
     */
    public List<String> frontSkuAutoPopulate() {
        // 需要一份 Clone, 不能修改缓存中的值
        List<String> allSkus = new ArrayList<>(Product.skus(false));
        final List<String> existSkus = this.cooperItems.stream().map(itm -> itm.sku).collect(Collectors.toList());
        CollectionUtils.filter(allSkus, o -> {
            for(String existSku : existSkus) {
                if(existSku.equals(o.toString())) return false;
            }
            return true;
        });
        return allSkus;
    }

    public List<Material> findMaterialNotExistCooper() {
        List<Material> materials = Material.findAll();
        List<CooperItem> items = this.cooperItems.stream()
                .filter(item -> item.type.equals(CooperItem.T.MATERIAL)).collect(Collectors.toList());
        List<Material> notExists = materials.stream().filter(material -> !items.contains(material))
                .collect(Collectors.toList());
        return notExists;
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
        List<Cooperator> cooperators = Cooperator.find("type=?", T.SUPPLIER).fetch();
        cooperators.sort((c1, c2) -> collator.compare(c1.name, c2.name));
        return cooperators;
    }

    /**
     * 返回所有供应商的名称集合
     *
     * @return
     */
    public static List<String> supplierNames() {
        return suppliers().stream().map(co -> co.name).collect(Collectors.toList());
    }

    /**
     * 所有快递商
     *
     * @return
     */
    public static List<Cooperator> shippers() {
        List<Cooperator> cooperators = Cooperator.find("type=?", T.SHIPPER).fetch();
        cooperators.sort((c1, c2) -> collator.compare(c1.name, c2.name));
        return cooperators;
    }


    public long showItemNum(boolean flag) {
        if(flag)
            return this.cooperItems.stream().filter(item -> item.type.equals(CooperItem.T.SKU)).count();
        else
            return this.cooperItems.stream().filter(item -> item.type.equals(CooperItem.T.MATERIAL)).count();
    }

}
