package jobs;

import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/10/12
 * Time: 11:27 AM
 */
public class FeedbackInfoFetchJobTest extends UnitTest {

    @Before
    public void login() throws ExecutionException, TimeoutException, InterruptedException {
        new KeepSessionJob().now().get(20, TimeUnit.SECONDS);
    }

    @Test
    public void testFeedbackDeal() {
        new FeedbackInfoFetchJob().now();
    }
}
