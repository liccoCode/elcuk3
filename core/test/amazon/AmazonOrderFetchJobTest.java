package amazon;

import jobs.AmazonOrderFetchJob;
import models.market.Account;
import models.market.JobRequest;
import models.market.Orderr;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

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

    //        @Test
    public void testUpdateState() {
        JobRequest.updateState(fetchJob.type());
    }

    //    @Test
    public void testAllOrdersXML() {
        Account account = Account.findById(2l);
        List<Orderr> orders = AmazonOrderFetchJob.allOrderXML(
                Play.getFile("test/html/21089866544.xml"), account);
        assertEquals(10, orders.size());
    }


    @Test
    public void testParseOrder() {
        JobRequest job = new JobRequest();
        job.path = "/Users/mac/javadevelop/caryelcuk2/core/test/html/21089866544.xml";
        job.account = Account.findById(2l);
        new AmazonOrderFetchJob().callBack(job);
    }

}
