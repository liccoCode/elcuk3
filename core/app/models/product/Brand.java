package models.product;

import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import java.util.List;

/**
 * 产品分层中第二级别的 Brand 品牌
 * User: wyattpan
 * Date: 4/25/12
 * Time: 3:21 PM
 */
@Entity
public class Brand extends GenericModel {

    /**
     * 品牌名称
     */
    @Id
    public String name;

    public String memo;

    /**
     * Brand 可以附属与很多类别
     */
    @ManyToMany(mappedBy = "brands")
    public List<Category> categories;

    @PrePersist
    public void prePersist() {
        this.name = this.name.toUpperCase();
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
}
