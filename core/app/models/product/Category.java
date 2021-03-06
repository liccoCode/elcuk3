package models.product;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.Caches;
import helper.DBUtils;
import models.User;
import models.embedded.CategorySettings;
import models.market.M;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 产品分层中第一级别的 Category 类别
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:44
 */
@Entity
public class Category extends GenericModel {

    private static final long serialVersionUID = -140523263681790753L;

    private static final Map<String, List<Category>> CATEGORY_CACHE = new ConcurrentHashMap<>();

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.REFRESH,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    @OrderBy("sku")
    @Expose
    public List<Product> products;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Expose
    public List<Brand> brands;

    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Expose
    public Team team;

    @ManyToMany(cascade = {CascadeType.REFRESH}, mappedBy = "categories", fetch = FetchType.LAZY)
    @Expose
    public List<User> users = new ArrayList<>();

    /**
     * Category拥有哪些模板
     */
    @ManyToMany(mappedBy = "categorys", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Expose
    public List<Template> templates;

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
        List<Brand> brandList = Brand.find("name IN " + JpqlSelect.inlineParam(brandIds)).fetch();
        // 过滤出不存在与此 Category 的 brand
        brandList = brandList.stream().filter(brand -> !this.brands.contains(brand)).collect(Collectors.toList());
        this.brands.addAll(brandList);
        this.save();
    }

    /**
     * 解除绑定
     *
     * @param brandIds
     */
    public void unbindBrands(List<String> brandIds) {
        List<Brand> brandList = Brand.find("name IN " + JpqlSelect.inlineParam(brandIds)).fetch();
        for(Brand brand : brandList) {
            if(Family.bcRelateFamily(brand, this).size() > 0) {
                Validation.addError("",
                        String.format("Brand %s 与 Category %s 拥有 Family 不允许删除.", brand.name, this.categoryId));
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
     */
    public List<Brand> unbrands() {
        List<Brand> brandList = Brand.all().fetch();
        return brandList.stream().filter(brand -> !this.brands.contains(brand)).collect(Collectors.toList());
    }


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;
        Category category = (Category) o;
        return categoryId != null ? categoryId.equals(category.categoryId) : category.categoryId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
        return result;
    }

    public static List<String> categoryIds() {
        List<Map<String, Object>> rows = DBUtils.rows("SELECT categoryId FROM Category ORDER BY categoryId");
        return rows.stream().map(row -> row.get("categoryId").toString()).collect(Collectors.toList());
    }

    public static boolean exist(String id) {
        return Category.count("categoryId=?", id) > 0;
    }

    public static boolean isExistIds(String ids) {
        Category.count("categoryId in (" + ids + ")");
        return Category.count("categoryId in (" + ids + ")") > 0;
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
     * 将获取的Category集合 转换成JSON，便于页面展示
     *
     * @return
     */
    public static F.T2<List<String>, List<String>> fetchCategorysJson() {
        List<String> categorys = Category.categorys(true);
        return new F.T2<>(categorys, categorys);
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
        List<String> categorys = new ArrayList<>();

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
        List<String> skus = new ArrayList<>();

        List<Map<String, Object>> rows = null;
        if(categoryIds != null && categoryIds.size() > 0) {
            SqlSelect sql = new SqlSelect().select("sku").from("Product")
                    .where(SqlSelect.whereIn("category_categoryId", categoryIds));
            rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        }
        if(rows == null) return skus;
        for(Map<String, Object> row : rows) {
            skus.add(row.get("sku").toString());
        }
        return skus;
    }

    public static List<String> getSKUs(String categoryId) {
        if(StringUtils.isBlank(categoryId)) {
            return new ArrayList<>();
        }
        return getSKUs(Arrays.asList(categoryId));
    }

    /**
     * 获取 Category 的 Listing ID
     *
     * @param categoryId
     * @param market     市场
     * @param isOnlySelf 是否只包含自己的(Easyacc) Listing
     * @return
     */
    public static List<String> listingIds(String categoryId, M market, boolean isOnlySelf) {
        List<String> listingIds = new ArrayList<>();
        List<Map<String, Object>> rows = null;
        if(StringUtils.isNotBlank(categoryId)) {
            SqlSelect sql = new SqlSelect().select("Distinct l.listingId AS listingId").from("Listing l")
                    .leftJoin("Product p ON  l.product_sku = p.sku")
                    .leftJoin("Category c ON p.category_categoryId = c.categoryId")
                    .leftJoin("Selling s ON l.listingId = s.listing_listingId");
            if(!categoryId.equalsIgnoreCase("all")) {
                sql.where("c.categoryId = ?").param(categoryId);
            }
            if(market != null) {
                sql.where("l.market = ?").param(market.name());
            }
            if(isOnlySelf) {
                sql.where("c.categoryId IS NOT NULL");
            }
            rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        }
        for(Map<String, Object> row : rows) {
            listingIds.add(row.get("listingId").toString());
        }
        return listingIds;
    }


    /**
     * 获取 Category 的 ASIN
     *
     * @param categoryId
     * @param market     市场
     * @param isOnlySelf 是否是只包含自己的(Easyacc) Listing
     * @return
     */
    public static List<String> asins(String categoryId, M market, boolean isOnlySelf) {
        List<String> asins = null;
        if(categoryId.equalsIgnoreCase("all")) {
            asins = Category.asinsByCategories(null, market, isOnlySelf);
        } else {
            asins = Category.asinsByCategories(Arrays.asList(categoryId), market, isOnlySelf);
        }
        return asins;
    }

    /**
     * 获取 Category 的 ASIN
     *
     * @return
     */
    public static List<String> asinsByCategories(List<String> categories, M market, boolean isOnlySelf) {
        List<String> asins = new ArrayList<>();
        List<Map<String, Object>> rows = null;
        SqlSelect sql = new SqlSelect().select("Distinct l.asin AS asin").from("Listing l")
                .leftJoin("Product p ON  l.product_sku = p.sku")
                .leftJoin("Category c ON p.category_categoryId = c.categoryId")
                .leftJoin("Selling s ON l.listingId = s.listing_listingId ");
        if(categories != null && !categories.isEmpty()) {
            sql.where("c.categoryId IN " + SqlSelect.inlineParam(categories));
        }
        if(market != null) {
            sql.where("l.market = ?").param(market.name());
        }
        if(isOnlySelf) {
            sql.where("c.categoryId IS NOT NULL");
        }
        rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            asins.add(row.get("asin").toString().toLowerCase());
        }
        return asins;
    }

    public static void updateCategory(String username, List<Category> categories) {
        CATEGORY_CACHE.remove(username);
        CATEGORY_CACHE.put(username, categories);
    }

    public static List<Category> categories(String username) {
        List<Category> categories = CATEGORY_CACHE.get(username);
        if(categories == null) {
            CATEGORY_CACHE.put(username, User.findByUserName(username).categories);
            categories = CATEGORY_CACHE.get(username);
        }
        return categories;
    }
}
