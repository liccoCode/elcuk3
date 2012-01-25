package market;

import models.market.Account;
import models.market.JobRequest;
import models.market.Orderr;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
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
        Set<Orderr> orders = Orderr.parseUpdateOrderXML(new File("/Users/wyattpan/elcuk2-data/2012/1/24/9968684844.csv"), Account.M.AMAZON_UK);
        for(Orderr or : orders) {
            System.out.println(or);
        }
    }

    @Test
    public void testParse2() {
        JobRequest job = JobRequest.findById(2l);
        job.dealWith();
    }
}
