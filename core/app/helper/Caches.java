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
    public static final String SALE_SELLING = "salesRankWithTime";

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

}
