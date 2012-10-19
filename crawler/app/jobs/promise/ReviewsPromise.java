package jobs.promise;

import helper.HTTP;
import models.AmazonListingReview;
import models.MT;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/19/12
 * Time: 10:39 PM
 */
public class ReviewsPromise extends Job<Set<AmazonListingReview>> {
    private MT market;
    private String asin;

    public ReviewsPromise(MT market, String asin) {
        this.market = market;
        this.asin = asin;
    }

    @Override
    public Set<AmazonListingReview> doJobWithResult() {
        Set<AmazonListingReview> reviews = new HashSet<AmazonListingReview>();
        int maxPage = 1;
        int page = 1;
        while(true) {
            String url = market.reviews(asin, page);
            if(StringUtils.isBlank(url)) continue;
            Logger.info("Fetch Reviews [%s]", url);
            HTTP.clearExpiredCookie();
            String html = HTTP.get(url);

            if(Play.mode.isDev()) {
                try {
                    FileUtils.writeStringToFile(new File(String.format("%s/elcuk2-data/reviews/%s/%s_%s.html", System.getProperty("user.home"), market.name(), asin, page)), html);
                } catch(IOException e) {
                    //ignore
                }
            }

            Document doc = Jsoup.parse(html);
            reviews.addAll(AmazonListingReview.parseReviewsFromReviewsListPage(doc, page));
            if(maxPage == 1) maxPage = AmazonListingReview.maxPage(doc);
            Logger.info("Page: %s / %s, Total Reviews: %s", page, maxPage, reviews.size());
            if(page++ == maxPage) break;
        }
        return AmazonListingReview.filterReviewWithAsinAndMarket(asin, market, reviews);
    }
}
