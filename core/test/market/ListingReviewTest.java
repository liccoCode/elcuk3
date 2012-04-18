package market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import helper.HTTP;
import models.market.AmazonListingReview;
import org.junit.Test;
import play.test.UnitTest;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 4/18/12
 * Time: 5:33 PM
 */
public class ListingReviewTest extends UnitTest {

    @Test
    public void testParseReview() throws IOException {
        //http://localhost:9001/reviews/afr/B005JSG7GE
//        WS.HttpResponse re = WS.url("http://localhost:9001/reviews/afr/B005JSG7GE").get();

        JsonElement reviews = HTTP.json("http://localhost:9001/reviews/afr/B005JSG7GE");
        JsonArray array = reviews.getAsJsonArray();
        for(JsonElement je : array) {
            AmazonListingReview review = AmazonListingReview.parseAmazonReviewJson(je);
            System.out.println(review);
        }
    }
}
