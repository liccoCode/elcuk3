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
public class ProductAttr extends Model implements Comparable {

    private static final long serialVersionUID = -7069181887748292548L;
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

    public void update() {
        ProductAttr productAttr = ProductAttr.findById(this.id);
        productAttr.attribute = this.attribute;
        productAttr.product = this.product;
        productAttr.value = this.value;
        productAttr.save();
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof ProductAttr) {
            if(this.attribute.sort > ((ProductAttr) o).attribute.sort) {
                return 1;
            }
            if(this.attribute.sort < ((ProductAttr) o).attribute.sort) {
                return -1;
            } else {
                return 0;
            }
        }
        return 0;
    }
}
