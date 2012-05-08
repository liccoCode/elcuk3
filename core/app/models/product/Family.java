package models.product;

import com.google.gson.annotations.Expose;
import play.db.jpa.GenericModel;

import javax.persistence.*;
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

    @OneToOne
    public Category category;

    @OneToOne
    public Brand brand;

    public static List<Family> bcRelateFamily(Brand b, Category c) {
        return Family.find("category=? AND brand=?", c, b).fetch();
    }

    @PrePersist
    private void prePersist() {
        this.family = this.family.toUpperCase();
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
}
