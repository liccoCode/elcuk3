package jobs.promise;

import helper.Currency;
import helper.Webs;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.junit.AfterClass;
import org.junit.Test;
import play.db.helper.SqlSelect;
import play.test.UnitTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/26/12
 * Time: 11:19 AM
 */
public class FinanceShippedOrdersTest extends UnitTest {
    static String orderIdUS = "112-1822029-9517052";
    static String orderIdDE = "303-6076357-1004331";
    static String orderIdUK = "202-2658375-2508302";
    static List<String> orderIds = Arrays.asList(orderIdUK, orderIdDE, orderIdUS);


    /**
     * 抓取 Amazon US 上的 Payment 信息, 第一步; productcharges & amazon
     */
    @Test
    public void testDoJob() throws ExecutionException, InterruptedException, IOException, ClassNotFoundException {
        Orderr ord = new Orderr();
        ord.orderId = orderIdUS;
        ord.state = Orderr.S.SHIPPED;
        ord.account = Account.find("type=?", M.AMAZON_US).first();
        ord.save();

        Webs.dev_login(ord.account);
        List<SaleFee> fees = new FinanceShippedOrders(Orderr.<Orderr>findById(orderIdUS)).now().get();
        assertEquals(2, fees.size(), 1);
        assertEquals("productcharges", fees.get(0).type.name);
        assertEquals(1289.7, fees.get(0).cost, 0.01);
        assertEquals(Currency.USD, fees.get(0).currency);

        assertEquals("amazon", fees.get(1).type.name);
        assertEquals(-375.63, fees.get(1).cost, 0.01);
    }

    /**
     * 抓取 Amazon DE 上的 Payment 信息, 第一步; productcharges & amazon
     */
    @Test
    public void testOrder2() throws IOException, ClassNotFoundException, ExecutionException, InterruptedException {
        Orderr ord = new Orderr();
        ord.orderId = orderIdDE;
        ord.state = Orderr.S.SHIPPED;
        ord.account = Account.find("type=?", M.AMAZON_DE).first();
        ord.save();

        Webs.dev_login(ord.account);
        List<SaleFee> fees = new FinanceShippedOrders(Orderr.<Orderr>findById(orderIdDE)).now().get();
        assertEquals(2, fees.size(), 1);
        assertEquals("productcharges", fees.get(0).type.name);
        assertEquals(1449.50, fees.get(0).cost, 0.01);
        assertEquals(Currency.EUR, fees.get(0).currency);

        assertEquals("amazon", fees.get(1).type.name);
        assertEquals(-162.47, fees.get(1).cost, 0.01);
    }

    /**
     * 抓取 Amazon UK 上的 Payment 信息, 第一步; productcharges & amazon
     * 拥有两行, 总共产生 4 条 SaleFee 记录
     */
    @Test
    public void testOrder3() throws IOException, ClassNotFoundException, ExecutionException, InterruptedException {
        Orderr ord = new Orderr();
        ord.orderId = orderIdUK;
        ord.state = Orderr.S.SHIPPED;
        ord.account = Account.find("type=?", M.AMAZON_UK).first();
        ord.save();

        Webs.dev_login(ord.account);
        List<SaleFee> fees = new FinanceShippedOrders(Orderr.<Orderr>findById(orderIdUK)).now().get();
        assertEquals(4, fees.size(), 1);

        //row1
        assertEquals("productcharges", fees.get(0).type.name);
        assertEquals(417.81, fees.get(0).cost, 0.01);
        assertEquals(Currency.GBP, fees.get(0).currency);
        assertEquals("amazon", fees.get(1).type.name);
        assertEquals(-54.16, fees.get(1).cost, 0.01);

        // row2
        assertEquals("productcharges", fees.get(0).type.name);
        assertEquals(241.89, fees.get(2).cost, 0.01);
        assertEquals(Currency.GBP, fees.get(2).currency);
        assertEquals("amazon", fees.get(3).type.name);
        assertEquals(-31.65, fees.get(3).cost, 0.01);
    }

    @AfterClass
    public static void clearUp() {
        SaleFee.delete("orderId IN " + SqlSelect.inlineParam(orderIds));
        Orderr.delete("orderId IN " + SqlSelect.inlineParam(orderIds));
    }
}
