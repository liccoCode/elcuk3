package models.finance;

import exception.PaymentException;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 所有费用的类型
 * User: wyattpan
 * Date: 3/19/12
 * Time: 10:21 AM
 */
@Entity
public class FeeType extends GenericModel {

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
    public List<FeeType> children;

    @Lob
    public String memo;

    public Date createdAt;
    public Date updateAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
        this.updateAt = this.createdAt;
        if(!Pattern.matches("\\w+", this.name))
            throw new PaymentException("FeeType 的 name 必须为字母");
        this.name = StringUtils.join(StringUtils.split(this.name, " ")).toLowerCase().trim();
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

    /**
     * 最顶层的节点
     *
     * @return
     */
    public static List<FeeType> tops() {
        return FeeType.find("parent IS NULL").fetch();
    }
}
