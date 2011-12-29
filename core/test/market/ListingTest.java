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
    public void testCreate() {
        Listing listing = new Listing();
        listing.asin = System.currentTimeMillis() + "";
        listing.market = "amazon.com";
        listing.title = "titititititle";
        listing.save();
    }
}
