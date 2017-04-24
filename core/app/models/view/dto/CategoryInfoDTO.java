package models.view.dto;

import jobs.categoryInfo.CategoryInfoFetchJob;
import models.product.Product;
import play.cache.Cache;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-4-2
 * Time: AM11:28
 */
public class CategoryInfoDTO implements Serializable {

    private static final long serialVersionUID = -5856478468681674167L;

    public String sku;

    /**
     * 总销量
     */
    public long total = 0;

    /**
     * 生命周期
     */
    public Product.L productState;

    /**
     * 本月销量
     */
    public int day30 = 0;

    /**
     * 销售等级
     */
    public Product.E salesLevel;

    /**
     * SKU 利润
     */
    public float profit = 0;

    /**
     * 利润率
     */
    public float profitMargins = 0;

    /**
     * 上周销售额
     */
    public float lastWeekSales = 0;

    /**
     * 上上周销售额
     */
    public float last2WeekSales = 0;

    /**
     * 上周销量
     */
    public int lastWeekVolume = 0;

    /**
     * 上上周销量
     */
    public int last2WeekVolume = 0;

    public CategoryInfoDTO() {
    }

    public CategoryInfoDTO(Product product) {
        this.sku = product.sku;
        this.productState = product.productState;
        this.salesLevel = product.salesLevel;
    }

    /**
     * 数据查询
     *
     * @param categoryId
     * @return
     */
    public static List<CategoryInfoDTO> query(String categoryId) {
        Map<String, List<CategoryInfoDTO>> dtoMap = Cache.get(CategoryInfoFetchJob.CategoryInfo_Cache, Map.class);
        if(dtoMap == null || dtoMap.size() == 0) {
            if(!CategoryInfoFetchJob.isRnning()) {
                new CategoryInfoFetchJob().now();
            }
            return null;
        }
        return dtoMap.get(categoryId);
    }

    /**
     * 计算 Category 利润率
     *
     * @param dtos
     * @return
     */
    public static float categoryProfit(List<CategoryInfoDTO> dtos) {
        float profit = 0;
        if(dtos != null) {
            for(CategoryInfoDTO dto : dtos) {
                profit += dto.profit;
            }
        }
        return profit;
    }
}
