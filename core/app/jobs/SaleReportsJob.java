package jobs;

import controllers.Login;
import controllers.Secure;
import helper.Caches;
import helper.LogUtils;
import jobs.driver.BaseJob;
import models.User;
import models.market.M;
import models.market.Selling;
import models.product.Category;
import models.view.dto.SaleReportDTO;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.utils.FastRuntimeException;
import services.MetricSaleReportService;

import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * 匹配 SKU
     */
    public final Pattern SKU = Pattern.compile("^sku:([0-9a-zA-Z]+-[0-9a-zA-Z]+-?[0-9a-zA-Z]*)");

    /**
     * 匹配 Selling
     */
    public final Pattern SELLING = Pattern.compile("^selling:\\w*");

    /**
     * 匹配 Category
     */
    public final Pattern CATEGORY = Pattern.compile("^category:(\\d*)");

    public Date from;
    public Date to;
    public M market;
    public String search;

    /**
     * Job执行时获取不到对应的登陆信息，只好手动传递过来好了，囧
     */
    public String username;

    public SaleReportsJob(Date from, Date to, M market, String search, String username) {
        this.from = from;
        this.to = to;
        this.market = market;
        this.search = search;
        this.username = username;
    }

    public static boolean isRunning(String key) {
        return StringUtils.isNotBlank(Cache.get(key + "_running", String.class));
    }

    public String buildKey() {
        User user = User.findByUserName(username);
        return Caches.Q.cacheKey(this.from, this.to, (this.market != null ? this.market : "AllMarket"),
                (this.search != null ? this.search : ""), user.username, "SaleReports");
    }

    public List<String> params() {
        User user = User.findByUserName(username);
        //category 权限
        List<String> categorys = User.getTeamCategorys(user);
        //sku 权限
        List<String> skus = User.getSkus(user);
        //selling 权限
        List<String> sellings = User.getSellings(user);

        String categoryId = isSearchCategory();
        if(StringUtils.isNotBlank(categoryId)) {
            if(!categorys.contains(categoryId)) {
                throw new FastRuntimeException(String.format("Sorry,没有查看 Category: %s 销售数据的权限", categoryId));
            }
            skus = Category.getSKUs(categoryId);
            return Selling.sids(skus);
        }

        String sku = isSearchSKU();
        if(StringUtils.isNotBlank(sku)) {
            if(!skus.contains(sku)) {
                throw new FastRuntimeException(String.format("Sorry,没有查看 SKU: %s 销售数据的权限", sku));
            }
            return Selling.sids(sku);

        }
        String sellingId = isSearchSelling();
        if(StringUtils.isNotBlank(sellingId)) {
            if(!sellings.contains(sellingId)) {
                throw new FastRuntimeException(String.format("Sorry,没有查看 Selling: %s 销售数据的权限", sellingId));
            }
            return Arrays.asList(sellingId);
        }
        return sellings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
        long begin = System.currentTimeMillis();

        List<String> sellingIds = params();
        if(this.market != null)
            org.apache.commons.collections.CollectionUtils.filter(sellingIds, new SellingIdPredicate());

        String runningKey = buildKey() + "_running";
        Cache.add(runningKey, runningKey);

        List<SaleReportDTO> dtos = new ArrayList<SaleReportDTO>();

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


    /**
     * 根据正则表达式搜索是否有类似 sku:123 这样的搜索如果有则直接进行 sku 搜索
     *
     * @return
     */
    public String isSearchSKU() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = SKU.matcher(this.search);
            if(matcher.find()) return matcher.group(1);
        }
        return null;
    }

    /**
     * 根据正则表达式搜索是否有类似 selling:123 这样的搜索如果有则直接进行 selling 搜索
     *
     * @return
     */
    public String isSearchSelling() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = SELLING.matcher(this.search);
            if(matcher.find()) return StringUtils.split(this.search, "selling:")[0];
        }
        return null;
    }

    /**
     * 根据正则表达式搜索是否有类似 category:123 这样的搜索如果有则直接进行 category 搜索
     *
     * @return
     */
    public String isSearchCategory() {
        if(StringUtils.isNotBlank(this.search)) {
            Matcher matcher = CATEGORY.matcher(this.search);
            if(matcher.find()) return matcher.group(1);
        }
        return null;
    }

    private class SellingIdPredicate implements org.apache.commons.collections.Predicate {
        @Override
        public boolean evaluate(Object o) {
            String sellingId = (String) o;
            return StringUtils.endsWithIgnoreCase(Selling.sidToMarket(sellingId).name(), market.name());
        }
    }
}
