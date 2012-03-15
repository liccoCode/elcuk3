package market;

import jobs.FeedbackCrawlJob;
import jobs.KeepSessionJob;
import models.market.Account;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/15/12
 * Time: 9:29 AM
 */
public class SessionLoginTest extends UnitTest {
    //    @Test
    public void testLoginTwice() throws InterruptedException {
        new KeepSessionJob().doJob();
    }

    @Test
    public void testFeedbackFetch() {
        Account ac = Account.findById(1l);
        ac.loginWebSite();
        new FeedbackCrawlJob().doJob();
    }
}
