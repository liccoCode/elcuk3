package jobs.works;

import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/6/12
 * Time: 10:55 AM
 */
public class ListingReviewsWorkTest extends UnitTest {

    @Test
    public void testReviewCrawl() throws ExecutionException, TimeoutException, InterruptedException {
        new ListingReviewsWork("B008YRG5JQ_amazon.com").now().get(30, TimeUnit.SECONDS);
    }
}
