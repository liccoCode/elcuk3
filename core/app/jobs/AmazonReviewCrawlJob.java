package jobs;

import jobs.works.ListingReviewWork;
import models.market.Listing;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/25/12
 * Time: 9:12 PM
 */
@Every("1mn")
public class AmazonReviewCrawlJob extends Job {
    @Override
    public void doJob() {
        List<Listing> listings = Listing.find("ORDER BY lastUpdateTime").fetch(10);
        for(Listing lst : listings) {
            new ListingReviewWork(lst.listingId).now();
        }
    }
}
