package jobs.works;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import helper.Crawl;
import helper.J;
import helper.Webs;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Validation;
import play.jobs.Job;
import play.libs.F;
import play.utils.FastRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/24/12
 * Time: 10:42 AM
 */
public class ListingReviewsWork extends Job<Listing> {
    /**
     * 以 LisitngId 的形式加载
     */
    private String listingId;

    /**
     * 直接搜索指定 Listing 对象的 Review
     */
    private Listing listing;


    public ListingReviewsWork(String listingId) {
        this.listingId = listingId;
    }

    public ListingReviewsWork(Listing listing) {
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
