package models.product;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
import models.embedded.CategorySettings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 产品分层中第一级别的 Category 类别
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:44
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Category extends GenericModel {

    @OneToMany(mappedBy = "category",
            cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    @OrderBy("sku")
    public List<Product> products;

    @ManyToMany(cascade = CascadeType.PERSIST)
    public List<Brand> brands;

    @Id
    @Expose
    public String categoryId;

    @Column(nullable = false, unique = true)
    @Expose
    @Required
    public String name;

    @Lob
    @Expose
    public String memo;

    @Column(length = 3000)
    public String productTerms = " ";

    @Embedded
    @Expose
    public CategorySettings settings = new CategorySettings();

    @Override
    public String toString() {
        return String.format("%s:%s", this.categoryId, this.name);
    }

    /**
     * Category 绑定品牌
     *
     * @param brandIds
     */
    public void bindBrands(List<String> brandIds) {
        List<Brand> brands = Brand.find("name IN " + JpqlSelect.inlineParam(brandIds)).fetch();
        // 过滤出不存在与此 Category 的 brand
        CollectionUtils.filter(brands, new FilterUnBrands(this.brands));
        this.brands.addAll(brands);
        this.save();
    }

    /**
     * 解除绑定
     *
     * @param brandIds
     */
    public void unbindBrands(List<String> brandIds) {
        List<Brand> brands = Brand.find("name IN " + JpqlSelect.inlineParam(brandIds)).fetch();
        for(Brand brand : brands) {
            if(Family.bcRelateFamily(brand, this).size() > 0) {
                Validation.addError("",
                        String.format("Brand %s 与 Category %s 拥有 Family 不允许删除.", brand.name,
                                this.categoryId));
                return;
            }
            this.brands.remove(brand);
        }
        this.save();
    }

    /**
     * 删除这个 Category, 需要进行删除检查
     */
    public void deleteCategory() {
        /**
         * 1. 有没有绑定的 Brand
         * 2. 绑定的每一个 Brand 是否有 Family
         */
        if(this.brands.size() > 0)
            Validation.addError("", String.format("拥有 %s brands 关联, 无法删除.", this.brands.size()));
        if(Validation.hasErrors()) return;
        this.delete();
    }

    /**
     * 没有绑定关系的品牌
     *
     * @return
     */
    public List<Brand> unbrands() {
        List<Brand> brands = Brand.all().fetch();
        CollectionUtils.filter(brands, new FilterUnBrands(this.brands));
        return brands;
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Category category = (Category) o;

        if(categoryId != null ? !categoryId.equals(category.categoryId) :
                category.categoryId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
        return result;
    }

    public static List<String> category_ids() {
        List<Map<String, Object>> rows = DBUtils
                .rows("SELECT categoryId FROM Category ORDER BY categoryId");
        List<String> categoryIds = new ArrayList<String>();
        for(Map<String, Object> row : rows) {
            categoryIds.add(row.get("categoryId").toString());
        }
        return categoryIds;
    }

    public static boolean exist(String id) {
        return Category.count("categoryId=?", id) > 0;
    }

    /**
     * 通过 SKU 返回 Category
     *
     * @param sku
     * @return
     */
    public static String skuToCategoryId(String sku) {
        if(StringUtils.isBlank(sku)) return "";
        return sku.substring(0, 2);
    }

    /**
     * 过滤出没有绑定的 Brands
     */
    private static class FilterUnBrands implements Predicate {
        private List<Brand> existBrands;

        private FilterUnBrands(List<Brand> existBrands) {
            this.existBrands = existBrands;
        }

        @Override
        public boolean evaluate(Object o) {
            Brand brand = (Brand) o;
            return !existBrands.contains(brand);
        }
    }
}
