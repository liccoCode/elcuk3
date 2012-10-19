package jobs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.Crawl;
import helper.Dates;
import jobs.works.ListingReviewsWork;
import models.Jobex;
import models.market.AmazonListingReview;
import models.market.Listing;
import org.joda.time.DateTime;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/25/12
 * Time: 9:12 PM
 */
@Every("10mn")
public class AmazonReviewCrawlJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonReviewCrawlJob.class.getName()).isExcute()) return;
        List<Listing> listings = Listing.find("ORDER BY lastUpdateTime").fetch(10);
        for(Listing lst : listings) {
            new ListingReviewsWork(lst.listingId).now();
        }

        List<AmazonListingReview> reviewsToBeCheck = AmazonListingReview.find("isRemove=false AND (updateAt is NULL OR updateAt<=?)",
                Dates.night(DateTime.now().minusDays(7).toDate())).fetch(10);
        for(AmazonListingReview review : reviewsToBeCheck) {
            JsonElement rvObj = Crawl.crawlReview(Listing.unLid(review.listingId)._2.toString(), review.reviewId);
            JsonObject obj = rvObj.getAsJsonObject();
            if(obj.get("isRemove").getAsBoolean()) {
                review.isRemove = true;
                review.save();
            }
        }
    }
}
