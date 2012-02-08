package market;

import jobs.FBAQtySyncJob;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 2/8/12
 * Time: 10:24 AM
 */
public class FBAQtySyncJobTest extends UnitTest {
    @Test
    public void downloadJob() {
        new FBAQtySyncJob().now();
    }
}
