package hibernate;

import models.market.Selling;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/28/12
 * Time: 1:04 PM
 */
public class SellingCacheStrategyTest extends UnitTest {

    @Before
    public void testLevelOneCache() {
        System.out.println("Before..");
        Selling s1 = Selling.findById("68-MAGGLASS-3x75BG,B001OQOK5U_amazon.co.uk");
        System.out.println(s1);

        Selling s2 = Selling.findById("68-MAGGLASS-3x75BG,B001OQOK5U_amazon.co.uk");
        System.out.println(s2);

        System.out.println(s1.equals(s2));
        System.out.println("s1.aps.quantity:" + s1.aps.quantity);
        s1.aps.quantity = 8;
        s1.save();

        System.out.println("s1.aps.quantity:" + s1.aps.quantity);

        Selling s3 = Selling.findById("68-MAGGLASS-3x75BG,B001OQOK5U_amazon.co.uk");
        System.out.println(s3);
        System.out.println("s3.aps.quantity:" + s3.aps.quantity);

        Selling s4 = Selling.findById("68-MAGGLASS-3x75BG,B001OQOK5U_amazon.co.uk");
        System.out.println(s4);

        System.out.println(s3.equals(s4));
    }

    @Test
    public void testLevelTwoCache() {
        System.out.println("Test...");
        Selling s1 = Selling.findById("68-MAGGLASS-3x75BG,B001OQOK5U_amazon.co.uk");
        System.out.println(s1);
        System.out.println(s1.aps.quantity);
    }
}
