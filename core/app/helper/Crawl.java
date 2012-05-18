package helper;

import com.google.gson.JsonElement;
import models.Server;

/**
 * Crawl 服务器暴露的接口
 * User: wyattpan
 * Date: 5/12/12
 * Time: 3:32 PM
 */
public class Crawl {
    public static JsonElement crawlListing(String market, String asin) {
        return HTTP.json(String.format("%s/listings/%s/%s", Server.server(Server.T.CRAWLER).url, market, asin));
    }

    public static JsonElement crawlOffers(String market, String asin) {
        return HTTP.json(String.format("%s/offers/%s/%s", Server.server(Server.T.CRAWLER).url, market, asin));
    }

    public static JsonElement crawlReview(String market, String asin) {
        return HTTP.json(String.format("%s/reviews/%s/%s", Server.server(Server.T.CRAWLER).url, market, asin));
    }

}
