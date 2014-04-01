package query;

import com.alibaba.fastjson.JSONObject;
import helper.Caches;
import helper.DBUtils;
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
import java.util.Map;

import models.SaleTarget;
import com.alibaba.fastjson.JSONArray;
import services.MetricProfitService;

/**
 * PM首页显示图形需要的数据
 * User: cary
 * Date: 14-3-17
 * Time: 下午6:20
 */
public class PmDashboardESQuery {
    /**
     * 饼状图
     *
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
            columnChart = new HighChart(Series.COLUMN);
            columnChart.title = year + "年月度销售额";
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
        /**
         * 获取TEAM的年度任务
         */
        SaleTarget target = SaleTarget.find(" targetYear=? and saleTargetType=? and fid=?", year,
                SaleTarget.T.YEAR, String.valueOf(team.id)).first();
        float task = 0;
        if(target != null) {
            task = target.saleAmounts - totalsalefee;
        }
        if(task < 0)
            task = 0;
        pie.add(task, "未完成目标");
        return pie;
    }

    /**
     * TEAM的销售额百分比
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

        String sql = "select categoryid From Category "
                + " where team_id=" + team.id;
        List<Map<String, Object>> categorys = DBUtils.rows(sql);
        float totalsaleprofit = 0f;
        for(Map<String, Object> category : categorys) {
            String categoryid = (String) category.get("categoryid");
            sql = "select sku From Product "
                    + " where category_categoryid='" + categoryid + "' ";
            List<Map<String, Object>> rows = DBUtils.rows(sql);
            if(rows != null && rows.size() > 0) {
                for(Map<String, Object> product : rows) {
                    String sku = (String) product.get("sku");

                    MetricProfitService service = new MetricProfitService(begin, end, null,
                            sku, null);
                    totalsaleprofit = totalsaleprofit + service.calProfit().totalprofit;
                }
            }
        }
        Series.Pie pie = new Series.Pie(team.name + " " + year + "利润目标百分比");
        if(totalsaleprofit == 0) {
            totalsaleprofit = 1;
        }
        pie.add(totalsaleprofit, "利润");
        /**
         * 获取TEAM的年度利润
         */
        SaleTarget target = SaleTarget.find(" targetYear=? and saleTargetType=? and fid=?", year,
                SaleTarget.T.YEAR, String.valueOf(team.id)).first();
        float task = 0;
        if(target != null) {
            task = target.saleAmounts * target.profitMargin / 100 - totalsaleprofit;
        }
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
        String sql = "";
        for(Team team : teams) {
            sql = "select categoryid From Category "
                    + " where team_id=" + team.id;
            List<Map<String, Object>> categorys = DBUtils.rows(sql);
            float totalsaleprofit = 0f;
            for(Map<String, Object> category : categorys) {
                String categoryid = (String) category.get("categoryid");
                sql = "select sku From Product "
                        + " where category_categoryid='" + categoryid + "' ";
                List<Map<String, Object>> rows = DBUtils.rows(sql);
                if(rows != null && rows.size() > 0) {
                    for(Map<String, Object> product : rows) {
                        String sku = (String) product.get("sku");
                        MetricProfitService service = new MetricProfitService(begin, end, null,
                                sku, null);
                        totalsaleprofit = totalsaleprofit + service.calProfit().totalprofit;
                    }

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
        DateTime time = DateTime.now().withYear(year);
        column.color = "#FFA500";
        for(int i = 1; i <= 12; i++) {
            Date begin = time.withMonthOfYear(i).withDayOfMonth(1).toDate();
            DateTime date = time.withMonthOfYear(i);
            //月的最后一天
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date.toDate());
            Date end = date.withDayOfMonth(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).toDate
                    ();

            List<Category> categorys = team.categorys;
            float totalsale = 0f;
            for(Category category : categorys) {
                MetricProfitService service = new MetricProfitService(begin, end, null,
                        null, null, category.categoryId);
                totalsale = totalsale + service.esSaleFee();
            }
            column.add(totalsale, i + "月");
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
        column.color = "#0000ff";
        for(int i = 1; i <= 12; i++) {
            /**
             * 获取TEAM的CATEGORY销售额
             */
            SaleTarget target = SaleTarget.find(" targetYear=? and "
                    + " saleTargetType=? and fid=?"
                    + " and targetMonth=?", year,
                    SaleTarget.T.MONTH, String.valueOf(team.id), i).first();
            float task = 0f;
            if(target != null) task = target.saleAmounts;
            column.add(task, i + "月");
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
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "最近六个月周销售额";
            List<Category> categorys = team.getCategorys();
            for(Category category : categorys) {
                lineChart.series(esSaleFeeLine(category, year));
            }
            Cache.add(key, lineChart, "8h");
        }
        return lineChart;
    }

    /**
     * TEAM每个Category销售额曲线图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart saleqtyline(final String type, final int year, final Team team) {

        String key = Caches.Q.cacheKey(type, year, team.name);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "最近六个月周销量";
            List<Category> categorys = team.getCategorys();
            for(Category category : categorys) {
                lineChart.series(esSaleQtyLine(category, year));
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
        Series.Line line = new Series.Line(category.name + "销售额");
        Date begin = DateTime.now().withTimeAtStartOfDay().plusDays(-180).toDate();
        Date end = DateTime.now().withTimeAtStartOfDay().toDate();
        //按照category计算每天的销量
        MetricProfitService profitservice = new MetricProfitService(begin, end, null,
                null, null, category.categoryId);
        JSONArray entries = profitservice.dashboardSaleFee(1);
        for(Object o : entries) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
        }
        line.sort();
        return line;
    }

    /**
     * 每个Category销售额
     *
     * @param category
     * @param year
     * @return
     */
    public static Series.Line esSaleQtyLine(Category category, int year) {
        Series.Line line = new Series.Line(category.name + "销量");
        Date begin = DateTime.now().withTimeAtStartOfDay().plusDays(-180).toDate();
        Date end = DateTime.now().withTimeAtStartOfDay().toDate();
        //按照category计算每天的销量
        MetricProfitService profitservice = new MetricProfitService(begin, end, null,
                null, null, category.categoryId);
        JSONArray entries = profitservice.dashboardSaleQty(1);
        for(Object o : entries) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
        }
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
            lineChart.title = year + "年月度利润率";
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
        line.color = "#FFA500";
        DateTime time = DateTime.now().withYear(year);
        for(int i = 1; i <= 12; i++) {
            Date begin = time.withMonthOfYear(i).withDayOfMonth(1).toDate();
            DateTime date = time.withMonthOfYear(i);
            //月的最后一天
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date.toDate());
            Date end = date.withDayOfMonth(calendar.getActualMaximum(Calendar.DAY_OF_MONTH)).toDate
                    ();
            String sql = "select categoryid From Category "
                    + " where team_id=" + team.id;
            List<Map<String, Object>> categorys = DBUtils.rows(sql);
            float totalsaleprofit = 0f;
            float totalsalefee = 0f;
            for(Map<String, Object> category : categorys) {
                String categoryid = (String) category.get("categoryid");
                sql = "select sku From Product "
                        + " where category_categoryid='" + categoryid + "' ";
                List<Map<String, Object>> rows = DBUtils.rows(sql);
                if(rows != null && rows.size() > 0) {
                    for(Map<String, Object> product : rows) {
                        String sku = (String) product.get("sku");
                        MetricProfitService profitservice = new MetricProfitService(begin, end, null,
                                sku, null);
                        Profit profit = profitservice.calProfit();
                        totalsaleprofit = totalsaleprofit + profit.totalprofit;
                        totalsalefee = totalsalefee + profit.totalfee;
                    }
                }
            }
            float rate = 0;
            if(totalsalefee != 0) {
                rate = totalsaleprofit / totalsalefee * 100;
            }
            line.add(rate, i + "月");
        }
        return line;
    }

    /**
     * 目标利润率曲线图
     *
     * @param year
     * @param team
     * @return
     */
    public static Series.Line profitTaskCategoryLine(String type, int year, Team team) {
        if(team == null) throw new FastRuntimeException("此方法 Team 必须指定");
        Series.Line line = new Series.Line(team.name + year + "年月度利润率目标");
        line.color = "#0000ff";
        for(int i = 1; i <= 12; i++) {
            /**
             * 获取TEAM的CATEGORY销售额
             */
            SaleTarget target = SaleTarget.find(" targetYear=? and "
                    + " saleTargetType=? and fid=?"
                    + " and targetMonth=?", year,
                    SaleTarget.T.MONTH, String.valueOf(team.id), i).first();
            float task = 0f;
            if(target != null) task = target.profitMargin;
            line.add(task, i + "月");
        }
        return line;
    }
}