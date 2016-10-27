package models.view.post;

import helper.DBUtils;
import models.market.M;
import models.view.SellingRecordsCharts;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 通过搜索 form 生成页面 HighChart 曲线
 * User: wyatt
 * Date: 8/22/13
 * Time: 10:13 AM
 */
public class SellingRecordLineChartPost extends Post<HighChart> {
    private static final long serialVersionUID = -4430976832961134222L;
    private SellingRecordsCharts drawer = new SellingRecordsCharts();

    public SellingRecordLineChartPost() {
    }

    public Date from = DateTime.now().withTimeAtStartOfDay().minusMonths(1).toDate();
    public Date to = new Date();

    public String market;
    public String categoryId;

    /**
     * 搜索字词
     */
    public String val;

    /**
     * selling, sku, category 三个种类
     */
    public String type = "selling";

    public boolean isSum() {
        return StringUtils.isBlank(this.val);
    }

    /**
     * 汇总曲线
     */
    private SqlSelect sumLine() {
        return new SqlSelect().select(
                "sr.date _date",
                // 费用
                "sum(sr.sales) as sales", "sum(sr.units) as units", "sum(sr.amzFee * sr.units) amzFee",
                "sum(sr.fbaFee * sr.units) fbaFee", "sum(sr.income * sr.units) income",
                // 利润
                "sum(sr.profit * sr.units) profit", "avg(sr.costProfitRatio) costProfitRatio",
                "avg(sr.saleProfitRatio) saleProfitRatio",
                // 成本
                "sum(sr.procureCost * sr.units) procureCost", "sum(sr.airCost * sr.units) airCost",
                "sum(sr.expressCost * sr.units) expressCost", "sum(sr.seaCost * sr.units) seaCost",
                "sum(sr.dutyAndVAT * sr.units) dutyAndVAT"
        );
    }

    /**
     * 单个 Selling 曲线
     */
    private SqlSelect singleLine() {
        return new SqlSelect().select(
                "sr.date _date",
                "sr.salePrice",
                // 费用
                "sr.sales sales", "sr.units units", "sr.amzFee amzFee",
                "sr.fbaFee fbaFee", "sr.income income",
                // 利润
                "sr.profit profit", "sr.costProfitRatio costProfitRatio", "sr.saleProfitRatio saleProfitRatio",
                // 成本
                "sr.procureCost procureCost", "sr.airCost airCost", "sr.expressCost expressCost", "sr.seaCost seaCost",
                "sr.dutyAndVAT dutyAndVAT"
        );
    }

    @Override
    public F.T2<String, List<Object>> params() {
        SqlSelect sql = isSum() ? sumLine() : singleLine();
        // ------- 汇总曲线 ----------
        /**
         * 0. 销售价格 (单个才出现)
         * --- 费用 ----
         * 1. 销售额
         * 2. 销量
         * 4. amazon 收费
         * 5. fba 收费
         * 6. 实际收入
         * --- 利润 ---
         * 7. 利润
         * 8. 成本利润率
         * 9. 销售利润率
         * --- 成本 ---
         * 10. 采购成本
         * 11. 空运成本
         * 12. 快递成本
         * 13. 海运成本
         * 14. 关税 VAT
         */
        sql.from("SellingRecord sr")
                .where("sr.date>=?").param(this.from)
                .where("sr.date<?").param(new DateTime(this.to).plusDays(1).withTimeAtStartOfDay().toDate())
                .groupBy("_date");

        if(StringUtils.isNotBlank(this.market)) {
            sql.where("sr.market=?").param(M.val(this.market).name());
        }

        // 仅仅当 isSum 统计的时候生效
        if(isSum() && StringUtils.isNotBlank(this.categoryId)) {
            sql.where(String.format("sr.selling_sellingId like '%s%%'", this.categoryId));
        }

        if(!isSum() && StringUtils.contains(this.val, "|")) {
            // 如果是 SellingId
            sql.where("sr.selling_sellingId=?").param(this.val.trim());
        } else if(!isSum()) {
            // 如果是 SKU;  将 Selling 处理为 SKU
            String sid = StringUtils.split(this.val, "|")[0];
            String sku = StringUtils.split(sid, ",")[0];
            sql.where(String.format("sr.selling_sellingId like '%s%%'", sku));
        }

        return new F.T2<>(sql.toString(), sql.getParams());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HighChart> query() {
        F.T2<String, List<Object>> t2 = this.params();
        List<Map<String, Object>> rows = DBUtils.rows(t2._1, t2._2.toArray());
        HighChart chart = new HighChart(Series.LINE);
        // 将各自曲线的计算分别打散到各自的方法中, 虽然便利多次, 方便权限控制

        /**
         * 0. 销售价格 (单个才出现)
         * --- 费用 ----
         * 1. 销售额
         * 2. 销量
         * 4. amazon 收费
         * 5. fba 收费
         * 6. 实际收入
         * --- 利润 ---
         * 7. 利润
         * 8. 成本利润率
         * 9. 销售利润率
         * --- 成本 ---
         * 10. 采购成本
         * 11. 空运成本
         * 12. 快递成本
         * 13. 海运成本
         * 14. 关税 VAT
         */

        // 费用
        if(!isSum()) {
            drawer.salePrice(chart, rows);
        }
        drawer.salesSeries(chart, rows);
        drawer.unitsSeries(chart, rows);
        drawer.amzFeeSeries(chart, rows);
        drawer.amzFbaFeeSeries(chart, rows);
        drawer.incomeSeries(chart, rows);

        // 利润
        drawer.profitSeries(chart, rows);
        drawer.costProfitRatioSeries(chart, rows);
        drawer.saleProfitRatioSeries(chart, rows);

        // 成本
        drawer.procureCostSeries(chart, rows);
        drawer.airCost(chart, rows);
        drawer.expressCost(chart, rows);
        drawer.seaCost(chart, rows);
        drawer.shipCostSeries(chart, rows);
        drawer.dutyAndVatSeries(chart, rows);

        return Arrays.asList(chart);
    }

}
