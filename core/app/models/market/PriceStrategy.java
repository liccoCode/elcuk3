package models.market;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;

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

    @OneToOne
    public Selling selling;

    @Enumerated(EnumType.STRING)
    public T type;

    /**
     * 成本, 计算价格的基础.
     */
    public Float cost;

    public Float margin;

    /**
     * 再怎么调整, 价格不能够低于 lowest!
     * <p/>
     * 最低价格, 可在成本的上下浮动, 这个作为判断调价的最低价格的门槛而不是成本
     */
    public Float lowest;

    // ----------------

    /**
     * 再怎么调整, 价格不能高于 maxMargin 计算出来的值!
     * <p/>
     * 最高利润, 如果价格可以浮动, 那么在最高与最低
     */
    public Float maxMargin;

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
}
