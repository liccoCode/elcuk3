package crawler;

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
 * Date: 9/21/12
 * Time: 3:25 PM
 */
public class CrawlListingTest extends UnitTest {
    Document usListing;
    Document deListing;

    Document deNoPriceListing = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/de.noprice.B008ORA2FY.html")));

    @Before
    public void setUpPage() {
        usListing = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/listing/us.B005FYNSZA.html")));
        deListing = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/listing/de.B005JSG7GE.html"), "ISO-8859-1"));
    }

    @Test
    public void testUSListing() {
        ListingC listing = ListingC.parseAmazon(usListing);
        assertEquals("B005FYNSZA", listing.asin);
        assertEquals("SanDisk", listing.byWho);
        assertEquals("SanDisk Cruzer Fit 16 GB USB Flash Drive SDCZ33-016G-B35", listing.title);
        assertEquals("B005FYNSZA_amazon.com", listing.listingId);
        assertEquals(312, listing.reviews, 0);
        assertEquals(4.4, listing.rating, 1);
        assertEquals(48, listing.likes);
        assertEquals(86, listing.totalOffers);
        assertNotNull(listing.technicalDetails);
        assertEquals(8, listing.saleRank);
        assertNotNull(listing.productDescription);
        assertEquals(4, listing.offers.size());
    }

    @Test
    public void testDEListing() {
        ListingC listing = ListingC.parseAmazon(deListing);
        assertEquals("B005JSG7GE", listing.asin);
        assertEquals("EasyAcc", listing.byWho);
        assertEquals("EasyAcc 5600mAh Portable Emergency USB externer akku pack ladegerät für Handy SmartPhone Device Apple iPhone, iPad, iPod / Nokia lumia / Samsung Galaxy / HTC One X, Sensation, Wildfire, Desire / Motorola Razr, Defy, Atrix, Milestone / LG Optimus / Sony Ericsson Xperia / Blackberry Bold curve torch / Sony PSP / Google Nexus 7 und Much more. [Mit der EU-Batterie-Ladegerät!]",
                listing.title);
        assertEquals("B005JSG7GE_amazon.de", listing.listingId);
        assertEquals(219, listing.reviews, 0);
        assertEquals(4.7, listing.rating, 1);
        assertEquals(63, listing.likes);
        assertEquals(10, listing.totalOffers);
        assertNotNull(listing.technicalDetails);
        assertEquals(5, listing.saleRank);
        assertNotNull(listing.productDescription);
        assertEquals(1, listing.offers.size());
    }

    @Test
    public void testDENoPriceListing() {
        ListingC listing = ListingC.parseAmazon(deNoPriceListing);
        assertEquals("B008ORA2FY", listing.asin);
        assertEquals("EasyAcc", listing.byWho);
        assertEquals("EasyAcc Black Leather Protective Folio Tasche hülle Case für GOOGLE Nexus 7 Tablet 8 / 16GB WIFI ASUS -mit Sleep / Wake Magnet / Multi-view-Stand und einem Stylus Eingabestifte als Bonus",
                listing.title);
        assertEquals("B008ORA2FY_amazon.de", listing.listingId);
        assertEquals(15, listing.reviews, 0);
        assertEquals(4.1, listing.rating, 1);
        assertEquals(3, listing.likes);
        assertEquals(0, listing.totalOffers);
        assertNotNull(listing.technicalDetails);
        assertEquals(50, listing.saleRank);
        assertNotNull(listing.productDescription);
        assertEquals(0, listing.offers.size());
    }
}
