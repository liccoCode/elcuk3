package controllers.market;

import com.google.gson.JsonElement;
import models.Server;
import models.market.Listing;
import models.market.ListingOffer;
import play.Logger;
import play.data.validation.Validation;
import play.libs.WS;
import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/28/11
 * Time: 12:49 AM
 */
public class Listings extends Controller {

    public static void crawl(String market, String asin) {
        JsonElement listing = WS.url(String.format("%s/listings/%s/%s", Server.server(Server.T.CRAWLER).url, market, asin))
                .get().getJson();
        Listing tobeSave = Listing.parseListingFromCrawl(listing);
        tobeSave.save();
        for(ListingOffer of : tobeSave.offers) {
            of.listing = null;
        }
        renderJSON(tobeSave);
    }

    public static void fetch(String asin, String market) {
        validation.required(asin);
        validation.required(market);
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        market = market.toLowerCase();
        String prefix;
        if("amazon.uk".equals(market)) {
            prefix = "amazon.co.uk";
        } else if("amazon.de".equals(market)) {
            prefix = "amazon.de";
        } else if("test".equals(market)) {
            prefix = "google.com";
        } else {
            prefix = "amazon.uk";
        }
        String url = String.format("http://www.%s/dp/%s", prefix, asin);
        Logger.info("URL: %s", url);
        String html = WS.url(url).get().getString("UTF-8");
        render(html);
    }
}
