package services;

import com.google.common.collect.Lists;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import helper.Currency;
import models.finance.FeeType;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.market.Selling;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.product.Product;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Validation;
import play.test.UnitTest;

import java.util.Date;
import java.util.Map;

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
        Validation.clear();
    }

    MetricShipCostService service = new MetricShipCostService();
    Selling s1;
    Selling s2;
    Product p1;
    Product p2;
    ProcureUnit pu1;
    ProcureUnit pu2;
    Shipment spSea;
    Shipment spAir;
    Shipment spExpress;


    //    @Test
    public void testSeaCost() {
        procureUnitBaseFixtures();
        shipmentFixtures();
        Map<String, Float> sellingSeaCost = service.seaCost(new Date());

        // 总运费(无VAT): 689.45443 USD, 总体积: 5.803 m3, 当前每 m3 为: 118.81 USD.
        // p1 体积为 (53*160*70) / (1000 * 1000* 1000) = .0005936 m3
        // 所以当前每一个 s1 运费为:  118.81 * .0005936 = .070525616
        // PS: 由于是测试数据, 所以本应该的 总运费 = selling 单个运费 * selling 运输数量 在这里会因 数量问题失败,
        // 主要用于验证算法是否正确.
        assertThat((double) sellingSeaCost.get(s1.sellingId), is(closeTo(.070525616, 0.02)));
    }

    @Test
    public void testAirCost() {
        procureUnitBaseFixtures();
        shipmentFixtures();
        Map<String, Float> sellingAirCost = service.airCost(new Date());

        // 总运费(无VAT): 3226.95 USD , 总重量: 822 kg, 当前每 kg 为: 3.925729927
        // p2 的重量为 0.33 kg
        // 所以当前每一个 s1 的运费为: 3.925729927 * 0.33 = 1.295490876
        assertThat((double) sellingAirCost.get(s2.sellingId), is(closeTo(1.23, 0.1)));
    }

    //    @Test
    public void testSellingVATFee() {
        procureUnitBaseFixtures();
        spSea = new Shipment().buildFromProcureUnits(Lists.newArrayList(pu1.id));
        spAir = new Shipment().buildFromProcureUnits(Lists.newArrayList(pu2.id));

        final Payment payment = FactoryBoy.create(Payment.class, "paid");
        FactoryBoy.create(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.payment = payment;
                target.feeType = FeeType.dutyAndVAT();
                target.shipment = spSea;
                target.unitPrice = 1.5875f;
                target.unitQty = 200;
                target.currency = Currency.GBP;
                //317.5 * 1.5 = 476.25
            }
        });
        FactoryBoy.create(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.payment = payment;
                target.feeType = FeeType.dutyAndVAT();
                target.shipment = spAir;
                target.unitPrice = 0.7366f;
                target.unitQty = 400;
                target.currency = Currency.USD;
                //294.64
            }
        });


        Map<String, Float> vats = service.sellingVATFee(new Date());
        // 780 -> 127.1 USD;  900 个数量, 计算单个申报价格
        assertThat((double) vats.get(s1.sellingId), is(closeTo(177.89, 1)));
        assertThat((double) vats.get(s2.sellingId), is(closeTo(595.99, 1)));
    }


    // ProcureUnit, Product, Selling 这些基础 Model 的 Fixtures
    private void procureUnitBaseFixtures() {
        s1 = FactoryBoy.create(Selling.class, "de", new BuildCallback<Selling>() {
            @Override
            public void build(Selling target) {
                target.sellingId = "Sell1";
            }
        });
        s2 = FactoryBoy.create(Selling.class, "de", new BuildCallback<Selling>() {
            @Override
            public void build(Selling target) {
                target.sellingId = "Sell2";
            }
        });

        p1 = FactoryBoy.create(Product.class, new BuildCallback<Product>() {
            @Override
            public void build(Product target) {
                target.sku = "88SPP8-BCB1";
                target.declaredValue = 1.5f;
                target.heigh = 53f;
                target.lengths = 160f;
                target.width = 70f;
            }
        });
        p2 = FactoryBoy.create(Product.class, new BuildCallback<Product>() {
            @Override
            public void build(Product target) {
                target.sku = "88SPP8-BSQ";
                target.declaredValue = 2.5f;
                target.heigh = 25f;
                target.lengths = 100f;
                target.width = 25f;
                target.weight = 0.33f;
            }
        });

        pu1 = FactoryBoy.create(ProcureUnit.class, "planSea", new BuildCallback<ProcureUnit>() {
            @Override
            public void build(ProcureUnit target) {
                target.selling = s1;
                target.product = p1;
                target.attrs.qty = 200;
                target.shipType = Shipment.T.SEA;
                target.attrs.planShipDate = new Date();
            }
        });

        pu2 = FactoryBoy.create(ProcureUnit.class, "planAir", new BuildCallback<ProcureUnit>() {
            @Override
            public void build(ProcureUnit target) {
                target.selling = s2;
                target.product = p2;
                target.attrs.qty = 400;
                target.shipType = Shipment.T.AIR;
                target.attrs.planShipDate = new Date();
            }
        });
    }


    private void shipmentFixtures() {
        spSea = FactoryBoy.create(Shipment.class, "sea");
        spAir = FactoryBoy.create(Shipment.class, "air");
        spExpress = FactoryBoy.create(Shipment.class);

        spSea.addToShip(pu1);
        spAir.addToShip(pu2);
        spExpress.addToShip(pu1);
        Payment payment = FactoryBoy.create(Payment.class, "paid");

        // 海运
        createPaymentUnit(FeeType.oceanfreight(), 72f, 5.803f, payment, Currency.USD, PaymentUnit.S.PAID, spSea);
        createPaymentUnit(FeeType.<FeeType>findById("loadingunloadingfee"),
                179.893f, 1f, payment, Currency.GBP, PaymentUnit.S.PAID, spSea);
        createPaymentUnit(FeeType.dutyAndVAT(), 315.56f, 1f, payment, Currency.USD, PaymentUnit.S.PAID, spSea);


        // 空运; 统一付款但中存在海运和空运费用, 费用需要被分开计算
        createPaymentUnit(FeeType.airFee(), 3.65f, 822, payment, Currency.USD, PaymentUnit.S.PAID, spAir);
        createPaymentUnit(FeeType.<FeeType>findById("loadingunloadingfee"),
                150f, 1, payment, Currency.GBP, PaymentUnit.S.PAID, spAir);
        createPaymentUnit(FeeType.dutyAndVAT(), 500f, 1, payment, Currency.USD, PaymentUnit.S.PAID, spAir);
    }

    private void createPaymentUnit(final FeeType feeType, final float unitPrice, final float unitQty,
                                   final Payment payment, final Currency currency, final PaymentUnit.S state,
                                   final Shipment shipment) {
        FactoryBoy.create(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = feeType;
                target.unitPrice = unitPrice;
                target.payment = payment;
                target.unitQty = unitQty;
                target.currency = currency;
                target.state = state;
                target.shipment = shipment;
            }
        });
    }

    private void createPaymentUnit(final FeeType feeType, final float unitPrice, final float unitQty,
                                   final Payment payment, final Currency currency, final PaymentUnit.S state,
                                   final ShipItem shipItem) {
        FactoryBoy.create(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.feeType = feeType;
                target.unitPrice = unitPrice;
                target.payment = payment;
                target.unitQty = unitQty;
                target.currency = currency;
                target.state = state;
                target.shipItem = shipItem;
                target.shipment = shipItem.shipment;
            }
        });
    }

    private void sellingShipCostFixturess() {
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
