package models.view.post;

import helper.DBUtils;
import models.market.M;
import models.market.SellingRecord;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;
import play.utils.FastRuntimeException;

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
public class SellingRecordChartsPost extends Post<HighChart> {
    private static final long serialVersionUID = -4430976832961134222L;

    public SellingRecordChartsPost() {
        this.lineType = Series.LINE;
    }

    public SellingRecordChartsPost(String lineType) {
        this.lineType = lineType;
    }

    public Date from = DateTime.now().withTimeAtStartOfDay().minusMonths(1).toDate();
    public Date to = new Date();

    public String market;
    public String categoryId;

    public String lineType = Series.LINE;

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
        SqlSelect sql = new SqlSelect();
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
        sql.select(
                "sr.date",
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
        ).from("SellingRecord sr")
                .where("date>=?").param(this.from)
                .where("date<?").param(new DateTime(this.to).plusDays(1).withTimeAtStartOfDay().toDate())
                .groupBy("sr.date");

        if(StringUtils.isNotBlank(this.market)) {
            sql.where("sr.market=?").param(M.val(this.market).name());
        }

        if(StringUtils.isNotBlank(this.categoryId)) {
            sql.where(String.format("sr.selling_sellingId like '%s%%'", this.categoryId));
        }

        return new F.T2<String, List<Object>>(sql.toString(), sql.getParams());
    }

    private boolean isColumn() {
        return "column".equals(this.lineType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HighChart> query() {
        F.T2<String, List<Object>> t2 = this.params();
        List<Map<String, Object>> rows = DBUtils.rows(t2._1, t2._2.toArray());
        HighChart chart;
        if(isColumn()) throw new FastRuntimeException("暂时还不支持 Column 柱状图.");
        else chart = new HighChart(Series.LINE);
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
//        this.salePrice(chart, rows);
        this.salesSeries(chart, rows);
        this.unitsSeries(chart, rows);
        this.amzFeeSeries(chart, rows);
        this.amzFbaFeeSeries(chart, rows);
        this.incomeSeries(chart, rows);

        // 利润
        this.profitSeries(chart, rows);
        this.costProfitRatioSeries(chart, rows);
        this.saleProfitRatioSeries(chart, rows);

        // 成本
        this.procureCostSeries(chart, rows);
        this.airCost(chart, rows);
        this.expressCost(chart, rows);
        this.seaCost(chart, rows);
        this.shipCostSeries(chart, rows);
        this.dutyAndVatSeries(chart, rows);

        return Arrays.asList(chart);
    }

    public HighChart salePrice(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("销售价格").add(date, NumberUtils.toFloat(row.get("salePrice").toString()));
            }
        });
    }

    /**
     * 销售额曲线
     *
     * @param highChart
     * @param rows
     * @return
     */
    private HighChart salesSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("销售额").add(date, NumberUtils.toFloat(row.get("sales").toString()));
            }
        });
    }

    /**
     * 销量曲线
     *
     * @param highChart
     * @param rows
     * @return
     */
    private HighChart unitsSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("销量").yAxis(1).add(date, NumberUtils.toFloat(row.get("units").toString()));
            }
        });
    }

    /**
     * 利润曲线
     */
    private HighChart profitSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("利润").add(date, NumberUtils.toFloat(row.get("profit").toString()));
            }
        });
    }

    /**
     * 成本利润率, (SellingRecordCaculateJob 中也有对应的计算)
     * 成本利润率 = 利润 / (采购成本 + 运输成本)
     */
    private HighChart costProfitRatioSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("成本利润率").yAxis(1)
                        .add(date, NumberUtils.toFloat(row.get("costProfitRatio").toString()));
            }
        });
    }

    /**
     * 销售利润率, (SellingRecordCaculateJob 中也有对应的计算)
     * 销售利润率 = 利润 / 销售额
     */
    private HighChart saleProfitRatioSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("销售利润率").yAxis(1)
                        .add(date, NumberUtils.toFloat(row.get("saleProfitRatio").toString()));
            }
        });
    }

    /**
     * 收入曲线
     */
    private HighChart incomeSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("实际收入").add(date, NumberUtils.toFloat(row.get("income").toString()));
            }
        });
    }

    /**
     * 采购成本曲线
     */
    private HighChart procureCostSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("采购成本")
                        .add(date, NumberUtils.toFloat(row.get("procureCost").toString()));
            }
        });
    }

    /**
     * 运输成本曲线
     */
    private HighChart shipCostSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                float expressCost = NumberUtils.toFloat(row.get("expressCost").toString()); // 33%
                float seaCost = NumberUtils.toFloat(row.get("seaCost").toString()); // 33%
                float airCost = NumberUtils.toFloat(row.get("airCost").toString()); // 33%
                highChart.series("运输成本").add(date, new SellingRecord(expressCost, seaCost, airCost).mergeToShipCost());
            }
        });
    }

    /**
     * 快递运输成本
     */
    private HighChart expressCost(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("快递成本").add(date, NumberUtils.toFloat(row.get("expressCost").toString()));
            }
        });
    }

    /**
     * 空运运输成本
     */
    private HighChart seaCost(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("海运成本").add(date, NumberUtils.toFloat(row.get("seaCost").toString()));
            }
        });
    }

    /**
     * 海运运输成本
     */
    private HighChart airCost(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("空运成本").add(date, NumberUtils.toFloat(row.get("airCost").toString()));
            }
        });
    }

    private HighChart dutyAndVatSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("关税VAT").add(date, NumberUtils.toFloat(row.get("dutyAndVAT").toString()));
            }
        });
    }

    /**
     * Amazon 收费曲线
     */
    private HighChart amzFeeSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                float amzFee = NumberUtils.toFloat(row.get("amzFee").toString());
                highChart.series("Amazon 收费").add(date, amzFee);
            }
        });
    }


    /**
     * Amazon FBA 收费曲线
     */
    private HighChart amzFbaFeeSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                float fbaFee = NumberUtils.toFloat(row.get("fbaFee").toString());
                highChart.series("FBA 收费").yAxis(1).add(date, fbaFee);
            }
        });
    }

    private HighChart rows(HighChart highChart, List<Map<String, Object>> rows, Callback callback) {
        for(Map<String, Object> row : rows) {
            Object key = row.get("date");
            Date date;
            if(key.getClass().equals(String.class)) {
                date = DateTime.parse(key.toString()).toDate();
            } else {
                date = (Date) key;
            }
            callback.each(highChart, date, row);
        }
        return highChart;
    }

    interface Callback {
        public void each(HighChart highChart, Date date, Map<String, Object> row);
    }
}
