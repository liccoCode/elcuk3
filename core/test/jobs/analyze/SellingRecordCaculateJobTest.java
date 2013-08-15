package jobs.analyze;

import factory.FactoryBoy;
import factory.callback.SequenceCallback;
import helper.Currency;
import models.market.Selling;
import models.procure.ProcureUnit;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.libs.F;
import play.test.UnitTest;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 8/15/13
 * Time: 11:16 AM
 */
public class SellingRecordCaculateJobTest extends UnitTest {
    @Before
    public void testSetUP() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testSellingProcreCost() {
        final Date now = new Date();
        Selling selling = FactoryBoy.create(Selling.class);
        final AtomicInteger sumCount = new AtomicInteger(0);
        FactoryBoy.batchCreate(3, ProcureUnit.class, "done", new SequenceCallback<ProcureUnit>() {
            @Override
            public void sequence(ProcureUnit target, int seq) {
                if(sumCount.get() >= 600) target.attrs.currency = Currency.USD;
                target.attrs.deliveryDate = now;
                sumCount.addAndGet(target.attrs.qty);
            }
        });
        JPA.em().flush();
        SellingRecordCaculateJob job = new SellingRecordCaculateJob();
        F.T2<Float, Integer> costAndQty = job.sellingProcreCost(selling, now);
        assertThat((double) costAndQty._1, closeTo(((600 * 19 / 6.13) + (300 * 19)) / sumCount.get(), 1));
        assertThat(costAndQty._2, is(900));
    }

    @Test
    public void testSellingProcreCostNoCost() {
        final Date now = new Date();
        Selling selling = FactoryBoy.create(Selling.class);
        SellingRecordCaculateJob job = new SellingRecordCaculateJob();
        F.T2<Float, Integer> costAndQty = job.sellingProcreCost(selling, now);
        assertThat((double) costAndQty._1, is(0d));
        assertThat((double) costAndQty._2, is(0d));
    }

    @Test
    public void testSellingShipCost() {
    }
}
