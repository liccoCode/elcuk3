package jobs;

import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/16/13
 * Time: 10:55 AM
 */
public class ShipmentsMonitorJobTest extends UnitTest {

    @Test
    public void testDoJob() throws InterruptedException, ExecutionException, TimeoutException {

        new ShipmentsMonitorJob().now().get(10, TimeUnit.SECONDS);
    }
}
