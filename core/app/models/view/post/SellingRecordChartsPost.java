package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.market.SellingRecord;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
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
public class SellingRecordChartsPost extends Post<HighChart> {
    private static final long serialVersionUID = -4430976832961134222L;

    public SellingRecordChartsPost() {
        this.lineType = Series.LINE;
    }

    public SellingRecordChartsPost(String lineType) {
        this.lineType = lineType;
    }

    public Date from = DateTime.now().minusMonths(1).toDate();
    public Date to = new Date();

    public String market;

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
        sql.select("sum(sr.sales) as sales", "sum(sr.units) as units", "sum(sr.income) income",
                "sum(sr.profit) profit", "sum(sr.amzFee) amzFee", "sum(sr.fbaFee) fbaFee",
                "sum(sr.procureNumberSum) procureNumberSum", "sum(sr.procureCost) procureCost",
                "sum(sr.expressKilogram) expressKilogram", "sum(sr.expressCost) expressCost",
                "sum(sr.airCost) airCost", "sum(sr.seaCost) seaCost",
                "avg(sr.salePrice) salePrice");
        if(StringUtils.isNotBlank(this.market)) {
            sql.where("sr.market=?").param(M.val(this.market).name());
        }
        this.whereLineType(sql);
        // from,to 会再 whereLineType 中改变
        sql.from("SellingRecord sr")
                .where("sr.date>=?").param(Dates.morning(this.from))
                .where("sr.date<=?").params(Dates.night(this.to));
        this.whereType(sql);
        //"l.product_sku,
        return new F.T2<String, List<Object>>(sql.toString(), sql.getParams());
    }

    private boolean isColumn() {
        return "column".equals(this.lineType);
    }

    private void whereLineType(SqlSelect sql) {
        if(StringUtils.isBlank(this.lineType) || "line".equals(this.lineType)) {
            sql.select("sr.date").groupBy("date");

        } else if(isColumn()) {
            this.to = new Date();
            this.from = new DateTime(this.to).minusMonths(12).toDate();
            sql.select("date_format(sr.date, '%Y-%m-01') as date")
                    .groupBy("date_format(sr.date, '%Y-%m')");
        }
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
        HighChart chart;
        if(isColumn()) chart = new HighChart(Series.COLUMN);
        else chart = new HighChart(Series.LINE);
        // 将各自曲线的计算分别打散到各自的方法中, 虽然便利多次, 方便权限控制
        // 销量方面
        this.salePrice(chart, rows);
        this.salesSeries(chart, rows);
        this.unitsSeries(chart, rows);
        // 利润方面
        this.profitSeries(chart, rows);
        this.costProfitRatioSeries(chart, rows);
        this.saleProfitRatioSeries(chart, rows);
        this.incomeSeries(chart, rows);
        // 成本方面
        this.shipCostSeries(chart, rows);
        this.airCost(chart, rows);
        this.expressCost(chart, rows);
        this.seaCost(chart, rows);
        this.procureCostSeries(chart, rows);
        // Amazon fee 方面
        this.amzFeeSeries(chart, rows);
        this.amzFbaFeeSeries(chart, rows);
        this.amzFeeRatioSeries(chart, rows);
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
                float profit = NumberUtils.toFloat(row.get("profit").toString());
                float units = NumberUtils.toInt(row.get("units").toString());
                float shipCost = NumberUtils.toFloat(row.get("expressCost").toString()) * units;
                float procureCost = NumberUtils.toFloat(row.get("procureCost").toString()) * units;
                highChart.series("成本利润率").yAxis(1)
                        .add(date, (shipCost + procureCost == 0) ? 0 : (profit / (shipCost + procureCost)));
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
                float profit = NumberUtils.toFloat(row.get("profit").toString());
                float sales = NumberUtils.toFloat(row.get("sales").toString());
                highChart.series("销售利润率").yAxis(1).add(date, (sales == 0) ? 0 : (profit / sales));
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
     * Amazon 收费比率
     */
    private HighChart amzFeeRatioSeries(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                float sales = NumberUtils.toFloat(row.get("sales").toString());
                float amzFee = NumberUtils.toFloat(row.get("amzFee").toString());
                highChart.series("Amazon 收费比率").yAxis(1).add(date, sales == 0 ? 0 : (amzFee / sales));
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
