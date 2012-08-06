package jobs;

import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/6/12
 * Time: 3:27 PM
 */
public class TicketStateSyncJobTest extends UnitTest {

    @Test
    public void testState() throws ExecutionException, TimeoutException, InterruptedException {
        TicketStateSyncJob ticketSyncJob = new TicketStateSyncJob();
        ticketSyncJob.now().get(30000, TimeUnit.SECONDS);
    }
}
