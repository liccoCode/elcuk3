package mws.v2;

import jobs.driver.DriverJob;
import org.junit.Test;
import play.test.UnitTest;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/19/13
 * Time: 4:11 PM
 */
public class MWSGetFeedJobTest extends UnitTest {

    @Test
    public void testJob() throws InterruptedException, TimeoutException, ExecutionException {
        new DriverJob().now();
    }
}
