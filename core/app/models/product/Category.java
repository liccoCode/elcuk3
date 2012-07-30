package models.product;

import com.google.gson.annotations.Expose;
import models.embedded.CategorySettings;
import models.support.TicketReason;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 产品分层中第一级别的 Category 类别
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:44
 */
@Entity
public class Category extends GenericModel {

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    @OrderBy("sku")
    public List<Product> products;

    @ManyToMany
    public List<Brand> brands;

    /**
     * Category 本身就必须拥有一定量的 AttrName , 用来限定其下的产品的属性
     */
    @ManyToMany
    public List<AttrName> attrNames;

    @OneToMany(mappedBy = "category")
    public List<TicketReason> reasons = new ArrayList<TicketReason>();

    @Id
    @Expose
    public String categoryId;

    @Column(nullable = false, unique = true)
    @Expose
    public String name;

    @Lob
    @Expose
    public String memo;

    @Embedded
    @Expose
    public CategorySettings settings;

    @Override
    public String toString() {
        return String.format("%s:%s", this.categoryId, this.name);
    }

    public void bindAndUnBindAttrs(List<AttrName> attrNames) {
        /**
         * 删除的时候有什么限制吗?
         *  对于 Category 上的, 删除了就删除了, 在 Product 中已经存在的不理会,让其继续存在就好了,
         *  在这个 Category 下新创建的 Product 才会收到影响
         */
        // 把 Category 所有的清理掉, 然后再重新绑定
        this.attrNames.clear();
        if(attrNames != null)
            for(AttrName at : attrNames) this.attrNames.add(at);
        this.save();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Category category = (Category) o;

        if(categoryId != null ? !categoryId.equals(category.categoryId) : category.categoryId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (categoryId != null ? categoryId.hashCode() : 0);
        return result;
    }
}
