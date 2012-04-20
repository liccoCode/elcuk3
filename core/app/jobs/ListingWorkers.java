package jobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import helper.HTTP;
import helper.Webs;
import models.Server;
import models.market.AmazonListingReview;
import models.market.Listing;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;

/**
 * 用来更新 Listing 的 Review 信息的线程, 这个线程并不需要执行得那么频繁, 基本上每 24 小时执行一次即可.
 * User: wyattpan
 * Date: 4/18/12
 * Time: 4:57 PM
 */
public class ListingWorkers extends Job {
    public enum T {
        /**
         * 抓取 Listing
         */
        L,
        /**
         * 抓取 Review
         */
        R
    }

    /**
     * 判断是否创建对应的任务是否在此刻执行进行执行
     *
     * @param t   ListingWorkers.T
     * @param lid 需要处理的 Listing 的 id
     */
    public static void goOrNot(T t, String lid) {
        switch(t) {
            case L:
                new L(lid).doJob(); // 不需要结果, 但需要为每个结果等待 10s
                break;
            case R:
                DateTime now = DateTime.now();
                int hourOfDay = now.getHourOfDay();
                if(hourOfDay >= 3 && hourOfDay <= 5) {
                    new R(lid).doJob();
                } else {
                    Logger.info("Hour Of Day [%s] is not within 3~5", hourOfDay);
                }
                break;
        }
    }

    /**
     * 最后执行 Listing 更新的地方; 使用 Job 的原因是在这里使用了多线程, 但 Play! 在 Job 中开启的线程中使用
     * JPA 保存等的时候无法进行, 所以只能继承 Job 来让 Play! 自动开启 JPA 与事务.
     */
    public static class L extends Job<Listing> {
        private String listingId;

        public L(String listingId) {
            this.listingId = listingId;
        }

        @Override
        public void doJob() {
            Listing listing = Listing.find("listingId=?", listingId).first();
            // Current Only Amazon
            if(listing == null) {
                Logger.error("The Listing Queue have error! Please check it immediately.");
                return;
            }
            if(listing.market.name().contains("EBAY")) {
                Logger.warn("ListingDriverlJob Current not support Ebay Web Site Listing.");
                return;
            }
            try {
                JsonElement lst = HTTP.json(String.format("%s/listings/%s/%s",
                        Server.server(Server.T.CRAWLER).url, listing.market.name(), listing.asin));
                Listing needCheckListing = Listing.parseAndUpdateListingFromCrawl(lst);
                needCheckListing.check();
                needCheckListing.save();
            } catch(Exception e) {
                Logger.warn("ListingDriverlJob[" + listingId + "]:" + e.getClass().getSimpleName() + "|" + e.getMessage());
            }
        }

    }

    public static class R extends Job<AmazonListingReview> {
        private String listingId;

        public R(String listingId) {
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
