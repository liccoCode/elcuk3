package jobs.promise;

import helper.HTTP;
import jobs.FinanceCheckJob;
import models.finance.SaleFee;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.Logger;
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
            orders = Orderr.find("SELECT o FROM Orderr o WHERE o.state=? AND SIZE(o.fees)<=1",
                    Orderr.S.REFUNDED).fetch(50);

        for(Orderr ord : orders) {
            if(ord.state != Orderr.S.REFUNDED) continue;
            // Refund 的首先删除原来的, 然后再重新添加新抓取的
            Logger.info("FinanceRefundOrders >> %s:%s", ord.orderId, ord.account.prettyName());
            List<SaleFee> fees = FinanceCheckJob.oneTransactionFee(
                    HTTP.get(ord.account.cookieStore(),
                            ord.account.type.oneTransactionFees(ord.orderId)));
            SaleFee.deleteOrderRelateFee(ord.orderId);
            for(SaleFee fee : fees) {
                fee.account = ord.account;
                fee.save();
            }
            ord.warnning = FinanceShippedOrders.isWarnning(fees);
            ord.save();
        }
    }
}
