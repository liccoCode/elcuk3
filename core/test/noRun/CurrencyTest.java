package noRun;

import helper.Webs;
import models.market.Account;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/19/12
 * Time: 1:07 PM
 */
public class CurrencyTest {
    @Test
    public void testParseUK() {
        String uk = "£-0.48";
        System.out.println("|" + uk.substring(1));
    }

    @Test
    public void testParseEU() {
        String eu = "EUR -1,20";
        System.out.println("|" + eu.substring(3).trim());
    }

    @Test
    public void testParseDate() {
        String time = "18 Mar 2012";
        System.out.println(DateTime.now().toString("dd MMM yyyy"));
        System.out.println(DateTime.parse(time, DateTimeFormat.forPattern("dd MMM yyyy")));
    }

    @Test
    public void testFulfillment() {
        String ful = "fbaperorderfulfillmentfee";
        String typeStr = StringUtils.replace(
                StringUtils.join(StringUtils.split(ful, " "), "").toLowerCase(),
                "fulfillment",
                "fulfilment");
        System.out.println(ful);
        System.out.println(typeStr);
    }

    @Test
    public void testNumberFormat() {
        NumberFormat nc_uk = NumberFormat.getCurrencyInstance(Locale.UK);
        NumberFormat nc_us = NumberFormat.getCurrencyInstance(Locale.US);
        NumberFormat nc_de = NumberFormat.getCurrencyInstance(Locale.GERMAN);
        NumberFormat nc_de2 = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        NumberFormat nc_fr = NumberFormat.getCurrencyInstance(Locale.FRENCH);
        NumberFormat nc_fr2 = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        NumberFormat nc_it = NumberFormat.getCurrencyInstance(Locale.ITALIAN);
        NumberFormat nc_it2 = NumberFormat.getCurrencyInstance(Locale.ITALY);

        System.out.println("NC_UK:" + nc_uk.format(1234.23));
        System.out.println("NC_US:" + nc_us.format(1234.23));
        System.out.println("NC_DE:" + nc_de.format(1234.23));
        System.out.println("NC_DE2:" + nc_de2.format(1234.23));
        System.out.println("NC_FR:" + nc_fr.format(1234.23));
        System.out.println("NC_FR2:" + nc_fr2.format(1234.23));
        System.out.println("NC_IT:" + nc_it.format(1234.23));
        System.out.println("NC_IT2:" + nc_it2.format(1234.23));

        System.out.println("__________________________________");
        NumberFormat nn_uk = NumberFormat.getNumberInstance(Locale.UK);
        NumberFormat nn_us = NumberFormat.getNumberInstance(Locale.US);
        NumberFormat nn_de = NumberFormat.getNumberInstance(Locale.GERMAN);
        NumberFormat nn_de2 = NumberFormat.getNumberInstance(Locale.GERMANY);
        NumberFormat nn_fr = NumberFormat.getNumberInstance(Locale.FRANCE);
        NumberFormat nn_fr2 = NumberFormat.getNumberInstance(Locale.FRENCH);
        NumberFormat nn_it = NumberFormat.getNumberInstance(Locale.ITALIAN);
        NumberFormat nn_it2 = NumberFormat.getNumberInstance(Locale.ITALY);
        System.out.println("NN_UK:" + nn_uk.format(1234.23));
        System.out.println("NN_US:" + nn_us.format(1234.23));
        System.out.println("NN_DE:" + nn_de.format(1234.23));
        System.out.println("NN_DE2:" + nn_de2.format(1234.23));
        System.out.println("NN_FR:" + nn_fr.format(1234.23));
        System.out.println("NN_FR2:" + nn_fr2.format(1234.23));
        System.out.println("NN_IT:" + nn_it.format(1234.23));
        System.out.println("NN_IT2:" + nn_it2.format(1234.23));
    }

    @Test
    public void parseCurrency() {
        /**
         *                             // 从 [EUR -1,61] 变成了 [€-1.21]
         cost = Webs.amazonPriceNumber(Account.M.AMAZON_UK, priceStr.substring(1).trim());
         */
        String priceStr = "EUR -1,61";
        String priceStr2 = "€-1.21";
        System.out.println(Webs.amazonPriceNumber(Account.M.AMAZON_UK, priceStr.substring(1).trim()));
        System.out.println(Webs.amazonPriceNumber(Account.M.AMAZON_DE, priceStr.substring(3).trim()));
        System.out.println("------------------------");
        System.out.println(Webs.amazonPriceNumber(Account.M.AMAZON_UK, priceStr2.substring(1).trim()));
        System.out.println(Webs.amazonPriceNumber(Account.M.AMAZON_DE, priceStr2.substring(3).trim()));
        System.out.println("------------------------");
        System.out.println(Webs.amazonPriceCurrency(Account.M.AMAZON_UK, priceStr2));
    }
}
