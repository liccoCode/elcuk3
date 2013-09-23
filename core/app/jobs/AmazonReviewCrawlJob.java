package jobs;

import jobs.works.ListingReviewsWork;
import models.Jobex;
import models.market.Listing;
import play.jobs.Job;

import java.util.Date;
import java.util.List;

/**
 * 用来发现新的 Reivew 与更新已经存在的 Review
 * * 周期:
 * - 轮询周期: 1mn
 * - Duration: 5mn (间隔时间可适当延长)
 * User: wyattpan
 * Date: 9/25/12
 * Time: 9:12 PM
 */
public class AmazonReviewCrawlJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonReviewCrawlJob.class.getName()).isExcute()) return;
        List<Listing> listings = Listing.latestNeedReviewListing(10);
        for(Listing lst : listings) {
            // 无论成功否, 被检查过就得记录下.
            lst.lastReviewCheckDate = new Date();
            lst.save();
            new ListingReviewsWork(lst.listingId).now();
        }
    }
}
