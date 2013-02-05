package jobs;

import helper.Currency;
import helper.Dates;
import jobs.promise.FinanceRefundOrders;
import jobs.promise.FinanceShippedOrders;
import models.finance.FeeType;
import models.finance.SaleFee;
import models.market.M;
import models.market.Orderr;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Play;
import play.template2.IO;
import play.test.Fixtures;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/12/12
 * Time: 5:11 PM
 */
public class FinanceCheckJobTest extends UnitTest {
    @BeforeClass
    public static void fixtures() {
        Fixtures.deleteDatabase();
        Fixtures.loadModels("/jobs/Account.yml");
    }

    @Before
    public void login() throws ExecutionException, InterruptedException {
        new KeepSessionJob().now().get();
    }

    //    @Test
    public void testFinance() {
        new FinanceCheckJob().now();
    }

    //    @Test
    public void testOneTransactionFeeDE() {
        String html = IO.readContentAsString(Play.getFile("test/html/payment.de.html"));
        List<SaleFee> fees = FinanceCheckJob.oneTransactionFee(html);

        assertEquals(4, fees.size());

        SaleFee fee = fees.get(0);
        assertEquals(-16.99, fee.cost.doubleValue(), 0.01/*误差值*/);
        assertEquals(M.AMAZON_DE, fee.market);
        assertEquals(Currency.EUR, fee.currency);
        //assertEquals("303-4807218-4159515", fee.order.orderId);
        assertEquals("303-5838665-8976304", fee.orderId);
        assertEquals(Currency.EUR.toUSD(-16.99f), fee.usdCost, 0.01);
        assertEquals("productcharges", fee.type.name);
        assertEquals("2012-04-07", Dates.date2Date(fee.date));


        SaleFee amazonFee = fees.get(1);
        assertEquals(1.09, amazonFee.cost.doubleValue(), 0.01);
        assertEquals(Currency.EUR.toUSD(1.09f), amazonFee.usdCost, 0.01);
        assertEquals("2012-04-07", Dates.date2Date(fee.date));


        fee = fees.get(2);
        assertEquals(16.99, fee.cost.doubleValue(), 0.01);
        assertEquals(Currency.EUR.toUSD(16.99f), fee.usdCost, 0.01);
        assertEquals("2012-03-30", Dates.date2Date(fee.date));

        amazonFee = fees.get(3);
        assertEquals(-3.99, amazonFee.cost.doubleValue(), 0.01);
        assertEquals(Currency.EUR.toUSD(-3.99f), amazonFee.usdCost, 0.01);
        assertEquals("2012-03-30", Dates.date2Date(amazonFee.date));
    }

    //    @Test
    public void testOneTransactionFeeUK() {
        String html = IO.readContentAsString(Play.getFile("test/html/payment.uk.html"));
        List<SaleFee> fees = FinanceCheckJob.oneTransactionFee(html);
        SaleFee fee = fees.get(0);
        assertEquals(21.98, fee.cost.doubleValue(), 0.01/*误差值*/);
        assertEquals(M.AMAZON_UK, fee.market);
        assertEquals(Currency.GBP, fee.currency);
        //assertEquals("026-5308409-7603533", fee.order.orderId);
        assertEquals("026-5308409-7603533", fee.orderId);
        assertEquals(Currency.GBP.toUSD(21.98f), fee.usdCost, 0.01);
        assertEquals("productcharges", fee.type.name);
        assertEquals("2012-11-23", Dates.date2Date(fee.date));

        SaleFee amazonFee = fees.get(1);
        assertEquals(-3.66, amazonFee.cost.doubleValue(), 0.01);
        assertEquals(Currency.GBP.toUSD(-3.66f), amazonFee.usdCost, 0.01);
    }

    //    @Test
    public void testOneTransactionFeeUS() {
        String html = IO.readContentAsString(Play.getFile("test/html/payment.us.html"));
        List<SaleFee> fees = FinanceCheckJob.oneTransactionFee(html);
        SaleFee fee = fees.get(0);
        assertEquals(21.98, fee.cost.doubleValue(), 0.01/*误差值*/);
        assertEquals(M.AMAZON_US, fee.market);
        assertEquals(Currency.USD, fee.currency);
        //assertEquals("110-6815187-8483453", fee.order.orderId);
        assertEquals("110-6815187-8483453", fee.orderId);
        assertEquals(Currency.USD.toUSD(21.98f), fee.usdCost, 0.01);
        assertEquals("productcharges", fee.type.name);
        assertEquals("2012-11-21", Dates.date2Date(fee.date));

        SaleFee amazonFee = fees.get(1);
        assertEquals(-5.50, amazonFee.cost.doubleValue(), 0.01);
        assertEquals(Currency.USD.toUSD(-5.50f), amazonFee.usdCost, 0.01);
    }

    //    @Test
    public void testFinanceShippedOrders() throws ExecutionException, InterruptedException {
        new FinanceShippedOrders(Orderr.<Orderr>findById("303-4405766-7121101")).now().get();
    }

    //    @Test
    public void testFinanceRefunedOrders() throws ExecutionException, InterruptedException {
        new FinanceRefundOrders("303-5838665-8976304").now().get();
    }

    @Test
    public void testSaleFee() {
        List<SaleFee> fees = new ArrayList<SaleFee>();
        SaleFee f1 = new SaleFee();
        f1.type = new FeeType("principal", null);
        f1.usdCost = 24f;
        fees.add(f1);

        SaleFee f2 = new SaleFee();
        f2.type = new FeeType("commison", null);
        f2.usdCost = 12f;
        fees.add(f2);

        assertEquals(true, FinanceShippedOrders.isWarnning(fees));
    }
}
