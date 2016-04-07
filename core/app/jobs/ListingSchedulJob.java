package jobs;

import helper.LogUtils;
import helper.Webs;
import models.Jobex;
import models.market.Listing;
import play.Logger;
import play.jobs.Job;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 更新系统中的所有 Listing 信息; 与 ListingDriverlJob 配合为典型的 "生产者<->消费者"模式;
 * <p/>
 * Lising 的调度者, 负责 Listing 的调度作用, 计算哪一些 Listing 需要更新, 并且更新的顺序应该是怎么样的
 * <p/>
 * 周期:
 * - 轮询周期: 1mn
 * - Duration: 5mn
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:39 AM
 * @deprecated
 */
public class ListingSchedulJob extends Job {
    // 生产者

    /**
     * 用来锁住初始化的代码
     */
    private static final Lock lock = new ReentrantLock();

    /**
     * 基础的 Listing 数据, 依照这些数据进行调度如果更新
     */
    private static Map<String, Listing> BASE_LISTING_MAP = new ConcurrentHashMap<String, Listing>();

    /**
     * Listing ID 的任务队列
     */
    private static final Queue<String> WORK_QUEUE = new LinkedBlockingQueue<String>();

    @Override
    public void doJob() throws Exception {
        long begin = System.currentTimeMillis();
        if(!Jobex.findByClassName(ListingSchedulJob.class.getName()).isExcute()) return;
        /**
         * 1. check init Listing queue
         * 2. check is need reload queue?
         */
        iniListingMap(false);

        // 如果 WORK_QUEUE 太大了, 则跳过这一次计算, 等待消耗.
        if(WORK_QUEUE.size() > 5000) {
            Logger.info("Skip Calculate Crawl Listings this time. Queue Size: %s",
                    WORK_QUEUE.size());
            return;
        }

        long now = System.currentTimeMillis();
        try {
            lock.lock();
            for(String listingId : BASE_LISTING_MAP.keySet()) {
                try {
                    Listing lst = BASE_LISTING_MAP.get(listingId);
                    //如果没有 lastUpdateTime 立即执行一次
                    if(lst.lastUpdateTime == null) lst.lastUpdateTime = now - 1;
                    /**
                     * - 超过当前时间了
                     * - 会有考虑如果有的 Listing 执行时间特别长, 导致后面更新不到怎么办? 这就是为什么一次性全部累加到 WORK_QUEUE 的原因, 宁可多更新也要避免更新不到.
                     */
                    if(now - lst.lastUpdateTime >= 0) {
                        if(lst.saleRank == null && lst.saleRank <= 0) continue;
                        WORK_QUEUE.add(listingId);
                        ListingSchedulJob
                                .lastUpdate(listingId, now + ListingSchedulJob.calInterval(lst));
                    }
                } catch(Exception e) {
                    Logger.error("Listing %s Error. -> %s", listingId, Webs.E(e));
                }
            }
        } finally {
            lock.unlock();
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin,"ListingSchedulJob")) {
            LogUtils.JOBLOG
                    .info(String.format("ListingSchedulJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }
    }

    public static String peekListingId() {
        return WORK_QUEUE.poll();
    }

    public static int queueSize() {
        return WORK_QUEUE.size();
    }

    /**
     * 重新初始化 ListingSchedulJob 的 基础 Listing 数据
     *
     * @param force
     */
    public static void iniListingMap(boolean force) {
        if(BASE_LISTING_MAP.size() == 0 || force) {
            try {
                lock.lock();
                if(BASE_LISTING_MAP.size() == 0 || force) {
                    List<Listing> listings = Listing.findAll();
                    if(force) BASE_LISTING_MAP.clear();
                    for(Listing lit : listings) BASE_LISTING_MAP.put(lit.listingId, lit);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 更新内存中的 Listing 时间信息
     *
     * @param listingId
     * @param lastUpdate
     */
    public static void lastUpdate(String listingId, long lastUpdate) {
        Listing lst = BASE_LISTING_MAP.get(listingId);
        if(lst == null) return;
        lst.lastUpdateTime = lastUpdate;
    }

    /**
     * 根据 Listing 计算抓取的间隔
     */
    public static long calInterval(Listing listing) {
        /**
         * 编写计算下一次抓取的间隔的计算算法;
         * 主旨:
         * - 情况变好, 增大检查间隔
         * - 情况变坏, 减小检查间隔
         */
        // 默认周期为 15 分钟; 900s
        long defaultInterval = TimeUnit.MINUTES.toMillis(15);

        // saleRank, review rating, reviews, likes, offers

        // 排名的修正, 需要减去
        long saleRankFix = 0;
        if(listing.saleRank != null) { // Max 120s
            if(listing.saleRank < 5000) saleRankFix += TimeUnit.SECONDS.toMillis(20);
            if(listing.saleRank < 3000) saleRankFix += TimeUnit.SECONDS.toMillis(30);
            if(listing.saleRank < 1000) saleRankFix += TimeUnit.SECONDS.toMillis(40);
            if(listing.saleRank < 500) saleRankFix += TimeUnit.SECONDS.toMillis(50);
            if(listing.saleRank < 100) saleRankFix += TimeUnit.SECONDS.toMillis(60);
        }

        long reviewRatingFix = 0;
        if(listing.rating != null) { // max 120s
            // 评论很好了, 可以增加检查的间隔
            if(listing.rating > 4.5) reviewRatingFix -= TimeUnit.SECONDS.toMillis(20);
            if(listing.rating > 4) reviewRatingFix += TimeUnit.SECONDS.toMillis(20);
            if(listing.rating > 3) reviewRatingFix += TimeUnit.SECONDS.toMillis(30);
            // 比较严重了
            if(listing.rating > 1) reviewRatingFix += TimeUnit.SECONDS.toMillis(50);
        }

        long reviewsFix = 0;
        if(listing.reviews != null) { // max 30s
            if(listing.reviews > 100) reviewsFix += TimeUnit.SECONDS.toMillis(10);
            if(listing.reviews > 30) reviewsFix += TimeUnit.SECONDS.toMillis(10);
            if(listing.reviews > 5) reviewsFix += TimeUnit.SECONDS.toMillis(10);
        }

        long likesFix = 0;
        if(listing.likes != null) { // max 30s
            if(listing.likes > 50) likesFix += TimeUnit.SECONDS.toMillis(10);
            if(listing.likes > 30) likesFix += TimeUnit.SECONDS.toMillis(20);
        }
        long interval = (defaultInterval - saleRankFix - reviewRatingFix - reviewsFix - likesFix);
        return interval < 0 ? TimeUnit.SECONDS.toMillis(30) : interval;
    }
}
