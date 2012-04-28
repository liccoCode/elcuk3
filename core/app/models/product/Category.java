package models.product;

import play.db.jpa.GenericModel;

import javax.persistence.*;
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

    @Id
    public String categoryId;

    @Column(nullable = false, unique = true)
    public String name;

    @Lob
    public String memo;

    @Override
    public String toString() {
        return String.format("%s:%s", this.categoryId, this.name);
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
