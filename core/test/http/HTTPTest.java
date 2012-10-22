package http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.Crawl;
import helper.Dates;
import helper.HTTP;
import models.market.AmazonListingReview;
import models.market.Listing;
import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/11/12
 * Time: 4:23 PM
 */
public class HTTPTest extends UnitTest {
    //    @Test
    public void test404Page() throws IOException {
        //http://www.amazon.co.uk/dp/B0042FL0CG
        FileUtils.writeStringToFile(new File("/Users/wyattpan/elcuk2-data/404.html"), HTTP.get("http://www.amazon.co.uk/dp/B0042FL0CG"));
    }

    //    @Test
    public void googl() {
        List<NameValuePair> param = new ArrayList<NameValuePair>();
        param.add(new BasicNameValuePair("longUrl", "http://e.easyacceu.com/attachs/image?a.fileName=DL|201207|00_31726882.pdf"));
//        param.add(new BasicNameValuePair("key", "AIzaSyC98ClsMbjwD0jp9SGNHMsQ9Twr4G9nqZc"));

        System.out.println(HTTP.postJson("https://www.googleapis.com/urlshortener/v1/url", param));
    }

    @Test
    public void testReview() {
        List<AmazonListingReview> reviewsToBeCheck = AmazonListingReview.find("isRemove=false AND (updateAt is NULL OR updateAt<=?)",
                Dates.night(DateTime.now().minusDays(7).toDate())).fetch(3);

        AmazonListingReview _404_review = new AmazonListingReview();
        _404_review.listingId = "B001OQOK5U_amazon.co.uk";
        _404_review.reviewId = "skxijij";
        reviewsToBeCheck.add(_404_review);
        for(AmazonListingReview review : reviewsToBeCheck) {
            JsonElement rvObj = Crawl.crawlReview(Listing.unLid(review.listingId)._2.toString(), review.reviewId);
            JsonObject obj = rvObj.getAsJsonObject();
            if(obj.get("isRemove").getAsBoolean()) {
                review.isRemove = true;
                System.out.println("----------");
                review.save();
            }
        }
    }
}
