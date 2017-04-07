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
    public static HighChart shipFeeByTypeColumn(final Date from, final Date to) {
        String key = Caches.Q.cacheKey(from, to, "shipFeeByType");
        HighChart columnChart = Cache.get(key, HighChart.class);
        if(columnChart != null) return columnChart;
        synchronized(key.intern()) {
            columnChart = new HighChart(Series.COLUMN);
            columnChart.title = String
                    .format("From:[%s] To:[%s]运输费用统计(USD)", Dates.date2Date(from), Dates.date2Date(to));
            for(Shipment.T t : Shipment.T.values()) {
                columnChart.series(shipColum(from, to, t.name(), "shipFee"));
            }
            columnChart.series(shipColum(from, to, "dedicated", "shipFee"));
            Cache.delete(key);
            Cache.add(key, columnChart, "4h");
        }
        return columnChart;
    }


    /**
     * 运输费用统计饼图(根据市场)
     */
    public static HighChart shipFeeByMarketPie(final Date from, final Date to, String type) {
        String key = Caches.Q.cacheKey(from, to, type, "shipFeeByMarket");
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;
        synchronized(key.intern()) {
            pieChart = new HighChart(Series.PIE);
            pieChart.title = String
                    .format("From:[%s] To:[%s] [%s]各市场运输费用统计(USD)", Dates.date2Date(from), Dates.date2Date(to), type);
            pieChart.series(shipPie(from, to, type, "shipFee"));
            Cache.delete(key);
            Cache.add(key, pieChart, "4h");
        }
        return pieChart;
    }

    /**
     * 运输费用统计饼图(根据市场)
     */
    public static HighChart shipFeeByMarketPieForDedicated(final Date from, final Date to, String type) {
        String key = Caches.Q.cacheKey(from, to, type, "shipFeeByMarket");
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;
        synchronized(key.intern()) {
            pieChart = new HighChart(Series.PIE);
            pieChart.title = String
                    .format("From:[%s] To:[%s] [%s]各市场运输费用统计(USD)", Dates.date2Date(from), Dates.date2Date(to), type);
            pieChart.series(shipPie(from, to, "dedicated", "shipFee"));
            Cache.delete(key);
            Cache.add(key, pieChart, "4h");
        }
        return pieChart;
    }

    /**
     * 运输重量统计柱状图(根据运输方式)
     */
    public static HighChart shipWeightByTypeColumn(final Date from, final Date to) {
        String key = Caches.Q.cacheKey(from, to, "shipWeightByType");
        HighChart columnChart = Cache.get(key, HighChart.class);
        if(columnChart != null) return columnChart;
        synchronized(key.intern()) {
            columnChart = new HighChart(Series.COLUMN);
            columnChart.title = String.format("From:[%s] To:[%s]运输重量统计(Kg)", Dates.date2Date(from), Dates.date2Date(to));
            for(Shipment.T t : Shipment.T.values()) {
                columnChart.series(shipColum(from, to, t.name(), "shipWeight"));
            }
            columnChart.series(shipColum(from, to, "dedicated", "dedicated"));
            Cache.delete(key);
            Cache.add(key, columnChart, "4h");
        }
        return columnChart;
    }

    /**
     * 运输重量统计饼图(根据市场)
     */
    public static HighChart shipWeightByMarketPie(final Date from, final Date to, Shipment.T type) {
        String key = Caches.Q.cacheKey(from, to, type, "shipWeightByMarket");
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;
        synchronized(key.intern()) {
            pieChart = new HighChart(Series.PIE);
            pieChart.title = String
                    .format("From:[%s] To:[%s] [%s]各市场运输重量统计(Kg)", Dates.date2Date(from), Dates.date2Date(to), type);
            pieChart.series(shipPie(from, to, type.name(), "shipWeight"));
            Cache.delete(key);
            Cache.add(key, pieChart, "4h");
        }
        return pieChart;
    }

    /**
     * @param from
     * @param to
     * @return
     */
    public static HighChart shipWeightByMarketPieForDedicated(final Date from, final Date to) {
        String key = Caches.Q.cacheKey(from, to, "DEDICATED", "shipWeightByMarket");
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;
        synchronized(key.intern()) {
            pieChart = new HighChart(Series.PIE);
            pieChart.title = String
                    .format("From:[%s] To:[%s] [%s]各市场运输重量统计(Kg)", Dates.date2Date(from), Dates.date2Date(to), "专线");
            pieChart.series(shipPie(from, to, "dedicated", "shipWeight"));
            Cache.delete(key);
            Cache.add(key, pieChart, "4h");
        }
        return pieChart;
    }

    /**
     * 运输准时到货率统计
     */
    public static HighChart arrivalRateLine(final int year, final Shipment.T shipType, final String countType) {
        String key;
        if(shipType == null) {
            key = Caches.Q.cacheKey(year, countType, "ArrivalRate");
        } else {
            key = Caches.Q.cacheKey(year, shipType, countType, "ArrivalRate");
        }
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = shipType == null ?
                    String.format("[%s]年度[%s]准时到货率", year, countType) :
                    String.format("[%s]年度[%s][%s]准时到货率", year, shipType, countType);
            if(shipType != null) {
                lineChart.series(rateLine(year, shipType, countType));
            } else {
                for(Shipment.T t : Shipment.T.values()) {
                    lineChart.series(rateLine(year, t, countType));
                }
            }
            Cache.delete(key);
            Cache.add(key, lineChart, "4h");
        }
        return lineChart;
    }


    public static Series.Column shipColum(Date from, Date to, String string_type, String flag) {
        from = Dates.morning(from);
        to = Dates.night(to);
        MetricShipmentService mes;
        if(StringUtils.equals(string_type, "dedicated")) {
            Series.Column column = new Series.Column("专线");
            column.color = "#A020F0";
            float result = 0f;
            mes = new MetricShipmentService(from, to, Shipment.T.EXPRESS);
            mes.isDedicated = true;
            if(StringUtils.equals(flag, "shipFee")) {
                result = mes.countShipFee();
            } else {
                result = mes.countShipWeight();
            }
            column.add(result, "dedicated");
            return column;
        } else {
            Shipment.T type = Shipment.T.valueOf(string_type);
            Series.Column column = new Series.Column(type.name());
            column.color = ProcuresHelper.rgb(type);
            float result = 0f;
            mes = new MetricShipmentService(from, to, type);
            if(StringUtils.equals(flag, "shipFee")) {
                result = mes.countShipFee();
            } else {
                result = mes.countShipWeight();
            }
            column.add(result, type.name());
            return column;
        }
    }


    public static Series.Pie shipPie(Date from, Date to, String string_type, String flag) {
        from = Dates.morning(from);
        to = Dates.night(to);
        if(StringUtils.equals(string_type, "dedicated")) {
            Series.Pie pie = new Series.Pie(String.format("From:[%s] To:[%s] [%s]各市场运输重量统计(Kg)", from, to, "专线"));
            float result = 0f;
            for(M m : M.values()) {
                MetricShipmentService mes = new MetricShipmentService(from, to, Shipment.T.EXPRESS, m);
                mes.isDedicated = true;
                if(StringUtils.equals(flag, "shipFee")) {
                    result = mes.countShipFee();
                } else {
                    result = mes.countShipWeight();
                }
                if(result > 0) pie.add(result, "dedicated");
            }
            return pie;
        } else {
            Shipment.T type = Shipment.T.valueOf(string_type);
            Series.Pie pie = new Series.Pie(String.format("From:[%s] To:[%s] [%s]各市场运输重量统计(Kg)", from, to, type));
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

    public static Series.Line rateLine(int year, Shipment.T shipType, String countType) {
        Series.Line line = new Series.Line(String.format(shipType.label()));
        line.color = ProcuresHelper.rgb(shipType);
        for(int i = 1; i <= 12; i++) {
            //分别计算每个月份的到货率情况
            Date from = Dates.getMonthFirst(year, i);
            Date to = Dates.getMonthLast(year, i);

            //准时
            SqlSelect sql = buildSqlHeader(year, shipType, countType, i);
            sql.andWhere("date_format(sp.planArrivDateForCountRate,'%y-%m-%d')=date_format(sp.receiptDate,'%y-%m-%d')");
            Object result = DBUtils.row(sql.toString(), sql.getParams().toArray()).get("count");
            float onTime = (result == null ? 0 : NumberUtils.toFloat(result.toString()));

            //提前
            sql = buildSqlHeader(year, shipType, countType, i);
            sql.andWhere("date_format(sp.planArrivDateForCountRate,'%y-%m-%d')>date_format(sp.receiptDate,'%y-%m-%d')");
            result = DBUtils.row(sql.toString(), sql.getParams().toArray()).get("count");
            float early = (result == null ? 0 : NumberUtils.toFloat(result.toString()));

            //超时
            sql = buildSqlHeader(year, shipType, countType, i);
            sql.andWhere("date_format(sp.planArrivDateForCountRate,'%y-%m-%d')<date_format(sp.receiptDate,'%y-%m-%d')");
            result = DBUtils.row(sql.toString(), sql.getParams().toArray()).get("count");
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
        String[] state = {Shipment.S.RECEIPTD.toString(), Shipment.S.RECEIVING.toString(), Shipment.S.DONE.toString()};
        SqlSelect sql = new SqlSelect().select("COUNT(*) count").from("Shipment sp")
                .where("state IN " + SqlSelect.inlineParam(state) + "")
                .andWhere("sp.type=?")
                .param(shipType.toString());
        if(StringUtils.equals(countType, "ReceiptDate")) {
            sql.andWhere("sp.receiptDate>=?").param(Dates.morning(from)).where("sp.receiptDate<=?")
                    .param(Dates.night(to));
        } else {
            sql.andWhere("sp.planBeginDate>=?").param(Dates.morning(from)).where("sp.planBeginDate<=?")
                    .param(Dates.night(to));
        }
        return sql;
    }
}
