package query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.Caches;
import helper.DBUtils;
import helper.Dates;
import models.SaleTarget;
import models.product.Category;
import models.product.Team;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import models.view.report.Profit;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.utils.FastRuntimeException;
import services.MetricProfitService;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * PM首页显示图形需要的数据
 * User: cary
 * Date: 14-3-17
 * Time: 下午6:20
 * @deprecated
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
        String key = Caches.Q.cacheKey(type, year, team.id);
        HighChart columnChart = Cache.get(key, HighChart.class);
        if(columnChart != null) return columnChart;
        synchronized(key.intern()) {
            columnChart = Cache.get(key, HighChart.class);
            if(columnChart != null) return columnChart;
            columnChart = new HighChart(Series.COLUMN);
            columnChart.title = year + "年月度销售额";
            columnChart.series(saleCategoryColumn(type, year, team));
            columnChart.series(saleTaskCategoryColumn(type, year, team));
            Cache.add(key, columnChart);
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
        String key = Caches.Q.cacheKey(type, year, team.id);
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;
        synchronized(key.intern()) {
            pieChart = Cache.get(key, HighChart.class);
            if(pieChart != null) return pieChart;
            pieChart = new HighChart(Series.PIE);
            pieChart.series(saleCategoryPie(type, year, team));
            Cache.add(key, pieChart);
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
        List<String> categorys = team.getStrCategorys();
        float totalsalefee = 0f;
        for(String categoryid : categorys) {
            MetricProfitService service = new MetricProfitService(begin, end, null,
                    null, null, categoryid);
            totalsalefee = totalsalefee + service.esSaleFee();
        }
        Series.Pie pie = new Series.Pie(team.name + " " + year + "销售额目标百分比");
        if(totalsalefee == 0f)
            totalsalefee = 1;
        pie.add(totalsalefee, "销售额");
        /**
         * 获取TEAM的年度任务
         */

        String sql = "select sum(saleAmounts*10000) as amount From SaleTarget "
                + " where targetYear=" + year
                + " and saleTargetType='" + SaleTarget.T.CATEGORY + "' "
                + " and " + SqlSelect.whereIn("fid", team.getStrCategorys());
        Map<String, Object> row = DBUtils.row(sql);

        float task = 0;
        if(row != null && row.size() > 0) {
            Object obj = row.get("amount");
            if(obj != null) {
                float amount = new Float(obj.toString());
                task = amount - totalsalefee;
            }
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
            List<String> categorys = team.getStrCategorys();
            float totalsalefee = 0f;
            for(String categoryid : categorys) {
                MetricProfitService service = new MetricProfitService(begin, end, null,
                        null, null, categoryid);
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

        List<String> categoryIds = team.getStrCategorys();
        //skus 集合
        List<String> skus = Category.getSKUs(categoryIds);
        double totalsaleprofit = 0f;
        for(String sku : skus) {
            MetricProfitService service = new MetricProfitService(begin, end, null,
                    sku, null);
            totalsaleprofit = totalsaleprofit + service.calProfit().totalprofit;
        }
        Series.Pie pie = new Series.Pie(team.name + " " + year + "利润目标百分比");
        if(totalsaleprofit == 0) {
            totalsaleprofit = 1;
        }
        pie.add(new Float(totalsaleprofit), "利润");

        /**
         * 获取TEAM的年度利润
         */
        String sql = "select sum(saleAmounts*profitMargin/100*10000) as amount From SaleTarget "
                + " where targetYear=" + year
                + " and saleTargetType='" + SaleTarget.T.CATEGORY + "' "
                + " and " + SqlSelect.whereIn("fid", team.getStrCategorys());
        Map<String, Object> row = DBUtils.row(sql);

        double task = 0;
        if(row != null && row.size() > 0) {
            Object obj = row.get("amount");
            if(obj != null) {
                float amount = new Float(obj.toString());
                task = amount - totalsaleprofit;
            }
        }
        if(task < 0)
            task = 0;
        pie.add(new Float(task), "未完成目标");
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
            List<String> categoryIds = team.getStrCategorys();
            //skus 集合
            List<String> skus = Category.getSKUs(categoryIds);
            double totalsaleprofit = 0f;
            for(String sku : skus) {
                MetricProfitService service = new MetricProfitService(begin, end, null,
                        sku, null);
                totalsaleprofit = totalsaleprofit + service.calProfit().totalprofit;
            }
            if(totalsaleprofit == 0f)
                totalsaleprofit = 1;
            pie.add(new Float(totalsaleprofit), team.name + "利润");
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

            List<String> categorys = team.getStrCategorys();
            float totalsale = 0f;
            for(String categoryid : categorys) {
                MetricProfitService service = new MetricProfitService(begin, end, null,
                        null, null, categoryid);
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
             *获取CATEGORY的月度销售额
             */
            String sql = "select sum(saleAmounts*10000) as amount From SaleTarget "
                    + " where targetYear=" + year
                    + " and targetmonth=" + i
                    + " and saleTargetType='" + SaleTarget.T.MONTH + "' "
                    + " and " + SqlSelect.whereIn("fid", team.getStrCategorys());
            Map<String, Object> row = DBUtils.row(sql);

            float task = 0;
            if(row != null && row.size() > 0) {
                Object obj = row.get("amount");
                if (obj!=null){
                task = new Float(obj.toString());
                }
            }
            column.add(task, i + "月");
        }
        return column;
    }

    /**
     * TEAM每个Category 最近六个月周销售额曲线图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart salefeeline(final String type, final int year, final Team team) {
        String key = Caches.Q.cacheKey(type, year, team.id);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = Cache.get(key, HighChart.class);
            if(lineChart != null) return lineChart;
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "最近六个月周销售额";
            List<Category> categorys = team.getObjCategorys();
            for(Category category : categorys) {
                lineChart.series(esSaleFeeLine(category, year));
            }
            Cache.add(key, lineChart);
        }
        return lineChart;
    }

    /**
     * TEAM每个Category 最近六个月周销量曲线图
     *
     * @param type
     * @param year
     * @param team
     * @return
     */
    public static HighChart saleqtyline(final String type, final int year, final Team team) {
        String key = Caches.Q.cacheKey(type, year, team.id);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = Cache.get(key, HighChart.class);
            if(lineChart != null) return lineChart;
            lineChart = new HighChart(Series.LINE);
            lineChart.title = "最近六个月周销量";
            List<Category> categorys = team.getObjCategorys();
            for(Category category : categorys) {
                lineChart.series(esSaleQtyLine(category, year));
            }
            Cache.add(key, lineChart);
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
        JSONArray buckets = profitservice.dashboardDateAvg("salefee", "cost_in_usd", true);

        for(Object o : buckets) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("key")),
                    new  java.math.BigDecimal(
                            entry.getJSONObject("fieldvalue").getFloat("value")/7)
                            .setScale(2,4)
                            .floatValue()
            );
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
        JSONArray entries = profitservice.dashboardDateAvg("orderitem", "quantity", true);
        for(Object o : entries) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("key")),
                    new  java.math.BigDecimal(
                          entry.getJSONObject("fieldvalue").getFloat("value")/7)
                          .setScale(2,4)
                          .floatValue()
            );
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
        String key = Caches.Q.cacheKey(type, year, team.id);
        HighChart lineChart = Cache.get(key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(key.intern()) {
            lineChart = Cache.get(key, HighChart.class);
            if(lineChart != null) return lineChart;
            lineChart = new HighChart(Series.LINE);
            lineChart.title = year + "年月度利润率";
            lineChart.series(profitCategoryLine(type, year, team));
            lineChart.series(profitTaskCategoryLine(type, year, team));
            Cache.add(key, lineChart);
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
            List<String> categoryIds = team.getStrCategorys();
            //skus 集合
            List<String> skus = Category.getSKUs(categoryIds);

            double totalsaleprofit = 0f;
            double totalsalefee = 0f;
            for(String sku : skus) {
                MetricProfitService profitservice = new MetricProfitService(begin, end, null,
                        sku, null);
                Profit profit = profitservice.calProfit();
                totalsaleprofit = totalsaleprofit + profit.totalprofit;
                totalsalefee = totalsalefee + profit.totalfee;
            }
            double rate = 0;
            if(totalsalefee != 0) {
                rate = totalsaleprofit / totalsalefee * 100;
            }
            line.add(new Float(rate), i + "月");
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
             * 获取TEAM的CATEGORY利润率
             */
            String sql = "select avg(profitMargin) as profit From SaleTarget "
                    + " where targetYear=" + year
                    + " and saleTargetType='" + SaleTarget.T.MONTH + "' "
                    + " and targetmonth=" + i
                    + " and " + SqlSelect.whereIn("fid", team.getStrCategorys());
            Map<String, Object> row = DBUtils.row(sql);

            float task = 0;
            if(row != null && row.size() > 0) {
                Object obj = row.get("profit");
                if(obj != null) {
                    task = new Float(obj.toString());
                }
            }
            line.add(task, i + "月");
        }
        return line;
    }

    /**
     * 返回 Category 全年每个月的销售额和销售额目标
     * 和已经完成的销售额目标, 并组装成 HightChart 使用的格式返回
     *
     * @return
     */
    public static HighChart ajaxHighChartCategorySalesAmount(String categoryId, int year) {
        String cacked_key = String.format("%s_%s_categoryinfo_salesamount", year, categoryId);
        HighChart columnChart = play.cache.Cache.get(cacked_key, HighChart.class);
        if(columnChart != null) return columnChart;
        synchronized(cacked_key.intern()) {
            columnChart = play.cache.Cache.get(cacked_key, HighChart.class);
            if(columnChart != null) return columnChart;
            columnChart = new HighChart(Series.COLUMN);
            columnChart.title = String.format("%s年度%s产品线销售额", year, categoryId);
            //已完成的柱状图
            columnChart.series(salesAmountColom(categoryId, year));
            //目标柱状图
            columnChart.series(salesAmountTargetColom(categoryId, year));
            Cache.delete(cacked_key);
            Cache.add(cacked_key, columnChart);
        }
        return columnChart;
    }

    /**
     * 销售额目标的柱状图
     *
     * @return
     */
    public static Series.Column salesAmountTargetColom(String categoryId, int year) {
        DateTime now = new DateTime().now().withYear(year);
        List<SaleTarget> saleTargetList = SaleTarget.find("fid=? AND targetYear=? AND saleTargetType=?", categoryId,
                now.getYear(), SaleTarget.T.MONTH).fetch();

        Series.Column column = new Series.Column("月度销售额目标");
        column.color = "#0000ff";
        for(int i = 0; i < saleTargetList.size(); i++) {
            float target = saleTargetList.get(i).saleAmounts * 10000;
            column.add(target, saleTargetList.get(i).targetMonth + "月");
        }
        return column;
    }

    /**
     * 已经完成的销售额的柱状图
     *
     * @return
     */
    public static Series.Column salesAmountColom(String categoryId, int year) {
        DateTime now = new DateTime().now().withYear(year);
        Series.Column column = new Series.Column("月度销售额");
        column.color = "#FFA500";
        float totalsale = 0f;
        for(int i = 1; i <= 12; i++) {
            DateTime month = now.withMonthOfYear(i);
            //获得每个月份的完整的开始日期
            DateTime begin = month.withDayOfMonth(1);
            //获得每个月份的完整的结束日期
            DateTime end = getMonthEndDate(month);

            MetricProfitService service = new MetricProfitService(begin.toDate(), end.toDate(), null,
                    null, null, categoryId);
            totalsale = service.esSaleFee();
            column.add(totalsale, i + "月");
        }
        return column;
    }

    /**
     * 返回 Category 全年每个月的利润率目标和利润率
     * 和已经完成的销售额目标, 并组装成 HightChart 使用的格式返回
     *
     * @return
     */
    public static HighChart ajaxHighChartCategorySalesProfit(String categoryId, int year) {
        String cacked_key = String.format("%s_%s_categoryinfo_salesprofit", year, categoryId);
        HighChart lineChart = play.cache.Cache.get(cacked_key, HighChart.class);
        if(lineChart != null) return lineChart;
        synchronized(cacked_key.intern()) {
            lineChart = play.cache.Cache.get(cacked_key, HighChart.class);
            if(lineChart != null) return lineChart;
            lineChart = new HighChart(Series.LINE);
            lineChart.title = String.format("%s年度%s产品线利润率", year, categoryId);
            //已完成曲线图
            lineChart.series(salesProfitLine(categoryId, year));
            //目标曲线图
            lineChart.series(salesProfitTargetLine(categoryId, year));
            Cache.delete(cacked_key);
            Cache.add(cacked_key, lineChart);
        }
        return lineChart;
    }

    /**
     * Category 利润率
     *
     * @param categoryId
     * @return
     */
    public static Series.Line salesProfitLine(String categoryId, int year) {
        DateTime now = new DateTime().now().withYear(year);
        Series.Line line = new Series.Line("月度利润率");
        line.color = "#FFA500";

        String sql = "select sku From Product "
                + " where category_categoryid='" + categoryId + "' ";
        List<Map<String, Object>> rows = DBUtils.rows(sql);

        double totalsaleprofit = 0f;
        double totalsalefee = 0f;
        for(int i = 1; i <= 12; i++) {
            totalsaleprofit = 0f;
            totalsalefee = 0f;
            DateTime month = now.withMonthOfYear(i);
            //获得每个月份的完整的开始日期
            DateTime begin = month.withDayOfMonth(1);
            //获得每个月份的完整的结束日期
            DateTime end = getMonthEndDate(month);

            if(rows != null && rows.size() > 0) {
                for(Map<String, Object> product : rows) {
                    String sku = (String) product.get("sku");
                    MetricProfitService profitservice = new MetricProfitService(begin.toDate(), end.toDate(), null,
                            sku, null);
                    Profit profit = profitservice.calProfit();
                    totalsaleprofit = totalsaleprofit + profit.totalprofit;
                    totalsalefee = totalsalefee + profit.totalfee;
                }
            }
            MetricProfitService service = new MetricProfitService(begin.toDate(), end.toDate(), null,
                    null, null, categoryId);
            double rate = 0;
            if(totalsalefee != 0) {
                rate = totalsaleprofit / totalsalefee * 100;
            }
            line.add(new Float(rate), i + "月");
        }
        return line;
    }

    /**
     * Category 利润率目标
     *
     * @param categoryId
     * @return
     */
    public static Series.Line salesProfitTargetLine(String categoryId, int year) {
        DateTime now = new DateTime().now().withYear(year);
        Series.Line line = new Series.Line("月度利润率目标");
        List<SaleTarget> saleTargetList = SaleTarget.find("fid=? AND targetYear=? AND saleTargetType=?", categoryId,
                now.getYear(), SaleTarget.T.MONTH).fetch();

        line.color = "#0000ff";
        for(int i = 0; i < saleTargetList.size(); i++) {

            float target = saleTargetList.get(i).profitMargin;

            line.add(target, saleTargetList.get(i).targetMonth + "月");
        }
        return line;
    }

    /**
     * 获取月份的最后一天
     *
     * @param month
     * @return
     */
    public static DateTime getMonthEndDate(DateTime month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(month.toDate());
        int maxDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        return month.withDayOfMonth(maxDayOfMonth);
    }
}