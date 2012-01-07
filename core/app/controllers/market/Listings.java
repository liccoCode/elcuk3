package controllers.market;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonElement;
import models.Server;
import models.market.Listing;
import play.Logger;
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
}
