package jobs.works;

import models.market.Account;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/8/13
 * Time: 4:40 PM
 */
public class ListingWorkTest extends UnitTest {

    @Test
    public void testCrawlOnece() throws Exception {
        Account.initOfferIds();
        new ListingWork("B007K4WYMQ_amazon.de", true).now().get(1, TimeUnit.MINUTES);
    }
}
