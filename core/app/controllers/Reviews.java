package controllers;

import models.User;
import models.market.AmazonListingReview;
import models.market.Feedback;
import models.market.Listing;
import models.market.Orderr;
import models.product.Category;
import models.view.Ret;
import play.db.helper.SqlSelect;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * 售后的 Review 与 Feedback 的处理
 * User: wyattpan
 * Date: 7/26/12
 * Time: 11:46 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Reviews extends Controller {

    public static void show(String rid) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(rid);
        if(review.orderr != null)
            renderArgs.put("f", Feedback.findById(review.orderr.orderId));
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
     * 显示当前登陆用户所有的负评 review
     */
    public static void showReviews() {
        User user = User.findByUserName(Secure.Security.connected());
        //categoryIds 集合
        List<String> categoryIds = new ArrayList<String>();
        for(Category category : User.getTeamCategorys(user)) {
            categoryIds.add(category.categoryId);
        }
        //skus 集合
        List<String> skus = Category.getSKUs(categoryIds);

        //listingIds 集合
        List<String> listingIds = new ArrayList<String>();
        for(String sku : skus) {
            listingIds.addAll(Listing.getAllListingBySKU(sku));
        }
        List<AmazonListingReview> reviews = AmazonListingReview.find("rating <= 3 AND listingId IN" + SqlSelect
                .inlineParam(listingIds)).fetch();
        render("Pmdashboards/_tr_review_info.html", reviews);
    }
}
