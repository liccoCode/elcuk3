package models.product;

import models.User;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 产品的扩展属性
 * User: mac
 * Date: 14-4-14
 * Time: AM9:50
 */
@Entity
public class Attribute extends Model {

    /**
     * 名称
     */
    @Required
    @Column(nullable = false, unique = true)
    public String name;

    public enum T {
        /**
         * 数字类型
         */
        NUMBER {
            @Override
            public String label() {
                return "数字";
            }
        },

        /**
         * 字符串类型
         */
        STRING {
            @Override
            public String label() {
                return "字符串";
            }
        };

        public abstract String label();
    }

    /**
     * 属性类型
     */
    @Required
    @Enumerated(EnumType.STRING)
    public T type;

    /**
     * 展示的顺序
     */
    @Required
    public Integer sort;

    /**
     * 创建人
     */
    @OneToOne
    public User createUser;

    /**
     * 创建日期
     */
    public Date createDate = new Date();

    /**
     * 属性属于哪些模板
     */
    @ManyToMany(mappedBy = "attributes", cascade = CascadeType.PERSIST)
    public List<Template> templates;

    /**
     * 拥有这个属性的产品
     */
    @ManyToMany(mappedBy = "attributes", cascade = CascadeType.PERSIST)
    public List<Product> products;

    public boolean exist() {
        return Attribute.count("name=?", this.name) > 0;
    }

    public void safeDelete() {
        if(this.templates != null && this.templates.size() > 0) {
            Validation.addError("",
                    String.format("拥有 %s 个 Template 关联，无法删除", this.templates.size()));
        }
        if(Validation.hasErrors()) return;
        this.delete();
    }

    /**
     * 更新
     */
    public void update(Attribute att) {
        this.name = att.name;
        this.type = att.type;
        this.sort = att.sort;
    }
}
