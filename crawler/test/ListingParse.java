import models.ListingC;
import org.junit.Test;
import play.libs.IO;
import play.test.UnitTest;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/18/12
 * Time: 9:58 AM
 */
public class ListingParse extends UnitTest {
    @Test
    public void testParseAvailable() {
        ListingC l = new ListingC(IO.readContentAsString(new File("/Users/wyattpan/elcuk2-data/listings/AUK/B007TR9VRU.html")));
        l.parseFromHTML(ListingC.T.AMAZON);
    }
}
