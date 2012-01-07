package models.market;

import models.product.Product;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.util.Date;

/**
 * 订单的具体订单项
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:42 AM
 */
@Entity
public class OrderItem extends Model {
    @ManyToOne
    public Orderr order;

    @OneToOne
    public Selling selling;

    @OneToOne
    public Product product;

    /**
     * 冗余字段, 产品名称
     */
    public String productName;

    /**
     * 冗余字段, 订单项产生的时间
     */
    public Date createDate;

    /**
     * 这个商品的销售
     */
    public Float price;

    /**
     * 如果做促销的话, 具体折扣的价格
     */
    public Float discountPrice;

    /**
     * 这个商品所承担的运输费用
     */
    public Float shippingPrice;

    /**
     * 对不同网站不同情况的 Fee 所产生费用的一个总计(经过 -/+ 计算后的) TODO 暂时没有启用
     */
    public Float feesAmaount;


    /**
     * 商品的数量; 一般情况下这个数量就为 1, 一个订单有多少个 Item 则需要有多少个 Item 项; 但为了避免批发形式的销售, 所以需要拥有一个 quantity 将
     * 批发形式的销售作为一个拥有数量的 OrderItem 来对待;
     */
    public Integer quantity;

    /**
     * 一个中性的记录消息的地方
     */
    public String memo = "";

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof OrderItem)) return false;
        if(!super.equals(o)) return false;

        OrderItem orderItem = (OrderItem) o;

        if(!order.orderId.equals(orderItem.order.orderId)) return false;
        if(!product.sku.equals(orderItem.product.sku)) return false;
        if(!selling.sellingId.equals(orderItem.selling.sellingId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + order.orderId.hashCode();
        result = 31 * result + selling.sellingId.hashCode();
        result = 31 * result + product.sku.hashCode();
        return result;
    }
}
