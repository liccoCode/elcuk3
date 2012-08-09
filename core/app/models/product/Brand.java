package models.product;

import com.google.gson.annotations.Expose;
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


    @Override
    public String toString() {
        return String.format("%s: %s", this.name, this.fullName);
    }
}
