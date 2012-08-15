package jobs;

import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/15/12
 * Time: 6:11 PM
 */
public class ShipmentSyncJobTest extends UnitTest {
    @Test
    public void testShipmentSyncJob() throws ExecutionException, TimeoutException, InterruptedException {
        new ShipmentSyncJob().now().get(30, TimeUnit.SECONDS);
    }
}
