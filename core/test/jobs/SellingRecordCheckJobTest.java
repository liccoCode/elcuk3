package jobs;

import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/29/12
 * Time: 5:10 PM
 */
public class SellingRecordCheckJobTest extends UnitTest {
    @Test
    public void testRun() throws ExecutionException, TimeoutException, InterruptedException {
        SellingRecordCheckJob job = new SellingRecordCheckJob();
        DateTime dt = DateTime.parse("2012-02-04");
        for(int i = 0; i < 3; i++) {
            job.fixTime = dt.plusDays(i);
            try {
                job.now().get(60, TimeUnit.SECONDS);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
