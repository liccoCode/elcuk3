package models.product;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
import models.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Template 这个 Model 只是用来辅助创建 Attribute 和 Product 之间的关系，不与 Product 产生直接关系
 * User: mac
 * Date: 14-4-14
 * Time: AM10:22
 */
@Entity
public class Template extends Model {
    private static final long serialVersionUID = 7140344406931626159L;
    /**
     * 模板名称
     */
    @Required
    public String name;

    /**
     * 模板所拥有的属性
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.MERGE)
    public List<TemplateAttribute> templateAttributes;

    /**
     * 模板属于哪些 Category
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

    public boolean exist() {
        return Template.count("id=?", id) > 0;
    }

    /**
     * 删除模板需要进行检查
     */
    public void deleteAttribute() {
        if(!this.templateAttributes.isEmpty()) {
            Validation.addError("", String.format("%s 模板拥有 %s 个 Attribute 关联，无法删除",
                    this.name, this.templateAttributes.size()));
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
        for(Long attributeId : attributeIds) {
            Attribute attribute = Attribute.findById(attributeId);
            this.addAttribute(attribute);
        }
    }

    public void addAttribute(Attribute attribute) {
        TemplateAttribute templateAttribute = new TemplateAttribute(this, attribute);
        if(templateAttribute.exists()) {
            throw new FastRuntimeException(String.format("模板[%s] 已经拥有属性[%s]!", this.name, attribute.name));
        }
        templateAttribute.save();
    }

    public void removeAttribute(Attribute attribute) {
        TemplateAttribute templateAttribute = TemplateAttribute.findByTemplateAndAttribute(this, attribute);
        if(!templateAttribute.exists()) {
            throw new FastRuntimeException(String.format("模板[%s] 未关联上属性[%s], 无法删除!", this.name, attribute.name));
        }
        templateAttribute.delete();
    }

    /**
     * 模板解除绑定属性
     *
     * @param attributeIds
     */
    public void unBindAttributes(List<Long> attributeIds) {
        List<Attribute> attributes = Attribute.find("id IN" + JpqlSelect.inlineParam(attributeIds)).fetch();
        for(Attribute attribute : attributes) {
            this.removeAttribute(attribute);
        }
    }

    public void saveDeclare(List<String> isDeclares) {
        StringBuilder sql = new StringBuilder(
                "UPDATE Template_Attribute a SET a.isDeclare = false WHERE a.templates_id = " + id);
        DBUtils.execute(sql.toString());

        for(String isDeclare : isDeclares) {
            sql = new StringBuilder("UPDATE Template_Attribute a SET a.isDeclare = true WHERE ");
            sql.append("a.templates_id = " + id + " AND a.attributes_id = " + isDeclare.split("_")[0]);
            DBUtils.execute(sql.toString());
        }
    }

    /**
     * 模板绑定 Category
     *
     * @param categoryIds
     */
    public void bindCategorys(List<String> categoryIds) {
        List<Category> list = Category.find("categoryId IN" + JpqlSelect.inlineParam(categoryIds)).fetch();
        this.categorys.addAll(list);
        this.save();
    }

    /**
     * 模板解绑 Category
     *
     * @param categoryIds
     */
    public void unBindCategorys(List<String> categoryIds) {
        List<Category> list = Category.find("categoryId IN" + JpqlSelect.inlineParam(categoryIds)).fetch();
        for(Category category : list) {
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
        List<Attribute> attributes = Attribute.all().fetch(200);
        if(this.templateAttributes == null) return attributes;
        CollectionUtils.filter(attributes, new FilterUnBindAttributes(this.templateAttributes));
        return attributes;
    }

    /**
     * 未绑定到模板的 Category
     *
     * @return
     */
    public List<Category> getUnBindCategorys() {
        List<Category> list = Category.all().fetch();
        if(this.categorys == null) return list;
        return list.stream().filter(category -> !this.categorys.contains(category)).collect(Collectors.toList());
    }

    private static class FilterUnBindAttributes implements Predicate {
        private List<TemplateAttribute> existAttributes;

        private FilterUnBindAttributes(List<TemplateAttribute> existAttributes) {
            this.existAttributes = existAttributes;
        }

        @Override
        public boolean evaluate(Object o) {
            Attribute attribute = (Attribute) o;
            return !existAttributes.contains(attribute);
        }
    }
}

