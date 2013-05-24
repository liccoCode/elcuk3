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
 * Time: 2:30 PM
 */
public class AmazonOrderItemDiscoverTest extends UnitTest {
    @Test
    public void testOrderItemDiscover()
            throws InterruptedException, ExecutionException, TimeoutException {
        AmazonOrderItemDiscover job = new AmazonOrderItemDiscover();
        job.now().get(1, TimeUnit.MINUTES);
    }
}
