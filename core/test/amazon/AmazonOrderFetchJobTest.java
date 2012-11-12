package amazon;

import helper.J;
import jobs.AmazonFBAWatchPlusJob;
import jobs.AmazonOrderFetchJob;
import models.market.Account;
import models.market.JobRequest;
import models.market.Orderr;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import org.junit.Test;
import play.Logger;
import play.Play;
import play.db.jpa.JPA;
import play.test.UnitTest;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/19/12
 * Time: 4:03 PM
 */
public class AmazonOrderFetchJobTest extends UnitTest {
    AmazonOrderFetchJob fetchJob = new AmazonOrderFetchJob();

    //    @Test
    public void testAmazonUS() {
        Account acc = Account.findById(131l);
        JobRequest job = JobRequest.checkJob(acc, fetchJob, acc.marketplaceId());
        job.request();
    }

    //    @Test
    public void testUpdateState() {
        JobRequest.updateState(fetchJob.type());
    }

    @Test
    public void testParseOrder() {
        JobRequest job = new JobRequest();
        job.path = Play.getFile("test/html/15285572344.txt").getPath();
        job.account = Account.<Account>findById(2l);
        new AmazonOrderFetchJob().callBack(job);
    }
}
