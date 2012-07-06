package query;

import helper.JPAs;
import models.market.Account;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.ArrayList;
import java.util.Date;
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
     * 加载出 OrderItem 中只含有 sku 与 qty 的 rows
     *
     * @param from
     * @param to
     * @param acc
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<F.T3<String, Integer, Float>> sku_qty_usdCost(Date from, Date to, Account acc) {
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
                    NumberUtils.toInt((row.get("qty") == null ? "0" : row.get("qty").toString()), 0),
                    NumberUtils.toFloat((row.get("usdCost") == null ? "0" : row.get("usdCost").toString()), 0)));
        }
        return triplus;
    }

    /**
     * 加载出 OrderItem 中只含有 sku,sellingId,qty,orderId 的 rows
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<F.T5<String, String, Integer, Date, String>> sku_sid_qty_date_aId(Date from, Date to, int filterQuantity) {
        List<Map> rows = JPAs.createQueryMap(new JpqlSelect()
                .select("oi.product.sku as sku, oi.selling.sellingId as sid, oi.quantity as qty, oi.createDate as _date, oi.order.account.id as aid")
                .from("OrderItem oi")
                .where("oi.createDate>=?").param(from)
                .where("oi.createDate<=?").param(to)
                .where("oi.quantity>?").param(filterQuantity)
                .orderBy("oi.createDate DESC")
        ).getResultList();
        List<F.T5<String, String, Integer, Date, String>> t4Rows = new ArrayList<F.T5<String, String, Integer, Date, String>>();
        for(Map row : rows) {
            t4Rows.add(new F.T5<String, String, Integer, Date, String>(
                    row.get("sku").toString(),
                    row.get("sid").toString(),
                    NumberUtils.toInt(row.get("qty").toString()),
                    (Date) row.get("_date"),
                    row.get("aid").toString()));
        }
        return t4Rows;
    }
}
