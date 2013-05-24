package mws;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersException;
import helper.J;
import models.market.Account;
import models.market.Orderr;
import org.junit.Test;
import play.libs.IO;
import play.test.UnitTest;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/24/13
 * Time: 11:29 AM
 */
public class MWSOrdersTest extends UnitTest {
    @Test
    public void testListOrders() throws MarketplaceWebServiceOrdersException {
        Account acc = Account.findById(2l);
        List<Orderr> orders = MWSOrders.listOrders(acc, 12);
        String sbd = J.json(orders);
        IO.writeContent(sbd,
                new File("/Users/wyatt/Programer/repos/elcuk2/core/orders.json")
        );
    }

}
