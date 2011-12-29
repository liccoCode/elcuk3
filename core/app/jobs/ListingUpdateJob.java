package jobs;

import models.market.Listing;
import play.Logger;
import play.jobs.Job;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:39 AM
 */
public class ListingUpdateJob extends Job {
    @Override
    public void doJob() throws Exception {
        Logger.info("ListingUpdateJob execute...");
    }
}
