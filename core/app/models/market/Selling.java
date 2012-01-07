package models.market;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * 已经正在进行销售的对象抽象
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:48 AM
 */
@Entity
public class Selling extends Model {

    /**
     * Selling 的状态
     */
    public enum S {
        /**
         * 新创建的, 准备开卖
         */
        NEW,
        /**
         * 已经正常开始进行销售
         */
        SELLING,
        /**
         * 由于没有库存已经自动下架
         */
        NO_INVENTORY,
        /**
         * 手动进行暂停销售, 根据不同网站的规则或者情况达到暂停销售的状态
         */
        HOlD,
        /**
         * 完全下架, 如果可以还能够重新上架
         */
        DOWN
    }

    /**
     * Selling 的类型
     */
    public enum T {
        AMAZON,
        FBA,
        EBAY
    }

    @ManyToOne
    public Listing listing;

    @OneToOne(cascade = CascadeType.ALL)
    public PriceStrategy priceStrategy;

    /**
     * 唯一的 SellingId, [asin]_[market], 这个可重复, 因为会有多个 Selling 实际上
     * 是上架的一个 Listing; 这个字段也想当与一个冗余字段.
     */
    @Column(nullable = false)
    public String sellingId;

    /**
     * 上架后用来唯一标示这个 Selling 的 Id;
     * 1. 在 Amazon 上架的唯一的 merchantSKU;
     * 2. 在 ebay 上架的唯一的 itemId;
     */
    @Column(nullable = false, unique = true)
    public String merchantSKU;

    @Column(nullable = false)
    public String asin;

    @Enumerated(EnumType.STRING)
    public Account.M market;

    @Enumerated(EnumType.STRING)
    public S state;

    @Enumerated(EnumType.STRING)
    public T type;


    /**
     * 动态计算的每天的销量
     */
    @Transient
    public Float ps = 0f;

    public Float price = 0f;

    public Float shippingPrice = 0f;

    /**
     * 这个 Selling 所属的哪一个用户
     */
    @ManyToOne
    public Account account;

    public void setAsin(String asin) {
        this.asin = asin;
        if(this.asin != null && this.market != null)
            this.sellingId = String.format("%s_%s", this.asin, this.market.toString());
    }

    public void setMarket(Account.M market) {
        this.market = market;
        if(this.asin != null && this.market != null)
            this.sellingId = String.format("%s_%s", this.asin, this.market.toString());
    }
}
