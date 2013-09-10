package jobs;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import models.Jobex;
import models.finance.SaleFee;
import models.market.Account;
import models.market.Orderr;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 6/4/13
 * Time: 6:37 PM
 */
public class AmazonFinanceCheckJobTest extends UnitTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        FeeTypeFactory.feeTypeInit();
    }

    @Ignore("选择性测试, 需要访问真实网络")
    @Test
    public void testDoJob() throws InterruptedException, ExecutionException, TimeoutException {
        FactoryBoy.create(Jobex.class, "financeCheck");

        final Account acc = FactoryBoy.create(Account.class, "de");
        acc.loginAmazonSellerCenter();
        FactoryBoy.create(Orderr.class, new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.account = acc;
            }
        });
        AmazonFinanceCheckJob job = new AmazonFinanceCheckJob();
        job.doJob();
        assertThat(SaleFee.count(), is(5l));
    }

    @Test
    public void testSaveFees() {
        FactoryBoy.create(Orderr.class);
        final Account acc = FactoryBoy.create(Account.class, "de");
        List<SaleFee> fees = new ArrayList<SaleFee>();
        for(int i = 0; i <= 4; i++) {
            fees.add(FactoryBoy.build(SaleFee.class, new BuildCallback<SaleFee>() {
                @Override
                public void build(SaleFee target) {
                    target.account = acc;
                }
            }));
        }

        AmazonFinanceCheckJob.saveFees(fees);

        assertThat(SaleFee.count(), is(5l));
    }

    @Test
    public void testDeleteSaleFees() {
        List<Orderr> orderrs = new ArrayList<Orderr>();
        for(int i = 0; i <= 4; i++) {
            final int finalI = i;
            orderrs.add(
                    FactoryBoy.build(Orderr.class, new BuildCallback<Orderr>() {
                        @Override
                        public void build(Orderr target) {
                            target.orderId = finalI + "";
                        }
                    })
            );
        }
        String sql = AmazonFinanceCheckJob.deleteSaleFees(orderrs);
        assertThat(sql, is("DELETE FROM SaleFee WHERE order_orderId in ('0', '1', '2', '3', '4')"));
    }
}
