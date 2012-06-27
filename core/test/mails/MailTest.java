package mails;

import jobs.OrderMailCheck;
import models.market.Listing;
import models.procure.Shipment;
import notifiers.Mails;
import org.junit.Test;
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
    public void testListingOfferMail() {
        List<Listing> listings = Listing.findAll();
        for(Listing li : listings) {
            li.check();
            li.save();
        }
    }

    //    @Test
    public void testAmazonUK_SHIPPED_MAIL_JOB() throws Exception {
        new OrderMailCheck().doJob();
    }

    @Test
    public void testShipmentClearance() {
        Shipment ship = Shipment.findById("SP|201206|00");
        Mails.shipment_clearance(ship);
        Mails.shipment_isdone(ship);
    }
}
