package jobs.works;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import helper.Crawl;
import helper.J;
import helper.Webs;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.market.Orderr;
import play.Logger;
import play.data.validation.Validation;
import play.db.DB;
import play.jobs.Job;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/24/12
 * Time: 10:42 AM
 * @deprecated
 */
public class ListingReviewsWork extends Job<Listing> {
    /**
     * 以 LisitngId 的形式加载
     */
    private String listingId;

    public ListingReviewsWork(String listingId) {
        this.listingId = listingId;
    }

    @Override
    public void doJob() {
        Listing listing = Listing.findById(listingId);
        // host/reviews/{market}/{asin}
        JsonElement reviews = null;
        try {
            reviews = Crawl.crawlReviews(listing.market.name(), listing.asin);
            /**
             * 解析出所有的 Tickets, 然后从数据库中加载出此 Listing 对应的所有 Tickets 然后进行判断这些 Tickets 是更新还是新添加?
             *
             * 新添加, 直接 Save
             *
             * 更新, 需要对某一些字段进行判断后更新并添加 Comment
             */
            JsonArray array = reviews.getAsJsonArray();
            Logger.warn(listingId+" array:"+array.size());
            for(JsonElement e : array) {
                AmazonListingReview review = AmazonListingReview.parseAmazonReviewJson(e); // 不是用 merge 是因为有些值需要处理
                AmazonListingReview fromDB = AmazonListingReview.findById(review.alrId);
                Logger.warn(listingId+" reviewid:"+review.reviewId);
                if(fromDB == null) {
                    if(listing.listingId.equals(review.listingId)) {
                        review.listing = listing;
                    } else {
                        review.listing = Listing.findById(review.listingId);
                    }

                    if (review.listing==null){
                        review.listing = Listing.findById(listing.listingId);
                    }

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
                    //加上捕捉，防止报错数据库回滚
                    try {
                        fromDB.updateAttr(review); // 更新
                        fromDB.checkMailAndTicket();
                    } catch(Exception fe) {
                        Logger.warn(Webs.E(fe) + "||" + J.json(Validation.errors()));
                    }
                }
            }
        } catch(Exception e) {
            Logger.warn("Listing Review have [%s].", Webs.E(e));
        } finally {
            listing.lastReviewCheckDate = new Date();
            listing.save();
            try {
                DB.getConnection().commit();
            } catch(Exception e) {
                Logger.warn("db connection [%s].", e.getMessage());
            }
        }

    }
}
