package controllers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import helper.Crawl;
import helper.Webs;
import models.Ret;
import models.Server;
import models.market.Account;
import models.market.Listing;
import models.market.ListingOffer;
import models.market.Selling;
import models.product.Category;
import models.product.Product;
import play.Logger;
import play.cache.Cache;
import play.cache.CacheFor;
import play.data.validation.Error;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * Listing 的引入:
 * 1. 手动根据 ASIN 添加进入系统
 * 2. 通过 Categry 批量抓取进入系统
 * User: wyattpan
 * Date: 12/28/11
 * Time: 12:49 AM
 */
@With({Secure.class, GzipFilter.class})
public class Listings extends Controller {

    @CacheFor(value = "6h", id = "listings#index")
    public static void index() {
        List<Category> cats = Category.find("ORDER BY categoryId").fetch();
        List<Account> accs = Account.all().fetch();
        render(cats, accs);
    }

    public static void prodListings(Product p, String m) {
        List<Listing> lsts = p.listings(m);
        render(lsts);
    }

    public static void listingSellings(Listing l, Account a) {
        List<Selling> sells = l.sellings(a);
        render(sells);
    }

    /**
     * 详细的查看 Listing
     *
     * @param lid ListingId
     */
//    @CacheFor(value = "10mn")
    public static void listing(String lid) {
        Listing lst = Listing.findById(lid);
        List<Account> accs = Account.all().fetch();

        List<String> sellingIds = new ArrayList<String>();
        List<Selling> cateSellings = Selling.find("listing.product.category=?", lst.product.category).fetch();
        for(Selling s : cateSellings) sellingIds.add(s.sellingId);
        renderArgs.put("sellingDataSource", new Gson().toJson(sellingIds));
        render(lst, accs);
    }

    public static void reload() {
        try {
            Cache.delete("listings#index");
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret(true, "缓存重新加载"));
    }

    public static void saleAmazonListing(Selling s) {
        /**
         * 从前台上传来的一系列的值检查
         */
        Validation.required(Messages.get("s.title"), s.aps.title);
        Validation.required(Messages.get("s.upc"), s.aps.upc);
        Validation.required(Messages.get("s.manufac"), s.aps.manufacturer);
        Validation.required(Messages.get("s.rbn"), s.aps.rbns);
        Validation.required(Messages.get("s.price"), s.aps.standerPrice);
        Validation.required(Messages.get("s.tech"), s.aps.keyFeturess);
        Validation.required(Messages.get("s.keys"), s.aps.searchTermss);
        Validation.required(Messages.get("s.prodDesc"), s.aps.productDesc);
        Validation.required(Messages.get("s.msku_req"), s.merchantSKU);
        if(Validation.hasErrors()) renderJSON(new Ret(Validation.current().errorsMap()));
        if(Selling.exist(s.merchantSKU)) renderJSON(new Ret(Messages.get("s.msku")));

        // 在 Controller 里面将值处理好
        Selling se = Listing.saleAmazon(s.listing, s);
        renderJSON(Webs.exposeGson(se));
    }

    public static void upcCheck(String upc) {
        /**
         * UPC 的检查;
         * 1. 在哪一些 Selling 身上使用过?
         * 2. 通过 UPC 与
         */
        try {
            List<Selling> upcSellings = Selling.find("aps.upc like '%" + upc + "%'").fetch();
            renderJSON(Webs.exposeGson(upcSellings));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    // --------------------------------------
    /*
        PS: 尽管上面的方法几乎一样, 但还是需要区分开;
        1. Play! 会自动将 Controller 方法的调用转换为 redirect
        2. 没有 Controller 方法会对应一个页面, 而我需要具体的页面
        3. 这样做能够为后续的操作提供修改的空间
      */

    public static void update(@Valid Selling s) {
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        s.save();
        renderJSON(s);
    }


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
        Logger.info(String.format("%s/listings/%s/%s", Server.server(Server.T.CRAWLER).url, market, asin));
        Listing tobeSave = null;
        try {
            JsonElement listing = Crawl.crawlListing(market, asin);
            tobeSave = Listing.parseAndUpdateListingFromCrawl(listing);
        } catch(Exception e) {
            renderJSON(new Error("Listing", "Listing is not valid[" + e.getMessage() + "]", new String[]{}));
        }
        if(tobeSave == null)
            renderJSON(new Error("Listing", "The Crawl Listing(" + asin + "," + market + ") is not exist!", new String[]{}));
        if(sku != null) tobeSave.product = Product.find("sku=?", sku).first();
        tobeSave.save();

        tobeSave.product = null;
        for(ListingOffer of : tobeSave.offers) of.listing = null;
        renderJSON(tobeSave);
    }

    /**
     * 为某一个 Listing 进行上架;
     * 区分 Market(Amazon, Ebay), Account
     * //TODO 还需要完成
     */
    public static void sale(String acc, String listingId) {
        validation.required(acc);
        validation.required(listingId);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Account acct = Account.find("uniqueName=?", acc).first();
        Listing listing = Listing.find("listingId=?", listingId).first();
        throw new UnsupportedOperationException("Recived Account[" + acct + "], Listing[" + listing + "]");
    }
}
