package jobs;

import helper.Dates;
import helper.Webs;
import jobs.driver.BaseJob;
import models.market.OrderItem;
import models.market.Orderr;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Every;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/22
 * Time: 下午4:22
 */
@Every("5mn")
public class AmazonOrderFinanceFindJob extends BaseJob {

/*    public void doit() {
        List<Orderr> orders = Orderr.find("synced=false AND feeflag=0 AND state IN (?,?) "
                + "ORDER BY createDate DESC", Orderr.S.SHIPPED, Orderr.S.REFUNDED).fetch(20);
        orders.forEach(order -> {
            Logger.info("OrderId:" + order.orderId + " 所属市场" + order.market.name() + " 订单日期 "
                    + Dates.date2DateTime(order.createDate) + "开始执行 AmazonFinanceCheckJob 方法 ");
            try {
                order.refreshFee();
            } catch(Exception e) {
                Logger.error(Webs.e(e));
            } finally {
                order.synced = true;
                order.feeflag = 2;
                order.save();
            }
        });
    }*/

    public void doit() {
        List<OrderItem> items = OrderItem.find("product.sku=? AND createDate>=? AND createDate<=?"
                        + " AND order.synced=false AND order.feeflag=0 AND order.state IN (?,?) "
                        + " ORDER BY createDate DESC",
                "11UNMIC5P-2A5FTUK", DateTime.now().minusMonths(2).toDate(), DateTime.now().toDate(),
                Orderr.S.SHIPPED, Orderr.S.REFUNDED).fetch(20);
        items.forEach(item -> {
            Logger.info("OrderId:" + item.order.orderId + " 所属市场" + item.order.market.name() + " 订单日期 "
                    + Dates.date2DateTime(item.order.createDate) + " 开始执行 AmazonOrderFinanceFindJob 方法 ");
            try {
                item.order.refreshFee();
                item.order.synced = true;
                item.order.feeflag = 2;
                item.order.memo = "AmazonOrderFinanceFindJob";
            } catch(Exception e) {
                item.order.memo = Webs.e(e);
                Logger.error(Webs.e(e));
            } finally {
                item.order.save();
            }
        });
    }

}
