package finance;

import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/19/12
 * Time: 1:51 PM
 */
public class SaleFeeParseTest extends UnitTest {

    //    @Test
    public void testFlatFilePerformance() {
        Account acc = Account.findById(2l);
        //Test performance, not very important
        SaleFee.flatFileFinanceParse(Play.getFile("test/html/15836299764_14day.txt"), acc);
    }

    @Test
    public void testFlatFile() {
        Account acc = Account.findById(2l);
        Map<String, List<SaleFee>> saleMap = SaleFee.flatFileFinanceParse(Play.getFile("test/html/15836299764_14day2.txt"), acc);

        assertEquals(9, saleMap.size());

        String orderId = "303-9140921-5296344";
        List<SaleFee> normalOrders = saleMap.get(orderId);

        SaleFee fee = normalOrders.get(0);
        assertEquals(orderId, fee.orderId);
        assertEquals(M.AMAZON_DE, fee.market);
        assertEquals("principal", fee.type.name);
        assertEquals(30.99, fee.cost.doubleValue(), 0.01);


        orderId = "SYSTEM";
        normalOrders = saleMap.get(orderId);

        assertEquals(7, normalOrders.size());

        fee = normalOrders.get(0);
        assertEquals("storage fee", fee.type.name);
        assertEquals(-274.14, fee.cost.doubleValue(), 0.01);
    }

    //    @Before
    public void login() {
        Account acc = Account.findById(1l);
        acc.loginAmazonSellerCenter();
    }
}
