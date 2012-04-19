package jobs;

import models.market.Listing;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 更新系统中的所有 Listing 信息; 与 ListingCrawlJob 配合为典型的 "生产者<->消费者"模式;
 * <p/>
 * 拥有一个更新队列, 这个任务为同时触发 创建需要更新的 Listing 与 消耗队列中的 Listing 的工作;
 * 不让线程自我不停的运行是因为通过此任务可以停止 Listing 的更新
 * <p/>
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:39 AM
 */
public class ListingSchedulJob extends Job {
    @Override
    public void doJob() throws Exception {
        /**
         * 由于所有的 Selling 都是我们自己的选择的, 所以更新时间可以固定; 固定每 40 分钟更新一次 Listing
         * 1. 判断 lastUpdateTime 超额 40 分钟的前 10 个
         * 2. 将计算出来的放入 queue 中
         */
        int MAX_SIZE_PER_TIME = 10;

        long now = System.currentTimeMillis();

        // ------ Listing ---------
        List<Listing> listings = Listing.find("lastUpdateTime<=? ORDER BY lastUpdateTime ASC",
                (now - TimeUnit.MINUTES.toMillis(40))).fetch(MAX_SIZE_PER_TIME);
        Logger.info("[" + new DateTime(now).toString("yyyy-MM-dd HH:mm:ss") + "] have " + listings.size() + " size Listing to be deal!");
        for(Listing li : listings) {
            if(ListingCrawlJob.QUEUE.contains(li.listingId)) {
                Logger.debug("Skip Listing[" + li.listingId + "], it`s exist!");
                continue;
            }
            ListingCrawlJob.QUEUE.add(li.listingId);
            Logger.debug("Add Listing[" + li.listingId + "] to Update Queue.");
        }

        // ----- Review ---------
        List<Listing> reviews = Listing.find("lastUpdateTime<=? ORDER BY lastUpdateTime ASC",
                now - TimeUnit.HOURS.toMillis(20)).fetch(MAX_SIZE_PER_TIME);
        Logger.info("[" + new DateTime(now).toString("yyyy-MM-dd HH:mm:ss") + "] have " + reviews.size() + " size Listing need to fetch Reviews!");
        for(Listing r : reviews) {
            if(ListingReviewCrawlJob.QUEUE.contains(r.listingId)) {
                Logger.debug("Skip Listing[" + r.listingId + "], it`s exist!");
                continue;
            }
            ListingReviewCrawlJob.QUEUE.add(r.listingId);
            Logger.debug("Add Listing[" + r.listingId + "] to Fetch Review Queue.");
        }

    }
}
