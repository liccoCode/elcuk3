package jobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import helper.HTTP;
import helper.Webs;
import models.Server;
import models.market.AmazonListingReview;
import models.market.Listing;
import play.Logger;
import play.jobs.Job;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 用来更新 Listing 的 Review 信息的线程, 这个线程并不需要执行得那么频繁, 基本上每 24 小时执行一次即可.
 * User: wyattpan
 * Date: 4/18/12
 * Time: 4:57 PM
 */
public class ListingReviewCrawlJob extends Job {
    /**
     * 记录 Listing的 listing 的队列
     */
    public static final LinkedBlockingQueue<String> QUEUE = new LinkedBlockingQueue<String>();

    /**
     * 用来控制线程此线程是否可以继续运行的, 如果任务全部结束, 那么则可以运行, 否则将被跳过这任务
     */
    public static final AtomicBoolean flag = new AtomicBoolean(true);

    private static final Integer CRAW_LISTING_NUM = 3;//一次任务抓取几个 Listing

    @Override
    public void doJob() {
        /**
         * 1. 检查是否可以运行这次任务
         * 2. 从队列中依次拿出任务并根据参数进行速度调节
         * 3. 将任务分发到不同的子 Job 中去
         */
        if(!flag.get()) {
            Logger.info("Last Time Job is not DONE yet...");
            return; // FLAG 为 false 则跳过这次任务
        }


        flag.set(false);
        for(int i = 0; i < CRAW_LISTING_NUM; i++) {
            String listingId = QUEUE.poll();
            if(listingId == null) {
                Logger.debug("Wow! Queue is empty!");
                continue;
            }
            new ReviewWorker(listingId).now();
        }
        flag.set(true);
        Logger.debug("ListingCrawlJob.Queue left " + QUEUE.size());
    }

    public static class ReviewWorker extends Job<AmazonListingReview> {
        private String listingId;

        public ReviewWorker(String listingId) {
            this.listingId = listingId;
        }

        @Override
        public void doJob() {
            Listing listing = Listing.findById(listingId);
            // host/reviews/{market}/{asin}
            JsonElement reviews = null;
            String url = String.format("%s/reviews/%s/%s",
                    Server.server(Server.T.CRAWLER).url, listing.market.name(), listing.asin);
            try {
                reviews = HTTP.json(url);
                /**
                 * 解析出所有的 Reviews, 然后从数据库中加载出此 Listing 对应的所有 Reviews 然后进行判断这些 Reviews 是更新还是新添加?
                 *
                 * 新添加, 直接 Save
                 *
                 * 更新, 需要对某一些字段进行判断后更新并添加 Comment
                 *
                 * TODO 这里单独加载每一个 Review 而不是使用批量加载, 尽管会有性能影响, 但现在这个不是问题的时候不考虑
                 */
                JsonArray array = reviews.getAsJsonArray();
                listing.lastUpdateTime = System.currentTimeMillis();
                for(JsonElement e : array) {
                    AmazonListingReview review = AmazonListingReview.parseAmazonReviewJson(e); // 不是用 merge 是因为有些值需要处理
                    AmazonListingReview fromDB = AmazonListingReview.findById(review.alrId);
                    if(fromDB == null) {
                        review.listing = listing;
                        review.save();// 创建新的
                    } else {
                        fromDB.updateAttr(review); // 更新
                    }
                }
                listing.save();
            } catch(Exception e) {
                Logger.warn("Listing Review[%s] have [%s].", url, Webs.E(e));
            }

        }

    }
}
