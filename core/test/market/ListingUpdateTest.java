package market;

import jobs.ListingCrawlJob;
import jobs.ListingSchedulJob;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 2/9/12
 * Time: 8:46 PM
 */
public class ListingUpdateTest extends UnitTest {
    @Test
    public void crawling() throws InterruptedException {
        new ListingSchedulJob().now();
        Thread.sleep(5000);
        new ListingCrawlJob().now();
    }
}
