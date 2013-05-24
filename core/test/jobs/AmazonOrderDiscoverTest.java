package jobs;

import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/24/13
 * Time: 12:38 PM
 */
public class AmazonOrderDiscoverTest extends UnitTest {

    @Test
    public void testOrderDiscover()
            throws InterruptedException, ExecutionException, TimeoutException {
        AmazonOrderDiscover job = new AmazonOrderDiscover();
        job.now().get(1, TimeUnit.MINUTES);
    }
}
