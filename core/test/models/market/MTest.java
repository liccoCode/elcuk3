package models.market;

import factory.FactoryBoy;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/6/13
 * Time: 10:57 AM
 */
public class MTest extends UnitTest {
    @Before
    public void dbSetUP() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testListingEditPageDE() {
        FactoryBoy.create(Account.class, "de");
        Selling de = FactoryBoy.build(Selling.class, "de");
        String url = M.listingEditPage(de);
        assertThat(de.market, is(M.AMAZON_DE));
        assertThat(url,
                is("https://catalog-sc.amazon.de/abis/product/DisplayEditProduct?sku=" + de.merchantSKU + "&asin=" +
                        de.asin));
    }

    @Test
    public void testListingEditPageUS() {
        FactoryBoy.create(Account.class, "us");
        Selling us = FactoryBoy.build(Selling.class, "us");
        System.out.println(us);
        String url = M.listingEditPage(us);
        assertThat(us.market, is(M.AMAZON_US));
        assertThat(url,
                is("https://catalog.amazon.com/abis/product/DisplayEditProduct?sku=" + us.merchantSKU + "&asin=" +
                        us.asin));
    }

    @Test
    public void testListingEditPageUK() {
        FactoryBoy.create(Account.class, "uk");
        Selling uk = FactoryBoy.build(Selling.class, "uk");
        String url = M.listingEditPage(uk);
        assertThat(uk.market, is(M.AMAZON_UK));
        assertThat(url,
                is("https://catalog-sc.amazon.co.uk/abis/product/DisplayEditProduct?sku=" + uk.merchantSKU + "&asin=" +
                        uk.asin));
    }
}
