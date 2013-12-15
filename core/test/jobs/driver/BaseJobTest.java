package jobs.driver;

import org.junit.Test;
import play.test.UnitTest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 12/13/13
 * Time: 10:51 PM
 */
public class BaseJobTest extends UnitTest {

    @Test
    public void testJob() throws InterruptedException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("jobId", 17);
        GJob.perform(Sample.class.getName(), args);

        new DriverJob().now();
    }
}
