package amazon;

import jobs.AmazonOrderFetchJob;
import models.market.Account;
import models.market.JobRequest;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.GregorianCalendar;

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
    public void testDate() {
        DateTime time = DateTime.now();
        time = time.minusDays(7);
        assertEquals(5, time.getMonthOfYear());
        assertEquals(14, time.getDayOfMonth());
        GregorianCalendar gc = new GregorianCalendar(time.getYear(), time.getMonthOfYear(),
                time.getDayOfMonth());
        System.out.println(gc);
        System.out.println(new DateTime(gc.getTime()).toString("yyyy-MM-dd HH:mm:ss"));
    }

    //    @Test
    public void testParseOrder() {
        JobRequest job = new JobRequest();
        job.path = "/Users/wyatt/Downloads/16061848184.txt";
        job.account = Account.findById(2l);
        new AmazonOrderFetchJob().callBack(job);
    }

}
