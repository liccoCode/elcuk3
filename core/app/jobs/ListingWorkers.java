package jobs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.Crawl;
import helper.J;
import helper.Webs;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.market.ListingOffer;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.jobs.Job;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

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
         * 抓取全部的 Offers
         */
        O,
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
                new L(lid).now(); // 不需要结果, 但需要为每个结果等待 10s
                break;
            case R:
                if(Play.mode.isProd()) {
                    int hourOfDay = DateTime.now().getHourOfDay();
                    if(hourOfDay >= 3 && hourOfDay <= 5) {
                        new R(lid).doJob();
                    } else {
                        Logger.debug("Hour Of Day [%s] is not within 3~5", hourOfDay);
                    }
                } else {
                    new R(lid).doJob();
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
                JsonElement lst = Crawl.crawlListing(listing.market.name(), listing.asin);
                Listing needCheckListing = Listing.parseAndUpdateListingFromCrawl(lst, false);
                if(needCheckListing == null) {
                    // 如果为 null , 则 7 天内不在检查
                    listing.lastUpdateTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7);
                    listing.saleRank = -2;// 使用 saleRank 为 -2 来标记此 Listing 已经 CLOSE 了
                    listing.save();
                    return;
                }
                try {
                    if(needCheckListing.totalOffers > 1) {
                        Logger.info("Listing (%s) fetch offers...", needCheckListing.listingId);
                        new O(needCheckListing).now().get(10, TimeUnit.SECONDS); // 等待 10 s
                    }
                } catch(Exception e) {
                    Logger.warn("Listing (%s) no offers.", this.listingId);
                }
                needCheckListing.check();
                needCheckListing.save();
            } catch(Exception e) {
                Logger.warn("ListingDriverlJob[" + listingId + "]:" + e.getClass().getSimpleName() + "|" + e.getMessage());
            }
        }


        /**
         * 这个仅仅附属与 ListingWorker.L , 只有当通过这个方法抓取的 Offers 为 0 的时候,才需要进行详细的 Offers 页面进行一次补充抓取
         */

    }

    public static class O extends Job<Listing> {
        private Listing listing;

        public O(Listing listing) {
            this.listing = listing;
        }

        @Override
        public void doJob() {
            JsonElement offersJson = Crawl.crawlOffers(this.listing.market.name(), this.listing.asin);
            JsonArray offers = offersJson.getAsJsonArray();
            if(this.listing.offers == null) this.listing.offers = new ArrayList<ListingOffer>();
            else this.listing.offers.clear();
            for(JsonElement offer : offers) {
                ListingOffer off = new ListingOffer();
                JsonObject of = offer.getAsJsonObject();
                off.name = of.get("name").getAsString();
                off.offerId = of.get("offerId").getAsString();
                off.price = of.get("price").getAsFloat();
                off.shipprice = of.get("shipprice").getAsFloat();
                off.fba = of.get("fba").getAsBoolean();
                off.buybox = of.get("buybox").getAsBoolean();
                off.cond = ListingOffer.C.val(of.get("cond").getAsString());
                off.listing = this.listing;
                this.listing.offers.add(off);
            }
        }
    }

    /**
     * 抓取 Review 的任务
     */
    public static class R extends Job<AmazonListingReview> {
        /**
         * 以 LisitngId 的形式加载
         */
        private String listingId;

        /**
         * 直接搜索指定 Listing 对象的 Review
         */
        private Listing listing;


        public R(String listingId) {
            this.listingId = listingId;
        }

        public R(Listing listing) {
            this.listing = listing;
        }

        /**
         * 从 ListingId 与 Listing 中选择出与进行搜索的 Listing, 并且会返回是否需要进行更新的标识
         * (如果是 Listing 的话不需要更新.)
         *
         * @return
         */
        private F.T2<Listing, Boolean> choseListing() {
            if(StringUtils.isBlank(this.listingId)) {
                if(this.listing == null) throw new FastRuntimeException("ListingId 与 Listing 必须拥有一个!");
                else return new F.T2<Listing, Boolean>(this.listing, false);
            } else {
                return new F.T2<Listing, Boolean>(Listing.<Listing>findById(this.listingId), true);
            }
        }

        @Override
        public void doJob() {
            F.T2<Listing, Boolean> action = choseListing();
            // host/reviews/{market}/{asin}
            JsonElement reviews = null;
            try {
                reviews = Crawl.crawlReviews(action._1.market.name(), action._1.asin);
                /**
                 * 解析出所有的 Tickets, 然后从数据库中加载出此 Listing 对应的所有 Tickets 然后进行判断这些 Tickets 是更新还是新添加?
                 *
                 * 新添加, 直接 Save
                 *
                 * 更新, 需要对某一些字段进行判断后更新并添加 Comment
                 *
                 * TODO 这里单独加载每一个 Review 而不是使用批量加载, 尽管会有性能影响, 但现在这个不是问题的时候不考虑
                 */
                JsonArray array = reviews.getAsJsonArray();
                action._1.lastUpdateTime = System.currentTimeMillis();
                for(JsonElement e : array) {
                    AmazonListingReview review = AmazonListingReview.parseAmazonReviewJson(e); // 不是用 merge 是因为有些值需要处理
                    AmazonListingReview fromDB = AmazonListingReview.findById(review.alrId);
                    if(fromDB == null) {
                        if(action._1.listingId.equals(review.listingId))
                            review.listing = action._1;
                        else
                            review.listing = Listing.findById(review.listingId);
                        review.createDate = review.reviewDate;
                        review.isOwner = review.listing.product != null;
                        Orderr ord = review.tryToRelateOrderByUserId();
                        if(ord != null) review.orderr = ord;
                        try {
                            review.createReview();// 创建新的
                            review.checkMailAndTicket();
                        } catch(Exception fe) {
                            Logger.warn(Webs.E(fe) + "|" + J.json(Validation.errors()));
                        }
                    } else {
                        fromDB.updateAttr(review); // 更新
                        fromDB.checkMailAndTicket();
                    }
                }
                if(action._2) action._1.save();
            } catch(Exception e) {
                Logger.warn("Listing Review have [%s].", Webs.E(e));
            }

        }

    }
}
