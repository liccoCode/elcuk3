package models.product;

import models.User;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 产品的扩展属性
 * User: mac
 * Date: 14-4-14
 * Time: AM9:50
 */
@Entity
public class Attribute extends Model {

    private static final long serialVersionUID = 3065159002460834755L;
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
    @OneToMany(mappedBy = "attribute")
    public List<TemplateAttribute> templateAttributes;

    public boolean exist() {
        return Attribute.count("name=?", this.name) > 0;
    }

    public void safeDelete() {
        if(this.templateAttributes != null && this.templateAttributes.size() > 0) {
            Validation.addError("", String.format("拥有 %s 个 Template 关联，无法删除", this.templateAttributes.size()));
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

    @Override
    public boolean equals(Object other) {
        if(other instanceof ProductAttr) {
            ProductAttr productAttr = (ProductAttr) other;
            return Objects.equals(this.id, productAttr.attribute.id);
        } else if(other instanceof TemplateAttribute) {
            TemplateAttribute templateAttribute = (TemplateAttribute) other;
            return Objects.equals(this.id, templateAttribute.attribute.id);
        }
        return super.equals(other);
    }
}
