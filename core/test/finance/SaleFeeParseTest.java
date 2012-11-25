package finance;

import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import org.junit.Test;
import play.Play;
import play.test.UnitTest;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
    public void testOnece() {
        Account acc = Account.findById(1l);
        Map<String, List<SaleFee>> saleMap = SaleFee.flatFileFinanceParse(new File("/Users/wyatt/Downloads/15224217864.txt"), acc);
    }

    //    @Test
    public void testFlatFile() {
        Account acc = Account.findById(2l);
        Map<String, List<SaleFee>> saleMap = SaleFee.flatFileFinanceParse(Play.getFile("test/html/15836299764_14day2.txt"), acc);

        assertEquals(11, saleMap.size());

        String orderId = "303-9140921-5296344";
        List<SaleFee> normalOrders = saleMap.get(orderId);

        SaleFee fee = normalOrders.get(0);
        assertEquals(orderId, fee.orderId);
        assertEquals(M.AMAZON_DE, fee.market);
        assertEquals("principal", fee.type.name);
        assertEquals(30.99, fee.cost.doubleValue(), 0.01);
        assertEquals(2d, fee.account.id.doubleValue(), 1);
        assertEquals(1, fee.qty, 0);


        orderId = "SYSTEM";
        normalOrders = saleMap.get(orderId);

        assertEquals(7, normalOrders.size());

        fee = normalOrders.get(0);
        assertEquals("storage fee", fee.type.name);
        assertEquals(-274.14, fee.cost.doubleValue(), 0.01);

        orderId = "302-9413522-1521148";
        normalOrders = saleMap.get(orderId);
        fee = normalOrders.get(0);
        assertEquals("refundcommission", fee.type.name);
        assertEquals(-0.22, fee.cost.doubleValue(), 0.01);
    }

    @Test
    public void testOrderId() {
        String orderId = "303-4067942-6061902";
        //303-4067942-6061902
        Pattern orderPattern = Pattern.compile("^\\d{3}-\\d{7}-\\d{7}$");
        assertEquals(true, Pattern.matches(orderPattern.pattern(), orderId));
    }

    //    @Before
    public void login() {
        Account acc = Account.findById(1l);
        acc.loginAmazonSellerCenter();
    }
}
