package jobs.PmDashboard;

import models.market.AmazonListingReview;
import models.market.Listing;
import models.view.dto.AbnormalDTO;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.db.helper.JpqlSelect;
import play.jobs.Job;
import query.ProductQuery;
import services.MetricProfitService;

import java.util.*;

/**
 * PM 首页异常信息处理 Job
 * <p/>
 * 会将所有的异常的数据计算出来缓存到redis
 * 然后在controller内根据条件去缓存获取对应的即可
 * <p/>
 * User: mac
 * Date: 14-3-21
 * Time: PM2:03
 */
public class AbnormalFetchJob extends Job {
    public static final String AbnormalDTO_DAY1SALEASMOUNT_CACHE = "abnormal_day1saleamount_info";
    public static final String AbnormalDTO_BEFORESALEASMOUNT_CACHE = "abnormal_beforesaleamount_info";
    public static final String AbnormalDTO_BEFORESALESPROFIT_CACHE = "abnormal_beforesalesprofit_info";
    public static final String AbnormalDTO_REVIEW_CACHE = "abnormal_review_info";



    @Override
    public void doJob() {
        //清空缓存
        Cache.delete(AbnormalDTO_DAY1SALEASMOUNT_CACHE);
        Cache.delete(AbnormalDTO_REVIEW_CACHE);
        Cache.delete(AbnormalDTO_BEFORESALEASMOUNT_CACHE);
        abnormal();
        long begin = System.currentTimeMillis();
        Logger.info("AbnormalFetchJob calculate.... [%sms]", System.currentTimeMillis() - begin);
    }

    /**
     * 异常信息的分析与计算
     *
     * @return
     */
    public void abnormal() {
        //获取所有的 sku
        List<String> skus = new ProductQuery().skus();

        for(String sku : skus) {
            //昨天销量异常
            fetchDay1SalesAmount(sku);
            //review异常
            fetchReview(sku);
            //历史销量异常
            fetchBeforeSalesAmount(sku);
            //历史利润率异常
            fetchBeforeSalesProfit(sku);
        }
    }

    /**
     * 计算出所有 昨天销售额异常的sku
     *
     * @param sku
     */
    private void fetchDay1SalesAmount(String sku) {
        List<AbnormalDTO> dtos = new ArrayList<AbnormalDTO>();
        DateTime day1 = new DateTime().now().plus(-1);
        MetricProfitService met = new MetricProfitService(day1.toDate(), day1.toDate(), null, sku, null);
        //昨天的销售额
        float day1Sales = met.esSaleFee();
        //过去四周同期平均值
        float mean = this.beforeMean(sku, met);
        //如果昨天销售额 小于 过去四周同期 销售额的平均值 20%或者以上，则视为异常 sku
        if(day1Sales <= (mean * 0.8)) {
            dtos.add(new AbnormalDTO(day1Sales, mean, sku, AbnormalDTO.T.DAY1));
        }
        Cache.add(AbnormalDTO_DAY1SALEASMOUNT_CACHE, dtos);
    }

    /**
     * 计算出所有review信息异常的sku
     *
     * @param sku
     */
    private void fetchReview(String sku) {
        List<AbnormalDTO> dtos = new ArrayList<AbnormalDTO>();
        List<String> listingIds = Listing.getAllListingBySKU(sku);
        List<AmazonListingReview> reviews = AmazonListingReview.find("listingId IN" + JpqlSelect.inlineParam(listingIds))
                .fetch();
        for(AmazonListingReview review : reviews) {
            if(review.rating <= 3) {
                dtos.add(new AbnormalDTO(sku, AbnormalDTO.T.REVIEW));
            }
        }
        Cache.add(AbnormalDTO_REVIEW_CACHE, dtos);
    }

    /**
     * 计算出所有 历史销售额异常的sku
     * <p/>
     * 历史销售额指的是：
     * 上上周六 到 上周五 的销售额 对比 上个周期的销售额
     *
     * @param sku
     */
    private void fetchBeforeSalesAmount(String sku) {
        List<AbnormalDTO> dtos = new ArrayList<AbnormalDTO>();
        DateTime monday = new DateTime(getMondayOfWeek());
        Float[] beforeSales = new Float[2];
        for(int i = 1; i <= 2; i++) {
            //上周五 以及 往前同期（上上周五）
            DateTime day3 = monday.plus(i * (-3));
            //两个礼拜前的的礼拜六 以及 往前同期（三个礼拜前的礼拜六）
            DateTime day9 = monday.plus(i * (-9));
            MetricProfitService met = new MetricProfitService(day3.toDate(), day9.toDate(), null, sku, null);
            beforeSales[i - 1] = met.esSaleFee();
        }
        if(beforeSales[0] <= (beforeSales[1] * 0.95)) {
            dtos.add(new AbnormalDTO(beforeSales[0], beforeSales[1], sku, AbnormalDTO.T.LAST));
        }
        Cache.add(AbnormalDTO_BEFORESALEASMOUNT_CACHE, dtos);
    }

    /**
     * 计算出所有 历史利润率异常的sku
     * <p/>
     * 历史利润率指的是：
     * 上上周六 到 上周五 的利润率 对比 上个周期的利润率
     *
     * @param sku
     */
    private void fetchBeforeSalesProfit(String sku) {
        List<AbnormalDTO> dtos = new ArrayList<AbnormalDTO>();
        DateTime monday = new DateTime(getMondayOfWeek());
        Float[] beforeSales = new Float[2];
        for(int i = 1; i <= 2; i++) {
            //上周五 以及 往前同期（上上周五）
            DateTime day3 = monday.plus(i * (-3));
            //两个礼拜前的的礼拜六 以及 往前同期（三个礼拜前的礼拜六）
            DateTime day9 = monday.plus(i * (-9));
            MetricProfitService met = new MetricProfitService(day3.toDate(), day9.toDate(), null, sku, null);
            beforeSales[i - 1] = met.calProfit().profitrate;
        }
        if(beforeSales[0] <= (beforeSales[1] * 0.95)) {
            dtos.add(new AbnormalDTO(beforeSales[0], beforeSales[1], sku, AbnormalDTO.T.LAST));
        }
        Cache.add(AbnormalDTO_BEFORESALESPROFIT_CACHE, dtos);
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
            met.begin = now.plus(i * (-7)).toDate();
            met.end = now.plus(i * (-7)).toDate();
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
