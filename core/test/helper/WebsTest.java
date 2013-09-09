package helper;

import org.junit.Test;
import play.test.UnitTest;

import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/6/13
 * Time: 1:43 PM
 */
public class WebsTest extends UnitTest {
    @Test
    public void testamzPriceToFormat() {
        String priceStr = Webs.amzPriceToFormat(1881999.99, "50.00");
        assertThat(priceStr, is("1,881,999.99"));
        priceStr = Webs.amzPriceToFormat(1881999.99, "50,00");
        assertThat(priceStr, is("1.881.999,99"));
    }

    @Test
    public void testAmzFormatToPrice() {
        double price = Webs.amzFormatToPrice("1,881,999.99");
        assertThat(price, is(1881999.99));
        price = Webs.amzFormatToPrice("1.881.999,99");
        assertThat(price, is(1881999.99));
    }

    @Test
    public void testNBFormat() {
        String de = Webs.NN_DE.format(1881999.99);
        String uk = Webs.NN_UK.format(1881999.99);
        String us = Webs.NN_US.format(1881999.99);
        assertThat(de, is("1.881.999,99"));
        assertThat(uk, is("1,881,999.99"));
        assertThat(us, is("1,881,999.99"));
    }

    @Test
    public void testNCFormat() {
        String de = Webs.NC_DE.format(1881999.99);
        String uk = Webs.NC_UK.format(1881999.99);
        String us = Webs.NC_US.format(1881999.99);
        assertThat(de, is("1.881.999,99 €"));
        assertThat(uk, is("£1,881,999.99"));
        assertThat(us, is("$1,881,999.99"));
    }
}
