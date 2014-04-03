package jobs.PmDashboard;

import helper.DBUtils;
import helper.Dates;
import jobs.driver.BaseJob;
import models.market.Listing;
import models.view.dto.AbnormalDTO;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import query.ProductQuery;
import services.MetricProfitService;

import java.util.*;

/**
 * PM 首页异常信息处理 Job
 * <p/>
 * 会将所有的异常的数据计算出来缓存到 Redis
 * 然后在controller内根据条件去缓存获取对应的即可
 * <p/>
 * User: mac
 * Date: 14-3-21
 * Time: PM2:03
 */
public class AbnormalFetchJob extends BaseJob {
    public static final String RUNNING = "anormalfetchjob_running";
    public static final String AbnormalDTO_CACHE = "abnormal_info";

    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
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
        //获取所有的 sku
        List<String> skus = new ProductQuery().skus();

        Map<String, List<AbnormalDTO>> dtoMap = new HashMap<String, List<AbnormalDTO>>();
        //准备数据容器
        List<AbnormalDTO> salesQty = new ArrayList<AbnormalDTO>();
        List<AbnormalDTO> reviews = new ArrayList<AbnormalDTO>();
        List<AbnormalDTO> salesAmount = new ArrayList<AbnormalDTO>();
        List<AbnormalDTO> salesProfits = new ArrayList<AbnormalDTO>();
        for(String sku : skus) {
            fetchSalesQty(sku, salesQty);
            fetchReview(sku, reviews);
            fetchSalesAmount(sku, salesAmount);
            fetchSalesProfit(sku, salesProfits);
        }

        dtoMap.put(AbnormalDTO.T.SALESQTY.toString(), salesQty);
        dtoMap.put(AbnormalDTO.T.REVIEW.toString(), reviews);
        dtoMap.put(AbnormalDTO.T.SALESAMOUNT.toString(), salesAmount);
        dtoMap.put(AbnormalDTO.T.SALESPROFIT.toString(), salesProfits);
        //将数据添加到缓存内
        Cache.add(AbnormalDTO_CACHE, dtoMap);
        Cache.delete(RUNNING);
    }

    /**
     * 计算出所有销量异常的sku
     */
    private void fetchSalesQty(String sku, List<AbnormalDTO> dtos) {
        DateTime day1 = new DateTime().now().plusDays(-1);
        MetricProfitService met = new MetricProfitService(day1.toDate(), day1.toDate(), null, sku, null);
        //昨天的销量
        float day1Sales = met.esSaleQty();
        //过去四周同期平均值
        float mean = this.beforeMean(sku, met);
        //如果昨天销量 小于 过去四周同期 销量的平均值 20%或者以上，则视为异常 sku
        if(day1Sales > 0 && mean > 0 && day1Sales <= (mean * 0.98)) {
            float difference = (mean - day1Sales) / mean * 100;
            dtos.add(new AbnormalDTO(day1Sales, mean, difference, sku, AbnormalDTO.T.SALESQTY));
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
                SqlSelect.whereIn("listingId", listingIds))
                .where("rating <= 3");//.where("reviewDate >=?").param(DateTime.now().plusDays(-1).toDate())

        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            if(NumberUtils.stringToInt(row.get("count").toString()) > 0) {
                dtos.add(new AbnormalDTO(sku, AbnormalDTO.T.REVIEW));
            }
        }
    }

    /**
     * 销售额异常的sku
     * <p/>
     * 历史销售额指的是：
     * 上上周六 到 上周五 的销售额 对比 上个周期的销售额
     *
     * @param sku
     */
    private void fetchSalesAmount(String sku, List<AbnormalDTO> dtos) {
        DateTime monday = new DateTime(Dates.getMondayOfWeek());
        Float[] beforeSales = new Float[2];
        for(int i = 1; i <= 2; i++) {
            //上周五 以及 往前同期（上上周五）
            DateTime day3 = monday.plusDays(i * (-3));
            //两个礼拜前的的礼拜六 以及 往前同期（三个礼拜前的礼拜六）
            DateTime day9 = monday.plusDays(i * (-9));
            MetricProfitService met = new MetricProfitService(day3.toDate(), day9.toDate(), null, sku, null);
            beforeSales[i - 1] = met.esSaleFee();
        }
        if(beforeSales[0] > 0 && beforeSales[1] > 0 && beforeSales[0] <= (beforeSales[1] * 0.98)) {
            float difference = (beforeSales[1] - beforeSales[0]) / beforeSales[1] * 100;
            dtos.add(new AbnormalDTO(beforeSales[0], beforeSales[1], difference, sku, AbnormalDTO.T.SALESAMOUNT));
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
    private void fetchSalesProfit(String sku, List<AbnormalDTO> dtos) {
        DateTime monday = new DateTime(Dates.getMondayOfWeek());
        Float[] beforeProfit = new Float[2];
        for(int i = 1; i <= 2; i++) {
            //上周五 以及 往前同期（上上周五）
            DateTime day3 = monday.plusDays(i * (-3));
            //两个礼拜前的的礼拜六 以及 往前同期（三个礼拜前的礼拜六）
            DateTime day9 = monday.plusDays(i * (-9));
            MetricProfitService met = new MetricProfitService(day3.toDate(), day9.toDate(), null, sku, null);
            beforeProfit[i - 1] = met.calProfit().profitrate;
        }
        if(beforeProfit[0] > 0 && beforeProfit[1] > 0 && beforeProfit[0] <= (beforeProfit[1] * 0.98)) {
            float difference = (beforeProfit[1] - beforeProfit[0]) / beforeProfit[1] * 100;
            dtos.add(new AbnormalDTO(beforeProfit[0], beforeProfit[1], difference, sku, AbnormalDTO.T.SALESPROFIT));
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
            beforeSales += met.esSaleQty();
        }
        return beforeSales / 4;
    }
}
