package models.product;

import com.google.gson.annotations.Expose;
import helper.Cached;
import helper.Caches;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 根据产品的两级分层再进行 Product 的 SKU 之前的一级细化的 Family 产品族;
 * <p/>
 * User: wyattpan
 * Date: 4/25/12
 * Time: 3:21 PM
 */
@Entity
public class Family extends GenericModel {

    @Id
    @Expose
    public String family;

    @OneToMany(mappedBy = "family")
    public List<Product> products;

    @ManyToOne
    public Category category;

    @ManyToOne
    public Brand brand;

    @PrePersist
    private void prePersist() {
        this.family = this.family.toUpperCase();
    }

    /**
     * 检查并且创建
     */
    public Family checkAndCreate() {
        if(this.brand == null || !this.brand.isPersistent()) throw new FastRuntimeException("没有指定 Brand!");
        if(this.category == null || !this.category.isPersistent()) throw new FastRuntimeException("没有指定 Category!");
        this.checkFamilyFormatValid();
        return this.save();
    }

    /**
     * 检查这个 Family 的名称是否合法
     */
    public void checkFamilyFormatValid() {
        if(this.family.equals(String.format("%s%s", this.category.categoryId, this.brand.name)))
            throw new FastRuntimeException("Family 还需要其他组成部分.");
        if(StringUtils.indexOf(this.family, this.category.categoryId) != 0)
            throw new FastRuntimeException("Family 必须以 " + this.category.categoryId + " 开头!");
        String emptyCategory = StringUtils.replaceOnce(this.family, this.category.categoryId, "");
        if(StringUtils.indexOf(emptyCategory, this.brand.name) != 0)
            throw new FastRuntimeException("Family 中的 Brand " + this.brand.name + " 必须紧接着 Category ");
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Family family1 = (Family) o;

        if(family != null ? !family.equals(family1.family) : family1.family != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (family != null ? family.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return this.family.toUpperCase();
    }

    public static List<Family> bcRelateFamily(Brand b, Category c) {
        return Family.find("category=? AND brand=?", c, b).fetch();
    }

    @SuppressWarnings("unchecked")
    @Cached("1h")
    public static List<String> familys(boolean clearCache) {
        List<String> familys = null;
        if(!clearCache) {
            familys = play.cache.Cache.get(Caches.FAMILYS, List.class);
            if(familys != null) return familys;
        }

        List<Family> familyList = Family.all().fetch();
        familys = new ArrayList<String>();
        for(Family fml : familyList) familys.add(fml.family);
        play.cache.Cache.delete(Caches.FAMILYS);
        play.cache.Cache.add(Caches.FAMILYS, familys, "1h");
        return familys;
    }

    /**
     * Family 下的 Product
     *
     * @return
     */
    public List<Product> products() {
        List<Product> products = Product.find("state <> 'DOWN' AND family_family = ?", this.family).fetch();
        return products;
    }
}
