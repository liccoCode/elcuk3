package market;

import models.market.Feedback;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Feedback 的功能测试
 * User: wyattpan
 * Date: 4/24/12
 * Time: 2:25 PM
 */
public class FeedbackTest extends UnitTest {
    @Test
    public void testOpenOsTicket() {
        Feedback fb = Feedback.findById("026-2151377-4388349");
        fb.checkMailAndTicket();
    }
}
