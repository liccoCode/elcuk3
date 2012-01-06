package market;

import models.market.Account;
import models.market.Orderr;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 1/6/12
 * Time: 11:10 AM
 */
public class OrderTest extends UnitTest {
    @Test
    public void saveOrder() {
        Orderr order = new Orderr();
        order.paymentDate = new Date();
        order.market = Account.M.AMAZON_UK;
        order.state = Orderr.S.PENDING;
        order.buyer = "wyatt";
        order.address = "address";
        order.email = "df";
        order.orderId = "88d8d8d8";
        

        order.save();
    }
}
