package jobs.promise;

import helper.HTTP;
import models.ListingOfferC;
import models.MT;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/19/12
 * Time: 10:34 PM
 */
public class OffersPromise extends Job<List<ListingOfferC>> {
    private MT market;
    private String asin;

    public OffersPromise(MT market, String asin) {
        this.market = market;
        this.asin = asin;
    }

    @Override
    public List<ListingOfferC> doJobWithResult() {
        HTTP.clearExpiredCookie();
        String url = market.offers(asin);
        if(StringUtils.isBlank(url)) return new ArrayList<ListingOfferC>();
        Logger.info("Fetch Offers: %s", url);
        String html = HTTP.get(url);
        if(Play.mode.isDev()) {
            try {
                FileUtils.writeStringToFile(new File(String.format("%s/elcuk2-data/listings/offers/%s/%s.html", System.getProperty("user.home"), market.name(), asin)), html);
            } catch(IOException e) {
                //ignore
            }
        }
        try {
            return ListingOfferC.parseOffers(market, Jsoup.parse(html));
        } catch(Exception e) {
            //ignore
        }
        return new ArrayList<ListingOfferC>();
    }
}
