package query;

import ext.ProcuresHelper;
import helper.Caches;
import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.procure.Shipment;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import play.cache.Cache;
import play.db.helper.SqlSelect;
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

    /**
     * 运输准时到货率统计
     */
    public static HighChart arrivalRateLine(final int year, final Shipment.T shipType, final String countType) {
        String key = Caches.Q.cacheKey(year, shipType, countType.toUpperCase(), "ArrivalRate");
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = String.format("[%s]年度[%s][%s]准时到货率", year, shipType, countType.toUpperCase());
            lineChart.series(rateLine(year, shipType, countType));
            Cache.delete(key);
            Cache.add(key, lineChart, "4h");
        }
        return lineChart;
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

    public static Series.Line rateLine(int year, Shipment.T shipType, String countType) {
        Series.Line line = new Series.Line(String.format("%s年度%s", year, shipType.label()));
        line.color = "#49A4C6";
        for(int i = 1; i <= 12; i++) {
            //分别计算每个月份的到货率情况
            Date from = Dates.getMonthFirst(year, i);
            Date to = Dates.getMonthLast(year, i);

            //准时
            SqlSelect sql = buildSqlHeader(year, shipType, countType, i);
            sql.andWhere("pro.planArrivDate=sp.receiptDate");
            Object result = DBUtils.row(sql.toString(), sql.getParams().toArray()).get("qty");
            float onTime = (result == null ? 0 : NumberUtils.toFloat(result.toString()));

            //提前
            sql = buildSqlHeader(year, shipType, countType, i);
            sql.andWhere("pro.planArrivDate>sp.receiptDate");
            result = DBUtils.row(sql.toString(), sql.getParams().toArray()).get("qty");
            float early = (result == null ? 0 : NumberUtils.toFloat(result.toString()));

            //超时
            sql = buildSqlHeader(year, shipType, countType, i);
            sql.andWhere("pro.planArrivDate<sp.receiptDate");
            result = DBUtils.row(sql.toString(), sql.getParams().toArray()).get("qty");
            float timeOut = (result == null ? 0 : NumberUtils.toFloat(result.toString()));

            float sum = onTime + early + timeOut;
            float rate = sum == 0 ? 0 : (((onTime + early) / (sum)) * 100);

            line.add(rate, i + "月<br/>准时抵达数量: " + onTime + "<br/>提前抵达数量: " + early + "<br/>超时抵达数量: " + timeOut +
                    "<br/>合计: " + sum + "<br/>准时到达率: " + String.format("%.2f", rate) + "%");
        }
        return line;
    }

    /**
     * 准备查询sql语句
     *
     * @return
     */
    private static SqlSelect buildSqlHeader(int year, Shipment.T shipType, String countType, int month) {
        Date from = Dates.getMonthFirst(year, month);
        Date to = Dates.getMonthLast(year, month);
        SqlSelect sql = new SqlSelect().select("COUNT(pro.qty) qty").from("Procureunit pro").leftJoin("ShipItem " +
                "si ON pro.id=si.unit_id").leftJoin("Shipment sp ON si.shipment_id=sp.id").where("sp.type=?")
                .param(shipType.toString());
        if(StringUtils.equals(countType, "receiptDate")) {
            sql.andWhere("sp.receiptDate>=?").param(from).where("sp.receiptDate<=?").param(to);
        } else {
            sql.andWhere("sp.planBeginDate>=?").param(from).where("sp.planBeginDate<=?").param(to);
        }
        return sql;
    }
}
