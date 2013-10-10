package jobs;

import jobs.works.ListingReviewsWork;
import models.Jobex;
import models.market.Listing;
import play.jobs.Job;

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

        /**
         * 抓取与更新 Listing Review 是一个渠道, 区别在于找到 review 之后的处理方式不一样.
         */

        List<Listing> listings = Listing.latestNeedReviewListing(10);
        for(Listing lst : listings) {
            new ListingReviewsWork(lst.listingId).now();
        }
    }
}
