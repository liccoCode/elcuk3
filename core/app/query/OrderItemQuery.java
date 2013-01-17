package query;

import helper.DBUtils;
import helper.Dates;
import helper.JPAs;
import models.market.*;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.DB;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.libs.F;
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
    /**
     * 加载出 OrderItem 中只含有 sku 与 qty 的 rows
     *
     * @param from
     * @param to
     * @param acc
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<F.T3<String, Integer, Float>> sku_qty_usdCost(Date from, Date to,
                                                              Account acc) {
        SqlSelect select = new JpqlSelect()
                .select("oi.product.sku as sku, oi.quantity as qty, oi.usdCost as usdCost")
                .from("OrderItem oi")
                .where("oi.createDate>=?").param(from)
                .where("oi.createDate<=?").param(to)
                .where("oi.quantity>0");
        if(acc != null) select.where("oi.order.account=?").param(acc);
        List<Map> rows = JPAs.createQueryMap(select).getResultList();
        List<F.T3<String, Integer, Float>> triplus = new ArrayList<F.T3<String, Integer, Float>>();
        for(Map row : rows) {
            triplus.add(new F.T3<String, Integer, Float>(
                    row.get("sku").toString().substring(0, 2),
                    NumberUtils
                            .toInt((row.get("qty") == null ? "0" : row.get("qty").toString()), 0),
                    NumberUtils.toFloat(
                            (row.get("usdCost") == null ? "0" : row.get("usdCost").toString()),
                            0)));
        }
        return triplus;
    }

    @SuppressWarnings("unchecked")
    public List<F.T5<String, F.T2<String, String>, Integer, Date, String>> sku_sid_asin_qty_date_aId(
            Date from, Date to, int filterQuantity) {
        SqlSelect sql = new SqlSelect()
                .select("oi.product_sku as sku", "oi.selling_sellingId as sid", "s.asin as asin",
                        "oi.quantity as qty", "oi.createDate as _date", "o.account_id as aid")
                .leftJoin("Orderr o ON o.orderId=oi.order_orderId")
                .leftJoin("Selling s ON s.sellingId=oi.selling_sellingId")
                .from("OrderItem oi")
                        // 对清理的孤立的 OrderItem 的过滤
                .where("oi.product_sku IS NOT NULL")
                .where("oi.createDate>=?").param(from)
                .where("oi.createDate<=?").param(to);
        if(filterQuantity > 0) sql.where("oi.quantity>?").param(filterQuantity);
        sql.orderBy("oi.createDate DESC");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        List<F.T5<String, F.T2<String, String>, Integer, Date, String>> t5Rows = new ArrayList<F.T5<String, F.T2<String, String>, Integer, Date, String>>();
        for(Map row : rows) {
            t5Rows.add(new F.T5<String, F.T2<String, String>, Integer, Date, String>(
                    row.get("sku").toString(),
                    new F.T2<String, String>(row.get("sid").toString(), row.get("asin").toString()),
                    NumberUtils.toInt(row.get("qty").toString()),
                    (Date) row.get("_date"),
                    row.get("aid").toString()
            ));
        }
        return t5Rows;
    }

    public List<AnalyzeVO> analyzeVos(Date from, Date to, M market) {
        SqlSelect sql = new SqlSelect()
                .select("oi.product_sku as sku", "oi.selling_sellingId as sid", "s.asin as asin",
                        "oi.quantity as qty", "oi.createDate as _date", "o.account_id as aid")
                .leftJoin("Orderr o ON o.orderId=oi.order_orderId")
                .leftJoin("Selling s ON s.sellingId=oi.selling_sellingId")
                .from("OrderItem oi")
                .where("oi.product_sku IS NOT NULL")
                .where("oi.createDate>=?").param(from)
                .where("oi.createDate<=?").param(to)
                .where("oi.market=?").param(market.name())
                .orderBy("oi.createDate DESC");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        List<AnalyzeVO> vos = new ArrayList<AnalyzeVO>();
        for(Map<String, Object> row : rows) {
            AnalyzeVO vo = new AnalyzeVO();
            vo.sku = row.get("sku").toString();
            vo.sid = row.get("sid").toString();
            vo.asin = row.get("asin").toString();
            vo.qty = NumberUtils.toInt(row.get("qty").toString());
            vo.date = (Date) row.get("_date");
            vo.aid = row.get("aid").toString();
            vos.add(vo);
        }
        return vos;
    }

    public String feedbackSKU(Feedback feedback) {
        Connection conn = DB.getConnection();

        String sku = null;
        try {
            PreparedStatement ps = conn
                    .prepareStatement("select product_sku from OrderItem where order_orderId=?");
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
                    "select count(i.product_sku) from Feedback f left join OrderItem i on f.orderr_orderId=i.order_orderId where i.product_sku=?");
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
                    "select quantity, product_sku from OrderItem where createDate>=? AND createDate<=?");
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
    public List<OrderItem> allNormalSaleOrderItem(Date from, Date to) {
        List<Map<String, Object>> rows = DBUtils.rows("SELECT oi.createDate, oi.quantity," +
                " oi.usdCost, oi.market FROM OrderItem oi" +
                " LEFT JOIN Orderr o ON oi.order_orderId=o.orderId" +
                " WHERE oi.createDate>=? AND oi.createDate<=? AND o.state NOT IN (?, ?, ?)",
                from, to, Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW);
        return rows2OrderItem(rows);
    }

    /**
     * 加载某一个 SKU 的所有正常销售的 OrderItem. 不包括 CANCEL, REFUNDED, RETURNEW 的订单
     *
     * @param sku
     * @param from
     * @param to
     * @return
     */
    public List<OrderItem> skuNormalSaleOrderItem(String sku, Date from, Date to) {
        List<Map<String, Object>> rows = DBUtils.rows("SELECT oi.createDate, oi.quantity," +
                " oi.usdCost, oi.market FROM OrderItem oi" +
                " LEFT JOIN Orderr o on oi.order_orderId=o.orderId" +
                " LEFT JOIN Product p on p.sku=oi.product_sku" +
                " WHERE p.sku=? AND oi.createDate>=? AND oi.createDate<=? AND" +
                " o.state NOT IN (?, ?, ?)",
                sku, from, to, Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW);

        return rows2OrderItem(rows);
    }

    public List<OrderItem> mskuNormalSaleOrderItem(String msku, Date from, Date to) {
        List<Map<String, Object>> rows = DBUtils.rows("SELECT oi.createDate, oi.quantity," +
                " oi.usdCost, oi.market FROM OrderItem oi" +
                " LEFT JOIN Orderr o on oi.order_orderId=o.orderId" +
                " LEFT JOIN Selling s on oi.selling_sellingId=s.sellingId" +
                " WHERE s.merchantSKU=? AND oi.createDate>=? AND oi.createDate<=? AND" +
                " o.state NOT IN (?, ?, ?)",
                msku, from, to, Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW);
        return rows2OrderItem(rows);
    }

    public List<OrderItem> mskuWithAccountNormalSaleOrderItem(String msku, long accId,
                                                              Date from, Date to) {
        List<Map<String, Object>> rows = DBUtils.rows("SELECT oi.createDate, oi.quantity," +
                " oi.usdCost, oi.market FROM OrderItem oi" +
                " LEFT JOIN Orderr o on oi.order_orderId=o.orderId" +
                " LEFT JOIN Selling s on oi.selling_sellingId=s.sellingId" +
                " LEFT JOIN Account a on s.account_id=a.id" +
                " WHERE s.merchantSKU=? AND a.id=? AND oi.createDate>=? AND oi.createDate<=? AND" +
                " o.state NOT IN (?, ?, ?)",
                msku, accId, from, to,
                Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW);
        return rows2OrderItem(rows);
    }

    private List<OrderItem> rows2OrderItem(List<Map<String, Object>> rows) {
        List<OrderItem> items = new ArrayList<OrderItem>();
        for(Map<String, Object> row : rows) {
            OrderItem itm = new OrderItem();
            itm.quantity = (Integer) row.get("quantity");
            itm.market = M.val(row.get("market").toString());
            itm.usdCost = (Float) row.get("usdCost");
            itm.createDate = new DateTime(row.get("createDate")).toDate();
            items.add(itm);
        }
        return items;
    }
}
