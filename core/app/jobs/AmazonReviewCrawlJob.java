package jobs;

import jobs.works.ListingReviewsWork;
import models.Jobex;
import models.market.Listing;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;

/**
 * 用来发现新的 Reivew 与更新已经存在的 Review
 * * 周期:
 * - 轮询周期: 5s
 * - Duration: 5mn
 * User: wyattpan
 * Date: 9/25/12
 * Time: 9:12 PM
 */
@Every("5s")
public class AmazonReviewCrawlJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonReviewCrawlJob.class.getName()).isExcute()) return;
        List<Listing> listings = Listing.find("ORDER BY lastUpdateTime").fetch(10);
        for(Listing lst : listings) {
            new ListingReviewsWork(lst.listingId).now();
        }
    }
}
