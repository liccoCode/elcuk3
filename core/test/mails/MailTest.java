package mails;

import jobs.OrderMailCheck;
import models.market.Feedback;
import models.market.Listing;
import models.procure.Shipment;
import notifiers.Mails;
import org.junit.Test;
import play.db.jpa.Transactional;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 2/20/12
 * Time: 11:40 AM
 */
public class MailTest extends UnitTest {

    //    @Test
    public void testShipmentClearance() {
        Shipment ship = Shipment.findById("SP|201206|00");
        Mails.shipment_clearance(ship);
        Mails.shipment_isdone(ship);
    }

    //    @Test
    public void orderReviewMail() {
        new OrderMailCheck().now();
    }

    @Test
    public void testFeedbackWarnning() {
        Feedback f = Feedback.findById("028-5216345-7276341");
        f.mailedTimes = 0;
        Mails.feedbackWarnning(f);
    }
}
