package models.product;

import com.google.gson.annotations.Expose;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.List;

/**
 * 产品分层中第二级别的 Brand 品牌
 * User: wyattpan
 * Date: 4/25/12
 * Time: 3:21 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Brand extends GenericModel {

    @OneToMany(mappedBy = "brand", fetch = FetchType.EAGER)
    public List<Family> families;

    /**
     * 品牌名称
     */
    @Id
    @Expose
    public String name;

    @Expose
    public String fullName;

    @Expose
    @Lob
    public String memo;

    /**
     * Brand 可以附属与很多类别
     */
    @ManyToMany(mappedBy = "brands", cascade = CascadeType.PERSIST)
    public List<Category> categories;

    @PrePersist
    public void prePersist() {
        this.name = this.name.toUpperCase();
    }

    public List<Category> unCategories() {
        List<Category> categories = Category.all().fetch();
        CollectionUtils.filter(categories, new FilterUnCategory(this.categories));
        return categories;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Brand brand = (Brand) o;

        if(name != null ? !name.equals(brand.name) : brand.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return String.format("%s: %s", this.name, this.fullName);
    }

    /**
     * 从 Brand 角度绑定 Category
     *
     * @param cateIds
     */
    public void bindCategories(List<String> cateIds) {
        List<Category> categories = Category.find("categoryId IN " + JpqlSelect.inlineParam(cateIds)).fetch();
        CollectionUtils.filter(categories, new FilterUnCategory(this.categories));
        for(Category cat : categories) {
            cat.brands.add(this);
            cat.save();
        }
    }

    /**
     * 从 Brand 角度接触 Category
     *
     * @param cateIds
     */
    public void unBindCategories(List<String> cateIds) {
        List<Category> categories = Category.find("categoryId IN " + JpqlSelect.inlineParam(cateIds)).fetch();
        for(Category cat : categories) {
            if(Family.bcRelateFamily(this, cat).size() > 0) {
                Validation.addError("", String.format("Brand %s 与 Category %s 拥有 Family 不允许删除.", this.name, cat.categoryId));
                return;
            }
            cat.brands.remove(this);
            cat.save();
        }
    }

    public static boolean exist(String name) {
        return Brand.count("name=?", name) > 0;
    }

    private static class FilterUnCategory implements Predicate {
        private List<Category> categories;

        private FilterUnCategory(List<Category> categories) {
            this.categories = categories;
        }

        @Override
        public boolean evaluate(Object o) {
            Category cat = (Category) o;
            return !categories.contains(cat);
        }
    }
}
