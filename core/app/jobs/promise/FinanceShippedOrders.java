package jobs.promise;

import helper.HTTP;
import jobs.FinanceCheckJob;
import models.finance.SaleFee;
import models.market.Orderr;
import play.Logger;
import play.jobs.Job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/23/12
 * Time: 2:55 PM
 */
public class FinanceShippedOrders extends Job<List<SaleFee>> {
    private Orderr order;

    public FinanceShippedOrders() {
    }

    public FinanceShippedOrders(Orderr order) {
        this.order = order;
    }

    @Override
    public List<SaleFee> doJobWithResult() throws Exception {
        List<Orderr> orders = null;
        if(this.order != null)
            orders = Arrays.asList(order);
        else {
            // 对于没有付款记录的订单, 需要将排除解析第二步的 principal fee 的订单全部拿出来进行 fees 的解析并且费用的个数小于等于 1 的(解析过那么必定>=2)
            orders = FinanceShippedOrders.orderrs();
        }

        List<SaleFee> fees = new ArrayList<SaleFee>();
        for(Orderr ord : orders) {
            if(ord.state != Orderr.S.SHIPPED) continue;
            Logger.info("FinanceShippedOrders >> %s:%s", ord.orderId, ord.account.prettyName());
            fees = FinanceCheckJob.oneTransactionFee(
                    HTTP.get(ord.account.cookieStore(),
                            ord.account.type.oneTransactionFees(ord.orderId)));
            for(SaleFee fee : fees) {
                fee.account = ord.account;
                fee.save();
            }
            ord.warnning = FinanceShippedOrders.isWarnning(fees);
            ord.save();
        }
        return fees;
    }

    /**
     * 根据 SaleFee 判断是否需要警告
     *
     * @param fees
     * @return
     */
    public static boolean isWarnning(List<SaleFee> fees) {
        float totalSales = 0;
        float totalMarketFees = 0;
        for(SaleFee fee : fees) {
            if(fee.type.parent != null && !"amazon".equals(fee.type.parent.name)) continue;
            if("principal".equals(fee.type.name) || "productcharges".equals(fee.type.name)) {
                totalSales += fee.usdCost;
            } else {
                totalMarketFees += fee.usdCost;
            }
        }
        return (totalSales > 0) && (totalMarketFees / totalSales) > 0.2;
    }

    public static List<Orderr> orderrs() {
        return Orderr
                .find("SELECT o FROM Orderr o WHERE o.state=? AND o.market=o.account.type AND SIZE(o.fees)<=1",
                        Orderr.S.SHIPPED).fetch(50);
    }
}
