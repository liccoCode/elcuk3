package query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.Caches;
import helper.Dates;
import models.market.M;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.joda.time.DateTime;
import play.cache.Cache;
import services.MetricProfitService;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-31
 * Time: 下午4:19
 */
public class SkuESQuery {
    /**
     * SKU销售额、销量、利润曲线图
     *
     * @param type
     * @param Sku
     * @param calType
     * @return
     */
    public static HighChart esSaleLine(final String type, final String Sku, final String calType) {
        String key = Caches.Q.cacheKey(type, Sku);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = Cache.get(key, HighChart.class);
            if(lineChart != null) return lineChart;
            lineChart = new HighChart(Series.LINE);
            if(calType.equals("fee")) {
                lineChart.title = "最近六个月周销售额";
            }
            if(calType.equals("qty")) {
                lineChart.title = "最近六个月周销量";
            }

            if(calType.equals("profit")) {
                lineChart.title = "最近六个月周利润";
            }

            for(M market : M.values()) {
                if(calType.equals("profit")) {
                    lineChart.series(esSaleProfitLine(market, Sku));
                } else
                    lineChart.series(esSaleFeeLine(market, Sku, calType));
            }
            Cache.add(key, lineChart, "8h");
        }
        return lineChart;
    }

    /**
     * 每个SKU销售额、销量
     *
     * @param market
     * @param sku
     * @param calType
     * @return
     */
    public static Series.Line esSaleFeeLine(M market, String sku, String calType) {
        Date begin = DateTime.now().withTimeAtStartOfDay().plusDays(-180).toDate();
        Date end = DateTime.now().withTimeAtStartOfDay().toDate();

        //按照category计算每天的销售额
        MetricProfitService profitservice = new MetricProfitService(begin, end, market,
                sku, null);
        Series.Line line = null;
        JSONArray entries = null;
        if(calType.equals("fee")) {
            line = new Series.Line(market.name() + "销售额");
            entries = profitservice.dashboardSaleFee(2);
        }
        if(calType.equals("qty")) {
            line = new Series.Line(market.name() + "销量");
            entries = profitservice.dashboardSaleQty(2);
        }
        for(Object o : entries) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
        }
        line.sort();
        return line;
    }

    /**
     * 每个SKU的利润
     *
     * @param market
     * @param sku
     * @return
     */
    public static Series.Line esSaleProfitLine(M market, String sku) {
        Series.Line line = new Series.Line(market.name() + "利润");

        DateTime time = DateTime.now().withTimeAtStartOfDay().plusDays(-180);
        Date begin = time.toDate();
        Date today = DateTime.now().withTimeAtStartOfDay().toDate();
        int i = 0;
        Date end = null;
        while(true) {
            begin = time.plusDays(i * 7).toDate();
            end = time.plusDays((i + 1) * 7).toDate();

            //按照SKU计算每周的利润
            MetricProfitService profitservice = new MetricProfitService(begin, end, market,
                    sku, null);
            Float profit = profitservice.calProfit().totalprofit;
            line.add(Dates.date2JDate(begin), profit);
            i = i + 1;

            /*结束日期大于了当天日期则退出*/
            if(begin.getTime() >= today.getTime()) break;
        }
        line.sort();
        return line;
    }

    /**
     * SKU采购价格曲线图
     *
     * @param type
     * @param Sku
     * @param calType
     * @return
     */
    public static HighChart esProcureLine(final String type, final String Sku, final String calType) {
        String key = Caches.Q.cacheKey(type, Sku);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = Cache.get(key, HighChart.class);
            if(lineChart != null) return lineChart;
            lineChart = new HighChart(Series.LINE);
            if(calType.equals("price")) {
                lineChart.title = "采购价格";
                lineChart.series(esProcureDate(Sku, "采购价格", "procurepayunit", "unit_price", "avg"));
            }
            if(calType.equals("qty")) {
                lineChart.title = "采购数量";
                lineChart.series(esProcureDate(Sku, "采购数量", "procurepayunit", "quantity", "sum"));
            }
            Cache.add(key, lineChart, "8h");
        }
        return lineChart;
    }

    /**
     * 得到ES的结果
     *
     * @param sku
     * @param titlename
     * @param tablename
     * @param fieldname
     * @param caltype
     * @return
     */
    public static Series.Line esProcureDate(String sku, String titlename, String tablename, String fieldname,
                                            String caltype) {
        Series.Line line = new Series.Line(sku + titlename);
        //按照sku计算采购价格
        MetricProfitService profitservice = new MetricProfitService(null, null, null,
                sku, null);
        JSONArray buckets = profitservice.skuProcureDate(tablename, fieldname, caltype);
        if(buckets == null) return line;
        for(Object o : buckets) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("key")), entry.getJSONObject("fieldvalue").getFloat("value"));
        }
        line.sort();
        return line;
    }

    /**
     * sku的总共采购数量
     *
     * @param sku
     * @return
     */
    public static float esProcureQty(String sku) {
        String key = Caches.Q.cacheKey(sku, "procureqty");
        String procureqty = Cache.get(key, String.class);
        if(procureqty != null) return new Float(procureqty);
        synchronized(key.intern()) {
            procureqty = Cache.get(key, String.class);
            if(procureqty != null) return new Float(procureqty);
            //按照sku统计所有采购数量
            MetricProfitService profitservice = new MetricProfitService(null, null, null,
                    sku, null);
            procureqty = String.valueOf(profitservice.esProcureQty());
            Cache.add(key, procureqty, "8h");
        }
        return new Float(procureqty);
    }
}
