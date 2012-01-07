package market;

import models.market.Listing;
import org.junit.Before;
import org.junit.Test;
import play.test.Fixtures;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/27/11
 * Time: 11:06 PM
 */
public class ListingTest extends UnitTest {
    @Before
    public void setup() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("ListingAndOffers.yml");
    }

    @Test
    public void lisingOwner() {
        Listing li = Listing.find("byAsin", "B005QSWWUW").first();
        System.out.println(li.offers);
        System.out.println("Delete: " + li.delete());
    }
}
