package query;

import helper.DBUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/23/13
 * Time: 10:01 AM
 */
public class SellingQTYQuery {

    public int sumQtyWithSKU(String sku) {
        return sumQtyWithColumn("product_sku", sku);
    }

    public int sumQtyWithSellingId(String sellingId) {
        return sumQtyWithColumn("selling_sellingId", sellingId);
    }

    private int sumQtyWithColumn(String column, String columnValue) {
        SqlSelect sql = new SqlSelect()
                .select("sum(qty) as qty")
                .from("SellingQTY")
                .where(column + "=?").param(columnValue);
        Object qtyObj = DBUtils.row(sql.toString(), sql.getParams().toArray()).get("qty");
        return NumberUtils.toInt(
                qtyObj == null ? "0" : qtyObj.toString()
        );
    }
}
