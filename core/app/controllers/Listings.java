package controllers;

import com.google.gson.JsonElement;
import models.Server;
import models.market.Account;
import models.market.Listing;
import models.market.ListingOffer;
import models.market.Selling;
import models.product.Category;
import models.product.Product;
import play.Logger;
import play.data.validation.Error;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Listing 的引入:
 * 1. 手动根据 ASIN 添加进入系统
 * 2. 通过 Categry 批量抓取进入系统
 * User: wyattpan
 * Date: 12/28/11
 * Time: 12:49 AM
 */
@With(GzipFilter.class)
public class Listings extends Controller {

    public static void l_index() {
        List<Category> cats = Category.find("order by categoryId").fetch();
        render(cats);
    }

    /**
     * 点击了左侧的 Prod_Cat 导航后, 中间加载这个 Product 所关联的 Listing
     */
    public static void l_listing(String sku) {
        validation.required(sku);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Product prod = Product.find("sku=?", sku).first();
        render(prod);
    }

    /**
     * 点击了 Listing 导航后, 中间加载这个 Listing 所关联的 Selling
     */
    public static void l_selling(String lid) {
        validation.required(lid);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Listing lst = Listing.find("listingId=?", lid).first();
        render(lst);
    }

    public static void l_listingDetail(String lid) {
        validation.required(lid);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Listing lst = Listing.find("listingId=?", lid).first();
        List<Account> accs = Account.all().fetch();
        render(lst, accs);
    }

    public static void l_prodDetail(String sku) {
        validation.required(sku);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Product prod = Product.find("sku=?", sku).first();
        render(prod);
    }

    public static void l_sellingDetail(String msku) {
        validation.required(msku);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Selling sell = Selling.find("merchantSKU=?", msku).first();
        if(sell == null) renderJSON(new Error("Selling", msku + " is not exist", new String[]{}));
        render(sell);
    }
    /*
        PS: 尽管上面的方法几乎一样, 但还是需要区分开;
        1. Play! 会自动将 Controller 方法的调用转换为 redirect
        2. 没有 Controller 方法会对应一个页面, 而我需要具体的页面
        3. 这样做能够为后续的操作提供修改的空间
      */

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
        JsonElement listing = WS.url(String.format("%s/listings/%s/%s", Server.server(Server.T.CRAWLER).url, market, asin)).get().getJson();
        Listing tobeSave = null;
        try {
            tobeSave = Listing.parseListingFromCrawl(listing);
        } catch(Exception e) {
            renderJSON(new Error("Listing", "Listing is not valid[" + e.getMessage() + "]", new String[]{}));
        }
        if(tobeSave == null)
            renderJSON(new Error("Listing", "Listing is null!", new String[]{}));
        if(sku != null) tobeSave.product = Product.find("sku=?", sku).first();
        tobeSave.save();

        tobeSave.product = null;
        for(ListingOffer of : tobeSave.offers) of.listing = null;
        renderJSON(tobeSave);
    }


    /**
     * 在 Amazon 上已经存在了 Selling, 将 Selling 重新帮顶到系统中的 Listing 身上.
     *
     * @param s
     * @param lid
     */
    public static void bindSelling(Selling s, String lid) {
        Listing lst = Listing.find("listingId=?", lid).first();
        if(lst == null) renderJSON(new Error("Listing", "Not valid listingId.", new String[]{}));
        lst.bindSelling(s);
        s.listing = null;
        renderJSON(s);
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
