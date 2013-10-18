package helper;

import net.sf.ehcache.concurrent.ReadWriteLockSync;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.modules.redis.RedisCacheImpl;

import java.util.Date;
import java.util.Set;

/**
 * 固定的缓存的 Key
 * User: wyattpan
 * Date: 1/20/12
 * Time: 11:01 PM
 */
public class Caches {
    /**
     * 借用了 EhCache 的 Lock
     */
    private static final ReadWriteLockSync LOCK = new ReadWriteLockSync();

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

    public static final String ORDERITEM_AJAXUNITRUNNING = "ajaxHighChartUnitOrder.running";
    public static final String SELLINGRECORD_RUNNING = "sellingRecordCaculateJob.running";

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
            for(Object obj : params) {
                if(obj == null) {
                    sbd.append("null");
                } else if(obj.getClass() == Date.class) {
                    sbd.append(Dates.date2Date((Date) obj));
                } else {
                    sbd.append(obj.toString());
                }
                sbd.append("|");
            }
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

    /**
     * 批量删除所有 redis 中的 running keys
     */
    @SuppressWarnings("unchecked")
    public static void clearRedisRunningKeys() {
        Set<String> runningkeys = RedisCacheImpl.getCacheConnection().keys("*.running");
        if(runningkeys != null && runningkeys.size() > 0) {
            Logger.info("Delete running keys: %s", StringUtils.join(runningkeys, ","));
            RedisCacheImpl.getCacheConnection().del(runningkeys.toArray(new String[runningkeys.size()]));
        }
    }

    /**
     * 有针对性的指定删除某些 running keys
     */
    public static void clearRunningCacheKey() {
        Cache.delete(ORDERITEM_AJAXUNITRUNNING);
        Cache.delete(SELLINGRECORD_RUNNING);
    }
}
