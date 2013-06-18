package query;

import helper.DBUtils;
import helper.Dates;
import models.market.Feedback;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.DB;
import play.db.helper.SqlSelect;
import query.vo.AnalyzeVO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        sql.where("oi.createDate>=?").param(from)
                .where("oi.createDate<=?").param(to)
                .where("oi.product_sku IS NOT NULL")
                .where("oi.quantity>0")
                .groupBy("p.category_categoryId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    public List<AnalyzeVO> groupCategory(Date from, Date to, M market) {
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
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
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
    public Map<String, Integer> analyzeDaySale(Date from, Date to, M market, boolean isSku) {
        Map<String, Integer> saleMap = new HashMap<String, Integer>();
        SqlSelect sql = new SqlSelect()
                .select("sum(oi.quantity) qty", "oi.selling_sellingId sid")
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
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            saleMap.put(row.get("sid").toString(), NumberUtils.toInt(row.get("qty").toString()));
        }
        return saleMap;
    }


    public String feedbackSKU(Feedback feedback) {
        Connection conn = DB.getConnection();

        String sku = null;
        try {
            PreparedStatement ps = conn
                    .prepareStatement("SELECT product_sku FROM OrderItem WHERE order_orderId=?");
            ps.setString(1, feedback.orderId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                sku = rs.getString("product_sku");
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            //ignore
        }
        return sku;
    }

    /**
     * SKU 的 Feedback 的数量
     *
     * @param sku
     * @return
     */
    public int skuFeedbackCount(String sku) {
        Connection conn = DB.getConnection();
        int count = 0;
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(i.product_sku) FROM Feedback f LEFT JOIN OrderItem i ON f.orderr_orderId=i.order_orderId WHERE i.product_sku=?");
            ps.setString(1, sku);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                count = rs.getInt(1);
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            //ignore
        }
        return count;
    }

    public Map<String, AtomicInteger> skuSales() {
        DateTime dt = DateTime.now();
        return skuSales(dt.minusYears(2).toDate(), dt.toDate());
    }

    public Map<String, AtomicInteger> skuSales(Date from, Date to) {
        Date begin = Dates.morning(from);
        Date end = Dates.night(to);

        Connection conn = DB.getConnection();

        Map<String, AtomicInteger> skuSales = new HashMap<String, AtomicInteger>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT quantity, product_sku FROM OrderItem WHERE createDate>=? AND createDate<=?");
            ps.setDate(1, new java.sql.Date(begin.getTime()));
            ps.setDate(2, new java.sql.Date(end.getTime()));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String sku = rs.getString("product_sku");
                int quantity = rs.getInt("quantity");
                if(!skuSales.containsKey(sku))
                    skuSales.put(sku, new AtomicInteger(quantity));
                else
                    skuSales.get(sku).addAndGet(quantity);
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            //ignore
        }

        return skuSales;
    }

    /**
     * sid 的销量曲线
     *
     * @param from
     * @param to
     * @param market
     * @param sid
     * @return
     */
    public List<AnalyzeVO> sidSalesAndUnits(Date from, Date to, M market, String sid) {
        SqlSelect sql = new SqlSelect()
                .select("sum(oi.quantity) qty", "sum(oi.usdCost) usdCost",
                        "DATE_FORMAT(oi.createDate, '%Y-%m-%d') as _date")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId")
                .leftJoin("Selling s ON oi.selling_sellingId=s.sellingId")
                .where("s.merchantSKU=?").param(sid)
                .where("oi.createDate>=?").param(market.withTimeZone(from).toDate())
                .where("oi.createDate<=?").param(market.withTimeZone(to).toDate())
                .where("oi.market=?").param(market.name())
                .where("o.state NOT IN (?,?,?)")
                .params(Orderr.S.CANCEL.name(), Orderr.S.REFUNDED.name(), Orderr.S.RETURNNEW.name())
                .groupBy("_date");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    /**
     * sku 的销量曲线
     *
     * @param from
     * @param to
     * @param market
     * @param sku
     * @return
     */
    public List<AnalyzeVO> skuSalesAndUnits(Date from, Date to, M market, String sku) {
        SqlSelect sql = new SqlSelect()
                .select("sum(oi.quantity) as qty", "sum(oi.usdCost) usdCost",
                        "DATE_FORMAT(oi.createDate, '%Y-%m-%d') as _date")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId")
                .leftJoin("Product p ON p.sku=oi.product_sku")
                .where("p.sku=?").param(sku)
                .where("oi.createDate>=?").param(market.withTimeZone(from).toDate())
                .where("oi.createDate<=?").param(market.withTimeZone(to).toDate())
                .where("oi.market=?").param(market.name())
                .where("o.state NOT IN (?,?,?)")
                .params(Orderr.S.CANCEL.name(), Orderr.S.REFUNDED.name(), Orderr.S.RETURNNEW.name())
                .groupBy("_date");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    /**
     * Cateogyr 的销量曲线
     *
     * @param from
     * @param to
     * @param market
     * @param categoryId
     * @return
     */
    public List<AnalyzeVO> categorySalesAndUnits(Date from, Date to, M market, String categoryId) {
        SqlSelect sql = new SqlSelect()
                .select("sum(oi.quantity) qty", "sum(oi.usdCost) usdCost",
                        "DATE_FORMAT(oi.createDate, '%Y-%m-%d') as _date")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId");
        if(StringUtils.isNotBlank(categoryId))
            sql.leftJoin("Product p ON p.sku=oi.product_sku").where("p.category_categoryId=?").param(categoryId);
        sql.where("o.createDate>=?").param(market.withTimeZone(from).toDate())
                .where("o.createDate<=?").param(market.withTimeZone(to).toDate())
                .where("oi.market=?").param(market.name())
                .where("o.state IN (?,?,?)")
                .params(Orderr.S.PAYMENT.name(), Orderr.S.PENDING.name(), Orderr.S.SHIPPED.name())
                .groupBy("_date");

        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    /**
     * 所有产品的销量曲线
     *
     * @param from
     * @param to
     * @param market
     * @return
     */
    public List<AnalyzeVO> allSalesAndUnits(Date from, Date to, M market) {
        return categorySalesAndUnits(from, to, market, null);
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
