package models.procure;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import helper.Currency;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.test.UnitTest;

import static org.hamcrest.core.Is.is;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 7/16/13
 * Time: 10:16 AM
 */
public class ShipmentFinanceTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        FeeTypeFactory.feeTypeInit();
    }

    private Shipment calculateDutySetUp() {
        final Shipment ship = FactoryBoy.create(Shipment.class);
        FactoryBoy.batchCreate(2, PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.shipment = ship;
                target.currency = Currency.CNY;
                target.amount = 2000;
                target.feeType = FeeType.dutyAndVAT();
            }
        });

        FactoryBoy.batchCreate(2, PaymentUnit.class, new BuildCallback<PaymentUnit>() {
            @Override
            public void build(PaymentUnit target) {
                target.shipment = ship;
                target.feeType = FeeType.expressFee();
            }
        });

        return ship;
    }

    @Test
    public void testCalculateDuty() {
        Shipment ship = calculateDutySetUp();

        // 强制取消以一级缓存中的内容
        JPA.em().clear();
        assertThat(PaymentUnit.count(), is(4l));
        Shipment shipmet = Shipment.findById(ship.id);
        assertThat(shipmet.fees.size(), is(4));

        PaymentUnit unit = shipmet.calculateDuty(Currency.CNY, 10000f);
        assertThat(unit.currency, is(Currency.CNY));
        assertThat(unit.amount, is((float) (10000 - 2 * 2000)));
        assertThat(unit.unitPrice, is((float) (10000 - 2 * 2000)));
        assertThat(unit.unitQty, is(1f));
        assertThat(PaymentUnit.count(), is(5l));
    }

    @Test
    public void testCalculateDutyDiffCurrency() {
        Shipment ship = calculateDutySetUp();

        // 强制取消以一级缓存中的内容
        JPA.em().clear();
        Shipment shipmet = Shipment.findById(ship.id);
        PaymentUnit unit = shipmet.calculateDuty(Currency.USD, 10000f);
        // 相同错误信息不重复
        assertThat(Validation.errors().size(), is(1));
        assertThat(Validation.errors().get(0).message(), is(containsString("关税费用应该为统一币种")));
    }
}
