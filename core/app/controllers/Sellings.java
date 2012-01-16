package controllers;

import models.market.Listing;
import models.market.Selling;
import org.jsoup.helper.Validate;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

/**
 * 控制 Selling
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:41
 */
public class Sellings extends Controller {


    /**
     * 远程更新, 并且更新本地数据库
     */
    public static void deploy(@Valid Selling s) {
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        s.deploy(s.merchantSKU);
        renderJSON(s);
    }

    public static void update(@Valid Selling s) {
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        s.localUpdate(s.merchantSKU);
        renderJSON(s);
    }

    /**
     * 将指定 merchantSKU 的 Selling 与指定的 listingId 进行关联
     *
     * @param msku
     * @param listingId
     */
    public static void assoListing(String msku, String listingId) {
        validation.required(msku);
        validation.required(listingId);

        Selling selling = Selling.find("merchantSKU=?", msku).first();
        Validate.notNull(selling);
        Listing listing = Listing.find("listingId=?", listingId).first();
        Validate.notNull(listing);
        selling.listing = listing;
        selling.save();
    }
}
