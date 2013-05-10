package jobs;

import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/10/13
 * Time: 10:39 AM
 */
public class ShipmentSyncJobTest extends UnitTest {
    @Test
    public void test() throws InterruptedException, ExecutionException, TimeoutException {
        new ShipmentSyncJob().now().get(20, TimeUnit.SECONDS);
    }
}
