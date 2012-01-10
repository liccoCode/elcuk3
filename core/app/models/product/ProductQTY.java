package models.product;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * 将产品的库存单独拿出来进行记录
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午6:00
 */
@Entity
public class ProductQTY extends Model {

    @ManyToOne
    public Product product;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REFRESH})
    public Whouse whouse;

    /**
     * 库存
     */
    public Integer qty = 0;

    /**
     * 仓库中被预定走的
     */
    public Integer pending = 0;

    /**
     * 仓库中不可销售的
     */
    public Integer unsellable = 0;
}
