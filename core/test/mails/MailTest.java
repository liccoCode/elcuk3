package mails;

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
    @Test
    public void testListingOfferMail() {
        List<Listing> listings = Listing.findAll();
        for(Listing li : listings) {
            li.check();
            li.save();
        }
    }
}
