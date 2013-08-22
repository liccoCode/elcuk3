package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.view.dto.HighChart;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
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
public class SellingRecordLinePost extends Post<HighChart> {
    private static final long serialVersionUID = -4430976832961134222L;

    public SellingRecordLinePost() {
    }

    public Date from = DateTime.now().minusMonths(1).toDate();
    public Date to = new Date();

    public String market;

    /**
     * 搜索字词
     */
    public String val;

    /**
     * selling, sku, category 三个种类
     */
    public String type = "selling";

    @Override
    public F.T2<String, List<Object>> params() {
        SqlSelect sql = new SqlSelect()
                .select("sr.date", "sum(sr.sales) as sales", "sum(sr.units) as units",
                        "sum(sr.procureCost) procureCost", "sum(sr.procureNumberSum) procureNumberSum",
                        "sum(sr.shipCost) shipCost", "sum(sr.shipNumberSum) shipNumberSum",
                        "sum(sr.income) income", "sum(sr.profit) profit")
                .from("SellingRecord sr")
                .where("sr.date>=?").param(Dates.morning(this.from))
                .where("sr.date<=?").params(Dates.night(this.to))
                .groupBy("date");
        if(StringUtils.isNotBlank(this.market)) {
            sql.where("sr.market=?").param(M.val(this.market).name());
        }
        this.whereType(sql);

        //"l.product_sku,
        return new F.T2<String, List<Object>>(sql.toString(), sql.getParams());
    }

    private void whereType(SqlSelect sql) {
        if("selling".equals(this.type)) {
            if(StringUtils.isNotBlank(this.val)) {
                sql.select("sr.selling_sellingId as sellingId")
                        .where("sr.selling_sellingId=?").param(this.val);
            }
        } else if("sku".equals(this.type)) {
            if(StringUtils.isNotBlank(this.val)) {
                sql.select("l.product_sku as sku")
                        .leftJoin("Selling s ON s.sellingId=sr.selling_sellingId")
                        .leftJoin("Listing l ON l.listingId=s.listing_listingId")
                        .where("l.product_sku=?").param(this.val);
            }
        } else if("category".equals(this.type)) {
            if(StringUtils.isNotBlank(this.val)) {
                sql.select("p.category_categoryId as categoryId")
                        .leftJoin("Selling s ON s.sellingId=sr.selling_sellingId")
                        .leftJoin("Listing l ON l.listingId=s.listing_listingId")
                        .leftJoin("Product p ON p.sku=l.product_sku")
                        .where("p.category_categoryId=?").param(this.val);
            }
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<HighChart> query() {
        F.T2<String, List<Object>> t2 = this.params();
        List<Map<String, Object>> rows = DBUtils.rows(t2._1, t2._2.toArray());
        HighChart chart = new HighChart();
        // 将各自曲线的计算分别打散到各自的方法中, 虽然便利多次, 方便权限控制
        this.salesLine(chart, rows);
        this.unitsLine(chart, rows);
        this.profitLine(chart, rows);
        this.costProfitRatioLine(chart, rows);
        this.saleProfitRatioLine(chart, rows);
        this.incomeLine(chart, rows);
        this.shipCostLine(chart, rows);
        this.procureCostLine(chart, rows);
        this.amazonFeeLine(chart, rows);
        return Arrays.asList(chart);
    }

    /**
     * 销售额曲线
     *
     * @param highChart
     * @param rows
     * @return
     */
    private HighChart salesLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            highChart.line("销售额").add(date, NumberUtils.toFloat(row.get("sales").toString()));
        }
        return highChart;
    }

    /**
     * 销量曲线
     *
     * @param highChart
     * @param rows
     * @return
     */
    private HighChart unitsLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            highChart.line("销量").add(date, NumberUtils.toFloat(row.get("units").toString()));
        }
        return highChart;
    }

    private HighChart profitLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            highChart.line("利润").add(date, NumberUtils.toFloat(row.get("profit").toString()));
        }
        return highChart;
    }

    /**
     * 成本利润率, (SellingRecordCaculateJob 中也有对应的计算)
     * 成本利润率 = 利润 / (采购成本 + 运输成本)
     *
     * @return
     */
    private HighChart costProfitRatioLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            float profit = NumberUtils.toFloat(row.get("profit").toString());
            float units = NumberUtils.toInt(row.get("units").toString());
            float shipCost = NumberUtils.toFloat(row.get("shipCost").toString()) * units;
            float procureCost = NumberUtils.toFloat(row.get("procureCost").toString()) * units;
            highChart.line("成本利润率").yAxis(1)
                    .add(date, (shipCost + procureCost == 0) ? 0 : (profit / (shipCost + procureCost)));
        }
        return highChart;
    }

    /**
     * 销售利润率, (SellingRecordCaculateJob 中也有对应的计算)
     * 销售利润率 = 利润 / 销售额
     *
     * @return
     */
    private HighChart saleProfitRatioLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            float profit = NumberUtils.toFloat(row.get("profit").toString());
            float sales = NumberUtils.toInt(row.get("sales").toString());
            highChart.line("销售利润率").yAxis(1).add(date, (sales == 0) ? 0 : (profit / sales));
        }
        return highChart;
    }

    private HighChart incomeLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            highChart.line("实际收入").add(date, NumberUtils.toFloat(row.get("income").toString()));
        }
        return highChart;
    }

    private HighChart procureCostLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            highChart.line("采购成本").add(date, NumberUtils.toFloat(row.get("procureCost").toString()));
        }
        return highChart;
    }

    private HighChart shipCostLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            highChart.line("运输成本").add(date, NumberUtils.toFloat(row.get("shipCost").toString()));
        }
        return highChart;
    }

    private HighChart amazonFeeLine(HighChart highChart, List<Map<String, Object>> rows) {
        for(Map<String, Object> row : rows) {
            Date date = (Date) row.get("date");
            float sales = NumberUtils.toFloat(row.get("sales").toString());
            float income = NumberUtils.toFloat(row.get("income").toString());
            highChart.line("Amazon 收费").add(date, sales - income);
        }
        return highChart;
    }
}
