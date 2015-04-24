package models.view.dto;

import java.util.HashMap;

/**
 * SKU 月度日均销量报表数据对象
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-4-23
 * Time: PM2:54
 */
public class DailySalesReportsDTO {
    public String category;
    public String sku;
    public String market;
    public HashMap<Integer, Float> sales = new HashMap<Integer, Float>();

    public DailySalesReportsDTO() {
    }

    public DailySalesReportsDTO(String category, String sku, String market) {
        this.category = category;
        this.sku = sku;
        this.market = market;
    }
}
