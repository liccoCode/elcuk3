package query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.Caches;
import helper.Dates;
import models.market.M;
import models.product.Category;
import models.product.Team;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.joda.time.DateTime;
import play.cache.Cache;
import services.MetricProfitService;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-31
 * Time: 下午4:19
 */
public class SkuESQuery {
    /**
     * SKU销售额曲线图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart salefeeline(final String type, final String Sku) {
        String key = Caches.Q.cacheKey(type, Sku);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "最近六个月周销售额";
            for(M market : M.values()) {
                lineChart.series(esSaleFeeLine(market, Sku));
            }
            Cache.add(key, lineChart, "8h");
        }
        return lineChart;
    }

    /**
     * SKU销量曲线图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart saleqtyline(final String type, final String Sku) {

        String key = Caches.Q.cacheKey(type, Sku);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "最近六个月周销量";

            for(M market : M.values()) {
                lineChart.series(esSaleQtyLine(market, Sku));
            }
            Cache.add(key, lineChart, "8h");
        }
        return lineChart;
    }


    /**
     * 每个Category销售额
     *
     * @param category
     * @param year
     * @return
     */
    public static Series.Line esSaleFeeLine(M market, String sku) {
        Series.Line line = new Series.Line(market.name() + "销售额");
        Date begin = DateTime.now().withTimeAtStartOfDay().plusDays(-180).toDate();
        Date end = DateTime.now().withTimeAtStartOfDay().toDate();

        //按照category计算每天的销售额
        MetricProfitService profitservice = new MetricProfitService(begin, end, market,
                sku, null);
        JSONArray entries = profitservice.dashboardSaleFee(2);
        for(Object o : entries) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
        }
        line.sort();
        return line;
    }


    /**
     * 每个SKU的销量
     *
     * @param category
     * @param year
     * @return
     */
    public static Series.Line esSaleQtyLine(M market, String sku) {
        Series.Line line = new Series.Line(market.name() + "销量");
        Date begin = DateTime.now().withTimeAtStartOfDay().plusDays(-180).toDate();
        Date end = DateTime.now().withTimeAtStartOfDay().toDate();
        //按照category计算每天的销量
        MetricProfitService profitservice = new MetricProfitService(begin, end, market,
                sku, null);
        JSONArray entries = profitservice.dashboardSaleQty(2);
        for(Object o : entries) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
        }
        line.sort();
        return line;
    }


    /**
     * SKU利润曲线图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart skuprofitline(final String type, final String Sku) {

        String key = Caches.Q.cacheKey(type, Sku);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "最近六个月周利润";

            for(M market : M.values()) {
                lineChart.series(esSaleProfitLine(market, Sku));
            }
            Cache.add(key, lineChart, "8h");
        }
        return lineChart;
    }


    /**
     * 每个SKU的利润
     *
     * @param category
     * @param year
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

}
