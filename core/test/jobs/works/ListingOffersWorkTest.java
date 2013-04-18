package jobs.works;

import models.market.Listing;
import org.junit.Test;
import play.test.UnitTest;

import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/8/13
 * Time: 4:16 PM
 */
public class ListingOffersWorkTest extends UnitTest {

    @Test
    public void testOffers() throws Exception {
        new ListingOffersWork("B007K4WYMQ_amazon.de").now().get(1, TimeUnit.MINUTES);
    }

    @Test
    public void testOffers2() throws Exception {
        new ListingOffersWork(Listing.<Listing>findById("B007K4WYMQ_amazon.de")).now()
                .get(1, TimeUnit.MINUTES);
    }
}
