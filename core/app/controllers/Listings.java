package controllers;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonElement;
import models.Server;
import models.market.Account;
import models.market.Listing;
import play.Logger;
import play.data.validation.Validation;
import play.libs.WS;
import play.mvc.Controller;

/**
 * Listing 的引入:
 * 1. 手动根据 ASIN 添加进入系统
 * 2. 通过 Categry 批量抓取进入系统
 * User: wyattpan
 * Date: 12/28/11
 * Time: 12:49 AM
 */
public class Listings extends Controller {

    /**
     * 抓取指定市场的 Listing 进入系统. 如果 Listing 存在这更新, 否则新创建保存
     *
     * @param market
     * @param asin
     */
    public static void crawl(String market, String asin) {
        Logger.info(String.format("%s/listings/%s/%s", Server.server(Server.T.CRAWLER).url, market, asin));
        JsonElement listing = WS.url(String.format("%s/listings/%s/%s", Server.server(Server.T.CRAWLER).url, market, asin)).get().getJson();
        Listing tobeSave = Listing.parseListingFromCrawl(listing);
        tobeSave.save();
        renderJSON(JSON.toJSONString(tobeSave));
    }

    /**
     * 为某一个 Listing 进行上架;
     * 区分 Market(Amazon, Ebay), Account
     * //TODO 还需要完成
     */
    public static void sale(String acc, String listingId) {
        validation.required(acc);
        validation.required(listingId);
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        Account acct = Account.find("uniqueName=?", acc).first();
        Listing listing = Listing.find("listingId=?", listingId).first();
        throw new UnsupportedOperationException("Recived Account[" + acct + "], Listing[" + listing + "]");
    }
}
