package controllers;

import models.market.AmazonListingReview;
import models.market.Feedback;
import models.market.Orderr;
import models.market.ReviewState;
import models.view.Ret;
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
        if(review.orderr != null) {
            renderArgs.put("f", Feedback.findById(review.orderr.orderId));
        }
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
}
