package query;

import helper.DBUtils;
import helper.LogUtils;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;

import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OrderItem Dao, 使用特定 JPQL 或者 SQL 语句加载的数据
 * User: wyattpan
 * Date: 7/4/12
 * Time: 11:54 AM
 */
public class OrderItemQuery {
    /**
     * 分析页面一段时间内的销量数据
     *
     * @param from
     * @param to
     * @param market
     * @param isSku  key 为 sku 或者 sid
     * @return
     */
    public Map<String, Integer> analyzeDaySale(Date from, Date to, M market, boolean isSku, Connection conn) {
        Map<String, Integer> saleMap = new HashMap<>();
        SqlSelect sql = new SqlSelect()
                .select("sum(oi.quantity) qty", (isSku ? "oi.product_sku" : "oi.selling_sellingId") + " k")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId")
                .where("o.createDate>=?").param(market.withTimeZone(from).toDate())
                .where("o.createDate<=?").param(market.withTimeZone(to).toDate())
                .where("o.market=?").param(market.name())
                .where("o.state IN (?,?,?)")
                .params(Orderr.S.PAYMENT.name(), Orderr.S.PENDING.name(), Orderr.S.SHIPPED.name());
        if(isSku) {
            sql.groupBy("oi.product_sku");
        } else {
            sql.groupBy("oi.selling_sellingId");
        }
        LogUtils.JOBLOG.info("qty1:" + sql.toString()+ " ---------"+sql.getParams().toString());
        List<Map<String, Object>> rows = DBUtils.rows(conn, sql.toString(), sql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            saleMap.put(row.get("k").toString(), NumberUtils.toInt(row.get("qty").toString()));
        }
        return saleMap;
    }
}
