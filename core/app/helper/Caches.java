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
     */
    public static final String SALE_SELLING = "salesRankWithTime";

    /**
     * Analyzes 页面的 Ajax 获取分析数据的 Line 图时使用的带有 msku 的缓存 key, 需要补充 msku 值区分唯一, 两个时间的毫秒数.
     */
    public static final String AJAX_SALE_LINE = "ajaxHighChartSelling_%s_%s_%s";
}
