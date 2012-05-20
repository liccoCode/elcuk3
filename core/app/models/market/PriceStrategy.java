package models.market;

import play.db.jpa.Model;

import javax.persistence.*;

/**
 * 每一个 Selling 都拥有一个 PriceStrategy, 控制其调价的所有信息;
 * <p/>
 * 所有在系统内的价格都是为 GBP, 在不同的地方需要计算的时候才利用
 * Currency 转换为不同的币种值
 * <p/>
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午5:37
 */
@Entity
public class PriceStrategy extends Model {

    /**
     * 不同的策略
     */
    public enum T {
        /**
         * 最低价格
         */
        LowestPrice,

        /**
         * 固定价格
         */
        FixedPrice
    }

    public PriceStrategy() {
    }

    /**
     * <pre>
     * 绑定此 Selling 的默认 PriceStrategy 对象
     *  - Type: FixedPrice
     *  - Margin: 0.3
     *  - Lowest: 0.85 * selling.aps.salePrice
     *  - Max: 1.5 * selling.aps.salePrice
     *  - Cost: 0.6 * selling.aps.salePrice
     * </pre>
     *
     * @param selling
     */
    public PriceStrategy(Selling selling) {
        this.selling = selling;
        this.type = T.FixedPrice;
        this.margin = 0.3f;
        this.lowest = selling.aps.salePrice * 0.85f;
        this.max = selling.aps.salePrice * 1.5f;
        this.differ = 0f;
        this.cost = selling.aps.salePrice * 0.6f;
    }

    @OneToOne(mappedBy = "priceStrategy")
    public Selling selling;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public T type;

    /**
     * 成本, 计算价格的基础.
     */
    @Column(nullable = false)
    public Float cost;

    @Column(nullable = false)
    public Float margin;

    /**
     * 再怎么调整, 价格不能够低于 lowest!
     * <p/>
     * 最低价格, 可在成本的上下浮动, 这个作为判断调价的最低价格的门槛而不是成本
     */
    @Column(nullable = false)
    public Float lowest;

    // ----------------

    /**
     * 再怎么调整, 价格不能高于 maxMargin 计算出来的值!
     * <p/>
     * 最高利润, 如果价格可以浮动, 那么在最高与最低
     */
    @Column(nullable = false)
    public Float max;

    /**
     * 当时用匹配 LowestPrice 策略的时候, 允许进行 differ 的微调.
     */
    public Float differ = 0f;

    /**
     * 基础的运费价格
     */
    public Float shippingPrice = 0f;

    /**
     * 多于一个数量后的运费的添加价格
     */
    public Float shippingPlus = 1f;

    public Float[] calculate() {
        //TODO 根据 PriceStrategy 所拥有的信息进行价格的计算; 这里的算法可以很复杂 @_@
        return new Float[]{10f, 0f};
    }

    public Float getDiffer() {
        if(this.differ == null) this.differ = 0f;
        return this.differ;
    }
}
