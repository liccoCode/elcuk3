package controllers;

import helper.J;
import helper.Webs;
import jobs.ListingWorkers;
import models.market.*;
import models.view.Ret;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/19/12
 * Time: 11:59 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class AmazonReviews extends Controller {
    public static void index() {
        render();
    }

    /**
     * 搜索框框中, 进行加载系统内的 Reviews 的数据
     *
     * @param asin
     * @param m
     */
    public static void ajaxMagic(String asin, String m) {
        List<AmazonListingReview> savedReviews = AmazonListingReview.find("listingId=? ORDER BY reviewDate DESC, rating", Listing.lid(asin, Account.M.val(m))).fetch();
        render(savedReviews);
    }

    /**
     * 点击[有/无]用
     */
    public static void click(String reviewId, boolean isUp) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(reviewId);
        Account acc = review.pickUpOneAccountToClick();
        F.T2<AmazonReviewRecord, String> t2 = acc.clickReview(review, isUp);
        renderJSON(J.json(t2));
    }

    public static void reCrawl(String asin, String m) {
        Account.M market = Account.M.val(m);
        String lid = Listing.lid(asin, market);
        try {
            if(!Listing.exist(lid)) {
                // 如果不存在, 先去抓取 Listing 然后再抓取 Review
                Listing.crawl(asin, market).<Listing>save();
            } else {
                new ListingWorkers.R(lid).now().get(30, TimeUnit.SECONDS);
            }
        } catch(Exception e) {
            throw new FastRuntimeException(Webs.S(e));
        }
        renderJSON(new Ret(true, AmazonListingReview.countListingReview(Listing.lid(asin, market)) + ""));
    }

    /**
     * 点击 Amazon Listing 的 Like 按钮
     */
    public static void like(String asin, String m) {
        Account.M market = Account.M.val(m);
        String lid = Listing.lid(asin, market);
        Listing listing = Listing.findById(lid);
        if(listing == null)
            throw new FastRuntimeException("Listing 不存在, 请通过 Amazon Recrawl 来添加.");
        Account acc = listing.pickUpOneAccountToClikeLike();
        F.T2<AmazonLikeRecord, String> t2 = acc.clickLike(listing);
        renderJSON(J.json(t2));
    }

}
