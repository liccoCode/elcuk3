package market;

import models.market.Feedback;
import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

/**
 * Feedback 的功能测试
 * User: wyattpan
 * Date: 4/24/12
 * Time: 2:25 PM
 */
public class FeedbackTest extends UnitTest {
    @Test
    public void testOpenOsTicket() {
        //281507 <- 303-3685104-8694754
        List<Feedback> fbs = Feedback.find("orderId in ('303-3685104-8694754', '302-6009937-1918748', '026-6088517-4415544')").fetch();
        for(Feedback fb : fbs) fb.checkMailAndTicket();
    }
}
