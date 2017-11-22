package jobs;

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
@Every("2mn")
public class AmazonOrderFinanceFindJob extends BaseJob {

    public void doit() {
        List<Orderr> orders = Orderr.find("synced=false AND feeflag=0 AND state IN ('SHIPPED','REFUNDED') "
                + "ORDER BY createDate DESC").fetch(20);
        orders.forEach(order -> {
            Logger.info("OrderId:" + order.orderId + " 开始执行 AmazonFinanceCheckJob 方法 ");
            order.refreshFee();
            order.synced = true;
            order.feeflag = 2;
            order.save();
        });
    }
}
