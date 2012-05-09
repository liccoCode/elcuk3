package market;

import com.google.gson.JsonElement;
import helper.HTTP;
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
    public void testParseAndUpdateListingFromCrawl() {
        JsonElement lst = HTTP.json(String.format("%s/listings/%s/%s", "http://e.easyacceu.com:9001", "uk", "B005JSG7GE"));
        try {
            Listing needCheckListing = Listing.parseAndUpdateListingFromCrawl(lst);
            needCheckListing.check();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
