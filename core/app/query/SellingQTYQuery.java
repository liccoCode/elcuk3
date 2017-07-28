package query;

import helper.DBUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/23/13
 * Time: 10:01 AM
 */
public class SellingQTYQuery {

    public Map<String, Integer> sumQtyWithSKU(Collection<String> skus) {
        return sumQtyWithColumn("product_sku", skus);
    }

    public Map<String, Integer> sumQtyWithSellingId(Collection<String> sellingIds) {
        return sumQtyWithColumn("selling_sellingId", sellingIds);
    }

    private Map<String, Integer> sumQtyWithColumn(String column, Collection<String> columnValues) {
        SqlSelect sql = new SqlSelect()
                .select("sum(qty) as qty", column + " as k")
                .from("SellingQTY")
                .where(column + " IN " + SqlSelect.inlineParam(columnValues))
                .groupBy("k");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, Integer> qtyMap = new HashMap<>();
        for(Map<String, Object> row : rows) {
            if(row.get("k") == null) continue;
            qtyMap.put(row.get("k").toString(), NumberUtils.toInt(row.get("qty").toString()));
        }
        return qtyMap;
    }
}
