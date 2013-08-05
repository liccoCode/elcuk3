package jobs.promise;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import helper.Dates;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.libs.F;
import play.libs.IO;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/4/13
 * Time: 10:48 AM
 */
public class FinanceShippedPromiseTest extends UnitTest {

    @Before
    public void setUP() {
        FactoryBoy.deleteAll();
        FeeTypeFactory.feeTypeInit();
    }

    @Test
    public void testTransactionURLs() {
        Account account = FactoryBoy.build(Account.class, "de");
        String orderId = "403-2580115-3776367";
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/promise/" + orderId + ".html"));
        FinanceShippedPromise promise = spy(new FinanceShippedPromise(account, account.type, 8));

        doReturn(html).when(promise).transactionView(anyString());
        List<String> urls = promise.transactionURLs(orderId);
        assertThat(urls.size(), is(1));
        assertThat(urls.get(0), is(containsString("transaction_id=2013154JN1dVmcbRKeKMsWCKF_9xg")));
    }

    @Test
    public void testProductCharges() {
        Account account = FactoryBoy.build(Account.class, "de");
        String orderId = "304-9007836-6625937";
        String url = "http://baidu.com?ie=UTF8&orderId=" + orderId + "&view=search";
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/promise/" + orderId + "_fee.html"));
        FinanceShippedPromise promise = spy(new FinanceShippedPromise(account, account.type, 8));

        List<SaleFee> fees = promise.productCharges(Jsoup.parse(html), url);
        assertThat(fees.size(), is(2));
        SaleFee fee = fees.get(0);

        assertThat(fee.orderId, is(orderId));
        assertThat(fee.cost, is(12.99f));
        assertThat(fee.usdCost, both(is(notNullValue())).and(is(not(12.99f))));
        assertThat(Dates.date2Date(fee.date), is("2013-06-04"));
        assertThat(fee.type.name, is("productcharges"));
        assertThat(fee.qty, is(1));
    }

    @Test
    public void testOtherFee() {
        Account account = FactoryBoy.build(Account.class, "de");
        String orderId = "403-2580115-3776367";
        String url = "http://baidu.com?ie=UTF8&orderId=" + orderId + "&view=search";
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/promise/" + orderId + "_fee.html"));
        FinanceShippedPromise promise = spy(new FinanceShippedPromise(account, account.type, 8));

        List<SaleFee> fees = promise.otherFee(Jsoup.parse(html), url);
        assertThat(fees.size(), is(1));
        SaleFee fee = fees.get(0);

        assertThat(fee.orderId, is(orderId));
        assertThat(fee.cost, is(2.79f));
        assertThat(fee.usdCost, both(is(notNullValue())).and(is(not(2.79f))));
        assertThat(fee.type.name, is("shipping"));
        assertThat(Dates.date2Date(fee.date), is("2013-06-03"));
        assertThat(fee.qty, is(1));
    }

    @Test
    public void testAmazonFee() {
        Account account = FactoryBoy.build(Account.class, "de");
        String orderId = "304-9007836-6625937";
        String url = "http://baidu.com?ie=UTF8&orderId=" + orderId + "&view=search";
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/promise/" + orderId + "_fee.html"));
        FinanceShippedPromise promise = spy(new FinanceShippedPromise(account, account.type, 8));

        List<SaleFee> fees = promise.amazonFee(Jsoup.parse(html), url);
        assertThat(fees.size(), is(3));

        assertThat(fees.get(0).type.name, is("commission"));
        assertThat(fees.get(1).type.name, is("fbapickpackfeeperunit"));
        assertThat(fees.get(2).type.name, is("fbaweighthandlingfee"));

        assertThat(fees.get(0).cost, is(-3.12f));
        assertThat(fees.get(1).cost, is(-2.70f));
        assertThat(fees.get(2).cost, is(-0.90f));
    }

    @Ignore("选择性测试, 直接访问到真是网络环境了")
    @Test
    public void testDoJobWithResult() throws InterruptedException, ExecutionException, TimeoutException {
        Account account = FactoryBoy.build(Account.class, "de");
        Orderr orderr = FactoryBoy.create(Orderr.class, new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.orderId = "402-0706151-5920331";
            }
        });

        account.cookieStore().clear();
        account.loginAmazonSellerCenter();
        List<Orderr> orders = new ArrayList<Orderr>();
        orders.add(orderr);
        FinanceShippedPromise promise = new FinanceShippedPromise(account, M.AMAZON_FR, 8);
        F.Promise<List<SaleFee>> feesPromise = promise.now();
        List<SaleFee> fees = feesPromise.get(3, TimeUnit.MINUTES);
        assertThat(fees.size(), is(5));
    }

}
