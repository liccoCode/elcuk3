package market;

import models.market.Listing;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/27/11
 * Time: 11:06 PM
 */
public class ListingTest extends UnitTest {
    @Test
    public void testJPATransaction() {
        Listing li = Listing.findById(1l);
        li.condition_ = "NEW";
    }
}
