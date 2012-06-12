package helper;

import net.sf.ehcache.concurrent.LockType;
import net.sf.ehcache.concurrent.ReadWriteLockSync;
import play.cache.Cache;

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

    /**
     * 阻塞式的获取缓存, 防止并发获取缓存的时候, 重复加载; 将这样的代码写在这里是因为还没有找到为 Play! 替换使用 EhCache 的 BlockingCache 的方法...(Ehcache.xml 也没找到...)
     * 所以在使用了 blockingGet 进行缓存获取的同时, 需要使用 blockingAdd 进行缓存的添加, 因为执行的当前线程需要进行锁的交替, 最后返回结果的时候也需要利用 blockingGet 来获取值
     * 返回, 这样才能释放掉最后记录的 lock count
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T blockingGet(String key, Class<T> clazz) {
        T t;
        try {
            LOCK.lock(LockType.READ); // 所有读取缓存的线程到需要读取锁, 目的是判断是否有线程已经获取了写入锁,如果并且不是当前线程则锁在这里
            t = Cache.get(key, clazz);
        } finally {
            LOCK.unlock(LockType.READ); // 对于当前线程, 如果读取锁没有方式完毕, 是无法获取写入锁的, 所以需要释放读取锁
        }
        if(t == null) {
            try {
                LOCK.lock(LockType.WRITE); // 如果数据为 null 则进行写入, 同时此线程获取写入锁, 看哪一个线程最先获取到唯一的写入锁
                t = Cache.get(key, clazz);
            } finally {
                // 当拥有锁的线程再一次访问缓存的时候, 缓存中有了数据, 则释放写入锁(-1)只有当,当前线程的锁持有写入锁的重入数量为0的时候才能够完全释放掉写入锁,其他线程才可以重新获取写入锁进行写入
                if(t != null) LOCK.unlock(LockType.WRITE);
            }
        }
        return t;
    }

    public static void blockingAdd(String key, Object value) {
        blockingAdd(key, value, null);

    }

    public static void blockingAdd(String key, Object value, String expiration) {
        try {
            LOCK.lock(LockType.WRITE); // 写入时候, 此线程获取写入锁, 如果此线程已经拥了写入锁那么则 hold count + 1
            Cache.add(key, value, expiration);
        } finally {
            LOCK.unlock(LockType.WRITE); // 放在 finally 是因为防止向 Cache 中添加缓存会有异常抛出, 但写入锁必须释放 hold count - 1
        }
    }

}
