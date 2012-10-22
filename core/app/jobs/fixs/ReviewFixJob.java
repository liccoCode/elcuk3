package jobs.fixs;

import helper.Webs;
import jobs.works.ListingReviewsWork;
import models.Jobex;
import models.market.Listing;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.jobs.Every;
import play.jobs.Job;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 更新一个一个 Listing 的 Review
 * 周期:
 * - 轮询周期: 1mn
 * - Duration: 8mn
 * User: wyattpan
 * Date: 5/18/12
 * Time: 3:13 PM
 */
@Every("1mn")
public class ReviewFixJob extends Job {
    @Override
    public void doJob() {
        if(!Jobex.findByClassName(ReviewFixJob.class.getName()).isExcute()) return;
        List<Listing> lis = Listing.find("order by lastUpdateTime").fetch(8);
        for(Listing l : lis) {
            if(StringUtils.isBlank(l.listingId)) continue;
            Logger.info("Review: %s", l.listingId);
            try {
                new ListingReviewsWork(l.listingId).now().get(10, TimeUnit.SECONDS);
                Logger.info("Review: %s Done.", l.listingId);
            } catch(Exception e) {
                Logger.warn(Webs.E(e));
            }
        }
    }
}
