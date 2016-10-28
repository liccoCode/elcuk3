package models.view.post;

import helper.DBUtils;
import models.view.SellingRecordsCharts;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 与 Line 曲线的 Post Form 给分开写逻辑.
 * User: wyatt
 * Date: 11/15/13
 * Time: 3:51 PM
 */
public class SellingRecordColumnChartPost extends Post<HighChart> {

    private SellingRecordsCharts drawer = new SellingRecordsCharts();
    private static final long serialVersionUID = 9099644401516079900L;

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
        return org.apache.commons.lang.StringUtils.isBlank(this.val);
    }

    private SqlSelect sumLine() {
        return new SqlSelect().select(
                "date_format(sr.date, '%Y-%m') _date",
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

    private SqlSelect singleLine() {
        return new SqlSelect().select(
                "date_format(sr.date, '%Y-%m') _date",
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
        boolean isSum = StringUtils.isBlank(this.val);
        SqlSelect sql = isSum ? sumLine() : singleLine();

        DateTime now = DateTime.now().withTimeAtStartOfDay();

        sql.from("SellingRecord sr")
                .where("date_format(sr.date, '%Y-%m')>=?").param(now.minusMonths(12).toString("yyyy-MM"))
                .where("date_format(sr.date, '%Y-%m')<=?").param(now.toString("yyyy-MM"))
                .groupBy("_date");

        // 仅仅当 isSum 统计的时候生效
        if(isSum && StringUtils.isNotBlank(this.categoryId)) {
            sql.where(String.format("sr.selling_sellingId like '%s%%'", this.categoryId));
        }

        if(!isSum && StringUtils.contains(this.val, "|")) {
            // 如果是 SellingId
            sql.where("sr.selling_sellingId=?").param(this.val.trim());
        } else if(!isSum) {
            // 如果是 SKU;  将 Selling 处理为 SKU
            String sid = StringUtils.split(this.val, "|")[0];
            String sku = StringUtils.split(sid, ",")[0];
            sql.where(String.format("sr.selling_sellingId like '%s%%'", sku));
        }
        return new F.T2<>(sql.toString(), sql.getParams());
    }


    @Override
    public List<HighChart> query() {
        F.T2<String, List<Object>> t2 = this.params();
        List<Map<String, Object>> rows = DBUtils.rows(t2._1, t2._2.toArray());
        HighChart chart = new HighChart(Series.COLUMN);

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
