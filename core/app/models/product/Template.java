package models.product;

import com.google.gson.annotations.Expose;
import models.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.List;

/**
 * Template 这个 Model 只是用来辅助创建 Attribute 和 Product 之间的关系，不与 Product 产生直接关系
 * User: mac
 * Date: 14-4-14
 * Time: AM10:22
 */
@Entity
public class Template extends Model {
    /**
     * 模板名称
     */
    @Required
    public String name;

    /**
     * 模板所拥有的属性
     */
    @ManyToMany(cascade = CascadeType.PERSIST)
    public List<Attribute> attributes;

    /**
     * 模板属于哪些Category
     */
    @ManyToMany(cascade = CascadeType.PERSIST)
    public List<Category> categorys;

    /**
     * 创建人
     */
    @OneToOne
    @Expose
    public User createUser;


    /**
     * 创建日期
     */
    public Date createDate = new Date();

    /**
     * 判断是否存在
     *
     * @param id
     * @return
     */
    public static boolean exist(Long id) {
        return Template.count("id=?", id) > 0;
    }

    /**
     * 删除模板需要进行检查
     */
    public void deleteAttribute() {
        if(this.attributes.size() > 0) {
            Validation.addError("", String.format("%s 模板拥有 %s 个 Attribute 关联，无法删除",
                    this.name, this.attributes.size()));
        }
        if(this.categorys.size() > 0) {
            Validation.addError("", String.format("%s 模板拥有 %s 个 Category 关联，无法删除",
                    this.name, this.categorys.size()));
        }
        if(Validation.hasErrors()) return;
        this.delete();
    }

    /**
     * 模板绑定属性
     *
     * @param attributeIds
     */
    public void bindAttributes(List<Long> attributeIds) {
        List<Attribute> attributes = Attribute.find("id IN" + JpqlSelect.inlineParam(attributeIds)).fetch();
        this.attributes.addAll(attributes);
        this.save();
    }

    /**
     * 模板解除绑定属性
     *
     * @param attributeIds
     */
    public void unBindAttributes(List<Long> attributeIds) {
        List<Attribute> attributes = Attribute.find("id IN" + JpqlSelect.inlineParam(attributeIds)).fetch();
        for(Attribute attribute : attributes) {
            this.attributes.remove(attribute);
        }
        this.save();
    }


    /**
     * 模板绑定 Category
     *
     * @param categoryIds
     */
    public void bindCategorys(List<String> categoryIds) {
        List<Category> categorys = Category.find("categoryId IN" + JpqlSelect.inlineParam(categoryIds)).fetch();
        this.categorys.addAll(categorys);
        this.save();
    }

    /**
     * 模板解绑 Category
     *
     * @param categoryIds
     */
    public void unBindCategorys(List<String> categoryIds) {
        List<Category> categorys = Category.find("categoryId IN" + JpqlSelect.inlineParam(categoryIds)).fetch();
        for(Category category : categorys) {
            this.categorys.remove(category);
        }
        this.save();
    }

    /**
     * 未绑定到模板的属性
     *
     * @return
     */
    public List<Attribute> getUnBindAttributes() {
        List<Attribute> attributes = Attribute.all().fetch();
        if(this.attributes == null) return attributes;
        CollectionUtils.filter(attributes, new FilterUnBindAttributes(this.attributes));
        return attributes;
    }

    /**
     * 未绑定到模板的 Category
     *
     * @return
     */
    public List<Category> getUnBindCategorys() {
        List<Category> categorys = Category.all().fetch();
        if(this.categorys == null) return categorys;
        CollectionUtils.filter(categorys, new FilterUnBindCategorys(this.categorys));
        return categorys;
    }

    private static class FilterUnBindAttributes implements Predicate {
        private List<Attribute> existAttributes;

        private FilterUnBindAttributes(List<Attribute> existAttributes) {
            this.existAttributes = existAttributes;
        }

        @Override
        public boolean evaluate(Object o) {
            Attribute attribute = (Attribute) o;
            return !existAttributes.contains(attribute);
        }
    }

    private static class FilterUnBindCategorys implements Predicate {
        private List<Category> existCategorys;

        private FilterUnBindCategorys(List<Category> existCategorys) {
            this.existCategorys = existCategorys;
        }

        @Override
        public boolean evaluate(Object o) {
            Category category = (Category) o;
            return !existCategorys.contains(category);
        }
    }
}