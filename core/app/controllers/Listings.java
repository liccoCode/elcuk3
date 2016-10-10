package controllers;

import com.google.gson.JsonElement;
import controllers.api.SystemOperation;
import helper.Crawl;
import helper.J;
import helper.Webs;
import models.Server;
import models.market.Account;
import models.market.Listing;
import models.market.M;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;

/**
 * Listing 的引入:
 * 1. 手动根据 ASIN 添加进入系统
 * 2. 通过 Categry 批量抓取进入系统
 * User: wyattpan
 * Date: 12/28/11
 * Time: 12:49 AM
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class Listings extends Controller {

    public static void index() {
        List<Category> cats = Category.find("ORDER BY categoryId").fetch();
        List<Account> accs = Account.openedSaleAcc();
        render(cats, accs);
    }

    public static void prodListings(Product p, String m) {
        List<Listing> lsts = p.listings(m);
        render(lsts, p);
    }

    /**
     * 详细的查看 Listing
     *
     * @param lid ListingId
     */
    public static void listing(String lid) {
        Listing lst = Listing.findById(lid);
        render(lst);
    }

    public static void reload() {
        try {
            Cache.delete("listings#index");
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret(true, "缓存重新加载"));
    }

    // --------------------------------------

    /**
     * 抓取指定市场的 Listing 进入系统. 如果 Listing 存在这更新, 否则新创建保存
     *
     * @param market
     * @param asin
     */
    public static void crawl(String market, String asin, String sku) {
        validation.required(market);
        validation.required(asin);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Logger.info(String.format("%s/listings/%s/%s", Server.server(Server.T.CRAWLER).url, market,
                asin));
        Listing tobeSave = null;
        try {
            tobeSave = Listing.crawl(asin, M.val(market));
        } catch(Exception e) {
            renderJSON(new Error("Listing", "Listing is not valid[" + e.getMessage() + "]",
                    new String[]{}));
        }
        if(tobeSave == null)
            renderJSON(new Error("Listing",
                    "The Crawl Listing(" + asin + "," + market + ") is not exist!",
                    new String[]{}));
        if(sku != null) tobeSave.product = Product.find("sku=?", sku).first();
        tobeSave.save();

        renderJSON(J.G(tobeSave));
    }

    public static void reCrawl(String asin, String m) {
        Listing l = Listing.findById(Listing.lid(asin, M.val(m)));
        if(!l.isPersistent()) renderJSON(new Ret("此 Listing 不存在,不允许 ReCrawl!"));
        JsonElement clst = Crawl.crawlListing(l.market.toString(), l.asin);
        Listing nLst = Listing.parseAndUpdateListingFromCrawl(clst, true);
        if(nLst != null) {
            nLst.checkAndSaveOffers();
            if(nLst.isPersistent()) renderJSON(new Ret());
        } else renderJSON(new Ret("更新失败."));
    }

    /**
     * 获取被跟踪的Lisitng列表
     */
    @Check("listings.trackedlistings")
    public static void trackedListings() {
        List<Listing> lsts = Listing.trackedListings();
        render(lsts);
    }

    /**
     * 关闭Listing 警告
     *
     * @param listingId
     */
    public static void closeWarnning(String listingId) {
        Listing listing = Listing.findById(listingId);
        listing.closeWarnning();
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("暂时关闭警告,两天内没有下架会再次开启");
        trackedListings();
    }

    @Check("listings.delete")
    public static void destroy(String listingId) {
        try {
            Listing listing = Listing.findById(listingId);
            listing.safeDelete();
            renderJSON(new Ret(true, "成功删除"));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
