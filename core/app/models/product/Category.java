package models.product;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.Caches;
import helper.DBUtils;
import models.embedded.CategorySettings;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import play.cache.*;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.libs.F;
import query.ProductQuery;

import javax.persistence.*;
import javax.persistence.Cache;
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

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public Team team;

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

    public static List<String> categoryIds() {
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

    /**
     * 将获取的Category集合 转换成JSON，便于页面展示
     *
     * @return
     */
    public static F.T2<List<String>, List<String>> fetchCategorysJson() {
        List<String> categorys = Category.categorys(true);
        return new F.T2<List<String>, List<String>>(categorys, categorys);
    }

    /**
     * 返回所有的 SKU
     *
     * @param forceClearCache 是否清除缓存
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("lifetime")
    public static List<String> categorys(boolean forceClearCache) {
        List<String> categorys = null;
        if(forceClearCache) {
            categorys = Category.allcategorys();
            play.cache.Cache.delete(Caches.CATEGORYS);
            play.cache.Cache.add(Caches.CATEGORYS, categorys, "10h");
        } else {
            categorys = play.cache.Cache.get(Caches.CATEGORYS, List.class);
            if(categorys != null) return categorys;
            categorys = Category.allcategorys();
            play.cache.Cache.add(Caches.CATEGORYS, categorys, "10h");
        }
        return categorys;
    }


    /**
     * 加载 Category 的所有
     *
     * @return
     */
    public static List<String> allcategorys() {
        SqlSelect sql = new SqlSelect().select("categoryid").from("Category");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        List<String> categorys = new ArrayList<String>();

        for(Map<String, Object> row : rows) {
            categorys.add(row.get("categoryid").toString());
        }

        return categorys;
    }

    /**
     * 根据 categoryIds 获取对应所有的 sku 集合
     *
     * @param categoryIds
     * @return
     */
    public static List<String> getSKUs(List<String> categoryIds) {
        List<String> skus = new ArrayList<String>();

        List<Map<String, Object>> rows = null;
        if(categoryIds != null && categoryIds.size() > 0) {
            SqlSelect sql = new SqlSelect().select("sku").from("Product")
                    .where(SqlSelect.whereIn("category_categoryId", categoryIds));
            rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        }
        for(Map<String, Object> row : rows) {
            skus.add(row.get("sku").toString());
        }
        return skus;
    }
}
