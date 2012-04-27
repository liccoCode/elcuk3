package models.product;

import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

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
    public String family;

    @OneToOne
    public Category category;

    @OneToOne
    public Brand brand;

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
}
