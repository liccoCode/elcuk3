package noRun;

import helper.Webs;
import models.market.Account;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/19/12
 * Time: 1:07 PM
 */
public class CurrencyTest {
    @Test
    public void testCur() {
        String fr = "3,34";
        String uk = "0.81";
        System.out.println(Webs.amazonPrice(Account.M.AMAZON_FR, fr));
        System.out.println(Webs.amazonPrice(Account.M.AMAZON_UK, uk));
        System.out.println(Webs.amazonPrice(Account.M.AMAZON_US, uk));

        System.out.println("--------------");
        System.out.println(Webs.nf_uk.format(-32));
        System.out.println(Webs.nf_de.format(-32));
        System.out.println(Webs.nf_us.format(-32));
    }

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
}
