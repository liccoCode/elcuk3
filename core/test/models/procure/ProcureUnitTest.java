package models.procure;

import factory.FactoryBoy;
import helper.Currency;
import models.ElcukRecord;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;

import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.matchers.JUnitMatchers.containsString;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 4/28/13
 * Time: 3:48 PM
 */
public class ProcureUnitTest extends UnitTest {
    @Before
    public void setUP() {
        FactoryBoy.deleteAll();
    }

    @Test
    public void testUpdate() {
        ProcureUnit planUnit = FactoryBoy.create(ProcureUnit.class, "plan");
        ProcureUnit updateUnit = FactoryBoy.build(ProcureUnit.class, "plan");

        updateUnit.attrs.price = 99f;
        updateUnit.attrs.currency = Currency.USD;

        planUnit.update(updateUnit, null);

        assertThat(planUnit.attrs.price, is(99f));
        assertThat(planUnit.attrs.currency, is(Currency.USD));
        assertThat(ElcukRecord.count(), is(1l));
    }

    @Test
    public void testBeforeDoneUpdate() {
        ProcureUnit unit = FactoryBoy.build(ProcureUnit.class, "plan");
        ProcureUnit updateUnit = FactoryBoy.build(ProcureUnit.class, "plan");

        updateUnit.attrs.planDeliveryDate = new Date();
        updateUnit.attrs.planShipDate = new Date();
        updateUnit.attrs.planArrivDate = new Date();
        updateUnit.attrs.planQty = 12;
        updateUnit.attrs.price = 1f;
        updateUnit.attrs.currency = Currency.GBP;
        updateUnit.attrs.qty = 1;
        updateUnit.shipType = Shipment.T.SEA;

        List<String> logs = unit.beforeDoneUpdate(updateUnit);

        assertThat(logs.size(), is(8));
        // 这个是 beforeDoneUpdate 中更新的顺序
        assertThat(logs.get(0), is(containsString("planDeliveryDate 从 空 变更为 2013-06-07")));
        assertThat(logs.get(1), is(containsString("planShipDate 从 空 变更为 2013-06-07")));
    }
}
