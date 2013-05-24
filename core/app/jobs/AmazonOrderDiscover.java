package jobs;

import com.amazonservices.mws.orders.model.ListOrdersRequest;
import models.market.Orderr;
import play.jobs.Job;

import java.util.List;

/**
 * 对 Amazon 订单的第一步, 发现订单
 * <p/>
 * The GetOrder operation has a maximum request quota of six and a restore rate of one request every minute.
 * User: wyatt
 * Date: 5/24/13
 * Time: 10:04 AM
 */
public class AmazonOrderDiscover extends Job<List<Orderr>> {

    @Override
    public void doJob() {
        ListOrdersRequest request = new ListOrdersRequest();
    }
}
