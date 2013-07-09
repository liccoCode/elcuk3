package models.procure;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import helper.Currency;
import models.ElcukRecord;
import models.finance.PaymentUnit;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import play.data.validation.Validation;
import play.test.UnitTest;

import java.util.Arrays;
import java.util.List;

import static models.procure.ProcureUnit.STAGE;
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
        Validation.clear();
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

        DateTime now = DateTime.now();
        updateUnit.attrs.planDeliveryDate = now.toDate();
        updateUnit.attrs.planShipDate = now.toDate();
        updateUnit.attrs.planArrivDate = now.toDate();
        updateUnit.attrs.planQty = 12;
        updateUnit.attrs.price = 1f;
        updateUnit.attrs.currency = Currency.GBP;
        updateUnit.attrs.qty = 1;
        updateUnit.shipType = Shipment.T.SEA;

        List<String> logs = unit.beforeDoneUpdate(updateUnit);

        assertThat(logs.size(), is(8));
        // 这个是 beforeDoneUpdate 中更新的顺序
        assertThat(logs.get(0), is(containsString("预计发货时间 从 空 变更为 " + now.toString("yyyy-MM-dd"))));
        assertThat(logs.get(1), is(containsString("预计运输时间 从 空 变更为 " + now.toString("yyyy-MM-dd"))));
    }

    @Test
    public void testRemoveNotValid() {
        List<ProcureUnit.STAGE> stages = Arrays.asList(STAGE.CLOSE, STAGE.DONE,
                STAGE.INBOUND, STAGE.SHIP_OVER, STAGE.SHIPPING);
        for(final STAGE stage : stages) {
            ProcureUnit unit = FactoryBoy.create(ProcureUnit.class, new BuildCallback<ProcureUnit>() {
                @Override
                public void build(ProcureUnit target) {
                    target.stage = stage;
                }
            });
            unit.remove();
            assertThat(Validation.errors().size(), is(1));
            assertThat(Validation.errors().get(0).message(), is(containsString("采购计划进行取消")));
        }
    }

    @Test
    public void testZRemove() {
//        List<ProcureUnit.STAGE> stages = Arrays.asList(STAGE.PLAN, STAGE.DELIVERY);
        List<ProcureUnit.STAGE> stages = Arrays.asList(STAGE.PLAN);
        for(final STAGE stage : stages) {
            ProcureUnit unit = FactoryBoy.create(ProcureUnit.class, "plan", new BuildCallback<ProcureUnit>() {
                @Override
                public void build(ProcureUnit target) {
                    target.stage = stage;
                }
            });
            PaymentUnit pu = FactoryBoy.create(PaymentUnit.class, "deny");

            unit.fees.add(pu);
            unit.save();

            assertThat(pu.procureUnit.id, is(unit.id));
            ProcureUnit unit1 = ProcureUnit.all().first();
            assertThat(unit1.fees.size(), is(1));

            assertThat(unit1.isPersistent(), is(true));
            unit1.remove();
            assertThat(unit1.isPersistent(), is(false));
            assertThat(PaymentUnit.count(), is(0l));
        }


    }
}
