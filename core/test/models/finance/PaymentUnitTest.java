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
import org.junit.Test;
import play.data.validation.Validation;
import play.test.UnitTest;

import java.util.List;

/**
 * 付费明细
 * User: wyatt
 * Date: 7/17/13
 * Time: 11:37 AM
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

        final TransportApply transportApply = new TransportApply();
        transportApply.serialNumber = "SQK-周伟-008-13";
        transportApply.save();

        PaymentUnit paymentUnit = FactoryBoy.create(PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.payment = null;
                target.amount = 12;
                target.shipment = FactoryBoy.lastOrCreate(Shipment.class);
                target.cooperator = FactoryBoy.lastOrCreate(Cooperator.class);
                target.shipment.apply = transportApply;
            }
        });

        paymentUnit.transportApprove();

        assertNotNull(paymentUnit.payment);

    }

    /**
     * 通过 paymentUnit 创建付款单
     * 条件：
     *  同一个请款单
     *  同一个工厂
     *
     * 预期结果：共属于同一个payment
     */
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

        final TransportApply transportApply = new TransportApply();
        transportApply.serialNumber = "SQK-周伟-008-13";
        transportApply.save();

        List<PaymentUnit> paymentUnits = FactoryBoy
                .batchCreate(2, PaymentUnit.class, new SequenceCallback<PaymentUnit>() {
                    @Override
                    public void sequence(PaymentUnit target, int i) {
                        target.payment = null;
                        target.amount = 12;
                        target.shipment = FactoryBoy.lastOrCreate(Shipment.class);

                        target.cooperator = FactoryBoy.lastOrCreate(Cooperator.class);
                        target.shipment.apply = transportApply;
                    }
                });

        PaymentUnit PaymentUnitOne = paymentUnits.get(0);
        PaymentUnit PaymentUnittwo = paymentUnits.get(1);

        PaymentUnitOne.transportApprove();
        PaymentUnittwo.transportApprove();

        assertNotNull(PaymentUnitOne.payment);
        assertNotNull(PaymentUnittwo.payment);

        assertEquals(PaymentUnitOne.payment,PaymentUnittwo.payment);
    }
}
