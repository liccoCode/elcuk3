//BEGIN GENERATED CODE
package jobs.categoryInfo;

import helper.DBUtils;
import helper.Dates;
import helper.LogUtils;
import models.product.Category;
import models.product.Product;
import models.view.dto.CategoryInfoDTO;
import models.view.report.Profit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.jobs.Job;
import services.MetricProfitService;

import java.util.*;

/**
 * Category 信息界面数据准备
 * <p/>
 * User: mac
 * Date: 14-4-2
 * Time: PM2:56
 */
public class CategoryInfoFetchJob extends Job {
    public static final String CategoryInfo_Cache = "categoryinfo_cache";
    public static final String RUNNING = "categoryinfofetchjob_running";

    @Override
    public void doJob() {
        if(isRnning()) return;
        long begin = System.currentTimeMillis();

        Cache.add(RUNNING, RUNNING);
        categoryinfo();
        Cache.delete(RUNNING);
        if(LogUtils.isslow(System.currentTimeMillis() - begin, "CategoryInfoFetchJob")) {
            LogUtils.JOBLOG.info(String
                    .format("CategoryInfoFetchJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }

    }

    public static boolean isRnning() {
        return StringUtils.isNotBlank(Cache.get(RUNNING, String.class));
    }

    /**
     * Category 信息数据计算
     */
    public void categoryinfo() {
        Cache.add(CategoryInfoFetchJob.RUNNING, CategoryInfoFetchJob.RUNNING);

        Map<String, List<CategoryInfoDTO>> dtoMap = new HashMap<>();
        List<Category> categorys = Category.findAll();
        for(Category category : categorys) {
            List<CategoryInfoDTO> categoryDtos = new ArrayList<>();
            for(Product product : category.products) {
                CategoryInfoDTO dto = new CategoryInfoDTO(product);
                //1、sku总销量(从ERP上线到今日)
                dto.total = total(product.sku);
                //2、sku本月销量(月初到月底)
                dto.day30 = day30(product.sku);
                //3、sku利润(今年)和sku利润率(今年)
                profitAndProfitMargins(dto);
                //5、sku上周销售额(上上周六到上周五)
                dto.lastWeekSales = lastWeekSales(product.sku);
                //6、sku上上周销售额(往上同期)
                dto.last2WeekSales = last2WeekSales(product.sku);
                //7、sku上周销量(上上周六到上周五)
                dto.lastWeekVolume = lastWeekVolume(product.sku);
                //8、sku上上周销量(往上同期)
                dto.last2WeekVolume = last2WeekVolume(product.sku);
                categoryDtos.add(dto);
            }
            dtoMap.put(category.categoryId, categoryDtos);
        }
        Cache.delete(CategoryInfo_Cache);
        Cache.add(CategoryInfo_Cache, dtoMap);
        Cache.delete(RUNNING);
    }

    /**
     * sku 总销量
     *
     * @return
     */
    public long total(String sku) {
        //从系统内第一笔订单产生的日期到最后一笔订单产生的日期
        SqlSelect sql = new SqlSelect().select("max(createDate) as max, min(createDate) as min").from("OrderItem");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        for(Map<String, Object> row : rows) {
            Date begin = (Date) row.get("min");
            Date end = (Date) row.get("max");
            MetricProfitService me = new MetricProfitService(begin, end, null, sku, null);
            float saleQty = me.esSaleQty();
            return (long) saleQty;
        }
        return (long) 0;
    }

    /**
     * 本月销量
     *
     * @param sku
     * @return
     */
    public int day30(String sku) {
        DateTime date = new DateTime().now();
        MetricProfitService me = new MetricProfitService(Dates.getMonthFirst(date.getMonthOfYear()), Dates.getMonthLast(
                date.getMonthOfYear()), null, sku, null);
        float saleQty = me.esSaleQty();
        return (int) saleQty;
    }

    /**
     * 利润(今年)和利润率(今年)
     *
     * @param
     * @return
     */
    public void profitAndProfitMargins(CategoryInfoDTO dto) {
        DateTime now = new DateTime().now();
        //获取本年第一天
        Date startDay = Dates.startDayYear(now.getYear());
        //获取本年最后一天
        Date endDay = Dates.endDayYear(now.getYear());
        MetricProfitService me = new MetricProfitService(startDay, endDay, null, dto.sku, null);
        Profit profit = me.calProfit();
        dto.profit = new Float(profit.totalprofit);
        dto.profitMargins = new Float(profit.profitrate);
    }

    /**
     * 上周销售额(上上周六 到 上周五)
     *
     * @return
     */
    public float lastWeekSales(String sku) {
        //上上周六
        DateTime begin = lastSaturday(1);
        //上周五
        DateTime end = lastFriday(1);
        MetricProfitService met = new MetricProfitService(begin.toDate(), end.toDate(), null, sku, null);
        return met.esSaleFee();
    }

    /**
     * 上上周销售额
     *
     * @return
     */
    public float last2WeekSales(String sku) {
        //上上上周六
        DateTime begin = lastSaturday(2);
        //上上周五
        DateTime end = lastFriday(2);
        MetricProfitService met = new MetricProfitService(begin.toDate(), end.toDate(), null, sku, null);
        return met.esSaleFee();
    }

    /**
     * 上周销量
     *
     * @return
     */
    public int lastWeekVolume(String sku) {
        //上上周六
        DateTime begin = lastSaturday(1);
        //上周五
        DateTime end = lastFriday(1);
        MetricProfitService met = new MetricProfitService(begin.toDate(), end.toDate(), null, sku, null);
        float saleQty = met.esSaleQty();
        return (int) saleQty;
    }

    /**
     * 上上周销量
     *
     * @return
     */
    public int last2WeekVolume(String sku) {
        //上上上周六
        DateTime begin = lastSaturday(2);
        //上上周五
        DateTime end = lastFriday(2);

        MetricProfitService met = new MetricProfitService(begin.toDate(), end.toDate(), null, sku, null);
        float saleQty = met.esSaleQty();
        return (int) saleQty;
    }

    /**
     * 获得周五
     *
     * @param plusWeekNumber 往前几周
     * @return
     */
    public DateTime lastFriday(int plusWeekNumber) {
        DateTime monday = new DateTime(Dates.getMondayOfWeek());

        monday = new DateTime(Dates.night(monday.toDate()));
        /**上周五只减三天，上上周五减少10天**/
        return monday.plusDays((plusWeekNumber - 1) * (-7) + (-3));

    }

    /**
     * 获得周六
     *
     * @param plusWeekNumber 往前几周
     * @return
     */
    public DateTime lastSaturday(int plusWeekNumber) {
        DateTime monday = new DateTime(Dates.getMondayOfWeek());

        monday = new DateTime(Dates.morning(monday.toDate()));
        /**上周六只减9天，上上周六减少16天**/
        return monday.plusDays((plusWeekNumber - 1) * (-7) + (-9));

    }
}
