package jobs;

import helper.Webs;
import jobs.driver.BaseJob;
import models.market.Orderr;
import play.Logger;
import play.jobs.Every;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/22
 * Time: 下午4:22
 */
@Every("3s")
public class AmazonOrderFinanceFindJob extends BaseJob {

    public void doit() {
        List<Orderr> orders = Orderr.find("synced=false AND feeflag=0 AND state IN (?,?) "
                + "ORDER BY createDate DESC", Orderr.S.SHIPPED, Orderr.S.REFUNDED).fetch(20);
        orders.forEach(order -> {
            Logger.info("OrderId:" + order.orderId + " 所属市场" + order.market.name() + " 开始执行 AmazonFinanceCheckJob 方法 ");
            try {
                order.refreshFee();
            } catch(Exception e) {
                Logger.error(Webs.e(e));
            }
            order.synced = true;
            order.feeflag = 2;
            order.save();
        });
    }
}
