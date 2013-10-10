package models.finance;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.callback.SequenceCallback;
import models.market.Account;
import models.market.Selling;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.data.validation.Validation;
import play.test.UnitTest;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * 付费明细
 */
public class PaymentUnitTest extends UnitTest {
    @Before
    public void setUP() {
        FactoryBoy.deleteAll();
        Validation.clear();
    }

    /**
     * 通过 paymentUnit 创建付款单
     */
    @Ignore
    @Test
    public void testTransportApprove() {
        FactoryBoy.create(Account.class, "de");
        FactoryBoy.create(Selling.class, "de");
        FactoryBoy.create(ProcureUnit.class);
        FactoryBoy.create(Shipment.class);

        FactoryBoy.create(Cooperator.class, "shipper", new BuildCallback<Cooperator>() {
            @Override
            public void build(Cooperator target) {
                target.paymentMethods.add(FactoryBoy.create(PaymentTarget.class));
            }
        });

        PaymentUnit paymentUnit = FactoryBoy.create(PaymentUnit.class, "noPayment", new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.shipment = FactoryBoy.lastOrCreate(Shipment.class);
                target.cooperator = FactoryBoy.lastOrCreate(Cooperator.class);
                target.shipment.apply = FactoryBoy.lastOrCreate(TransportApply.class);
            }
        });

        paymentUnit.transportApprove();

        assertNotNull(paymentUnit.payment);

        assertThat(Payment.count(), is(Long.parseLong("1")));
    }


    /**
     * 通过 paymentUnit 创建付款单
     * 条件：
     * 同一个请款单
     * 同一个费用联系人
     * <p/>
     * 预期结果：共属于同一个payment
     */
    @Ignore
    @Test
    public void testTransportApproveManyCooperator() {
        FactoryBoy.create(Account.class, "de");
        FactoryBoy.create(Selling.class, "de");
        FactoryBoy.create(ProcureUnit.class);
        FactoryBoy.create(Shipment.class);

        FactoryBoy.create(Cooperator.class, "shipper", new BuildCallback<Cooperator>() {
            @Override
            public void build(Cooperator target) {
                target.paymentMethods.add(FactoryBoy.create(PaymentTarget.class));
            }
        });

        List<PaymentUnit> paymentUnits = FactoryBoy
                .batchCreate(2, PaymentUnit.class, "noPayment", new SequenceCallback<PaymentUnit>() {
                    @Override
                    public void sequence(PaymentUnit target, int i) {
                        target.shipment = FactoryBoy.lastOrCreate(Shipment.class);
                        target.cooperator = FactoryBoy.lastOrCreate(Cooperator.class);
                        target.shipment.apply = FactoryBoy.lastOrCreate(TransportApply.class);
                    }
                });

        PaymentUnit paymentUnitOne = paymentUnits.get(0);
        PaymentUnit paymentUnittwo = paymentUnits.get(1);

        paymentUnitOne.transportApprove();
        paymentUnittwo.transportApprove();

        assertNotNull(paymentUnitOne.payment);
        assertNotNull(paymentUnittwo.payment);

        assertEquals(paymentUnitOne.payment, paymentUnittwo.payment);
    }

    /**
     * 通过 paymentUnit 创建付款单
     +* 条件：
     * 同一个请款单sa           ]\
     * 不同费用联系人
     * <p/>
     * 预期结果：不同费用联系人同的不同，产生两条对应的 payment
     */
    @Test
    public void testTransportApproveDifferentCooperator() {
        FactoryBoy.create(Account.class, "de");
        FactoryBoy.create(Selling.class, "de");
        FactoryBoy.create(ProcureUnit.class);
        FactoryBoy.create(Shipment.class);
        FactoryBoy.create(PaymentTarget.class);
         final List<Cooperator> cooperators = FactoryBoy.batchCreate(2, Cooperator.class, "shipper",
                new SequenceCallback<Cooperator>() {
                    @Override
                    public void sequence(Cooperator target, int i) {
                        target.name = "cooperName_" + i;
                        target.paymentMethods.add(FactoryBoy.lastOrCreate(PaymentTarget.class));
                    }
                });

        List<PaymentUnit> paymentUnits = FactoryBoy
                .batchCreate(2, PaymentUnit.class, "noPayment", new SequenceCallback<PaymentUnit>() {
                    @Override
                    public void sequence(PaymentUnit target, int i) {
                        // i 初始值为：1
                        target.cooperator = cooperators.get(i-1);
                        target.shipment = FactoryBoy.lastOrCreate(Shipment.class);
                        target.shipment.apply = FactoryBoy.lastOrCreate(TransportApply.class);
                    }
                });

        PaymentUnit paymentUnitOne = paymentUnits.get(0);
        PaymentUnit paymentUnittwo = paymentUnits.get(1);

        paymentUnitOne.transportApprove();
        paymentUnittwo.transportApprove();

        assertNotNull(paymentUnitOne.payment);
        assertNotNull(paymentUnittwo.payment);

        assertNotSame(paymentUnitOne.payment, paymentUnittwo.payment);
    }
}
