package mails;

import jobs.CheckerProductCheckJob;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/24/12
 * Time: 3:24 PM
 */
public class CheckerProductCheckJobTest extends UnitTest {
    @Test
    public void testCheckMail() {
        new CheckerProductCheckJob().now();
    }
}
