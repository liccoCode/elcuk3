package jobs;

import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/12/12
 * Time: 5:11 PM
 */
public class FinanceCheckJobTest extends UnitTest {
    @Before
    public void login() throws ExecutionException, InterruptedException {
        new KeepSessionJob().now().get();
    }

    @Test
    public void testFinance() {
        new FinanceCheckJob().now();
    }

}
