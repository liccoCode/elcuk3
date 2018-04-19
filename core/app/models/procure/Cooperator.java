package models.procure;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.User;
import models.embedded.ERecordBuilder;
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
import java.util.*;
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

    private static final long serialVersionUID = -6185353048205737293L;

    private static final RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.CHINA);

    public enum T {
        /**
         * 供应商
         */
        SUPPLIER {
            @Override
            public String label() {
                return "供应商";
            }
        },
        /**
         * 货代
         */
        SHIPPER {
            @Override
            public String label() {
                return "运输商";
            }
        };

        /**
         * 简单的名称
         *
         * @return
         */
        public abstract String label();
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
     * 省
     */
    @Expose
    public String province;

    /**
     * 市
     */
    @Expose
    public String city;

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

    /**
     * 物料首付款
     */
    public int materialFirst = 0;

    /**
     * 物料尾款
     */
    public int materialTail = 100;

    /**
     * 是否可见
     */
    public boolean visible = true;

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
                return "轻检";
            }
        },
        MEDIUM {
            @Override
            public String label() {
                return "中检";
            }
        },
        SEVERR {
            @Override
            public String label() {
                return "重检";
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

    public Date updateDate;

    public enum OP {
        WorkShop {
            @Override
            public String label() {
                return "作坊";
            }
        },
        Distributor {
            @Override
            public String label() {
                return "经销商";
            }
        },
        Factory {
            @Override
            public String label() {
                return "厂家";
            }
        };

        public abstract String label();
    }

    public enum C {
        THREE {
            public String label() {
                return "每行3个";
            }
        },
        FOUR {
            public String label() {
                return "每行4个";
            }
        };

        public abstract String label();
    }

    /**
     * 下载条码的格式
     */
    @Enumerated(EnumType.STRING)
    public C barCode;

    /**
     * 性质
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public OP nature;


    /**
     * 运输商的仓库
     */
    @OneToMany
    public List<Whouse> whouses;

    public void checkAndUpdate() {
        this.check();
        if(Validation.hasErrors()) return;
        this.projectName = Login.current().projectName;
        if(this.id == null) {
            this.creator = Login.current();
            this.createDate = new Date();
        } else {
            this.updateDate = new Date();
        }
        this.save();
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
        List<String> allSkus = new ArrayList<>(Product.skus(false));
        final List<String> existSkus = this.cooperItems.stream()
                .filter(itm -> Objects.equals(itm.type, CooperItem.T.SKU))
                .map(itm -> itm.sku).collect(Collectors.toList());
        CollectionUtils.filter(allSkus, o -> {
            for(String existSku : existSkus) {
                if(existSku.equals(o.toString())) return false;
            }
            return true;
        });
        return allSkus;
    }

    public List<Material> findMaterialNotExistCooper() {
        List<Material> materials = Material.find("isDel = 0", null).fetch();
        List<String> items = this.cooperItems.stream()
                .filter(item -> item.type.equals(CooperItem.T.MATERIAL))
                .map(item -> item.material.code).collect(Collectors.toList());
        return materials.stream().filter(material -> !items.contains(material.code)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;
        Cooperator that = (Cooperator) o;
        return this.id != null ? this.id.equals(that.id) : that.id == null;
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
     * 返回所有供应商
     */
    public static List<Cooperator> suppliersForShipment() {
        List<Cooperator> cooperators = Cooperator.find("type=?", T.SHIPPER).fetch();
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

    public boolean showRed() {
        return this.cooperItems.stream().anyMatch(item -> Objects.equals(item.status, CooperItem.S.Pending));
    }

    public static Cooperator findB2bCooperator(User.COR projectName) {
        switch(projectName) {
            case OUTXE:
                return Cooperator.findById(209L);
            case Brandworl:
                return Cooperator.findById(210L);
            case EASYACC:
            default:
                return Cooperator.findById(208L);
        }
    }

    public void createItemByProduct(CooperItem item, Product product) {
        item.cooperator = this;
        item.sku = product.sku.trim();
        item.product = product;
        item.createDate = new Date();
        item.type = CooperItem.T.SKU;
        item.status = CooperItem.S.Pending;
        item.creator = Login.current();
        item.createDate = new Date();
        item.save();
        new ERecordBuilder("copItem.save").msgArgs(this.name, item.sku, item.currency.symbol() + item.price).fid(item.id)
                .save();
    }

    public String containCategory() {
        StringBuffer sb = new StringBuffer();
        List<String> categories = this.cooperItems.stream()
                .filter(item -> Objects.equals(item.type, CooperItem.T.SKU))
                .map(item -> item.product.category.categoryId).distinct().collect(Collectors.toList());
        categories.forEach(category -> sb.append(category).append(","));
        return sb.toString().substring(0, sb.toString().length() > 0 ? sb.toString().length() - 1 : 0);
    }

}
