package jobs.promise;

import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/26/12
 * Time: 11:19 AM
 */
public class FinanceShippedOrdersTest extends UnitTest {
    @Test
    public void testDoJob() throws ExecutionException, InterruptedException {
        new FinanceShippedOrders().now().get();
    }
}
