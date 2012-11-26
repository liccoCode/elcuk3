package jobs.promise;

import models.market.Account;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 11/25/12
 * Time: 1:40 PM
 */
public class FinanceCrawlPromiseTest extends UnitTest {

    @Test
    public void test() throws ExecutionException, InterruptedException {
        Account acc = Account.findById(2l);
        new FinanceCrawlPromise(acc, DateTime.parse("2012-11-14").toDate()).now().get();
    }
}
