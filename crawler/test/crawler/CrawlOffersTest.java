package crawler;

import models.ListingOfferC;
import models.MT;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import play.Play;
import play.libs.IO;
import play.test.UnitTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/21/12
 * Time: 4:08 PM
 */
public class CrawlOffersTest extends UnitTest {
    Document deOffers = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/offers/de.B004Q3C98S.html")));
    Document usOffers = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/offers/us.B005FYNSZA.html")));
    Document ukOffers = Jsoup.parse(IO.readContentAsString(Play.getFile("test/html/offers/uk.B0037FLUYU.html")));

    @Test
    public void deOffers() {
        List<ListingOfferC> offers = ListingOfferC.parseOffers(MT.ADE, deOffers);
        assertEquals(15, offers.size());
        ListingOfferC offer = offers.get(2);
        assertEquals(false, offer.fba);
        assertEquals(0, offer.shipprice, 2);
        assertEquals(24.59, offer.price, 2);
        assertEquals("new", offer.cond);
        assertEquals(MT.ADE, offer.market);
        assertEquals("HE Trading (beim Kauf gelten die AGB und Versandbedingungen der HE GmbH)", offer.name);
        assertEquals("AQ5RAM5E77DPM", offer.offerId);
        assertEquals(false, offer.buybox);
    }

    @Test
    public void usOffers() {
        List<ListingOfferC> offers = ListingOfferC.parseOffers(MT.AUS, usOffers);
        assertEquals(30, offers.size());
        ListingOfferC offer = offers.get(4);
        assertEquals(true, offer.fba);
        assertEquals(0, offer.shipprice, 2);
        assertEquals(10.88, offer.price, 2);
        assertEquals("new", offer.cond);
        assertEquals(MT.AUS, offer.market);
        assertEquals("Amazon.com", offer.name);
        assertEquals("amazon.com", offer.offerId);
        assertEquals(false, offer.buybox);

        offer = offers.get(5);
        assertEquals(false, offer.fba);
        assertEquals(5.95, offer.shipprice, 2);
        assertEquals(5.04, offer.price, 2);
        assertEquals("new", offer.cond);
        assertEquals(MT.AUS, offer.market);
        assertEquals("Media-Mart", offer.name);
        assertEquals("A28LEC9BBHMQ7G", offer.offerId);
        assertEquals(false, offer.buybox);
    }

    @Test
    public void ukOffers() {
        List<ListingOfferC> offers = ListingOfferC.parseOffers(MT.AUK, ukOffers);
        assertEquals(15, offers.size());
        ListingOfferC offer = offers.get(4);
        assertEquals(true, offer.fba);
        assertEquals(0, offer.shipprice, 2);
        assertEquals(10.37, offer.price, 2);
        assertEquals("new", offer.cond);
        assertEquals(MT.AUK, offer.market);
        assertEquals("Amazon.co.uk", offer.name);
        assertEquals("amazon.co.uk", offer.offerId);
        assertEquals(false, offer.buybox);

        offer = offers.get(10);
        assertEquals(false, offer.fba);
        assertEquals(4.59, offer.shipprice, 2);
        assertEquals(10.18, offer.price, 2);
        assertEquals("new", offer.cond);
        assertEquals(MT.AUK, offer.market);
        assertEquals("booklover618", offer.name);
        assertEquals("ATZRQKJF2XY17", offer.offerId);
        assertEquals(false, offer.buybox);
    }
}

