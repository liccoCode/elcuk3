package market;

import jobs.AmazonOrderFetchJob;
import jobs.AmazonOrderUpdateJob;
import models.market.JobRequest;
import models.market.M;
import models.market.Orderr;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/25/12
 * Time: 2:59 PM
 */
public class OrderUpdateParseTest extends UnitTest {
    @Test
    public void testParse() {
        Set<Orderr> orders = AmazonOrderUpdateJob.updateOrderXML(Play.getFile("test/html/13934056984.csv"), M.AMAZON_DE);
        for(Orderr or : orders) {
            System.out.println(or);
        }
    }

    public void testParse2() {
        JobRequest job = JobRequest.findById(2l);
        new AmazonOrderFetchJob().callBack(job);
    }
}
