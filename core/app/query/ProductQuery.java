package query;

import helper.DBUtils;
import models.product.Product;
import play.db.helper.SqlSelect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/23/13
 * Time: 9:40 AM
 */
public class ProductQuery {

    /**
     * 加载 Product 的所有 SKU
     *
     * @return
     */
    public List<String> skus() {
        SqlSelect sql = new SqlSelect().select("sku").from("Product");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        List<String> skus = new ArrayList<String>();

        for(Map<String, Object> row : rows) {
            skus.add(row.get("sku").toString());
        }

        return skus;
    }

    /**
     * 加载 Product 所有的 SKU 和 State
     *
     * @return
     */
    public Map<String, Product.S> skuAndStates() {
        Map<String, Product.S> products = new HashMap<String, Product.S>();
        SqlSelect sql = new SqlSelect().select("sku", "state").from("Product");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        for(Map<String, Object> row : rows) {
            Product.S state;
            try {
                state = Product.S.valueOf(row.get("state").toString());
            } catch(Exception e) {
                state = Product.S.NEW;
            }
            products.put(row.get("sku").toString(), state);
        }
        return products;
    }
}
