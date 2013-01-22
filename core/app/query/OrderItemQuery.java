package query;

import helper.DBUtils;
import helper.Dates;
import models.market.Feedback;
import models.market.M;
import models.market.Orderr;
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


    public List<AnalyzeVO> analyzeVos(Date from, Date to, M market) {
        SqlSelect sql = new SqlSelect()
                .select("oi.product_sku as sku", "oi.selling_sellingId as sid", "s.asin as asin",
                        "oi.quantity as qty", "oi.createDate as _date", "o.account_id as aid",
                        "oi.market")
                .leftJoin("Orderr o ON o.orderId=oi.order_orderId")
                .leftJoin("Selling s ON s.sellingId=oi.selling_sellingId")
                .from("OrderItem oi")
                .where("oi.product_sku IS NOT NULL")
                .where("oi.market=?").param(market.name())
                .where("oi.createDate>=?").param(from)
                .where("oi.createDate<=?").param(to)
                .orderBy("oi.createDate DESC");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
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
     * 所有正常销售的订单的订单项目. 不包括 CANCEL, REFUNDED, RETURNEW 的订单
     *
     * @return
     */
    public List<AnalyzeVO> allNormalSaleOrderItem(Date from, Date to, M market) {
        SqlSelect sql = new SqlSelect()
                .select("oi.createDate as _date", "oi.quantity as qty", "oi.usdCost", "oi.market")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId")
                .where("oi.createDate>=?").param(from)
                .where("oi.market=?").param(market.name())
                .where("oi.createDate<=?").param(to)
                .where("o.state NOT IN (?,?,?)")
                .params(Orderr.S.CANCEL.name(), Orderr.S.REFUNDED.name(),
                        Orderr.S.RETURNNEW.name());
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    /**
     * 加载某一个 SKU 的所有正常销售的 OrderItem. 不包括 CANCEL, REFUNDED, RETURNEW 的订单
     *
     * @param sku
     * @param from
     * @param to
     * @return
     */
    public List<AnalyzeVO> skuNormalSaleOrderItem(String sku, Date from, Date to, M market) {
        SqlSelect sql = new SqlSelect()
                .select("oi.createDate as _date", "oi.quantity as qty", "oi.usdCost", "oi.market")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId")
                .leftJoin("Product p ON p.sku=oi.product_sku")
                .where("p.sku=?").param(sku)
                .where("oi.createDate>=?").param(from)
                .where("oi.market=?").param(market.name())
                .where("oi.createDate<=?").param(to)
                .where("oi.createDate<=?").param(to)
                .where("o.state NOT IN (?,?,?)")
                .params(Orderr.S.CANCEL.name(), Orderr.S.REFUNDED.name(),
                        Orderr.S.RETURNNEW.name());
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
    }

    public List<AnalyzeVO> mskuWithAccountNormalSaleOrderItem(String msku, Long accId,
                                                              Date from, Date to, M market) {
        SqlSelect sql = new SqlSelect()
                .select("oi.createDate as _date", "oi.quantity as qty", "oi.usdCost", "oi.market")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON oi.order_orderId=o.orderId")
                .leftJoin("Selling s ON oi.selling_sellingId=s.sellingId");
        // 如果有 account 就增加过滤
        if(accId != null)
            sql.leftJoin("Account a ON s.account_id=a.id").where("a.id=?").param(accId);
        sql.where("s.merchantSKU=?").param(msku)
                .where("oi.createDate>=?").param(from)
                .where("oi.market=?").param(market.name())
                .where("oi.createDate<=?").param(to)
                .where("o.state NOT IN (?,?,?)")
                .params(Orderr.S.CANCEL.name(), Orderr.S.REFUNDED.name(),
                        Orderr.S.RETURNNEW.name());
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        return rows2Vo(rows);
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
