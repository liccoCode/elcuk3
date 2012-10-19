package jobs.promise;

import helper.HTTP;
import models.ListingC;
import models.MT;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/19/12
 * Time: 10:27 PM
 */
public class ListingPromise extends Job<ListingC> {
    private MT market;
    private String asin;

    public ListingPromise(MT market, String asin) {
        this.market = market;
        this.asin = asin;
    }

    @Override
    public ListingC doJobWithResult() {
        HTTP.clearExpiredCookie();
        String url = market.listing(asin);
        Logger.info("Fetch Listing: %s", url);
        String html = HTTP.get(url);
        if(Play.mode.isDev()) {
            try {
                FileUtils.writeStringToFile(new File(String.format("%s/elcuk2-data/listings/%s/%s.html", System.getProperty("user.home"), market.name(), asin)), html);
            } catch(IOException e) {
                //ignore
            }
        }
        try {
            return ListingC.parseAmazon(Jsoup.parse(html));
        } catch(NullPointerException e) {
            //ignore
        }
        return new ListingC();
    }
}
