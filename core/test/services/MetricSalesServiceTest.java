package services;

import com.google.common.collect.Lists;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import models.finance.FeeType;
import models.finance.SaleFee;
import models.market.*;
import org.apache.commons.lang.math.JVMRandom;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.util.Date;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/15/13
 * Time: 12:38 PM
 */
public class MetricSalesServiceTest extends UnitTest {
    @Before
    public void setUp() {
        FactoryBoy.deleteAll();
        FeeTypeFactory.feeTypeInit();
        service = new MetricSalesService();
    }

    MetricSalesService service;

    @Test
    public void testSellingUnits() {
        FactoryBoy.create(Account.class, "de");
        Selling s = FactoryBoy.create(Selling.class, "de");

        Date date = DateHelper.t("2013-09-10 10:00:00"); // 德国时区
        createOrderAndOrderItem(2, date);
        createOrderAndOrderItem(3, date);

        Map<String, Integer> sellUnits = service.sellingUnits(date, M.AMAZON_DE);

        assertThat(OrderItem.count(), is(2l));
        assertThat(sellUnits.get(s.sellingId), is(5));
    }

    @Test
    public void testSellingOrders() {
        FactoryBoy.create(Account.class, "de");
        Selling s = FactoryBoy.create(Selling.class, "de");

        Date date = DateHelper.t("2013-09-10 10:00:00"); // 德国时区
        createOrderAndOrderItem(2, date);
        createOrderAndOrderItem(3, date);

        Map<String, Integer> sellOrders = service.sellingOrders(date, M.AMAZON_DE);

        assertThat(OrderItem.count(), is(2l));
        assertThat(sellOrders.get(s.sellingId), is(2));
    }

    @Test
    public void testSellingSales() {
        FactoryBoy.create(Account.class, "de");
        Selling s = FactoryBoy.create(Selling.class, "de");

        Date date = DateHelper.t("2013-09-10 10:00:00"); // 德国时区
        createOrderAndOrderItem(2, date);
        createOrderAndOrderItem(3, date);

        Map<String, Integer> sellUnits = service.sellingUnits(date, M.AMAZON_DE);
        Map<String, Float> sellSales = service.sellingSales(date, Lists.newArrayList(s), sellUnits);

        assertThat(OrderItem.count(), is(2l));
        assertThat((double) sellSales.get(s.sellingId), is(closeTo(160.98/*(31 * 5) + (2.99 * 2)*/, 0.2)));
    }

    @Test
    public void testSellingSalesWithIn10Days() {
        FactoryBoy.create(Account.class, "de");
        Selling s = FactoryBoy.create(Selling.class, "de");

        Date date = DateHelper.afterMinuts(10);
        createOrderAndOrderItem(2, date);
        createOrderAndOrderItem(3, date);

        Map<String, Integer> sellUnits = service.sellingUnits(date, M.AMAZON_DE);
        Map<String, Float> sellSales = service.sellingSales(date, Lists.newArrayList(s), sellUnits);

        assertThat(OrderItem.count(), is(2l));
//        float totalSales = Currency.EUR.toUSD(s.aps.salePrice) * 5 ; 不考虑 shipping
        assertThat((double) s.aps.salePrice, is(closeTo(9.99, 0.1)));
        assertThat((double) sellSales.get(s.sellingId), is(closeTo(65/*totalSales */, 0.5)));
    }


    private void createOrderAndOrderItem(final int quantity, final Date date) {
        final Orderr o = FactoryBoy.create(Orderr.class, "de", new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.orderId = JVMRandom.nextLong(1000000) + "";
                target.createDate = date;
            }
        });
        FactoryBoy.create(OrderItem.class, "de", new BuildCallback<OrderItem>() {
            @Override
            public void build(OrderItem target) {
                target.id = JVMRandom.nextLong(3000000) + "";
                target.order = o;
                target.quantity = quantity;
                target.createDate = date;
            }
        });

        FactoryBoy.create(SaleFee.class, "de", new BuildCallback<SaleFee>() {
            @Override
            public void build(SaleFee target) {
                target.type = FeeType.productCharger();
                target.order = o;
                target.qty = quantity;
                target.usdCost = 31f * target.qty;
            }
        });

        FactoryBoy.create(SaleFee.class, "de", new BuildCallback<SaleFee>() {
            @Override
            public void build(SaleFee target) {
                target.type = FeeType.shipping();
                target.order = o;
                target.usdCost = 2.99f;
            }
        });

        FactoryBoy.create(SaleFee.class, "de", new BuildCallback<SaleFee>() {
            @Override
            public void build(SaleFee target) {
                target.type = FeeType.findById("fbaperorderfulfilmentfee");
                target.order = o;
                target.usdCost = -2.99f;
            }
        });
    }
}
