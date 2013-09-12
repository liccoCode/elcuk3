package services;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import helper.Currency;
import models.finance.FeeType;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.market.Selling;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import org.junit.Before;
import org.junit.Test;
import play.libs.F;
import play.test.UnitTest;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 9/11/13
 * Time: 5:50 PM
 */
public class MetricShipCostServiceTest extends UnitTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        FeeTypeFactory.feeTypeInit();
    }

    MetricShipCostService service = new MetricShipCostService();

    @Test
    public void testExpressCost() {
        Selling sell = FactoryBoy.create(Selling.class, "de");
        sellingShipCostFixtures();

        F.T2<Float, Float> t2 = service.expressCost(sell, new Date());
        // 124.7 USD
        assertThat((double) t2._1, is(closeTo(6.2, 0.1d)));
        assertThat(t2._2, is(20f));
    }

    @Test
    public void testAirCost() {
        Selling sell = FactoryBoy.create(Selling.class, "de");
        sellingShipCostFixtures();
        F.T2<Float, Float> t2 = service.airCost(sell, new Date());
        // 1049.18 USD
        assertThat((double) t2._1, is(closeTo(5.24, 0.1d)));
        assertThat(t2._2, is(200f));
    }

    @Test
    public void testSeaCost() {
        Selling sell = FactoryBoy.create(Selling.class, "de");
        sellingShipCostFixtures();
        F.T2<Float, Float> t2 = service.seaCost(sell, new Date());
        // 1426.22 USD
        assertThat((double) t2._1, is(closeTo(4.75, 0.1d)));
        assertThat(t2._2, is(300f));
    }

    private void sellingShipCostFixtures() {
        /**
         * 1. 准备 1 个 FBA 快递的运输费用
         * --- 760 (20) 快递费, 200 关税
         * 2. 准备 1 个海运的运输费用
         * --- 8700 (300) 海运费,  300 关税
         * 3. 准备 1 个空运的运输费用
         * --- 6400 (200) 空运费, 280 关税
         * 4. 计算当当天的运输成本
         */
        FactoryBoy.create(Payment.class, "paid");
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
                target.state = PaymentUnit.S.PAID;
            }
        });
        PaymentUnit expressOtherFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.dutyAndVAT();
                target.unitPrice = 200;
                target.unitQty = 1;
                target.currency = Currency.CNY;
                target.state = PaymentUnit.S.PAID;
            }
        });
        expressShipment.items.get(0).produceFee(expressShipfee, expressShipfee.feeType);
        expressShipment.produceFee(expressOtherFee);

        // 2 海运
        ProcureUnit unit2 = FactoryBoy.create(ProcureUnit.class, "planSea");
        Shipment seaShipment = FactoryBoy.create(Shipment.class, "sea");
        seaShipment.addToShip(unit2);
        PaymentUnit seaShipFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.findById("oceanfreight");
                target.unitPrice = 29;
                target.unitQty = 300;//300 kg
                target.currency = Currency.CNY;
                target.state = PaymentUnit.S.PAID;
            }
        });
        PaymentUnit seaShipOtherFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.dutyAndVAT();
                target.unitPrice = 300;
                target.unitQty = 1;
                target.currency = Currency.CNY;
                target.state = PaymentUnit.S.PAID;
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
                target.feeType = FeeType.findById("airfee");
                target.unitPrice = 32;
                target.unitQty = 200;//300 kg
                target.currency = Currency.CNY;
                target.state = PaymentUnit.S.PAID;
            }
        });
        PaymentUnit airShipOtherFee = FactoryBoy.build(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = FeeType.dutyAndVAT();
                target.unitPrice = 280;
                target.unitQty = 1;
                target.currency = Currency.CNY;
                target.state = PaymentUnit.S.PAID;
            }
        });
        airShipment.produceFee(airShipFee);
        airShipment.produceFee(airShipOtherFee);
    }
}
