package market;

import jobs.AmazonSellingSyncJob;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/6/12
 * Time: 4:39 PM
 */
public class AmazonSellingSyncJobTest extends UnitTest {

    @Test
    public void request() {
        new AmazonSellingSyncJob().doJob();
    }
}
