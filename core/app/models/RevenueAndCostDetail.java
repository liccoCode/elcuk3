package models;

import com.google.gson.annotations.Expose;
import play.db.jpa.GenericModel;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.sql.Date;

/**
 * 主营业务收入与成本 DTO
 * User: mac
 * Date: 15-5-22
 * Time: PM3:02
 */
@javax.persistence.Entity
public class RevenueAndCostDetail extends GenericModel {

    @Id
    @GeneratedValue
    @Expose
    public Long id;

    /**
     * 品线
     */
    public String category;

    /**
     * 月销量
     */
    public int monthly_qty;

    /**
     * 本年累计销量
     */
    public int annual_qty;

    /**
     * 去年同期销量
     */
    public int same_period_qty;

    /**
     * 月销售额
     */
    public float monthly_sales;

    /**
     * 本年累计销售额
     */
    public float annual_sales;

    /**
     * 去年同期销售额
     */
    public float same_period_sales;

    /**
     * 月成本
     */
    public float monthly_cost;

    /**
     * 本年累计成本
     */
    public float annual_cost;

    /**
     * 去年同期成本
     */
    public float same_period_cost;

    /**
     * 创建日期
     */
    public Date create_at;
}
