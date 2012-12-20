package jobs.promise;

import helper.Currency;
import helper.Webs;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.test.UnitTest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/26/12
 * Time: 11:19 AM
 */
public class FinanceShippedOrdersTest extends UnitTest {
    static String orderId = "002-0031172-2185004";

    @BeforeClass
    public static void setUPOrder() throws IOException, ClassNotFoundException {
        Orderr ord = new Orderr();
        ord.orderId = orderId;
        ord.state = Orderr.S.SHIPPED;
        ord.account = Account.find("type=?", M.AMAZON_US).first();
        ord.save();

        Webs.dev_login(ord.account);
    }


    /**
     * 抓取 Amazon 上的 Payment 信息, 第一步; productcharges & amazon
     */
    @Test
    public void testDoJob() throws ExecutionException, InterruptedException {
        assertEquals(1, Orderr.count(), 1);
        List<SaleFee> fees = new FinanceShippedOrders(Orderr.<Orderr>findById(orderId)).now().get();
        assertEquals(2, fees.size(), 1);
        assertEquals(fees.get(0).type.name, "productcharges");
        assertEquals(fees.get(0).cost, 42.99, 0.01);
        assertEquals(fees.get(0).currency, Currency.USD);

        assertEquals(fees.get(1).type.name, "amazon");
        assertEquals(fees.get(1).cost, -13.49, 0.01);
    }

    @Test
    public void testOrderrs() {
        List<Orderr> orderrs = FinanceShippedOrders.orderrs();
        assertEquals(1, orderrs.size(), 1);
    }

    @AfterClass
    public static void clearUp() {
        SaleFee.delete("orderId=?", orderId);
        Orderr.delete("orderId=?", orderId);
    }
}
