package review;

import models.AmazonListingReview;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/17/12
 * Time: 4:43 PM
 */
public class CrawlReviewTest extends UnitTest {
    Document deNewPage;
    Document deOldPage;
    Document deOnePageOld;
    Document deOnePageNew;

    Document ukNewPage;

    Document usNewPage;
    Document usOneNewPage;

    Document frOldPage;

    @Before
    public void setUp() {
        deNewPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.de.B007H4J80K_1.html")));
        deOldPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.de.B007H4J80K_1_old.html")));
        deOnePageOld = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.de.one.RC34O9N1UQQ2F_old.html")));
        deOnePageNew = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.de.one.RC34O9N1UQQ2F.html")));

        ukNewPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.uk.B005JSG7GE.html")));

        usNewPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.us.B005VBNYDS.html")));
        usOneNewPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.us.one.B005VBNYDS.html")));

        frOldPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.fr.B0041OQFAU.html"), "ISO-8859-1"));
//        frOldPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/review.fr.B0041OQFAU.html")));
    }

    @Test
    public void testDEReviewListNewPage() {
        // 测试 DE 新 Review List 页面
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewsFromReviewsListPage(deNewPage, 1);
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
    public void testDEReviewListOldPage() {
        // 测试 DE 老 Review List 页面
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewsFromReviewsListPage(deNewPage, 1);
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
    public void testDEOneReviewNewPage() throws IOException {
        // 测试 DE 新 One Review 页面
        AmazonListingReview review = AmazonListingReview.parseReviewFromOnePage(deOnePageNew);

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
    public void testDEOneReviewOldPage() {
        // 测试 DE 老 One Review 页面
        AmazonListingReview review = AmazonListingReview.parseReviewFromOnePage(deOnePageOld);
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

    @Test
    public void testUSReviewListNewPage() {
        //  测试 us Review List 页面
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewsFromReviewsListPage(usNewPage, 1);
        assertEquals(10, reviews.size());
        AmazonListingReview review = reviews.get(2);
        assertEquals("B005VBNYDS_AMAZON.COM_A3S9JMG0IRE0BQ", review.alrId);
        assertEquals("B005VBNYDS_amazon.com", review.listingId);
        assertEquals(5.0, review.rating.doubleValue(), 1);
        assertEquals("A Powerful Sleek Charger!", review.title);
        assertNotNull("review", review.review);
        assertEquals(152, review.helpUp, 0);
        assertEquals(173, review.helpClick, 0);
        assertEquals("Matt", review.username);
        assertEquals("A3S9JMG0IRE0BQ", review.userid);
        assertEquals("2012-02-01", review.reviewDate);
        assertEquals(true, review.purchased);
        assertEquals(false, review.resolved);
        assertEquals(5.0, review.lastRating.doubleValue(), 1);
        assertEquals("RLPA8JUZ8NVE0", review.reviewId);
        assertEquals(true, review.isVedio);
        assertEquals(false, review.isRealName);
        assertEquals(false, review.isVineVoice);
        assertEquals(0, review.topN);
        assertEquals("http://ecx.images-amazon.com/images/I/71FkkZ%2BivzS._SX320_PHcustomer-video-vignette_PIvideo-reviews-bottom,BottomLeft,0,43_OU01_PIcustomer-video-play,BottomLeft,130,-12_CR0,0,0,0_.png",
                review.vedioPicUrl);
        assertEquals(3, review.reviewRank);
        assertEquals("", review.comment);
        assertEquals(8, review.comments);
    }

    @Test
    public void testUSOneReviewNewPage() {
        // 测试 us One Review 页面
        AmazonListingReview review = AmazonListingReview.parseReviewFromOnePage(usOneNewPage);
        assertEquals("B005VBNYDS_AMAZON.COM_A3S9JMG0IRE0BQ", review.alrId);
        assertEquals("B005VBNYDS_amazon.com", review.listingId);
        assertEquals(5.0, review.rating.doubleValue(), 1);
        assertEquals("A Powerful Sleek Charger!", review.title);
        assertNotNull("review", review.review);
        assertEquals(152, review.helpUp, 0);
        assertEquals(173, review.helpClick, 0);
        assertEquals("Matt", review.username);
        assertEquals("A3S9JMG0IRE0BQ", review.userid);
        assertEquals("2012-02-01", review.reviewDate);
        assertEquals(true, review.purchased);
        assertEquals(false, review.resolved);
        assertEquals(5.0, review.lastRating.doubleValue(), 1);
        assertEquals("RLPA8JUZ8NVE0", review.reviewId);
        assertEquals(true, review.isVedio);
        assertEquals(false, review.isRealName);
        assertEquals(false, review.isVineVoice);
        assertEquals(0, review.topN);
        assertEquals("http://ecx.images-amazon.com/images/I/71FkkZ%2BivzS._SX320_PHcustomer-video-vignette_PIvideo-reviews-bottom,BottomLeft,0,43_OU01_PIcustomer-video-play,BottomLeft,130,-12_CR0,0,0,0_.png",
                review.vedioPicUrl);
        assertEquals(-1, review.reviewRank);
        assertEquals("", review.comment);
        assertEquals(8, review.comments);
    }

    @Test
    public void testUKReviewListNewPage() {
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewsFromReviewsListPage(ukNewPage, 1);
        assertEquals(10, reviews.size());
        AmazonListingReview review = reviews.get(1);
        //http://www.amazon.co.uk/EasyAcc-Portable-Emergency-Universal-external/dp/B006FRD8QU/ref=cm_cr_pr_orig_subj
        // 这里是 B005JSG7GE 的 Listing 但是是 B006FRD8QU 的 Review
        assertEquals("B006FRD8QU_AMAZON.CO.UK_A3NLZ7F5280GOA", review.alrId);
        assertEquals("B006FRD8QU_amazon.co.uk", review.listingId);
        assertEquals(5.0, review.rating.doubleValue(), 1);
        assertEquals("A handy device, with good attention to detail and many connectors.", review.title);
        assertNotNull("review", review.review);
        assertEquals(16, review.helpUp, 0);
        assertEquals(16, review.helpClick, 0);
        assertEquals("Nick Stevens \"starbase1\"", review.username);
        assertEquals("A3NLZ7F5280GOA", review.userid);
        assertEquals("2012-06-28", review.reviewDate);
        assertEquals(true, review.purchased);
        assertEquals(false, review.resolved);
        assertEquals(5.0, review.lastRating.doubleValue(), 1);
        assertEquals("R2T01BWRF0FIRF", review.reviewId);
        assertEquals(false, review.isVedio);
        assertEquals(true, review.isRealName);
        assertEquals(false, review.isVineVoice);
        assertEquals(1000, review.topN);
        assertEquals("", review.vedioPicUrl);
        assertEquals(2, review.reviewRank);
        assertEquals("", review.comment);
        assertEquals(0, review.comments);
    }

    @Test
    public void testFRReviewListOldPage() {
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewsFromReviewsListPage(frOldPage, 1);
        assertEquals(10, reviews.size());
        AmazonListingReview review = reviews.get(5);
        assertEquals("B0041OQFAU_AMAZON.FR_A2HSEVAZLZQSS7", review.alrId);
        assertEquals("B0041OQFAU_amazon.fr", review.listingId);
        assertEquals(5.0, review.rating.doubleValue(), 1);
        assertEquals("Très bien", review.title);
        assertNotNull("review", review.review);
        assertEquals(2, review.helpUp, 0);
        assertEquals(2, review.helpClick, 0);
        assertEquals("Patrick Ottavi", review.username);
        assertEquals("A2HSEVAZLZQSS7", review.userid);
        assertEquals("2012-07-13", review.reviewDate);
        assertEquals(true, review.purchased);
        assertEquals(false, review.resolved);
        assertEquals(5.0, review.lastRating.doubleValue(), 1);
        assertEquals("R3IVQU7JUPSZNK", review.reviewId);
        assertEquals(false, review.isVedio);
        assertEquals(false, review.isRealName);
        assertEquals(false, review.isVineVoice);
        assertEquals(500, review.topN);
        assertEquals("", review.vedioPicUrl);
        assertEquals(6, review.reviewRank);
        assertEquals("", review.comment);
        assertEquals(0, review.comments);
    }
}
