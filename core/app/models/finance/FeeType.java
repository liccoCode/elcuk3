package models.finance;

import exception.PaymentException;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 所有费用的类型
 * User: wyattpan
 * Date: 3/19/12
 * Time: 10:21 AM
 */
@Entity
public class FeeType extends GenericModel {

    private static final long serialVersionUID = -7697550855014607999L;

    public FeeType() {
    }

    public FeeType(String name, FeeType parent) {
        this.name = name;
        this.parent = parent;
    }

    @Id
    public String name;

    @ManyToOne(cascade = CascadeType.ALL)
    public FeeType parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    public List<FeeType> children = new ArrayList<>();

    /**
     * 这项费用的简单名称
     */
    public String nickName;

    @Lob
    public String memo;

    /**
     * 快捷查询
     */
    public String shortcut;

    public Date createdAt;
    public Date updateAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.updateAt = this.createdAt;
        if(!Pattern.matches("\\w+", this.name))
            throw new PaymentException("FeeType 的 name 必须为字母");
        this.name = StringUtils.join(StringUtils.split(this.name, " ")).toLowerCase().trim();
        if(StringUtils.isBlank(this.nickName))
            this.nickName = this.name;
    }

    @PreUpdate
    public void preUpdate() {
        this.updateAt = new Date();
    }

    /**
     * 删除一个费用类型; 要检查是否可以删除
     */
    public void remove() {
        if(this.children.size() > 0)
            throw new PaymentException(
                    String.format("费用 %s 拥有 %s 个子费用类型, 无法删除.", this.name, this.children.size()));

        long count = SaleFee.count("type=?", this);
        if(count > 0)
            throw new PaymentException(
                    String.format("费用 %s 拥有 %s 个 SaleFee, 无法删除.", this.name, count));
        FeeType.delete("name=?", this.name);
    }

    /**
     * 设置费用的父类型;只允许两级
     *
     * @param parent
     * @return
     */
    public FeeType parent(FeeType parent) {
        if(parent == null) return this;
        if(parent.parent != null)
            throw new FastRuntimeException(String.format("只允许两层父子节点. %s 非顶级节点.", parent.name));
        this.parent = parent;
        return this.save();
    }

    public static FeeType amazon() {
        return FeeType.findById("amazon");
    }

    public static List<FeeType> fbaFees() {
        List<FeeType> amzFees = amazon().children;
        List<FeeType> fbaFees = new ArrayList<>();
        for(FeeType fee : amzFees) {
            //TODO 需要在数据结构上将 FBA 与 Amazon 费用区分开.
            if(fee.name.contains("fba") || fee.name.contains("fulfil")) {
                fbaFees.add(fee);
            }
        }
        return fbaFees;
    }

    /**
     * 寻找所有采购费用类型的子类型
     *
     * @return
     */
    public static List<FeeType> procure() {
        return FeeType.find("parent.name=?", "procure").fetch();
    }

    /**
     * 与物流相关的费用
     *
     * @return
     */
    public static List<FeeType> transports() {
        return FeeType.find("parent.name=?", "transport").fetch();
    }

    /**
     * 最顶层的节点
     *
     * @return
     */
    public static List<FeeType> tops() {
        return FeeType.find("parent IS NULL").fetch();
    }

    /**
     * 固定需要的 采购预付款
     *
     * @return
     */
    public static FeeType cashpledge() {
        return FeeType.findById("cashpledge");
    }

    public static FeeType mediumPayment() {
        return FeeType.findById("mediumpayment");
    }

    /**
     * 固定需要的 采购款
     *
     * @return
     */
    public static FeeType procurement() {
        return FeeType.findById("procurement");
    }

    public static FeeType productCharger() {
        return FeeType.findById("productcharges");
    }

    public static FeeType promotions() {
        return FeeType.findById("promorebates");
    }

    public static FeeType rework() {
        return FeeType.findById("rework");
    }

    /**
     * Amazon 的 Shipping 费用
     *
     * @return
     */
    public static FeeType shipping() {
        return FeeType.findById("shipping");
    }

    /**
     * 快递费
     *
     * @return
     */
    public static FeeType expressFee() {
        return FeeType.findById("transportshipping");
    }

    public static FeeType oceanfreight() {
        return FeeType.findById("oceanfreight");
    }

    public static FeeType airFee() {
        return FeeType.findById("airFee");
    }


    /**
     * 运输关税
     *
     * @return
     */
    public static FeeType dutyAndVAT() {
        return FeeType.findById("dutyandvat");
    }

    public static String mappingTypeName(String name) {
        if(Objects.equals(name, "shippingcharge"))
            return "shipping";
        if(Objects.equals(name, "codchargeback"))
            return "codfee";
        return name;
    }
}
