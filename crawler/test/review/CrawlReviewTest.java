package review;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import helper.HTTP;
import models.AmazonListingReview;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;
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
    Document onePageOld;
    Document onePageNew;

    @Before
    public void setUp() {
        newPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.B007H4J80K_1.html")));
        oldPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.B007H4J80K_1_old.html")));
        onePageOld = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.one.RC34O9N1UQQ2F_old.html")));
        onePageNew = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.one.RC34O9N1UQQ2F.html")));
    }

    @Test
    public void testNewPage() {
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewsFromReviewsListPage(newPage, 1);
        assertEquals(9, reviews.size());
        AmazonListingReview review = reviews.get(5);
        assertEquals("B007H4J80K_AMAZON.DE_A3LJE46CWDGG5Q", review.alrId);
        assertEquals("B007H4J80K_amazon.de", review.listingId);
        assertEquals(2.0, review.rating.doubleValue(), 1);
        assertEquals("ultra-rezension", review.title);
        assertNotNull("review", review.review);
        assertEquals(0, review.helpUp, 0);
        assertEquals(2, review.helpClick, 0);
        assertEquals("Bla", review.username);
        assertEquals("A3LJE46CWDGG5Q", review.userid);
        assertEquals("2012-06-19", review.reviewDate);
        assertEquals(true, review.purchased);
        assertEquals(false, review.resolved);
        assertEquals(2.0, review.lastRating.doubleValue(), 1);
        assertEquals("R3HS1DVGNBEMOK", review.reviewId);
        assertEquals(false, review.isVedio);
        assertEquals(false, review.isRealName);
        assertEquals(false, review.isVineVoice);
        assertEquals(0, review.topN);
        assertEquals("", review.vedioPicUrl);
        assertEquals(6, review.reviewRank);
        assertEquals("", review.comment);
        assertEquals(1, review.comments);


//        System.out.println("NewPage ----------------------------------------");
//        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(reviews.get(0)));
    }

    @Test
    public void testOldPage() {
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewsFromReviewsListPage(newPage, 1);
        assertEquals(9, reviews.size());

        AmazonListingReview review = reviews.get(0);
        assertEquals("B007H4J80K_AMAZON.DE_A1X504FPX6O8G0", review.alrId);
        assertEquals("B007H4J80K_amazon.de", review.listingId);
        assertEquals(2.0, review.rating.doubleValue(), 1);
        assertEquals("Magnete beschädigen möglicherweise das MacBook Air 13''", review.title);
        assertNotNull("review", review.review);
        assertEquals(0, review.helpUp, 0);
        assertEquals(3, review.helpClick, 0);
        assertEquals("Max4711", review.username);
        assertEquals("A1X504FPX6O8G0", review.userid);
        assertEquals("2012-07-24", review.reviewDate);
        assertEquals(true, review.purchased);
        assertEquals(false, review.resolved);
        assertEquals(2.0, review.lastRating.doubleValue(), 1);
        assertEquals("R22VLENNIF5GXZ", review.reviewId);
        assertEquals(false, review.isVedio);
        assertEquals(false, review.isRealName);
        assertEquals(false, review.isVineVoice);
        assertEquals(0, review.topN);
        assertEquals("", review.vedioPicUrl);
        assertEquals(1, review.reviewRank);
        assertEquals("", review.comment);
        assertEquals(0, review.comments);
//        System.out.println("OldPage ----------------------------------------");
//        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(reviews.get(0)));
    }

    @Test
    public void testParseOnePage() throws IOException {
        System.out.println("Single Review New ---------------------------------------------");
        AmazonListingReview review = AmazonListingReview.parseReviewFromOnePage(onePageNew);

        assertEquals("B0081PYPFM_AMAZON.DE_A2KFEEE1890N7W", review.alrId);
        assertEquals("B0081PYPFM_amazon.de", review.listingId);
        assertEquals(5.0, review.rating.doubleValue(), 1);
        assertEquals("Gutes Zweitnetzteil für ASUS Transformer", review.title);
        assertNotNull("review", review.review);
        assertEquals(0, review.helpUp, 0);
        assertEquals(1, review.helpClick, 0);
        assertEquals("Freaky Priest \"Technikfreak\"", review.username);
        assertEquals("A2KFEEE1890N7W", review.userid);
        assertEquals("2012-07-30", review.reviewDate);
        assertEquals(true, review.purchased);
        assertEquals(false, review.resolved);
        assertEquals(5.0, review.lastRating.doubleValue(), 1);
        assertEquals("RC34O9N1UQQ2F", review.reviewId);
        assertEquals(false, review.isVedio);
        assertEquals(false, review.isRealName);
        assertEquals(false, review.isVineVoice);
        assertEquals(0, review.topN);
        assertEquals("", review.vedioPicUrl);
        assertEquals(-1, review.reviewRank);
        assertEquals("", review.comment);
        assertEquals(1, review.comments);
//        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(review));
    }

    @Test
    public void testParseOnePageOld() {
        System.out.println("Single Review Old ---------------------------------------------");
        AmazonListingReview review = AmazonListingReview.parseReviewFromOnePage(onePageOld);
        assertEquals("B0081PYPFM_AMAZON.CO.UK_A2KFEEE1890N7W", review.alrId);
        assertEquals("B0081PYPFM_amazon.co.uk", review.listingId);
        assertEquals(5.0, review.rating.doubleValue(), 1);
        assertEquals("Gutes Zweitnetzteil für ASUS Transformer", review.title);
        assertNotNull("review", review.review);
        assertEquals(0, review.helpUp, 0);
        assertEquals(1, review.helpClick, 0);
        assertEquals("Freaky Priest \"Technikfreak\"", review.username);
        assertEquals("A2KFEEE1890N7W", review.userid);
        assertEquals("2012-07-30", review.reviewDate);
        assertEquals(true, review.purchased);
        assertEquals(false, review.resolved);
        assertEquals(5.0, review.lastRating.doubleValue(), 1);
        assertEquals("RC34O9N1UQQ2F", review.reviewId);
        assertEquals(false, review.isVedio);
        assertEquals(false, review.isRealName);
        assertEquals(false, review.isVineVoice);
        assertEquals(0, review.topN);
        assertEquals("", review.vedioPicUrl);
        assertEquals(-1, review.reviewRank);
        assertEquals("", review.comment);
        assertEquals(0, review.comments);
//        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(review));
    }

}
