package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import jobs.works.ListingReviewsWork;
import models.market.*;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;
import java.util.Set;

/**
 * 获取、记录、自动执行 Amazon 账户对Listing的动作
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/19/12
 * Time: 11:59 AM
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
@Check("amazonoperations")
public class AmazonOperations extends Controller {

    public static void index() {
        Set<String> allAsin = Listing.allASIN();
        renderArgs.put("asins", J.json(allAsin));
        render();
    }

    /**
     * 搜索框框中, 进行加载系统内的 Tickets 的数据
     *
     * @param asin
     * @param m
     */
    public static void ajaxMagic(String asin, String m, String orderby) {
        M market = M.val(m);
        if(StringUtils.isBlank(orderby)) orderby = "createDate";
        List<AmazonListingReview> savedReviews = AmazonListingReview.listingReviews(Listing.lid(asin, market), orderby);
        Listing lst = Listing.findById(Listing.lid(asin, market));
        render(savedReviews, lst);
    }

    public static void reviewTable(String asin, String m) {
        M market = M.val(m);
        String lid = Listing.lid(asin, market);
        Listing lst = Listing.findById(lid);
        List<F.T2<Long, Integer>> rows = lst.reviewMonthTable();

        //不存在,则重新抓取
        if(!Listing.exist(lid)) {
            // 如果不存在, 先去抓取 Listing 然后再抓取 Review
            lst = Listing.crawl(asin, market);
            if(lst != null) {
                lst.save();
                await(new ListingReviewsWork(lid).now());
                rows = lst.reviewMonthTable();
            }
        } else {
            if(rows.size() <= 0) {
                await(new ListingReviewsWork(lid).now());
                rows = lst.reviewMonthTable();
            }
        }
        render(rows);
    }

    /**
     * 点击[有/无]用
     */
    public static void click(String reviewId, boolean isUp) {
        AmazonListingReview review = AmazonListingReview.findByReviewId(reviewId);
        F.T2<Account, Integer> accT2 = review.pickUpOneAccountToClick();
        F.T2<AmazonReviewRecord, String> t2 = accT2._1.clickReview(review, isUp);
        renderJSON(J.json(new F.T2<Integer, String>(accT2._2, J.G(t2._1))));
    }

    public static void reCrawl(String asin, String m) {
        M market = M.val(m);
        String lid = Listing.lid(asin, market);
        try {
            if(!Listing.exist(lid)) {
                // 如果不存在, 先去抓取 Listing 然后再抓取 Review
                Listing lst = Listing.crawl(asin, market);
                if(lst != null) {
                    lst.save();
                    await(new ListingReviewsWork(lid).now());
                } else
                    renderJSON(new Ret(false, "Amazon 上 Listing 不存在或者已经被删除."));
            } else {
                await(new ListingReviewsWork(lid).now());
            }
        } catch(Exception e) {
            throw new FastRuntimeException(Webs.S(e));
        }
        renderJSON(new Ret(true, AmazonListingReview.countListingReview(Listing.lid(asin, market)) + ""));
    }

    /**
     * 检查还剩下多少次可点击
     */
    public static void checkLeftClicks(List<String> rvIds) {
        List<F.T2<String, Integer>> reviewLeftClicks = AmazonListingReview.reviewLeftClickTimes(rvIds);
        renderJSON(J.json(reviewLeftClicks));
    }


    /**
     * 获取Listing的wishlist
     *
     * @param asin
     * @param m
     */
    public static void wishList(String asin, String m) {
        M market = M.val(m);
        String lid = Listing.lid(asin, market);
        F.T2<Long, Long> wishlist = AmazonWishListRecord.wishList(asin, market);
        Listing listing = Listing.findById(lid);
        List<AmazonWishListRecord> records = AmazonWishListRecord.wishListInfos(lid);
        render(wishlist, listing, records);
    }

    /**
     * 添加Listing到WishList
     *
     * @param asin
     * @param m
     */
    public static void addToWishList(String asin, String m) {
        M market = M.val(m);
        String lid = Listing.lid(asin, market);
        Listing listing = Listing.findById(lid);
        if(listing == null)
            throw new FastRuntimeException("Listing 不存在, 请通过 Amazon Recrawl 来添加.");
        F.T2<Account, Integer> accT2 = listing.pickUpOneAccountToWishList();
        boolean success = accT2._1.addToWishList(listing);
        renderJSON(success);
    }

}
