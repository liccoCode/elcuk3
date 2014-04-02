package models.view.dto;

import models.product.Product;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-4-2
 * Time: AM11:28
 */
public class CategoryInfoDTO implements Serializable {

    public String sku;

    /**
     * 总销量
     */
    public int total = 0;

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
     * 产品线总利润
     */
    public float categoryProfit;

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
    public float last2weekSales = 0;

    /**
     * 上周销量
     */
    public int lastWeekVolume = 0;

    /**
     * 上上周销量
     */
    public int last2WeekVolume = 0;
}
