package models.product;

import com.alibaba.fastjson.annotation.JSONField;
import exception.FastException;
import models.market.Listing;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:55 AM
 */
@Entity
public class Product extends Model {
    /**
     * 此产品所能够符合的上架的货架, 不能够集联删除, 删除 Product 是一个很严重的事情!
     * 需要检测 Product 相关的数据
     */
    @OneToMany(mappedBy = "product", cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JSONField(serialize = false)
    public List<Listing> listings;

    @ManyToOne
    public Category category;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH}, orphanRemoval = true)
    public List<Product> relates;

    /**
     * Product 的所有库存;
     * 将产品删除了, 库存不允许删除, 库存记录变为"孤儿"
     */
    @OneToMany(mappedBy = "product", cascade = {CascadeType.ALL}, orphanRemoval = true)
    public List<ProductQTY> qtys;

    /**
     * 唯一的标示
     */
    @Column(nullable = false, unique = true)
    @Required
    public String sku;

    @Required
    public String productName;

    public Float lengths;

    public Float heigh;

    public Float width;

    public Float weight;


    /**
     * 删除 Product 前需要检查与 Product 有直接关系的各种对象.
     */
    @PreRemove
    public void checkDelete() {
        if(this.listings != null && this.listings.size() > 0) {
            throw new FastException("Product [" + this.sku + "] have relate Listing, cannot be delete.");
        }
        for(ProductQTY qty : this.qtys) {
            if(qty.pending + qty.unsellable + qty.qty > 0) {
                throw new FastException("Product [" + this.sku + "] hava quantity, cannot be delete.");
            }
        }

    }
}
