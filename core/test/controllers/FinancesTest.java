package controllers;

import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/28/12
 * Time: 10:42 AM
 */
public class FinancesTest extends UnitTest {
    @Test
    public void parseSellingFromUrl() {
        String url = "http://www.amazon.co.uk/dp/B00AE3TZCW";
        String[] parts = Elcuk.parseUrl(url);

        assertEquals(3, parts.length);
        assertEquals("http://www.amazon.co.uk/dp/B00AE3TZCW", parts[0]);
        assertEquals("amazon.co.uk", parts[1]);
        assertEquals("B00AE3TZCW", parts[2]);
    }

    @Test
    public void httpsUrl() {
        String url = "https://www.amazon.de/dp/B00AE3TZCW";
        String[] parts = Elcuk.parseUrl(url);

        assertEquals(3, parts.length);
        assertEquals("https://www.amazon.de/dp/B00AE3TZCW", parts[0]);
        assertEquals("amazon.de", parts[1]);
        assertEquals("B00AE3TZCW", parts[2]);
    }

    @Test
    public void noWWW() {
        String url = "http://amazon.de/dp/B00AE3TZCW";
        String[] parts = Elcuk.parseUrl(url);

        assertEquals(3, parts.length);
        assertEquals("http://amazon.de/dp/B00AE3TZCW", parts[0]);
        assertEquals("amazon.de", parts[1]);
        assertEquals("B00AE3TZCW", parts[2]);
    }

    @Test
    public void parseSellingFromUrlGp() {
        String url = "http://www.amazon.co.uk/gp/product/B00AE3TZCW";
        String[] parts = Elcuk.parseUrl(url);

        assertEquals(3, parts.length);
        assertEquals("http://www.amazon.co.uk/gp/product/B00AE3TZCW", parts[0]);
        assertEquals("amazon.co.uk", parts[1]);
        assertEquals("B00AE3TZCW", parts[2]);
    }

    @Test
    public void httpsUrlGp() {
        String url = "https://www.amazon.de/gp/product/B00AE3TZCW";
        String[] parts = Elcuk.parseUrl(url);

        assertEquals(3, parts.length);
        assertEquals("https://www.amazon.de/gp/product/B00AE3TZCW", parts[0]);
        assertEquals("amazon.de", parts[1]);
        assertEquals("B00AE3TZCW", parts[2]);
    }

    @Test
    public void noWWWGp() {
        String url = "http://amazon.de/dp/B00AE3TZCW";
        String[] parts = Elcuk.parseUrl(url);

        assertEquals(3, parts.length);
        assertEquals("http://amazon.de/dp/B00AE3TZCW", parts[0]);
        assertEquals("amazon.de", parts[1]);
        assertEquals("B00AE3TZCW", parts[2]);
    }
}
