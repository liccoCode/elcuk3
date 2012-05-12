package market;

import models.market.Account;
import models.market.Selling;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/12/12
 * Time: 5:08 PM
 */
public class SellingDelop extends UnitTest {
    @Test
    public void testDelop() {
        Account.<Account>findById(1l).loginWebSite();

        Selling sell = Selling.findById("68-MAGGLASS-3X75BG,B001OQOK5U_amazon.co.uk");
        sell.deploy();
    }
}
