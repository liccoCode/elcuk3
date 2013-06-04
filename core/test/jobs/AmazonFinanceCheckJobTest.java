package jobs;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import models.finance.SaleFee;
import models.market.Account;
import models.market.Orderr;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.ArrayList;
import java.util.List;

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

    //    @Ignore
    @Test
    public void testDoJob() {
        AmazonFinanceCheckJob job = new AmazonFinanceCheckJob();
        final Account acc = FactoryBoy.create(Account.class, "ide");
        job.now();
    }

    @Test
    public void testSaveFees() {
        FactoryBoy.create(Orderr.class);
        final Account acc = FactoryBoy.create(Account.class, "ide");
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
