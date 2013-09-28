package jobs.analyze;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import factory.finance.FeeTypeFactory;
import helper.Currency;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.market.Selling;
import models.procure.ProcureUnit;
import models.procure.Shipment;
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
        FeeTypeFactory.feeTypeInit();
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
        assertThat(costAndQty._2, is(0));
    }

    private void sellingShipCostFixtures() {
        /**
         * 1. 准备 1 个 FBA 快递的运输费用
         * 2. 准备 1 个海运的运输费用
         * 3. 准备 1 个空运的运输费用
         * 4. 计算当当天的运输成本
         */
        // 1 快递
        ProcureUnit unit = FactoryBoy.create(ProcureUnit.class, "plan");
        Shipment expressShipment = FactoryBoy.create(Shipment.class);
        expressShipment.addToShip(unit);
        PaymentUnit expressShipfee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.expressFee();
                target.unitPrice = 38;
                target.unitQty = 20;
                target.currency = Currency.CNY;
            }
        });
        PaymentUnit expressOtherFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.dutyAndVAT();
                target.unitPrice = 200;
                target.unitQty = 1;
                target.currency = Currency.USD;
            }
        });
        expressShipment.items.get(0).produceFee(expressShipfee, FeeType.expressFee());
        expressShipment.produceFee(expressOtherFee);

        // 2 海运
        ProcureUnit unit2 = FactoryBoy.create(ProcureUnit.class, "planSea");
        Shipment seaShipment = FactoryBoy.create(Shipment.class, "sea");
        seaShipment.addToShip(unit2);
        PaymentUnit seaShipFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.expressFee();
                target.unitPrice = 29;
                target.unitQty = 300;//300 kg
                target.currency = Currency.CNY;
            }
        });
        PaymentUnit seaShipOtherFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.dutyAndVAT();
                target.unitPrice = 300;
                target.currency = Currency.USD;
            }
        });
        seaShipment.produceFee(seaShipFee);
        seaShipment.produceFee(seaShipOtherFee);


        // 3 空运
        ProcureUnit unit3 = FactoryBoy.create(ProcureUnit.class, "planAir");
        Shipment airShipment = FactoryBoy.create(Shipment.class, "air");
        airShipment.addToShip(unit3);
        PaymentUnit airShipFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.expressFee();
                target.unitPrice = 32;
                target.unitQty = 200;//300 kg
                target.currency = Currency.CNY;
            }
        });
        PaymentUnit airShipOtherFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.dutyAndVAT();
                target.unitPrice = 280;
                target.currency = Currency.USD;
            }
        });
        airShipment.produceFee(airShipFee);
        airShipment.produceFee(airShipOtherFee);
    }
}
