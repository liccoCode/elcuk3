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
public class FinanceShippedOrders extends Job {
    private String orderId;

    public FinanceShippedOrders() {
    }

    public FinanceShippedOrders(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public void doJob() {
        List<Orderr> orders = null;
        if(StringUtils.isNotBlank(this.orderId))
            orders = Arrays.asList(Orderr.<Orderr>findById(this.orderId));
        else
            orders = Orderr.find("SELECT o FROM Orderr o LEFT JOIN o.fees f WHERE o.state=? AND f.type.name!=?", Orderr.S.SHIPPED, "principal").fetch(50);

        for(Orderr ord : orders) {
            if(ord.state != Orderr.S.SHIPPED) continue;
            List<SaleFee> fees = FinanceCheckJob.oneTransactionFee(
                    HTTP.get(ord.account.cookieStore(), ord.account.type.oneTransactionFees(ord.orderId)));
            for(SaleFee fee : fees) {
                fee.account = ord.account;
                fee.save();
            }
        }
    }
}
