package jobs.promise;

import helper.Webs;
import models.finance.SaleFee;
import models.market.Account;
import models.market.Orderr;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/26/12
 * Time: 11:03 AM
 */
public class FinanceRefundOrdersTest extends UnitTest {
    //TODO 修要修改 OrderId
    String orderId = "002-0031172-2185004";

    @Before
    public void setUPOrder() throws IOException, ClassNotFoundException {
        Orderr ord = new Orderr();
        ord.orderId = orderId;
        ord.account = Account.findById(131l);
        ord.save();

        Webs.dev_login(ord.account);
    }

    @Test
    public void testDoJob() throws ExecutionException, InterruptedException {
        new FinanceRefundOrders(orderId).now().get();
        List<SaleFee> fees = SaleFee.find("orderId=?", orderId).fetch();
        assertEquals(4, fees.size(), 1);
    }

    @After
    public void clearUp() {
        SaleFee.delete("orderId=?", orderId);
        Orderr.delete("orderId=?", orderId);
    }
}
