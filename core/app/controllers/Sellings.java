package controllers;

import com.alibaba.fastjson.JSON;
import models.market.Listing;
import models.market.PriceStrategy;
import models.market.Selling;
import org.jsoup.helper.Validate;
import play.data.validation.Valid;
import play.mvc.Controller;

/**
 * 控制 Selling
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:41
 */
public class Sellings extends Controller {

    /**
     * 仅仅是手动的添加一个 Selling 与其必须的 PriceStrategy 进入系统;
     * <p/>
     * 不涉及其需要关联的 Listing
     *
     * @param s
     */
    public static void c(@Valid Selling s) {
        Validate.notNull(s.priceStrategy);
        s.save();
        renderJSON(s);
    }

    public static void r(Long id) {
        renderJSON(JSON.toJSONString(Selling.findById(id)));
    }

    public static void u(@Valid Selling s) {
        renderJSON(JSON.toJSONString(s.save()));
    }

    public static void strategyU(PriceStrategy ps) {
        validation.required(ps.id);
        renderJSON(ps.save());
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
