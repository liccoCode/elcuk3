package jobs;

import helper.Caches;
import helper.LogUtils;
import jobs.driver.BaseJob;
import models.market.M;
import models.market.Selling;
import models.view.dto.SaleReportDTO;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import services.MetricSaleReportService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 销售统计报表处理 Job
 * <p/>
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-6-16
 * Time: PM4:26
 */
public class SaleReportsJob extends BaseJob {
    private MetricSaleReportService service = new MetricSaleReportService();

    public Date from;
    public Date to;
    public M market;
    public String search;
    /**
     * Job执行时获取不到对应的登陆信息，只好手动传递过来好了，囧
     */
    public String username;

    public List<String> sellingIds;

    public SaleReportsJob(Date from, Date to, M market, String search, String username, List<String> sellingIds) {
        this.from = from;
        this.to = to;
        this.market = market;
        this.search = search;
        this.username = username;
        this.sellingIds = sellingIds;
    }

    public static boolean isRunning(String key) {
        return StringUtils.isNotBlank(Cache.get(key + "_running", String.class));
    }

    public String buildKey() {
        return Caches.Q.cacheKey(this.from, this.to, (this.market != null ? this.market : "AllMarket"),
                (this.search != null ? this.search : ""), username, "SaleReports");
    }


    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
        long begin = System.currentTimeMillis();


        String runningKey = buildKey() + "_running";
        Cache.add(runningKey, runningKey);

        List<SaleReportDTO> dtos = new ArrayList<>();

        //匹配 SellingID 中的 Category
        Pattern CATEGORYID = Pattern.compile("\\d*");
        for(String sid : sellingIds) {
            Matcher matcher = CATEGORYID.matcher(sid);
            String categoryId = matcher.find() ? matcher.group() : null;
            String sku = Selling.sidToSKU(sid);
            M market = Selling.sidToMarket(sid);

            Float sales = service.countSales(from, to, market, sid);
            Float salesAmount = service.countSalesAmount(from, to, market, sid);

            if(sales != 0 || salesAmount != 0) {
                dtos.add(new SaleReportDTO(categoryId.length() <= 3 ? categoryId : "", sku, sid, market,
                        (float) (Math.round(sales * 100)) / 100, (float) (Math.round(salesAmount * 100)) / 100));
            }
        }

        Cache.add(buildKey(), dtos, "4h");
        Cache.delete(runningKey);
        LogUtils.JOBLOG.info(String.format("SaleReportPost execute with key: %s, Calculate time: %s", buildKey(),
                System.currentTimeMillis() - begin));
    }
}
