package jobs.promise;

import helper.HTTP;
import jobs.FinanceCheckJob;
import models.finance.SaleFee;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.jobs.Job;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/23/12
 * Time: 2:55 PM
 */
public class FinanceRefundOrders extends Job {
    private String orderId;

    public FinanceRefundOrders() {
    }

    public FinanceRefundOrders(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public void doJob() {
        List<Orderr> orders = null;
        if(StringUtils.isNotBlank(this.orderId))
            orders = Arrays.asList(Orderr.<Orderr>findById(this.orderId));
        else
            orders = Orderr.find("state=? AND SIZE(fees)<=2"/*如果是 REFUNDED , 应该有 4 个*/, Orderr.S.REFUNDED).fetch(50);

        for(Orderr ord : orders) {
            if(ord.state != Orderr.S.REFUNDED) continue;
            // Refund 的首先删除原来的, 然后再重新添加新抓取的
            for(SaleFee fee : ord.fees)
                fee.delete();
            List<SaleFee> fees = FinanceCheckJob.oneTransactionFee(
                    HTTP.get(ord.account.cookieStore(), ord.account.type.oneTransactionFees(ord.orderId)));
            for(SaleFee fee : fees) {
                fee.account = ord.account;
                fee.save();
            }
        }
    }
}
