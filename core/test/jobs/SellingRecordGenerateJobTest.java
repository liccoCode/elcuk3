package jobs;

import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/8/12
 * Time: 4:40 PM
 */
public class SellingRecordGenerateJobTest extends UnitTest {

    @Test
    public void testNow() throws ExecutionException, InterruptedException {
        new SellingRecordGenerateJob(DateTime.now().minusDays(3)).now().get();
    }
}
