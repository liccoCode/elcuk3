package noRun;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/25/12
 * Time: 7:41 PM
 */
public class ExtranTest {
    @Test
    public void testParsePrice() throws ParseException {
        String price_uk = "£11.99";
        String price_de = "EUR 16,99";
        String price_fr = "EUR 57,75";
        String price_it = "EUR 15,99";
        String price_es = "EUR 61,24";
        String price_us = "$12.39";
        NumberFormat nf_uk = NumberFormat.getCurrencyInstance(Locale.UK);
        NumberFormat nf_eu = NumberFormat.getNumberInstance(Locale.GERMAN);
        NumberFormat nf_us = NumberFormat.getCurrencyInstance(Locale.US);

        System.out.println(nf_uk.parse(price_uk));
        System.out.println(nf_eu.parse(price_de.split(" ")[1]));
        System.out.println(nf_us.parse(price_us));

        System.out.println("---");
        System.out.println(nf_eu.format(7.99));
    }

    @Test
    public void interSuffix() {
        BigDecimal bid = new BigDecimal(32.985).setScale(2, RoundingMode.UP);
        System.out.println(bid.floatValue());
    }
}
