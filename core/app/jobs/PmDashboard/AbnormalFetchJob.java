package jobs.PmDashboard;

import helper.DBUtils;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.view.dto.AbnormalDTO;
import models.view.post.AbnormalPost;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.jobs.Job;
import play.jobs.On;
import query.ProductQuery;
import services.MetricProfitService;

import java.util.*;

/**
 * PM 首页异常信息处理 Job
 * <p/>
 * 会将所有的异常的数据计算出来缓存到 Redis
 * 然后在controller内根据条件去缓存获取对应的即可
 *
 * 轮询: 7 13 22 三个时间点执行三次
 * <p/>
 * User: mac
 * Date: 14-3-21
 * Time: PM2:03
 */
@On("0 0 0,7,13,22 * * ?")
public class AbnormalFetchJob extends Job {
    public static final String RUNNING = "anormal_running";
    public static final String AbnormalDTO_CACHE = "abnormal_info";

    @Override
    public void doJob() {
        if(isRnning()) return;
        long begin = System.currentTimeMillis();
        abnormal();
        Logger.info("AbnormalFetchJob calculate.... [%sms]", System.currentTimeMillis() - begin);
    }

    public static boolean isRnning() {
        return StringUtils.isNotBlank(Cache.get(RUNNING, String.class));
    }

    /**
     * 异常信息的分析与计算
     *
     * @return
     */
    public void abnormal() {
        Cache.add(RUNNING, RUNNING);
        //获取所有的 sku
        List<String> skus = new ProductQuery().skus();

        Map<String, List<AbnormalDTO>> dtoMap = new HashMap<String, List<AbnormalDTO>>();
        //准备数据容器
        List<AbnormalDTO> day1s = new ArrayList<AbnormalDTO>();
        List<AbnormalDTO> reviews = new ArrayList<AbnormalDTO>();
        List<AbnormalDTO> befores = new ArrayList<AbnormalDTO>();
        List<AbnormalDTO> profits = new ArrayList<AbnormalDTO>();
        for(String sku : skus) {
            //昨天销量异常
            fetchDay1SalesAmount(sku, day1s);
            //review异常
            fetchReview(sku, reviews);
            //历史销量异常
            fetchBeforeSalesAmount(sku, befores);
            //历史利润率异常
            fetchBeforeSalesProfit(sku, profits);
        }

        dtoMap.put(AbnormalPost.T.DAY1.toString(), day1s);
        dtoMap.put(AbnormalPost.T.REVIEW.toString(), reviews);
        dtoMap.put(AbnormalPost.T.BEFOREAMOUNT.toString(), befores);
        dtoMap.put(AbnormalPost.T.BEFOREPROFIT.toString(), profits);
        //将数据添加到缓存内
        Cache.add(AbnormalDTO_CACHE, dtoMap);
        Cache.delete(RUNNING);
    }

    /**
     * 计算出所有 昨天销售额异常的sku
     */
    private void fetchDay1SalesAmount(String sku, List<AbnormalDTO> dtos) {
        DateTime day1 = new DateTime().now().plusDays(-1);
        MetricProfitService met = new MetricProfitService(day1.toDate(), day1.toDate(), null, sku, null);
        //昨天的销售额
        float day1Sales = met.esSaleFee();
        //过去四周同期平均值
        float mean = this.beforeMean(sku, met);
        //如果昨天销售额 小于 过去四周同期 销售额的平均值 20%或者以上，则视为异常 sku
        if(day1Sales > 0 && mean > 0 && day1Sales <= (mean * 0.8)) {
            float difference = (mean - day1Sales) / mean * 100;
            dtos.add(new AbnormalDTO(day1Sales, mean, difference, sku, AbnormalDTO.T.DAY1));
        }
    }

    /**
     * 计算出所有review信息异常的sku
     *
     * @param sku
     */
    private void fetchReview(String sku, List<AbnormalDTO> dtos) {
        List<String> listingIds = Listing.getAllListingBySKU(sku);

        SqlSelect sql = new SqlSelect().select("count(*) as count").from("AmazonListingReview").where(
                SqlSelect.whereIn("listingId", listingIds)).where("rating <= 3");

        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            if(NumberUtils.stringToInt(row.get("count").toString()) > 0) {
                dtos.add(new AbnormalDTO(sku, AbnormalDTO.T.REVIEW));
            }
        }
    }

    /**
     * 计算出所有 历史销售额异常的sku
     * <p/>
     * 历史销售额指的是：
     * 上上周六 到 上周五 的销售额 对比 上个周期的销售额
     *
     * @param sku
     */
    private void fetchBeforeSalesAmount(String sku, List<AbnormalDTO> dtos) {
        DateTime monday = new DateTime(getMondayOfWeek());
        Float[] beforeSales = new Float[2];
        for(int i = 1; i <= 2; i++) {
            //上周五 以及 往前同期（上上周五）
            DateTime day3 = monday.plusDays(i * (-3));
            //两个礼拜前的的礼拜六 以及 往前同期（三个礼拜前的礼拜六）
            DateTime day9 = monday.plusDays(i * (-9));
            MetricProfitService met = new MetricProfitService(day3.toDate(), day9.toDate(), null, sku, null);
            beforeSales[i - 1] = met.esSaleFee();
        }
        if(beforeSales[0] > 0 && beforeSales[1] > 0 && beforeSales[0] <= (beforeSales[1] * 0.95)) {
            float difference = (beforeSales[1] - beforeSales[0]) / beforeSales[1] * 100;
            dtos.add(new AbnormalDTO(beforeSales[0], beforeSales[1], difference, sku, AbnormalDTO.T.BEFOREAMOUNT));
        }
    }

    /**
     * 计算出所有 历史利润率异常的sku
     * <p/>
     * 历史利润率指的是：
     * 上上周六 到 上周五 的利润率 对比 上个周期的利润率
     *
     * @param sku
     */
    private void fetchBeforeSalesProfit(String sku, List<AbnormalDTO> dtos) {
        DateTime monday = new DateTime(getMondayOfWeek());
        Float[] beforeProfit = new Float[2];
        for(int i = 1; i <= 2; i++) {
            //上周五 以及 往前同期（上上周五）
            DateTime day3 = monday.plusDays(i * (-3));
            //两个礼拜前的的礼拜六 以及 往前同期（三个礼拜前的礼拜六）
            DateTime day9 = monday.plusDays(i * (-9));
            MetricProfitService met = new MetricProfitService(day3.toDate(), day9.toDate(), null, sku, null);
            beforeProfit[i - 1] = met.calProfit().profitrate;
        }
        if(beforeProfit[0] > 0 && beforeProfit[1] > 0 && beforeProfit[0] <= (beforeProfit[1] * 0.95)) {
            float difference = (beforeProfit[1] - beforeProfit[0]) / beforeProfit[1] * 100;
            dtos.add(new AbnormalDTO(beforeProfit[0], beforeProfit[1], difference, sku, AbnormalDTO.T.BEFOREPROFIT));
        }
    }

    /**
     * 该 sku 过去四周同期的销售额的平均值
     *
     * @return
     */
    private float beforeMean(String sku, MetricProfitService met) {
        DateTime now = new DateTime().now();
        float beforeSales = 0;
        for(int i = 1; i <= 4; i++) {
            //每次都减去7天
            met.begin = now.plusDays(i * (-7)).toDate();
            met.end = now.plusDays(i * (-7)).toDate();
            met.sku = sku;
            beforeSales += met.esSaleFee();
        }
        return beforeSales / 4;
    }

    /**
     * 获取当前时间的星期一时间
     *
     * @return
     */
    public Date getMondayOfWeek() {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        //设置一周起始日期为星期一
        calendar.setFirstDayOfWeek(1);
        //设置格式
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        //获取当前周的星期一
        return calendar.getTime();
    }
}
