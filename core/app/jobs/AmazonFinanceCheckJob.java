package jobs;

import helper.LogUtils;
import jobs.analyze.SellingSaleAnalyzeJob;
import jobs.promise.FinanceShippedPromise;
import models.Jobex;
import models.finance.SaleFee;
import models.market.Account;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
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
    public static final String RUNNING = "amazonfinancecheckjob_running";

    @Override
    public void doJob() throws InterruptedException, ExecutionException, TimeoutException {
        if(isRnning()) return;

        long begin = System.currentTimeMillis();
        // 不和两个大计算量的任何重合
        if(SellingSaleAnalyzeJob.isRnning()) return;
        // 1. 寻找需要处理的订单, 并且按照 market 进行分组
        // 2. 派发给 Promise Job 进行出来.
        if(!Jobex.findByClassName(AmazonFinanceCheckJob.class.getName()).isExcute()) return;

        try {
            Cache.add(RUNNING, RUNNING);

            /**
             * 这里设置 6 的原因为:
             * 1. 模拟抓取 Fee 需要借用登陆的 Cookie, 同时需要 changeRegion, 这个是肯定需要锁定.
             * 2. 这里锁定的时间不能够太长, 不然会导致前端 Selling 更新失败.
             * 3. 平均每个订单需要 10s 时间处理完成;
             */
            int orderSize = 8;
            DateTime now = DateTime.now();
            int hourOfDay = now.getHourOfDay();
            int dayOfWeek = now.getDayOfWeek();
            // 如果是晚上, 则加大抓去量
            if(hourOfDay >= 19 || hourOfDay <= 9) orderSize = 24;
            if(Arrays.asList(6, 7).contains(dayOfWeek)) orderSize = 24;

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

                // 让 DB 的查询在同一个线程, 但抓取进入另外的线程
                // TODO 性能有问题
                String jpql = "account=? AND market=? AND state IN (?,?) AND feeflag=0 ORDER BY createDate DESC";
                List<Orderr> orders = Orderr.find(jpql, acc, m, Orderr.S.SHIPPED, Orderr.S.REFUNDED).fetch(orderSize);
                new FinanceShippedPromise(acc, m, Orderr.ids(orders)).now();
            }
        } finally {
            Cache.delete(RUNNING);
        }

        if(LogUtils.isslow(System.currentTimeMillis() - begin)) {
            LogUtils.JOBLOG.info(String
                    .format("AmazonFinanceCheckJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }

    }

    /**
     * 确保同一时间只有一个 analyzes 正在计算
     *
     * @return
     */
    public static boolean isRnning() {
        return StringUtils.isNotBlank(Cache.get(RUNNING, String.class));
    }

    public static void saveFees(List<SaleFee> fees) {
        PreparedStatement psmt = null;
        try {
            psmt = DB.getConnection().prepareStatement(
                    "INSERT INTO SaleFee(account_id, order_orderId, type_name, market, memo, orderId, `DATE`, cost, currency, usdCost, qty)" +
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
        } finally {
            try {
                if(psmt != null) psmt.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String deleteSaleFees(List<Orderr> orders) {
        return deleteSaleFees(Orderr.ids(orders));
    }

    public static String deleteSaleFees(Collection<String> orderIds) {
        String sql = "DELETE FROM SaleFee WHERE " + SqlSelect.whereIn("order_orderId", orderIds);
        DB.execute(sql);
        return sql;
    }

    public static void updateFeeFlag(Collection<String> orderIds) {
        String sql = "update Orderr set feeflag=2 WHERE " + SqlSelect.whereIn("orderId", orderIds);
        DB.execute(sql);
    }
}
