package jobs;

import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/23/12
 * Time: 5:24 PM
 */
public class FAndRNotificationJobTest extends UnitTest {
    @Test
    public void testMail() {
        new FAndRNotificationJob().now();
    }
}
