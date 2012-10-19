package controllers;

import jobs.promise.ListingPromise;
import jobs.promise.OffersPromise;
import jobs.promise.ReviewPromise;
import jobs.promise.ReviewsPromise;
import models.AmazonListingReview;
import models.ListingC;
import models.ListingOfferC;
import models.MT;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.utils.FastRuntimeException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ListingCs extends Controller {

    /**
     * 根据市场, asin 来抓取 ListingC
     *
     * @param market us, uk, de, it
     * @param asin   amazon asin
     */
    public static void listing(String market, String asin) throws IOException {
        validation.required(market);
        validation.required(asin);
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        MT m = MT.val(market);
        if(m == null) renderJSON("{flag:false, message:'invalid market[us,uk,de,it,es,fr]'}");
        ListingC lst = await(new ListingPromise(m, asin).now());
        renderJSON(lst);
    }

    public static void offers(String market, String asin) throws IOException {
        ////http://www.amazon.co.uk/gp/offer-listing/B007TR9VRU
        MT m = MT.val(market);
        if(m == null) throw new FastRuntimeException("Market is inValid!");
        List<ListingOfferC> offers = await(new OffersPromise(m, asin).now());
        renderJSON(offers);
    }

    /**
     * 抓取这个 ListingC 的 Review, 一次性最多抓取 5 页或者抓取到最后一页中所有的 Reviews;
     *
     * @param market
     * @param asin
     */
    public static void reviews(String market, String asin) throws IOException {
        /**
         * 持续抓取, 直到抓取回来的次数 > 5 或者次数与最大页面一样则不再进行抓取;
         * 抓取过程中解析出得 Review 全部返回.
         */
        final MT m = MT.val(market);
        if(m == null) throw new FastRuntimeException("Market is inValid!");
        Set<AmazonListingReview> reviews = await(new ReviewsPromise(m, asin).now());
        renderJSON(reviews);
    }

    public static void review(String market, String reviewId) throws IOException {
        AmazonListingReview review = await(new ReviewPromise(market, reviewId).now());
        renderJSON(review);
    }
}
