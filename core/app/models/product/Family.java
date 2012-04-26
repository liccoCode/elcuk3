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
}
