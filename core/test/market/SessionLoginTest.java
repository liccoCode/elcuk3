package market;

import helper.HTTP;
import jobs.FeedbackCrawlJob;
import jobs.KeepSessionJob;
import models.market.Account;
import org.apache.http.client.CookieStore;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

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
        new FeedbackCrawlJob().doJob();
    }

    @Test
    public void testFeedbackFetch() {
        List<Account> accs = Account.openedAcc();
        for(Account acc : accs) {
            acc.loginWebSite();
        }
        for(Account acc : accs) {
            System.out.println(acc.cookieStore().hashCode() + "::::::" + acc.cookieStore());
            System.out.println("==================================");
        }
        System.out.println(HTTP.get("http://www.baidu.com"));
        CookieStore store = HTTP.client().getCookieStore();
        System.out.println(store.hashCode() + "::::::" + store);
    }
}
