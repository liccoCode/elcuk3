package jobs;

import models.finance.SaleFee;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.db.DB;
import play.db.helper.SqlSelect;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * @deprecated 落为工具方法
 */
public class AmazonFinanceCheckJob {
    public static final String RUNNING = "amazonfinancecheckjob_running";

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