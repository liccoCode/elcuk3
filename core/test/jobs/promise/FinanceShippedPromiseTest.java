package jobs.promise;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import helper.Currency;
import helper.Dates;
import helper.Webs;
import models.finance.FeeType;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.Play;
import play.db.DB;
import play.libs.F;
import play.libs.IO;
import play.test.UnitTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
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
        FinanceShippedPromise promise = spy(new FinanceShippedPromise(account, account.type, null, 8));

        doReturn(html).when(promise).transactionView(anyString());
        List<String> urls = promise.transactionURLs(orderId);
        assertThat(urls.size(), is(1));
        assertThat(urls.get(0), is(containsString("transaction_id=2013154JN1dVmcbRKeKMsWCKF_9xg")));
    }

    @Ignore("选择性测试, 直接访问到真是网络环境了")
    @Test
    public void testTransactionURLsUS() throws IOException, ClassNotFoundException {
        Account account = FactoryBoy.create(Account.class, "us");
        Webs.devLogin(account);
        String orderId = "102-6196603-4956252";
        FinanceShippedPromise promise = new FinanceShippedPromise(account, account.type, null, 8);

        List<String> urls = promise.transactionURLs(orderId);
        assertThat(urls.size(), is(1));
        assertThat(urls.get(0), is(containsString("transaction_id=2013274_hAo33APRC6VqnQ0J2qnng")));
    }

    @Test
    public void testProductCharges() {
        Account account = FactoryBoy.build(Account.class, "de");
        String orderId = "304-9007836-6625937";
        String url = "http://baidu.com?ie=UTF8&orderId=" + orderId + "&view=search";
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/promise/" + orderId + "_fee.html"));
        FinanceShippedPromise promise = new FinanceShippedPromise(account, account.type, null, 8);

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
        FinanceShippedPromise promise = new FinanceShippedPromise(account, account.type, null, 8);

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
        FinanceShippedPromise promise = new FinanceShippedPromise(account, account.type, null, 8);

        List<SaleFee> fees = promise.amazonFee(Jsoup.parse(html), url);
        assertThat(fees.size(), is(3));

        assertThat(fees.get(0).type.name, is("commission"));
        assertThat(fees.get(1).type.name, is("fbapickpackfeeperunit"));
        assertThat(fees.get(2).type.name, is("fbaweighthandlingfee"));

        assertThat(fees.get(0).cost, is(-3.12f));
        assertThat(fees.get(1).cost, is(-2.70f));
        assertThat(fees.get(2).cost, is(-0.90f));
    }

    @Test
    public void testPromotionFees() {
        Account account = FactoryBoy.build(Account.class, "us");
        String orderId = "107-2874174-4269032";
        String url = "http://baidu.com?ie=UTF8&orderId=" + orderId + "&view=search";
        String html = IO.readContentAsString(Play.getFile("test/html/jobs/promise/" + orderId + "_fee.html"));
        FinanceShippedPromise promise = new FinanceShippedPromise(account, account.type, null, 8);

        List<SaleFee> fees = promise.promotionFee(Jsoup.parse(html), url);
        assertThat(fees.size(), is(2));

        assertThat(fees.get(0).qty, is(1));
        assertThat(fees.get(1).qty, is(1));

        assertThat(fees.get(0).cost, is(-1.96f));
        assertThat(fees.get(1).cost, is(-0.64f));
    }

    @Ignore("选择性测试, 直接访问到真是网络环境了")
    @Test
    public void testDoJobWithResultUS() throws Exception {
        Account account = FactoryBoy.create(Account.class, "us");
        Orderr orderr = FactoryBoy.create(Orderr.class, new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.orderId = "102-6196603-4956252";
            }
        });

        // 因为 FactoryBoy.deleteAll 对 SaleFee 进行了 DDL 操作, 必须将当前 Transaction commit 才可以, 否则会出现
        // Lock wait timeout exceeded; try restarting transaction
        // ref: http://gladness.itpub.net/post/6254/62883
        DB.getConnection().commit();

        Webs.devLogin(account);

        List<Orderr> orders = new ArrayList<Orderr>();
        orders.add(orderr);
        FinanceShippedPromise promise = new FinanceShippedPromise(account, M.AMAZON_US, Orderr.ids(orders), 8);
        F.Promise<List<SaleFee>> feesPromise = promise.now();
        List<SaleFee> fees = feesPromise.get(1, TimeUnit.MINUTES);
        assertThat(fees.size(), is(8));

        assertThat(fees.get(0).type, is(FeeType.productCharger()));
        assertThat(fees.get(0).qty, is(2));
        assertThat((double) fees.get(0).cost, is(closeTo(34.98, 0.1)));

        assertThat(fees.get(1).type, is(FeeType.promotions()));
        assertThat(fees.get(1).qty, is(2));
        assertThat((double) fees.get(1).cost, is(0d));

        assertThat(fees.get(2).type, is(FeeType.shipping()));
        assertThat((double) fees.get(2).cost, is(closeTo(-8.45, 0.1)));

        assertThat(fees.get(3).type, is(FeeType.shipping()));
        assertThat((double) fees.get(3).cost, is(closeTo(8.45, 0.1)));

        assertThat(fees.get(4).type, is(FeeType.findById("commission")));
        assertThat(fees.get(5).type, is(FeeType.findById("fbaperorderfulfilmentfee")));
        assertThat(fees.get(6).type, is(FeeType.findById("fbaperunitfulfillmentfee")));
        assertThat(fees.get(7).type, is(FeeType.findById("fbaweightbasedfee")));

        assertThat(fees.get(2).currency, is(Currency.USD));
    }

    @Ignore("选择性测试, 直接访问到真是网络环境了")
    @Test
    public void testDoJobWithResultDE() throws Exception {
        Account account = FactoryBoy.create(Account.class, "de");
        Orderr orderr = FactoryBoy.create(Orderr.class, new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.orderId = "028-3132033-6134754";
            }
        });

        // 因为 FactoryBoy.deleteAll 对 SaleFee 进行了 DDL 操作, 必须将当前 Transaction commit 才可以, 否则会出现
        // Lock wait timeout exceeded; try restarting transaction
        // ref: http://gladness.itpub.net/post/6254/62883
        DB.getConnection().commit();

        Webs.devLogin(account);

        List<Orderr> orders = new ArrayList<Orderr>();
        orders.add(orderr);
        FinanceShippedPromise promise = new FinanceShippedPromise(account, M.AMAZON_DE, Orderr.ids(orders), 8);
        F.Promise<List<SaleFee>> feesPromise = promise.now();
        List<SaleFee> fees = feesPromise.get(1, TimeUnit.MINUTES);
        assertThat(fees.size(), is(7));

        assertThat(fees.get(0).type, is(FeeType.productCharger()));
        assertThat(fees.get(1).type, is(FeeType.productCharger()));
        assertThat(fees.get(2).type, is(FeeType.promotions()));
        assertThat(fees.get(3).type, is(FeeType.promotions()));

        assertThat(fees.get(4).type, is(FeeType.findById("commission")));
        assertThat(fees.get(5).type, is(FeeType.findById("fbapickpackfeeperunit")));
        assertThat(fees.get(6).type, is(FeeType.findById("fbaweighthandlingfee")));

        assertThat(fees.get(2).currency, is(Currency.EUR));
        assertThat((double) fees.get(2).cost, is(closeTo(-1.06d, 0.01)));
    }

    @Ignore("选择性测试, 直接访问到真是网络环境了")
    @Test
    public void testDoJobWithResultUK() throws Exception {
        Account account = FactoryBoy.create(Account.class, "uk");
        Orderr orderr = FactoryBoy.create(Orderr.class, new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.orderId = "026-1779697-4118709";
            }
        });

        // 因为 FactoryBoy.deleteAll 对 SaleFee 进行了 DDL 操作, 必须将当前 Transaction commit 才可以, 否则会出现
        // Lock wait timeout exceeded; try restarting transaction
        // ref: http://gladness.itpub.net/post/6254/62883
        DB.getConnection().commit();

        Webs.devLogin(account);

        List<Orderr> orders = new ArrayList<Orderr>();
        orders.add(orderr);
        FinanceShippedPromise promise = new FinanceShippedPromise(account, M.AMAZON_UK, Orderr.ids(orders), 8);
        F.Promise<List<SaleFee>> feesPromise = promise.now();
        List<SaleFee> fees = feesPromise.get(1, TimeUnit.MINUTES);
        assertThat(fees.size(), is(9));

        assertThat(fees.get(0).type, is(FeeType.productCharger()));
        assertThat(fees.get(1).type, is(FeeType.productCharger()));
        assertThat(fees.get(2).type, is(FeeType.promotions()));
        assertThat(fees.get(3).type, is(FeeType.promotions()));
        assertThat(fees.get(4).type, is(FeeType.shipping()));

        assertThat(fees.get(5).type, is(FeeType.findById("commission")));
        assertThat(fees.get(6).type, is(FeeType.findById("fbapickpackfeeperunit")));
        assertThat(fees.get(7).type, is(FeeType.findById("fbaweighthandlingfee")));
        assertThat(fees.get(8).type, is(FeeType.findById("shippingchargeback")));

        assertThat(fees.get(2).currency, is(Currency.GBP));
        assertThat((double) fees.get(2).cost, is(closeTo(-1.36d, 0.01)));
    }

    @Ignore("选择性测试, 直接访问到真是网络环境了")
    @Test
    public void testDoJobWithResultES() throws Exception {
        Account account = FactoryBoy.create(Account.class, "de");
        Orderr orderr = FactoryBoy.create(Orderr.class, new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.orderId = "403-8021184-5905937";
            }
        });

        // 因为 FactoryBoy.deleteAll 对 SaleFee 进行了 DDL 操作, 必须将当前 Transaction commit 才可以, 否则会出现
        // Lock wait timeout exceeded; try restarting transaction
        // ref: http://gladness.itpub.net/post/6254/62883
        DB.getConnection().commit();

        Webs.devLogin(account);

        List<Orderr> orders = new ArrayList<Orderr>();
        orders.add(orderr);
        FinanceShippedPromise promise = new FinanceShippedPromise(account, M.AMAZON_ES, Orderr.ids(orders), 8);
        F.Promise<List<SaleFee>> feesPromise = promise.now();
        List<SaleFee> fees = feesPromise.get(1, TimeUnit.MINUTES);
        assertThat(fees.size(), is(10));

        assertThat(fees.get(0).type, is(FeeType.productCharger()));
        assertThat(fees.get(1).type, is(FeeType.productCharger()));
        assertThat(fees.get(2).type, is(FeeType.promotions()));
        assertThat(fees.get(3).type, is(FeeType.promotions()));
        assertThat(fees.get(4).type, is(FeeType.shipping()));

        assertThat(fees.get(5).type, is(FeeType.findById("commission")));
        assertThat(fees.get(6).type, is(FeeType.findById("crossborderfulfilmentfee")));
        assertThat(fees.get(7).type, is(FeeType.findById("fbapickpackfeeperunit")));
        assertThat(fees.get(8).type, is(FeeType.findById("fbaweighthandlingfee")));
        assertThat(fees.get(9).type, is(FeeType.findById("shippingchargeback")));

        assertThat(fees.get(5).currency, is(Currency.EUR));
        assertThat((double) fees.get(5).cost, is(closeTo(-7.10d, 0.01)));
        assertThat(fees.get(4).currency, is(Currency.EUR));
        assertThat((double) fees.get(4).cost, is(closeTo(2.99d, 0.01)));
    }

    @Ignore("选择性测试, 直接访问到真是网络环境了")
    @Test
    public void testDoJobWithResultFR() throws Exception {
        Account account = FactoryBoy.create(Account.class, "de");
        Orderr orderr = FactoryBoy.create(Orderr.class, new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.orderId = "171-5516934-2662725";
            }
        });

        // 因为 FactoryBoy.deleteAll 对 SaleFee 进行了 DDL 操作, 必须将当前 Transaction commit 才可以, 否则会出现
        // Lock wait timeout exceeded; try restarting transaction
        // ref: http://gladness.itpub.net/post/6254/62883
        DB.getConnection().commit();

        Webs.devLogin(account);

        List<Orderr> orders = new ArrayList<Orderr>();
        orders.add(orderr);
        FinanceShippedPromise promise = new FinanceShippedPromise(account, M.AMAZON_FR, Orderr.ids(orders), 8);
        F.Promise<List<SaleFee>> feesPromise = promise.now();
        List<SaleFee> fees = feesPromise.get(1, TimeUnit.MINUTES);
        assertThat(fees.size(), is(8));

        assertThat(fees.get(0).type, is(FeeType.productCharger()));
        assertThat(fees.get(1).type, is(FeeType.productCharger()));
        assertThat(fees.get(2).type, is(FeeType.promotions()));
        assertThat(fees.get(3).type, is(FeeType.promotions()));

        assertThat(fees.get(4).type, is(FeeType.findById("commission")));
        assertThat(fees.get(5).type, is(FeeType.findById("crossborderfulfilmentfee")));
        assertThat(fees.get(6).type, is(FeeType.findById("fbapickpackfeeperunit")));
        assertThat(fees.get(7).type, is(FeeType.findById("fbaweighthandlingfee")));

        assertThat(fees.get(4).currency, is(Currency.EUR));
        assertThat((double) fees.get(4).cost, is(closeTo(-14.59d, 0.01)));
        assertThat(fees.get(5).currency, is(Currency.EUR));
        assertThat((double) fees.get(5).cost, is(closeTo(-4.20d, 0.01)));
    }

    @Ignore("选择性测试, 直接访问到真是网络环境了")
    @Test
    public void testDoJobWithResultIT() throws Exception {
        Account account = FactoryBoy.create(Account.class, "de");
        Orderr orderr = FactoryBoy.create(Orderr.class, new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.orderId = "403-8722498-3247509";
            }
        });

        // 因为 FactoryBoy.deleteAll 对 SaleFee 进行了 DDL 操作, 必须将当前 Transaction commit 才可以, 否则会出现
        // Lock wait timeout exceeded; try restarting transaction
        // ref: http://gladness.itpub.net/post/6254/62883
        DB.getConnection().commit();

        Webs.devLogin(account);

        List<Orderr> orders = new ArrayList<Orderr>();
        orders.add(orderr);
        FinanceShippedPromise promise = new FinanceShippedPromise(account, M.AMAZON_IT, Orderr.ids(orders), 8);
        F.Promise<List<SaleFee>> feesPromise = promise.now();
        List<SaleFee> fees = feesPromise.get(1, TimeUnit.MINUTES);
        assertThat(fees.size(), is(12));

        assertThat(fees.get(0).type, is(FeeType.productCharger()));
        assertThat(fees.get(0).currency, is(Currency.EUR));
        assertThat((double) fees.get(0).cost, is(closeTo(37.99d, 0.01)));
        assertThat(fees.get(1).type, is(FeeType.productCharger()));
        assertThat(fees.get(2).type, is(FeeType.productCharger()));
        assertThat(fees.get(3).type, is(FeeType.productCharger()));

        assertThat(fees.get(4).type, is(FeeType.promotions()));
        assertThat(fees.get(5).type, is(FeeType.promotions()));
        assertThat(fees.get(6).type, is(FeeType.promotions()));
        assertThat(fees.get(6).currency, is(Currency.EUR));
        assertThat((double) fees.get(6).cost, is(closeTo(-0.66d, 0.01)));
        assertThat(fees.get(7).type, is(FeeType.promotions()));
        assertThat(fees.get(7).currency, is(Currency.EUR));
        assertThat((double) fees.get(7).cost, is(closeTo(0.0d, 0.01)));

        assertThat(fees.get(8).type, is(FeeType.findById("commission")));
        assertThat(fees.get(8).currency, is(Currency.EUR));
        assertThat((double) fees.get(8).cost, is(closeTo(-9.12d, 0.01)));
        assertThat(fees.get(9).type, is(FeeType.findById("crossborderfulfilmentfee")));
        assertThat(fees.get(9).currency, is(Currency.EUR));
        assertThat((double) fees.get(9).cost, is(closeTo(-8.40d, 0.01)));
        assertThat(fees.get(10).type, is(FeeType.findById("fbapickpackfeeperunit")));
        assertThat(fees.get(11).type, is(FeeType.findById("fbaweighthandlingfee")));

    }
}
