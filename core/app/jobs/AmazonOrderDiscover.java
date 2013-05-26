package jobs;

import com.amazonservices.mws.orders.MarketplaceWebServiceOrdersException;
import helper.DBUtils;
import helper.Webs;
import models.Jobex;
import models.market.Account;
import models.market.Orderr;
import mws.MWSOrders;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.jobs.Job;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对 Amazon 订单的第一步, 发现订单
 * <p/>
 * The GetOrder operation has a maximum request quota of 6 and a restore rate of 1 request every minute.
 * <p/>
 * 每隔 10mn 获取最近 30 mn 的订单. 每 6 分钟 API Quote 会恢复满(6次)
 * - 轮询周期: 1mn
 * - Duration: 10mn
 * User: wyatt
 * Date: 5/24/13
 * Time: 10:04 AM
 */
public class AmazonOrderDiscover extends Job<List<Orderr>> {

    @Override
    public void doJob() {
        if(!Jobex.findByClassName(AmazonOrderDiscover.class.getName()).isExcute()) return;
        List<Account> accounts = Account.openedSaleAcc();
        for(Account acc : accounts) {
            try {
                List<Orderr> orders = MWSOrders.listOrders(acc, 30);
                Map<String, Boolean> existOrders = AmazonOrderDiscover.ordersExist(orders);

                List<Orderr> toUpdate = new ArrayList<Orderr>();
                List<Orderr> toSave = new ArrayList<Orderr>();
                for(Orderr orderr : orders) {
                    if(existOrders.containsKey(orderr.orderId))
                        toUpdate.add(orderr);
                    else
                        toSave.add(orderr);
                }

                AmazonOrderDiscover.updateOrders(toUpdate);
                AmazonOrderDiscover.saveOrders(toSave);

            } catch(MarketplaceWebServiceOrdersException e) {
                Logger.warn("Account %s is not fecth Order because of [%s]",
                        acc.username, Webs.S(e));
            }
        }
    }

    public static void updateOrders(List<Orderr> toUpdateOrders) {
        try {
            PreparedStatement pst = DB.getConnection().prepareStatement(
                    "UPDATE Orderr SET state=?, shipLevel=?, paymentDate=?," +
                            " city=?, country=?, postalCode=?, market=?, " +
                            " phone=?, province=?, reciver=?, address=?" +
                            " WHERE orderId=?"
            );
            int i = 1;
            for(Orderr orderr : toUpdateOrders) {
                pst.setString(i++, orderr.state.name());
                pst.setString(i++, orderr.shipLevel);
                pst.setTimestamp(i++,
                        orderr.paymentDate == null ? null : new Timestamp(orderr.paymentDate.getTime()));
                pst.setString(i++, orderr.city);
                pst.setString(i++, orderr.country);
                pst.setString(i++, orderr.postalCode);
                pst.setString(i++, orderr.market.name());
                pst.setString(i++, orderr.phone);
                pst.setString(i++, orderr.province);
                pst.setString(i++, orderr.reciver);
                pst.setString(i++, orderr.address);
                pst.setString(i, orderr.orderId);
                pst.addBatch();
                i = 1;
            }
            int[] results = pst.executeBatch();
            Logger.info("AmazonOrderDiscover Update %s Orders. [%s](%s)",
                    toUpdateOrders.size(), Webs.intArrayString(results), results.length
            );
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveOrders(List<Orderr> toSaveOrders) {
        try {
            PreparedStatement pst = DB.getConnection().prepareStatement(
                    "INSERT INTO Orderr(orderId, market, account_id, state, shipLevel, paymentDate, createDate, reviewMailed, warnning)" +
                            " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
            int i = 1;
            for(Orderr orderr : toSaveOrders) {
                pst.setString(i++, orderr.orderId);
                pst.setString(i++, orderr.market.name());
                pst.setLong(i++, orderr.account.id);
                pst.setString(i++, orderr.state.name());
                pst.setString(i++, orderr.shipLevel);
                pst.setTimestamp(i++,
                        orderr.paymentDate == null ? null : new Timestamp(orderr.paymentDate.getTime()));
                pst.setTimestamp(i++, new Timestamp(orderr.createDate.getTime()));
                pst.setBoolean(i++, orderr.reviewMailed);
                pst.setBoolean(i, orderr.warnning);
                pst.addBatch();
                i = 1;
            }
            int[] results = pst.executeBatch();
            Logger.info("AmazonOrderDiscover Save %s Orders. [%s](%s)",
                    toSaveOrders.size(), Webs.intArrayString(results), results.length
            );
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查 Orders 已经在数据库中存在的订单
     *
     * @param orderrs
     * @return
     */
    public static Map<String, Boolean> ordersExist(List<Orderr> orderrs) {
        List<String> orderIds = new ArrayList<String>();
        Map<String, Boolean> existOrders = new HashMap<String, Boolean>();
        for(Orderr orderr : orderrs) {
            orderIds.add(orderr.orderId);
        }

        SqlSelect sql = new SqlSelect()
                .select("orderId", "count(orderId) as cnt")
                .from("Orderr")
                .where(SqlSelect.whereIn("orderId", orderIds))
                .groupBy("orderId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        for(Map<String, Object> row : rows) {
            int cnt = NumberUtils.toInt(row.get("cnt").toString());
            if(cnt > 0)
                existOrders.put(row.get("orderId").toString(), true);
        }
        return existOrders;
    }
}
