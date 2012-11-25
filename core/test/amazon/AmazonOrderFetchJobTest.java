package amazon;

import jobs.AmazonOrderFetchJob;
import models.market.Account;
import models.market.JobRequest;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
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
        job.path = "/Users/wyatt/Downloads/16061848184.txt";
        job.account = Account.findById(2l);
        new AmazonOrderFetchJob().callBack(job);
    }

    @Test
    public void testFetchOrder() {
        List<Integer> i = new ArrayList<Integer>();
        for(int a = 0; a < 1000; a++) i.add(a);

        List<Integer> subList = i.subList(0, 100);
        System.out.println(subList);
        subList.clear();
        subList = i.subList(0, 100);
        System.out.println(subList);
        subList.clear();
        subList = i.subList(0, i.size() > 1000 ? 1000 : i.size());
        System.out.println(subList);
        subList.clear();
        System.out.println(i);
    }
}
