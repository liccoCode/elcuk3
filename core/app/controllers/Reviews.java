package controllers;

import helper.J;
import models.market.*;
import models.view.Ret;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 售后的 Review 与 Feedback 的处理
 * User: wyattpan
 * Date: 7/26/12
 * Time: 11:46 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Reviews extends Controller {
    public static void index() {
        List<AmazonListingReview> news = AmazonListingReview.negtiveReviewsFilterByState(ReviewState.NEW);

        render(news);
    }

    public static void show(String rid) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(rid);
        if(review.orderr != null)
            renderArgs.put("f", Feedback.findById(review.orderr.orderId));

        F.T2<List<ListingReason>, List<String>> reasons = review.unTagedReasons();
        renderArgs.put("reasons_json", J.json(reasons._2));
        renderArgs.put("cat", review.listing.product.category);
        renderArgs.put("unTagReasons", reasons._1);
        render(review);
    }

    public static void tryOrder(String rid) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(rid);
        Orderr ord = review.tryToRelateOrderByUserId();
        if(ord != null) {
            review.orderr = ord;
            review.save();
        }
        renderJSON(new Ret(ord != null));
    }

    /**
     * 给 Listing 添加原因
     */
    public static void tagReason(String reason, String reviewId) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(reviewId);
        ListingReason lr = ListingReason.findByReason(reason);
        renderJSON(J.G(review.addWhyNegtive(lr)));
    }

    public static void unTagReason(String reason, String reviewId) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(reviewId);
        ListingReason lr = ListingReason.findByReason(reason);
        review.reasons.remove(lr);
        review.save();
        renderJSON(J.G(lr));
    }
}
