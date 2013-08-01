package jobs;

import jobs.promise.FinanceShippedPromise;
import models.Jobex;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.joda.time.DateTime;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.jobs.Job;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Amazon 中用于检查 Finance 信息的任务
 * <p/>
 * 周期:
 * - 轮询周期: 1mn
 * - Duration: 2mn
 * User: wyatt
 * Date: 6/4/13
 * Time: 10:39 AM
 */
public class AmazonFinanceCheckJob extends Job {

    @Override
    public void doJob() throws InterruptedException, ExecutionException, TimeoutException {
        // 1. 寻找需要处理的订单, 并且按照 market 进行分组
        // 2. 派发给 Promise Job 进行出来.
        if(!Jobex.findByClassName(AmazonFinanceCheckJob.class.getName()).isExcute()) return;
        /**
         * 这里设置 6 的原因为:
         * 1. 模拟抓取 Fee 需要借用登陆的 Cookie, 同时需要 changeRegion, 这个是肯定需要锁定.
         * 2. 这里锁定的时间不能够太长, 不然会导致前端 Selling 更新失败.
         * 3. 平均每个订单需要 10s 时间处理完成;
         */
        int orderSize = 8;
        int hourOfDay = DateTime.now().getHourOfDay();
        // 如果是晚上, 则加大抓去量
        if(hourOfDay >= 19 || hourOfDay <= 9) orderSize = 24;
        List<Account> accounts = Account.openedSaleAcc();
        Map<String, Account> accMap = new HashMap<String, Account>();
        for(Account acc : accounts) {
            accMap.put(acc.type.name(), acc);
        }

        for(M m : M.values()) {
            if(m == M.EBAY_UK) continue;
            Account acc = null;
            if(Arrays.asList(M.AMAZON_IT, M.AMAZON_FR, M.AMAZON_DE, M.AMAZON_ES).contains(m)) {
                acc = accMap.get(M.AMAZON_DE.name());
            } else {
                acc = accMap.get(m.name());
            }

            new FinanceShippedPromise(acc, m, orderSize).now();
        }

    }

    public static void saveFees(List<SaleFee> fees) {
        try {
            PreparedStatement psmt = DB.getConnection().prepareStatement(
                    "INSERT INTO SaleFee(account_id, order_orderId, type_name, market, memo, orderId, `date`, cost, currency, usdCost, qty)" +
                            " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            int i = 1;
            for(SaleFee fee : fees) {
                psmt.setLong(i++, fee.account.id);
                psmt.setString(i++, fee.orderId);
                psmt.setString(i++, fee.type.name);
                psmt.setString(i++, fee.market.name());
                psmt.setString(i++, "");
                psmt.setString(i++, fee.orderId);
                psmt.setTimestamp(i++, new Timestamp(fee.date.getTime()));
                psmt.setFloat(i++, fee.cost);
                psmt.setString(i++, fee.currency.name());
                psmt.setFloat(i++, fee.usdCost);
                psmt.setInt(i, fee.qty);

                i = 1;
                psmt.addBatch();
            }
            psmt.executeBatch();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static String deleteSaleFees(List<Orderr> orders) {
        List<String> orderIds = new ArrayList<String>();
        for(Orderr order : orders) {
            orderIds.add(order.orderId);
        }
        String sql = "DELETE FROM SaleFee WHERE " + SqlSelect.whereIn("order_orderId", orderIds);
        DB.execute(sql);
        return sql;
    }
}
