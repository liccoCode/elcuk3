package jobs.fixs;

import helper.Webs;
import jobs.ListingWorkers;
import models.market.Listing;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.jobs.Job;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/18/12
 * Time: 3:13 PM
 */
public class ReviewFixJob extends Job {
    @Override
    public void doJob() {
        List<Listing> lis = Listing.find("order by lastUpdateTime").fetch(8);
        for(Listing l : lis) {
            if(StringUtils.isBlank(l.listingId)) continue;
            Logger.info("Review: %s", l.listingId);
            try {
                new ListingWorkers.R(l.listingId).now().get(10, TimeUnit.SECONDS);
                Logger.info("Review: %s Done.", l.listingId);
            } catch(Exception e) {
                Logger.warn(Webs.E(e));
            }
        }
    }
}
