import models.AmazonListingReview;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-2
 * Time: 下午5:13
 */
public class ListingCrawl extends UnitTest {
    public static String HOME = System.getProperty("user.home");

    @Test
    public void testMaxPage() throws IOException {
        Document doc = null;
        for(int i = 1; i <= 5; i++) {
            doc = Jsoup.parse(new File(ListingCrawl.HOME + "/elcuk2-data/reviews/AUK/B004BTWMEI_" + i + ".html"), "UTF-8");
            Assert.assertEquals(10, AmazonListingReview.maxPage(doc));
        }
    }

    @Test
    public void testMaxPage2() throws IOException {
        Document doc = Jsoup.parse(new File(ListingCrawl.HOME + "/elcuk2-data/reviews/AUK/B005JSG7GE_1.html"), "UTF-8");
        Assert.assertEquals(4, AmazonListingReview.maxPage(doc));
    }

    @Test
    public void testParseReviewFR() throws IOException {
        //FR
        Document doc = Jsoup.parse(new File(ListingCrawl.HOME + "/elcuk2-data/reviews/AFR/B005JSG7GE_1.html"), "UTF-8");
        List<AmazonListingReview> reviews = AmazonListingReview.parseReviewFromHTML(doc);
        Assert.assertEquals(2, reviews.size());
        Assert.assertEquals(1, reviews.get(1).helpClick.longValue());
    }

    @Test
    public void testParseReviewDE() throws IOException {
        // DE
        Document doc = Jsoup.parse(new File(ListingCrawl.HOME + "/elcuk2-data/reviews/ADE/B005JSG7GE_1.html"), "UTF-8");
        Assert.assertEquals(10, AmazonListingReview.parseReviewFromHTML(doc).size());
    }

    @Test
    public void testParseReviewUK() throws IOException {
        //UK
        Document doc = Jsoup.parse(new File(ListingCrawl.HOME + "/elcuk2-data/reviews/AUK/B005JSG7GE_1.html"), "UTF-8");
        Assert.assertEquals(10, AmazonListingReview.parseReviewFromHTML(doc).size());

    }
}
