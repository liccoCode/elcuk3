package review;

import com.google.gson.Gson;
import models.AmazonListingReview;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/17/12
 * Time: 4:43 PM
 */
public class CrawlReviewTest extends UnitTest {
    Document newPage;
    Document oldPage;

    @Before
    public void setUp() {
        newPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.B007H4J80K_1.html")));
        oldPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.B007H4J80K_1_old.html")));
    }

    @Test
    public void testNewPage() {
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewFromHTML(newPage, 1);
        assertEquals(9, reviews.size());
        System.out.println("NewPage ----------------------------------------");
        System.out.println(new Gson().toJson(reviews.get(0)));
    }

    @Test
    public void testOldPage() {
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewFromHTML(newPage, 1);
        assertEquals(9, reviews.size());
        System.out.println("OldPage ----------------------------------------");
        System.out.println(new Gson().toJson(reviews.get(0)));
    }

}
