package models.product;

import play.db.jpa.GenericModel;

import javax.persistence.Id;

/**
 * 根据产品的两级分层再进行 Product 的 SKU 之前的一级细化的 Family 产品族;
 * <p/>
 * User: wyattpan
 * Date: 4/25/12
 * Time: 3:21 PM
 */
public class Family extends GenericModel {

    @Id
    public String family;

    public Brand brand;

    public Category category;
}
