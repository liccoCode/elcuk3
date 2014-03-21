package services;

import com.alibaba.fastjson.JSONObject;
import helper.Caches;
import helper.Dates;
import models.product.Category;
import models.product.Product;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import models.view.report.Profit;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.utils.FastRuntimeException;
import models.product.Team;

import java.util.Date;
import java.util.List;
import java.util.Calendar;

import com.alibaba.fastjson.JSONArray;

/**
 * PM首页显示图形需要的数据
 * User: cary
 * Date: 14-3-17
 * Time: 下午6:20
 */
public class MetricPmService {

    /**
     * 饼状图
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static Series.Pie saleCategoryPie(String type, int year, Team team) {
        if(type.equals("sale")) {
            return saleCategoryPie(year, team);
        } else if(type.equals("profit")) {
            return profitCategoryPie(year, team);
        } else if(type.equals("teamsale")) {
            return teamSalePie(year);
        } else if(type.equals("teamprofit")) {
            return teamProfitPie(year);
        } else
            return null;
    }

    /**
     * 曲线图计算
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart categoryLine(final String type, final int year, final Team team) {
        if(type.equals("profitrateline")) {
            return profitrateline(type, year, team);
        } else if(type.equals("salefeeline")) {
            return salefeeline(type, year, team);
        }
        return null;
    }


    /**
     * 柱状图计算
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart categoryColumn(final String type, final int year, final Team team) {
        String key = Caches.Q.cacheKey(type, year, team.name);
        HighChart columnChart = Cache.get(key, HighChart.class);
        if(columnChart != null) return columnChart;
        synchronized(key.intern()) {
            columnChart = Cache.get(key, HighChart.class);
            if(columnChart != null) return columnChart;
            columnChart = new HighChart(Series.COLUMN);
            columnChart.series(saleCategoryColumn(type, year, team));
            columnChart.series(saleTaskCategoryColumn(type, year, team));
            Cache.add(key, columnChart, "8h");
        }
        return columnChart;
    }


    /**
     * 饼状图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart categoryPie(final String type, final int year, final Team team) {
        String key = Caches.Q.cacheKey(type, year, team.name);
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;
        synchronized(key.intern()) {
            pieChart = Cache.get(key, HighChart.class);
            if(pieChart != null) return pieChart;
            pieChart = new HighChart(Series.PIE);
            pieChart.series(saleCategoryPie(type, year, team));
            Cache.add(key, pieChart, "8h");
        }
        return pieChart;
    }


    /**
     * 销售额百分比
     *
     * @param year
     * @param team
     * @return
     */
    public static Series.Pie saleCategoryPie(int year, Team team) {
        if(team == null) throw new FastRuntimeException("此方法 Team 必须指定");
        //年的第一天
        Date begin = Dates.startDayYear(year);
        Date end = Dates.endDayYear(year);
        List<Category> categorys = team.categorys;
        float totalsalefee = 0f;
        for(Category category : categorys) {
            MetricProfitService service = new MetricProfitService(begin, end, null,
                    null, null, category.categoryId);
            totalsalefee = totalsalefee + service.esSaleFee();
        }
        Series.Pie pie = new Series.Pie(team.name + " " + year + "销售额目标百分比");
        if(totalsalefee == 0f)
            totalsalefee = 1;
        pie.add(totalsalefee, "销售额");
        float task = 10000f - totalsalefee;
        if(task < 0)
            task = 0;
        pie.add(task, "未完成目标");
        return pie;
    }

    /**
     * 销售额百分比
     *
     * @param year
     * @return
     */
    public static Series.Pie teamSalePie(int year) {
        //年的第一天
        Date begin = Dates.startDayYear(year);
        Date end = Dates.endDayYear(year);
        List<Team> teams = Team.findAll();
        Series.Pie pie = new Series.Pie(year + "TEAM销售额百分比");
        for(Team team : teams) {
            List<Category> categorys = team.categorys;
            float totalsalefee = 0f;
            for(Category category : categorys) {
                MetricProfitService service = new MetricProfitService(begin, end, null,
                        null, null, category.categoryId);
                totalsalefee = totalsalefee + service.esSaleFee();
            }
            if(totalsalefee == 0f)
                totalsalefee = 1;
            pie.add(totalsalefee, team.name + "销售额");
        }
        return pie;
    }

    /**
     * 利润百分比
     *
     * @param year
     * @param team
     * @return
     */
    public static Series.Pie profitCategoryPie(int year, Team team) {
        if(team == null) throw new FastRuntimeException("此方法 Team 必须指定");
        //年的第一天
        Date begin = Dates.startDayYear(year);
        Date end = Dates.endDayYear(year);
        List<Category> categorys = team.categorys;
        float totalsaleprofit = 0f;
        for(Category category : categorys) {
            /**
             * 利润必须指定SKU计算
             */
            List<Product> products = category.products;
            for(Product product : products) {
                MetricProfitService service = new MetricProfitService(begin, end, null,
                        product.sku, null);
                totalsaleprofit = totalsaleprofit + service.calProfit().totalprofit;
            }
        }
        Series.Pie pie = new Series.Pie(team.name + " " + year + "利润目标百分比");
        if(totalsaleprofit == 0) {
            totalsaleprofit = 1;
        }
        pie.add(totalsaleprofit, "利润");
        float task = 10000f - totalsaleprofit;
        if(task < 0)
            task = 0;
        pie.add(task, "未完成目标");
        return pie;
    }


    /**
     * 利润百分比
     *
     * @param year
     * @return
     */
    public static Series.Pie teamProfitPie(int year) {
        //年的第一天
        Date begin = Dates.startDayYear(year);
        Date end = Dates.endDayYear(year);
        List<Team> teams = Team.findAll();
        Series.Pie pie = new Series.Pie(year + "TEAM利润百分比");
        for(Team team : teams) {
            List<Category> categorys = team.categorys;
            float totalsaleprofit = 0f;
            for(Category category : categorys) {
                List<Product> products = category.products;
                for(Product product : products) {
                    MetricProfitService service = new MetricProfitService(begin, end, null,
                            product.sku, null);
                    totalsaleprofit = totalsaleprofit + service.calProfit().totalprofit;
                }
            }
            if(totalsaleprofit == 0f)
                totalsaleprofit = 1;
            pie.add(totalsaleprofit, team.name + "利润");
        }
        return pie;
    }


    /**
     * 销售额柱状图
     *
     * @param year
     * @param team
     * @return
     */
    public static Series.Column saleCategoryColumn(String type, int year, Team team) {
        if(team == null) throw new FastRuntimeException("此方法 Team 必须指定");

        Series.Column column = new Series.Column(team.name + year + "年月度销售额");
        for(int i = 1; i <= 12; i++) {
            Date begin = DateTime.now().withDayOfMonth(i).toDate();
            Date end = DateTime.now().withDayOfMonth(i).toDate();

            List<Category> categorys = team.categorys;
            float totalsale = 0f;
            for(Category category : categorys) {
                MetricProfitService service = new MetricProfitService(begin, end, null,
                        null, null, category.categoryId);
                totalsale = totalsale + service.esSaleFee();
            }
            column.add(totalsale, i);
        }

        return column;
    }


    /**
     * 目标柱状图
     *
     * @param year
     * @param team
     * @return
     */
    public static Series.Column saleTaskCategoryColumn(String type, int year, Team team) {
        if(team == null) throw new FastRuntimeException("此方法 Team 必须指定");
        Series.Column column = new Series.Column(team.name + year + "年月度目标");
        for(int i = 1; i <= 12; i++) {
            /**
             * 每月第一天为开始时间
             */
            DateTime month = DateTime.now().withMonthOfYear(i);
            Date begin = month.withDayOfMonth(1).toDate();
            /**
             * 每月最后一天为结束时间
             */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(begin);
            Date end = month.withDayOfMonth(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).toDate();

            column.add(200f, i);
        }
        return column;
    }


    /**
     * TEAM每个Category销售额曲线图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart salefeeline(final String type, final int year, final Team team) {

        String key = Caches.Q.cacheKey(type, year, team.name);
        HighChart lineChart = Cache.get(key, HighChart.class);
        //if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "销售额";
            List<Category> categorys = team.getCategorys();
            for(Category category : categorys) {
                lineChart.series(esSaleFeeLine(category, year));
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
    public static Series.Line esSaleFeeLine(Category category, int year) {
        Series.Line line = new Series.Line(category.name);

        Date begin = DateTime.now().withTimeAtStartOfDay().plusDays(-30).toDate();
        Date end = DateTime.now().withTimeAtStartOfDay().toDate();

        //   Date end = DateTime.now().withTimeAtStartOfDay().withYear(2013).toDate();
        //  Date begin = DateTime.now().withTimeAtStartOfDay().withYear(2013).plusDays(-30).toDate();


        //按照category计算每天的销量
        MetricProfitService profitservice = new MetricProfitService(begin, end, null,
                null, null, category.categoryId);
        JSONArray entries = profitservice.dashboarSaleFee();
        for(Object o : entries) {
            JSONObject entry = (JSONObject) o;

            line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
        }

//        DateTime datePointer = new DateTime(begin);
//        while(datePointer.getMillis() <= end.getTime()) {
//            line.add(0f, Dates.date2JDate(begin));
//            datePointer = datePointer.plusDays(1);
//        }
        //line.add(Dates.date2JDate(begin),100f);
        //line.add(Dates.date2JDate(end),1000f);
        line.sort();

        return line;
    }


    /**
     * TEAM每月利润率曲线图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart profitrateline(final String type, final int year, final Team team) {
        String key = Caches.Q.cacheKey(type, year, team.name);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.series(profitCategoryLine(type, year, team));
            lineChart.series(profitTaskCategoryLine(type, year, team));
            Cache.add(key, lineChart, "8h");
        }
        return lineChart;
    }


    /**
     * 利润率曲线图
     *
     * @param year
     * @param team
     * @return
     */
    public static Series.Line profitCategoryLine(String type, int year, Team team) {
        if(team == null) throw new FastRuntimeException("此方法 Team 必须指定");

        Series.Line line = new Series.Line(team.name + year + "年月度利润率");
        for(int i = 1; i <= 12; i++) {
            Date begin = DateTime.now().withDayOfMonth(i).toDate();
            Date end = DateTime.now().withDayOfMonth(i).toDate();

            List<Category> categorys = team.categorys;
            float totalsaleprofit = 0f;
            float totalsalefee = 0f;
            for(Category category : categorys) {
                List<Product> products = category.products;
                for(Product product : products) {
                    MetricProfitService profitservice = new MetricProfitService(begin, end, null,
                            product.sku, null);
                    Profit profit = profitservice.calProfit();
                    totalsaleprofit = totalsaleprofit + profit.totalprofit;
                    totalsalefee = totalsalefee + profit.totalfee;
                }
            }
            float rate = 0;
            if(totalsalefee != 0) {
                rate = totalsaleprofit / totalsalefee;
            }
            line.add(100f, i);
        }

        return line;
    }


    /**
     * 利润曲线图
     *
     * @param year
     * @param team
     * @return
     */
    public static Series.Line profitTaskCategoryLine(String type, int year, Team team) {
        if(team == null) throw new FastRuntimeException("此方法 Team 必须指定");
        Series.Line line = new Series.Line(team.name + year + "年月度目标");
        for(int i = 1; i <= 12; i++) {
            /**
             * 每月第一天为开始时间
             */
            DateTime month = DateTime.now().withMonthOfYear(i);
            Date begin = month.withDayOfMonth(1).toDate();
            /**
             * 每月最后一天为结束时间
             */
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(begin);
            Date end = month.withDayOfMonth(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).toDate();

            line.add(200f, i);
        }
        return line;
    }


}
