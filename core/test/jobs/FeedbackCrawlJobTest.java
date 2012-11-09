package jobs;

import models.market.Account;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/9/12
 * Time: 4:22 PM
 */
public class FeedbackCrawlJobTest extends UnitTest {

    @Test
    public void testFetchOnePage() {
        Account acc = Account.findById(1l);
        FeedbackCrawlJob.fetchAccountFeedbackOnePage(acc, acc.type, 2);
    }
}
