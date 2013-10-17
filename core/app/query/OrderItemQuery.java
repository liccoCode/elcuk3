package query;

import helper.DBUtils;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.DB;
import play.db.helper.SqlSelect;
import play.utils.FastRuntimeException;
import query.vo.AnalyzeVO;

import java.sql.Connection;
import java.util.*;

/**
 * OrderItem Dao, 使用特定 JPQL 或者 SQL 语句加载的数据
 * User: wyattpan
 * Date: 7/4/12
 * Time: 11:54 AM
 */
public class OrderItemQuery {

    public List<AnalyzeVO> groupCategory(Date from, Date to, Long accId) {
        SqlSelect sql = new SqlSelect()
                // tip: just a hack
                .select("p.category_categoryId as sku",
                        "sum(oi.quantity) as qty",
                        "sum(oi.usdCost) as usdCost")
                .from("OrderItem oi")
                .leftJoin("Product p on p.sku=oi.product_sku");
        if(accId != null) {
            sql.leftJoin("Orderr o ON o.orderId=oi.order_orderId")
                    .where("o.account_id=?").param(accId);
        }
        sql.where("o.createDate>=?").param(from)
                .where("o.createDate<=?").param(to)
                .where("oi.product_sku IS NOT NULL")
                .where("oi.quantity>0")
                .groupBy("p.category_categoryId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    public List<AnalyzeVO> groupCategory(Date from, Date to, M market) {
        return groupCategory(from, to, market, DB.getConnection());
    }

    public List<AnalyzeVO> groupCategory(Date from, Date to, M market, Connection conn) {
        SqlSelect sql = new SqlSelect()
                // tip: just a hack
                .select("p.category_categoryId as sku",
                        "sum(oi.quantity) as qty",
                        "sum(oi.usdCost) as usdCost")
                .from("OrderItem oi")
                .leftJoin("Product p on p.sku=oi.product_sku")
                .where("oi.market=?").param(market.name())
                .where("oi.createDate>=?").param(from)
                .where("oi.createDate<=?").param(to)
                .where("oi.product_sku IS NOT NULL")
                .where("oi.quantity>0")
                .groupBy("p.category_categoryId");
        List<Map<String, Object>> rows = DBUtils.rows(conn, sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

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
        Map<String, Integer> saleMap = new HashMap<String, Integer>();
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
        List<Map<String, Object>> rows = DBUtils.rows(conn, sql.toString(), sql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            saleMap.put(row.get("k").toString(), NumberUtils.toInt(row.get("qty").toString()));
        }
        return saleMap;
    }

    public Map<String, Integer> analyzeDaySale(Date from, Date to, M market, boolean isSku) {
        return analyzeDaySale(from, to, market, isSku, DB.getConnection());
    }


    /**
     * 获取 AnalyzeVO 的门面方法, 内涵处理 all, cateogyrId, sid, sku 的派发
     *
     * @param market
     * @param val
     * @param type
     * @param from
     * @param to
     * @return
     */
    public static List<AnalyzeVO> getAnalyzeVOsFacade(M market, String val, String type, Date from, Date to) {
        Connection conn = DB.getConnection();
        List<AnalyzeVO> lineVos;
        OrderItemQuery query = new OrderItemQuery();
        if("all".equals(val)) {
            lineVos = query.allSalesAndUnits(from, to, market, conn);
        } else if(val.matches("^\\d{2}$")) {
            lineVos = query.categorySalesAndUnits(from, to, market, val, conn);
        } else if("sid".equals(type)) {
            lineVos = query.sidSalesAndUnits(from, to, market, val, conn);
        } else if("sku".equals(type)) {
            lineVos = query.skuSalesAndUnits(from, to, market, val, conn);
        } else {
            throw new FastRuntimeException("不支持的类型!");
        }
        return lineVos;
    }

    /**
     * sid 的销量曲线
     *
     * @param from
     * @param to
     * @param market
     * @param sid
     * @param conn
     * @return
     */
    public List<AnalyzeVO> sidSalesAndUnits(Date from, Date to, M market, String sid, Connection conn) {
        SqlSelect sql = new SqlSelect()
                .select("sum(oi.quantity) qty", "sum(oi.usdCost) usdCost",
                        "DATE_FORMAT(DATE_ADD(oi.createDate, INTERVAL " + market.timeZoneOffset() +
                                " HOUR), '%Y-%m-%d') as _date")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId")
                .leftJoin("Selling s ON oi.selling_sellingId=s.sellingId")
                .where("s.merchantSKU=?").param(sid)
                .where("o.createDate>=?").param(market.withTimeZone(from).toDate())
                .where("o.createDate<=?").param(market.withTimeZone(to).toDate())
                .where("o.market=?").param(market.name())
                .where("o.state NOT IN (?,?,?)")
                .params(Orderr.S.CANCEL.name(), Orderr.S.REFUNDED.name(), Orderr.S.RETURNNEW.name())
                .groupBy("_date");
        List<Map<String, Object>> rows = DBUtils.rows(conn, sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    /**
     * sku 的销量曲线
     *
     * @param from
     * @param to
     * @param market
     * @param sku
     * @param conn
     * @return
     */
    public List<AnalyzeVO> skuSalesAndUnits(Date from, Date to, M market, String sku, Connection conn) {
        SqlSelect sql = new SqlSelect()
                .select("sum(oi.quantity) as qty", "sum(oi.usdCost) usdCost",
                        "DATE_FORMAT(DATE_ADD(oi.createDate, INTERVAL " + market.timeZoneOffset() +
                                " HOUR), '%Y-%m-%d') as _date")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId")
                .leftJoin("Product p ON p.sku=oi.product_sku")
                .where("p.sku=?").param(sku)
                .where("o.createDate>=?").param(market.withTimeZone(from).toDate())
                .where("o.createDate<=?").param(market.withTimeZone(to).toDate())
                .where("o.market=?").param(market.name())
                .where("o.state NOT IN (?,?,?)")
                .params(Orderr.S.CANCEL.name(), Orderr.S.REFUNDED.name(), Orderr.S.RETURNNEW.name())
                .groupBy("_date");
        List<Map<String, Object>> rows = DBUtils.rows(conn, sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    /**
     * Cateogyr 的销量曲线
     *
     * @param from
     * @param to
     * @param market
     * @param categoryId
     * @param conn
     * @return
     */
    public List<AnalyzeVO> categorySalesAndUnits(Date from, Date to, M market, String categoryId, Connection conn) {
        SqlSelect sql = new SqlSelect()
                .select("sum(oi.quantity) qty", "sum(oi.usdCost) usdCost",
                        "DATE_FORMAT(DATE_ADD(oi.createDate, INTERVAL " + market.timeZoneOffset() +
                                " HOUR), '%Y-%m-%d') as _date")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId");
        if(StringUtils.isNotBlank(categoryId))
            sql.leftJoin("Product p ON p.sku=oi.product_sku")
                    .where("p.category_categoryId=?").param(categoryId);
        sql.where("o.createDate>=?").param(market.withTimeZone(from).toDate())
                .where("o.createDate<=?").param(market.withTimeZone(to).toDate())
                .where("o.market=?").param(market.name())
                .where("o.state IN (?,?,?)")
                .params(Orderr.S.PAYMENT.name(), Orderr.S.PENDING.name(), Orderr.S.SHIPPED.name())
                .groupBy("_date");

        List<Map<String, Object>> rows = DBUtils.rows(conn, sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    /**
     * 所有产品的销量曲线
     *
     * @param from
     * @param to
     * @param market
     * @param conn
     * @return
     */
    public List<AnalyzeVO> allSalesAndUnits(Date from, Date to, M market, Connection conn) {
        return categorySalesAndUnits(from, to, market, null, conn);
    }

    private List<AnalyzeVO> rows2Vo(List<Map<String, Object>> rows) {
        List<AnalyzeVO> items = new ArrayList<AnalyzeVO>();
        for(Map<String, Object> row : rows) {
            AnalyzeVO vo = new AnalyzeVO();
            if(row.get("sku") != null)
                vo.sku = row.get("sku").toString();
            if(row.get("qty") != null)
                vo.qty = ((Number) row.get("qty")).intValue();
            if(row.get("market") != null)
                vo.market = M.val(row.get("market").toString());
            if(row.get("usdCost") != null)
                vo.usdCost = ((Number) row.get("usdCost")).floatValue();
            if(row.get("_date") != null)
                vo.date = new DateTime(row.get("_date")).toDate();
            if(row.get("sid") != null)
                vo.sid = row.get("sid").toString();
            if(row.get("aid") != null)
                vo.aid = row.get("aid").toString();
            items.add(vo);
        }
        return items;
    }

}
