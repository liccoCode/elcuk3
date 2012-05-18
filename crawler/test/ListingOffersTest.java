import models.ListingOfferC;
import models.MT;
import org.junit.Test;
import play.libs.IO;
import play.test.UnitTest;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/18/12
 * Time: 10:50 AM
 */
public class ListingOffersTest extends UnitTest {
    @Test
    public void testListingOffers() {
        List<ListingOfferC> offers = new ListingOfferC(MT.AUK).parseOffers(IO.readContentAsString(
                new File("/Users/wyattpan/elcuk2-data/listings/offers/AUK/B004Z4SJ78.html"), "UTF-8"));
        for(ListingOfferC of : offers)
            System.out.println(of);
    }
}
