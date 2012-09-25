package jobs;

import models.market.Listing;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/25/12
 * Time: 6:29 PM
 */
public class ListingScheduleJob extends UnitTest {
    @Test
    public void testCalInteval() {
        Listing lst = Listing.findById("B005JSG7GE_amazon.de");
        System.out.println(ListingSchedulJob.calInterval(lst));
    }
}
