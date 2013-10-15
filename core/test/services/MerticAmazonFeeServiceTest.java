package services;

import com.google.common.collect.Lists;
import factory.FactoryBoy;
import factory.callback.BuildCallback;
import factory.finance.FeeTypeFactory;
import models.finance.FeeType;
import models.finance.SaleFee;
import models.market.OrderItem;
import models.market.Orderr;
import models.market.Selling;
import org.junit.Before;
import org.junit.Test;
import play.test.UnitTest;
import util.DateHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/12/13
 * Time: 1:55 PM
 */
public class MerticAmazonFeeServiceTest extends UnitTest {
    @Before
    public void before() {
        FactoryBoy.deleteAll();
        FeeTypeFactory.feeTypeInit();
        service = new MetricAmazonFeeService();
    }

    private MetricAmazonFeeService service;

    private void deAmazonFeeDB(final Date createDate) {
        FactoryBoy.create(Selling.class, "de");
        FactoryBoy.create(Orderr.class, "de", new BuildCallback<Orderr>() {
            @Override
            public void build(Orderr target) {
                target.createDate = createDate;
            }
        });
        FactoryBoy.create(OrderItem.class, "de", new BuildCallback<OrderItem>() {
            @Override
            public void build(OrderItem target) {
                target.createDate = target.order.createDate;
            }
        });
        FactoryBoy.create(SaleFee.class, "de", new BuildCallback<SaleFee>() {
            @Override
            public void build(SaleFee target) {
                target.type = FeeType.findById("crossborderfulfilmentfee");
                target.usdCost = 6f;
            }
        });

        FactoryBoy.create(SaleFee.class, "de", new BuildCallback<SaleFee>() {
            @Override
            public void build(SaleFee target) {
                target.type = FeeType.findById("fbaperunitfulfillmentfee");
                target.usdCost = 3f;
            }
        });
    }

    /**
     * 检查 DE 的 FBA 费用统计
     */
    @Test
    public void testQueryDeAmazonFee() {
        deAmazonFeeDB(DateHelper.t("2013-09-21 18:23:00"));
        Selling s = Selling.all().first();

        assertThat(FeeType.amazon().children.size(), is(greaterThan(1)));

        Map<String, Integer> sellOrders = new HashMap<String, Integer>();
        sellOrders.put(s.sellingId, 1);
        Map<String, Float> map = service.sellingAmazonFee(DateHelper.t("2013-09-21"), Lists.newArrayList(s), sellOrders);

        assertThat(map.get(s.sellingId).doubleValue(), is(closeTo(9, 0.1)));
    }


    /**
     * 检查不在时间段内没有 Selling Fee
     */
    @Test
    public void testNoSaleFeeDEAmazonFee() {
        deAmazonFeeDB(DateHelper.t("2013-09-21 00:00:00"));
        Selling s = Selling.all().first();

        assertThat(FeeType.amazon().children.size(), is(greaterThan(1)));

        Map<String, Integer> sellOrders = new HashMap<String, Integer>();
        sellOrders.put(s.sellingId, 1);
        Map<String, Float> map = service.sellingAmazonFee(DateHelper.t("2013-09-21"), Lists.newArrayList(s), sellOrders);

        assertThat(map.get(s.sellingId), is(nullValue()));
    }
}
