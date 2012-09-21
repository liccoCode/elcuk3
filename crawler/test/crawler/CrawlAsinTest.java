package crawler;

import com.google.gson.Gson;
import models.ListingC;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/17/12
 * Time: 4:55 PM
 */
public class CrawlAsinTest extends UnitTest {
    Document newPage;
    Document oldPage;

    @Before
    public void setUp() {
        newPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/asin.B007H4J80K.html")));
        oldPage = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/asin.B007H4J80K_old.html")));
    }

    @Test
    public void testNewPage() {
        System.out.println("NewPage: -------------------------");
        System.out.println(new Gson().toJson(ListingC.parseAmazon(newPage)));
    }

}
