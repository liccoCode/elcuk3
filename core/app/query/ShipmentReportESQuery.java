package query;

import ext.ProcuresHelper;
import helper.Caches;
import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.apache.commons.lang3.StringUtils;
import play.cache.Cache;
import services.MetricShipmentService;

import java.util.Date;

/**
 * 运输报表查询类
 * <p/>
 * User: mac
 * Date: 14-6-4
 * Time: PM12:06
 */
public class ShipmentReportESQuery {

    /**
     * 运输费用统计柱状图(根据运输方式)
     */
    public static HighChart shipFeeByTypeColumn(final int year, final int month) {
        String key = Caches.Q.cacheKey(year, month, "shipFeeByType");
        HighChart columnChart = Cache.get(key, HighChart.class);
        if(columnChart != null) return columnChart;
        synchronized(key.intern()) {
            columnChart = new HighChart(Series.COLUMN);
            columnChart.title = String.format("[%s]年度[%s]月份运输费用统计(USD)", year, month);
            for(Shipment.T t : Shipment.T.values()) {
                columnChart.series(shipColum(year, month, t, "shipFee"));
            }
            Cache.delete(key);
            Cache.add(key, columnChart, "4h");
        }
        return columnChart;
    }


    /**
     * 运输费用统计饼图(根据市场)
     */
    public static HighChart shipFeeByMarketPie(final int year, final int month, Shipment.T type) {
        String key = Caches.Q.cacheKey(year, month, type, "shipFeeByMarket");
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;
        synchronized(key.intern()) {
            pieChart = new HighChart(Series.PIE);
            pieChart.title = String.format("[%s]年度[%s]月份[%s]各市场运输费用统计(USD)", year, month, type);
            pieChart.series(shipPie(year, month, type, "shipFee"));
            Cache.delete(key);
            Cache.add(key, pieChart, "4h");
        }
        return pieChart;
    }

    /**
     * 运输重量统计柱状图(根据运输方式)
     */
    public static HighChart shipWeightByTypeColumn(final int year, final int month) {
        String key = Caches.Q.cacheKey(year, month, "shipWeightByType");
        HighChart columnChart = Cache.get(key, HighChart.class);
        if(columnChart != null) return columnChart;
        synchronized(key.intern()) {
            columnChart = new HighChart(Series.COLUMN);
            columnChart.title = String.format("[%s]年度[%s]月份运输重量统计(Kg)", year, month);
            for(Shipment.T t : Shipment.T.values()) {
                columnChart.series(shipColum(year, month, t, "shipWeight"));
            }
            Cache.delete(key);
            Cache.add(key, columnChart, "4h");
        }
        return columnChart;
    }

    /**
     * 运输重量统计饼图(根据市场)
     */
    public static HighChart shipWeightByMarketPie(final int year, final int month, Shipment.T type) {
        String key = Caches.Q.cacheKey(year, month, type, "shipWeightByMarket");
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;
        synchronized(key.intern()) {
            pieChart = new HighChart(Series.PIE);
            pieChart.title = String.format("[%s]年度[%s]月份[%s]各市场运输重量统计(Kg)", year, month, type);
            pieChart.series(shipPie(year, month, type, "shipWeight"));
            Cache.delete(key);
            Cache.add(key, pieChart, "4h");
        }
        return pieChart;
    }

    public static Series.Column shipColum(int year, int month, Shipment.T type, String flag) {
        Date from = Dates.getMonthFirst(year, month);
        Date to = Dates.getMonthLast(year, month);
        Series.Column column = new Series.Column(type.name());
        column.color = ProcuresHelper.rgb(type);
        float result = 0f;
        MetricShipmentService mes = new MetricShipmentService(from, to, type);
        if(StringUtils.equals(flag, "shipFee")) {
            result = mes.countShipFee();
        } else {
            result = mes.countShipWeight();
        }
        column.add(result, type.name());
        return column;
    }


    public static Series.Pie shipPie(int year, int month, Shipment.T type, String flag) {
        Date from = Dates.getMonthFirst(year, month);
        Date to = Dates.getMonthLast(year, month);
        Series.Pie pie = new Series.Pie(String.format("[%s]年度[%s]月份[%s]各市场运输重量统计(Kg)", year, month, type));
        float result = 0f;
        for(M m : M.values()) {
            MetricShipmentService mes = new MetricShipmentService(from, to, type, m);
            if(StringUtils.equals(flag, "shipFee")) {
                result = mes.countShipFee();
            } else {
                result = mes.countShipWeight();
            }
            if(result > 0) pie.add(result, m.name());
        }
        return pie;
    }
}
