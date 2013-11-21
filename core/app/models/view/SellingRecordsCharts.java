package models.view;

import models.market.SellingRecord;
import models.view.highchart.HighChart;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用于生成 SellingRecords 的曲线图的
 * User: wyatt
 * Date: 11/21/13
 * Time: 6:04 PM
 */
public class SellingRecordsCharts {

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
    public HighChart salesSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart unitsSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart profitSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart costProfitRatioSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart saleProfitRatioSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart incomeSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart procureCostSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart shipCostSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart expressCost(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart seaCost(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart airCost(HighChart highChart, List<Map<String, Object>> rows) {
        return rows(highChart, rows, new Callback() {
            @Override
            public void each(HighChart highChart, Date date, Map<String, Object> row) {
                highChart.series("空运成本").add(date, NumberUtils.toFloat(row.get("airCost").toString()));
            }
        });
    }

    public HighChart dutyAndVatSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart amzFeeSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
    public HighChart amzFbaFeeSeries(HighChart highChart, List<Map<String, Object>> rows) {
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
            Object key = row.get("_date");
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
