package helper;

import play.cache.Cache;

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
     * 根据不同类型的 Server 给缓存起来, 拥有也 type 参数
     */
    public static final String SERVERS = "server_%s";

    /**
     * 根据 market, cat 来进行缓存不同 Market 与 Category 的 Selling, 给"缺货预警"页面使用
     */
    public static final String WARN_ITEM_SELLING = "warn_item_%s_%s";

    public static final String SKUS = "caches.skus";

    public static final String FAMILYS = "caches.familys";

    /**
     * Query Cache
     */
    public static class Q {
        /**
         * 根据 Params 来获取 Query Cache 的 Key
         *
         * @param params
         * @return
         */
        public static String cacheKey(Object... params) {
            StringBuilder sbd = new StringBuilder();
            for(Object obj : params)
                sbd.append((obj == null) ? "null" : obj.toString()).append("|");
            sbd.deleteCharAt(sbd.length() - 1);
            return sbd.toString();
        }

        /**
         * Default Cache for 1h
         *
         * @param element
         * @param params
         */
        public static void put(Object element, Object... params) {
            Cache.add(Q.cacheKey(params), element, "1h");
        }

        /**
         * 指定缓存多长时间
         *
         * @param element
         * @param exprestion
         * @param params
         */
        public static void put(Object element, String exprestion, Object... params) {
            Cache.add(Q.cacheKey(params), element, exprestion);
        }
    }

}
