package models.market;

import helper.Currency;
import models.product.Product;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

/**
 * Created by licco on 16/1/21.
 */
@Entity
public class BtbOrderItem extends Model {

    @ManyToOne
    public Product product;

    public int qty;

    /**
     * 售价
     */
    public BigDecimal price;

    /**
     * 单位
     */
    public Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    public BtbOrder btbOrder;

    public static String findProductName(String sku) {
        Product pro = Product.findById(sku);
        return pro.productName;
    }


}
