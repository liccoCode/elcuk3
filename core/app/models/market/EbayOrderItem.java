package models.market;

import models.product.Product;
import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/8/7
 * Time: 下午3:32
 */
@Entity
@DynamicUpdate
public class EbayOrderItem extends GenericModel {

    private static final long serialVersionUID = 3027788446309714161L;
    /**
     * 为保持更新的时候的唯一性, 所以将起 Id 设置为 orderId_sku
     */
    @Id
    public String id;

    @ManyToOne(fetch = FetchType.LAZY)
    public EbayOrder order;

    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    public int quantity;

    /**
     * 冗余字段, 订单项产生的时间
     */
    public Date createDate;

}

