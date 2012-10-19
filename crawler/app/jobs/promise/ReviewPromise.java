package jobs.promise;

import helper.HTTP;
import models.AmazonListingReview;
import models.MT;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import play.Logger;
import play.Play;
import play.jobs.Job;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/19/12
 * Time: 10:18 PM
 */
public class ReviewPromise extends Job<AmazonListingReview> {
    private String market;
    private String reviewId;

    public ReviewPromise(String market, String reviewId) {
        this.market = market;
        this.reviewId = reviewId;
    }

    @Override
    public AmazonListingReview doJobWithResult() throws Exception {
        MT m = MT.val(market);
        String url = m.review(reviewId);
        HTTP.clearExpiredCookie();
        Logger.info("Fetch Single Review [%s]", url);
        String html = HTTP.get(url);

        if(Play.mode.isDev())
            FileUtils.writeStringToFile(new File(String.format("%s/elcuk2-data/review/%s/%s.html", System.getProperty("user.home"), m.name(), reviewId)), html);

        try {
            if(HTTP.is404(html)) return new AmazonListingReview(true);
            else return AmazonListingReview.parseReviewFromOnePage(Jsoup.parse(html));
        } catch(Exception e) {
            return new AmazonListingReview();
        }
    }
}
