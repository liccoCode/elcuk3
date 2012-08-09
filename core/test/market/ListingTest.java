package market;

import com.google.gson.JsonElement;
import helper.GTs;
import helper.HTTP;
import models.market.Account;
import models.market.Listing;
import models.market.M;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            Listing needCheckListing = Listing.parseAndUpdateListingFromCrawl(lst, true);
            needCheckListing.check();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSellingUploadImage() {
        Account acc = Account.findById(1l);
        acc.changeRegion(M.AMAZON_DE);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("asin", "B0083QX8AW"));
        System.out.println(HTTP.upload(acc.cookieStore(),
                acc.type.uploadImageLink(),
                params,
                GTs.MapBuilder.map("MAIN", new File("/Users/wyattpan/elcuk2-data/uploads/10HTCEVO3D-1900S_0.jpg"))
                        .put("PT01", new File("/Users/wyattpan/elcuk2-data/uploads/10HTCEVO3D-1900S_1.jpg")).build()));
    }
}
