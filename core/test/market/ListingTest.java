package market;

import com.google.gson.JsonElement;
import helper.HTTP;
import models.embedded.AmazonProps;
import models.market.Account;
import models.market.Listing;
import models.market.Selling;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/27/11
 * Time: 11:06 PM
 */
public class ListingTest extends UnitTest {
    //    @Test
    public void testParseAndUpdateListingFromCrawl() {
        JsonElement lst = HTTP.json(String.format("%s/listings/%s/%s", "http://e.easyacceu.com:9001", "uk", "B005JSG7GE"));
        try {
            Listing needCheckListing = Listing.parseAndUpdateListingFromCrawl(lst);
            needCheckListing.check();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSaleAmazon() {
        Listing lst = Listing.findById("B005UO12HQ_amazon.co.uk");
        Selling sell = new Selling();
        sell.account = Account.findById(1l);
        sell.listing = lst;

        sell.aps = new AmazonProps();
        sell.aps.title = "SANER® 1900mAh rechargeable Li-ion Battery for HTC EVO 3D - Extra Long Life, Compatible with HTC Shooter, Amaze 4G, Sensation XL)";
        sell.aps.upc = "614444720150";
//        sell.merchantSKU = String.format("10HTCEVO3D-1900S,%s", sell.aps.upc);
        sell.merchantSKU = String.format("10HTCEVO3D-1900S,3");
        //614444720150
        //0600743015066
        sell.aps.manufacturerPartNumber = "EasyAcc";
//        sell.aps.rbns = new String[]{"1499996031", "430483031"};
        sell.aps.rbns = new String[]{"430204031", "695423031"};
        sell.aps.quantity = 0;
        sell.aps.standerPrice = 23.99f;
        sell.aps.salePrice = 15.99f;
        sell.aps.startDate = DateTime.parse("2011-03-01").toDate();
        sell.aps.endDate = DateTime.parse("2013-03-01").toDate();
        sell.aps.keyFeturess = new String[5];
        for(int i = 0; i < 5; i++)
            sell.aps.keyFeturess[i] = "skdjk-" + i;

        sell.aps.searchTermss = new String[5];
        for(int i = 0; i < 5; i++)
            sell.aps.searchTermss[i] = "searchTerms-" + i;

        sell.aps.productDesc = "ProductDesc";

//        Product.saleAmazon(sell);
    }
}
