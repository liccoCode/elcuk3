package helper;

/**
 * 固定的缓存的 Key
 * User: wyattpan
 * Date: 1/20/12
 * Time: 11:01 PM
 */
public class Caches {
    /**
     * Selling 的销量排名的 Selling 的缓存 key
     * PS: 缓存 20mn
     */
    public static final String SALE_SELLING = "salesRankWithTime_%s";

    /**
     * Analyzes 页面的 Ajax 获取分析数据的 Line(销量) 图时使用的带有 msku 的缓存 key, 需要补充 msku 值区分唯一, 两个时间的毫秒数.
     * PS: 缓存 20mn
     */
    public static final String AJAX_SALE_LINE = "ajaxHighChartSelling_%s_%s_%s";

    /**
     * Analyzes 页面的 Ajax 获取分析数据的 Line(销售额) 图时使用的带有 msku 的缓存 key, 需要补充 msku 值区分唯一, 两个时间的毫秒数.
     * PS: 缓存 20mn
     */
    public static final String AJAX_PRICE_LINE = "ajaxHighChartSales_%s_%s_%s";

    /**
     * Application 首页所显示的订单数据的情况表格, 需要有一个 [%s] 天数作为 key;
     * PS: 缓存 2h
     */
    public static final String FRONT_ORDER_TABLE = "front_order_table_%s";

    /**
     * 根据不同类型的 Server 给缓存起来, 拥有也 type 参数
     */
    public static final String SERVERS = "server_%s";

    /**
     * 根据 market, cat 来进行缓存不同 Market 与 Category 的 Selling, 给"缺货预警"页面使用
     */
    public static final String WARN_ITEM_SELLING = "warn_item_%s_%s";

}
