package helper;

import com.google.gson.JsonElement;
import models.Server;
import play.Logger;

/**
 * Crawl 服务器暴露的接口
 * User: wyattpan
 * Date: 5/12/12
 * Time: 3:32 PM
 */
public class Crawl {
    public static JsonElement crawlListing(String market, String asin) {
        Logger.info("crawlListing %s", crawlUrl("listing", market, asin));
        return HTTP.json(crawlUrl("listing", market, asin));
    }

    public static JsonElement crawlOffers(String market, String asin) {
        Logger.info("crawlOffers %s", crawlUrl("offers", market, asin));
        return HTTP.json(crawlUrl("offers", market, asin));
    }

    public static JsonElement crawlReviews(String market, String asin) {
        Logger.info("crawlReview[s] %s", crawlUrl("reviews", market, asin));
        return HTTP.json(crawlUrl("reviews", market, asin));
    }

    public static JsonElement crawlReview(String market, String reviewId) {
        Logger.info("crawlReview %s", crawlUrl("review", market, reviewId));
        return HTTP.json(crawlUrl("review", market, reviewId));
    }

    private static String crawlUrl(String action, String market, String asin) {
        return String.format("%s/%s/%s/%s.json",
                Server.server(Server.T.CRAWLER).url, action, marketurl(market), asin);
    }

    private static String marketurl(String market) {
        String key = "";
        if(market.equals("amazon.co.uk") || market.equals("AMAZON_UK")) {
            key = "uk";
        } else if(market.equals("amazon.de") || market.equals("AMAZON_DE")) {
            key = "de";
        } else if(market.equals("amazon.fr") || market.equals("AMAZON_FR")) {
            key = "fr";
        } else if(market.equals("amazon.it") || market.equals("AMAZON_IT")) {
            key = "it";
        } else if(market.equals("amazon.es") || market.equals("AMAZON_ES")) {
            key = "es";
        } else if(market.equals("amazon.com") || market.equals("AMAZON_US")) {
            key = "us";
        } else if(market.equals("amazon.co.jp") || market.equals("AMAZON_JP")) {
            key = "jp";
        }
        return key;
    }
}
