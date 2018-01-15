package models.product;

import com.google.gson.annotations.Expose;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 产品分层中第二级别的 Brand 品牌
 * User: wyattpan
 * Date: 4/25/12
 * Time: 3:21 PM
 * @deprecated
 */
@Entity
public class Brand extends GenericModel {

    private static final long serialVersionUID = 2582809738694904003L;

    @OneToMany(mappedBy = "brand", fetch = FetchType.EAGER)
    public List<Family> families;

    /**
     * 品牌名称
     */
    @Id
    @Expose
    public String name;

    @Expose
    @Required
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
        List<Category> categoryList = Category.all().fetch();
        return categoryList.stream().filter(category -> !this.categories.contains(category))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;
        Brand brand = (Brand) o;
        return name != null ? name.equals(brand.name) : brand.name == null;
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
        List<Category> list = Category.find("categoryId IN " + JpqlSelect.inlineParam(cateIds)).fetch();
        list = list.stream().filter(category -> !this.categories.contains(category)).collect(Collectors.toList());
        for(Category cat : list) {
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
        List<Category> list = Category.find("categoryId IN " + JpqlSelect.inlineParam(cateIds)).fetch();
        for(Category cat : list) {
            if(Family.bcRelateFamily(this, cat).size() > 0) {
                Validation.addError("",
                        String.format("Brand %s 与 Category %s 拥有 Family 不允许删除.", this.name, cat.categoryId));
                return;
            }
            cat.brands.remove(this);
            cat.save();
        }
    }

    public static boolean exist(String name) {
        return Brand.count("name=?", name) > 0;
    }

    /**
     * Brand 和 Category 下第一个 Family 的 名称
     *
     * @param categoryId
     * @return
     */
    public String firstFamily(String categoryId) {
        Family fa = Family.find("category_categoryId=? AND brand_name=?", categoryId, this.name).first();
        return fa != null ? fa.family : "";
    }
}
