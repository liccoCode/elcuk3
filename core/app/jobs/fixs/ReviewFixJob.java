package jobs.fixs;

import helper.Webs;
import jobs.ListingDriverlJob;
import jobs.ListingWorkers;
import play.Logger;
import play.jobs.Job;

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
        for(int i = 0; i < 5; i++) {
            String listingId = ListingDriverlJob.QUEUE.peek();
            Logger.info("Review: %s", listingId);
            try {
                new ListingWorkers.R(listingId).now().get(10, TimeUnit.SECONDS);
                Logger.info("Review: %s Done.", listingId);
            } catch(Exception e) {
                Logger.warn(Webs.E(e));
            }
        }
    }
}
