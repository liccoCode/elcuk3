package amazon;

import jobs.AmazonOrderFetchJob;
import models.market.Account;
import models.market.JobRequest;
import org.junit.Test;
import play.test.UnitTest;

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
}
