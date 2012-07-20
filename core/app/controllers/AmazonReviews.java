package controllers;

import helper.J;
import models.market.Account;
import models.market.AmazonListingReview;
import models.market.AmazonReviewRecord;
import models.market.Listing;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

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
     * 点击[有]用
     */
    public static void makeUp(String reviewId) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(reviewId);
        Account acc = review.pickUpOneAccountToClick();
        F.T2<AmazonReviewRecord, String> t2 = acc.clickReview(review, true);
        renderJSON(J.json(t2));
    }

    /**
     * 点击[无]用
     */
    public static void makeDown(String reviewId) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(reviewId);
        Account acc = review.pickUpOneAccountToClick();
        F.T2<AmazonReviewRecord, String> t2 = acc.clickReview(review, true);
        renderJSON(J.json(t2));
    }
}
