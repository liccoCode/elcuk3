package models.product;

import play.db.jpa.GenericModel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * 把各种各样的属性全部提取出来, 用来管理属性的名称
 * User: wyattpan
 * Date: 4/26/12
 * Time: 10:27 AM
 */
@Entity
public class AttrName extends GenericModel {

    /**
     * 被哪一些 Category 所引用
     */
    @ManyToMany(mappedBy = "attrNames")
    public List<Category> categories;

    /**
     * 被哪一些 Product 所引用
     */
    @ManyToMany(mappedBy = "attrNames")
    public List<Product> products;

    @Id
    public String name;

    @Column(length = 100)
    public String fullName;

    public String memo;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        AttrName attrName = (AttrName) o;

        if(name != null ? !name.equals(attrName.name) : attrName.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
