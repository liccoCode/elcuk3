package models.product;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Product 的扩展属性值
 * User: mac
 * Date: 14-4-17
 * Time: PM2:05
 */
@Entity
public class ProductAttr extends Model {
    /**
     * 属于哪个产品
     */
    @ManyToOne
    public Product product;

    /**
     * 附加的属性
     */
    @OneToOne
    public Attribute attribute;

    /**
     * 附加属性的值
     */
    @Lob
    public String value = " ";
}
