package models.market;

import com.google.gson.annotations.Expose;
import helper.Currency;
import models.product.Product;
import play.data.validation.Required;
import play.db.jpa.Model;

import javax.persistence.*;
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
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Required
    public Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    public BtbOrder btbOrder;

    /**
     * num, b2b下载pi时用
     */
    @Transient
    public int index;

    public static String findProductName(String sku) {
        Product pro = Product.findById(sku);
        return pro.productName;
    }


}
