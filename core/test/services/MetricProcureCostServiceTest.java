package services;

import factory.FactoryBoy;
import factory.callback.BuildCallback;
import helper.Currency;
import models.market.Selling;
import models.procure.ProcureUnit;
import org.junit.Before;
import org.junit.Test;
import play.libs.F;
import play.test.UnitTest;
import util.DateHelper;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * 采购成本算法测试
 * User: wyatt
 * Date: 10/15/13
 * Time: 10:44 AM
 */
public class MetricProcureCostServiceTest extends UnitTest {

    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        service = new MetricProcureCostService();
    }

    MetricProcureCostService service;

    @Test
    public void testSellingProcreCost() {
        Selling s = FactoryBoy.create(Selling.class, "de");
        FactoryBoy.create(ProcureUnit.class, "plan", new BuildCallback<ProcureUnit>() {
            @Override
            public void build(ProcureUnit target) {
                target.attrs.deliveryDate = DateHelper.t("2013-10-15");
                target.attrs.currency = Currency.CNY;
                target.attrs.price = 20f;
                target.attrs.qty = 200;
                // 4000 CNY -> 640 USD
            }
        });
        FactoryBoy.create(ProcureUnit.class, "plan", new BuildCallback<ProcureUnit>() {
            @Override
            public void build(ProcureUnit target) {
                target.attrs.deliveryDate = DateHelper.t("2013-10-15");
                target.attrs.currency = Currency.USD;
                target.attrs.price = 10f;
                target.attrs.qty = 200;
                // 2000 USD
            }
        });
        FactoryBoy.create(ProcureUnit.class, "plan", new BuildCallback<ProcureUnit>() {
            @Override
            public void build(ProcureUnit target) {
                target.attrs.deliveryDate = DateHelper.t("2013-10-15");
                target.attrs.currency = Currency.GBP;
                target.attrs.price = 8f;
                target.attrs.qty = 300;
                // 2400 GBP -> 3600+ USD
            }
        });

        FactoryBoy.create(ProcureUnit.class, "plan", new BuildCallback<ProcureUnit>() {
            // 应该不被统计进来
            @Override
            public void build(ProcureUnit target) {
                target.attrs.deliveryDate = DateHelper.t("2013-10-14");
                target.attrs.currency = Currency.GBP;
                target.attrs.price = 8f;
                target.attrs.qty = 300;
                // 2400 GBP -> 3600+ USD
            }
        });

        F.T2<Float, Integer> t2 = service.sellingProcreCost(s, DateHelper.t("2013-10-15"));

//        assertThat((double) t2._1, is(closeTo(6240, 100)));
        //Currency.GBP.toUSD(8 * 300f) + Currency.CNY.toUSD(20 * 200f) + (10 * 200f)) = 6278.968
        assertThat((double) (t2._1 * t2._2), is(closeTo(6278.9, 5)));
        assertThat(t2._2, is(700));
        assertThat((double) t2._1, is(closeTo(8.969, 0.2)));
    }
}
