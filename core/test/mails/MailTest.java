package mails;

import jobs.OrderMailCheck;
import models.market.Listing;
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

    @Test
    public void testAmazonUK_SHIPPED_MAIL_JOB() throws Exception {
        new OrderMailCheck().doJob();
    }
}
