package controllers;

import models.market.*;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

/**
 * WishList Controller
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/19/12
 * Time: 11:59 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class AmazonWishLists extends Controller {

    /**
     * 获取Listing的wishlist
     *
     * @param asin
     * @param m
     */
    public static void wishList(String asin, String m) {
        M market = M.val(m);
        F.T5<String, String, Long, Long, Long> wishlist=AmazonWishListRecord.WishList(asin, market);
        render(wishlist);
    }

    /**
     * 添加Listing到WishList
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
        boolean success=accT2._1.addToWishList(listing);
        renderJSON(success);
    }

}
