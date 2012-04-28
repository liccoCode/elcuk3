package models.product;

import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * 具体需要关联到的 Product 身上去的 Attribute 属性
 * User: wyattpan
 * Date: 4/26/12
 * Time: 10:29 AM
 */
@Entity
public class Attribute extends GenericModel {

    @OneToOne
    public AttrName attName;

    @OneToOne
    public Product product;

    /**
     * 属性 ID, 为  [sku]_[attname]
     */
    @Id
    public String id;

    public String value;
}
