package models;

import com.google.gson.annotations.Expose;
import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * 系统内的操作日志的记录;
 * <p/>
 * User: cay
 * Date: 1/16/15
 * Time: 11:50 AM
 */
@Entity
public class ProfitRecord extends Model {
    @Expose
    public String market;
    @Expose
    public String category_id;
    @Expose
    public int year;
    @Expose
    public int month;
    @Expose
    public float qty;
    @Expose
    public float fee;
    @Expose
    public float profit;
    @Expose
    public float profit_rate;
    @Expose
    public float target_qty;
    @Expose
    public float target_fee;
    @Expose
    public float last_month_qty;
    @Expose
    public float last_month_fee;
    @Expose
    public float last_month_profit;
    @Expose
    public float last_month_profit_rate;
    @Expose
    public float last_year_qty;
    @Expose
    public float last_year_fee;
    @Expose
    public float last_year_profit;
    @Expose
    public float last_year_profit_rate;
    @Expose
    public float qty_rate;
    @Expose
    public float fee_rate;
    @Expose
    public float diff_last_month_qty;
    @Expose
    public float diff_last_month_fee;
    @Expose
    public float diff_last_month_profit;
    @Expose
    public float diff_last_month_profit_rate;
    @Expose
    public float diff_last_year_qty;
    @Expose
    public float diff_last_year_fee;
    @Expose
    public float diff_last_year_profit;
    @Expose
    public float diff_last_year_profit_rate;
    @Expose
    public float theoretical_qty;
    @Expose
    public float theoretical_profit;
    @Expose
    public float theoretical_profit_rate;
}
