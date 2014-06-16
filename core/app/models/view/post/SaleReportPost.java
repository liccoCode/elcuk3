package models.view.post;

import controllers.Login;
import helper.Caches;
import helper.LogUtils;
import models.User;
import models.market.M;
import models.market.Selling;
import models.product.Category;
import models.view.dto.SaleReportDTO;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
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
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-6-9
 * Time: AM10:17
 */
public class SaleReportPost {
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
     * 拥有权限的Selling集合
     */
    private List<String> sellingIds;

    public SaleReportPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.from = now.toDate();
        this.to = now.toDate();

    }

    public List<String> params() {
        User user = Login.current();
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


    public List<SaleReportDTO> query() {
        User user = Login.current();
        String key = Caches.Q.cacheKey(this.from, this.to, (this.market != null ? this.market : "AllMarket"),
                user.username,
                "SaleReports");
        List<SaleReportDTO> dtos = Cache.get(key, List.class);
        if(dtos == null) {
            sellingIds = params();
            new SaleReportsCalculateJob(key).start();
            throw new FastRuntimeException("正在后台计算中，请稍后再尝试~");
        }
        return dtos;
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

    /**
     * 销售报表计算Job
     */
    class SaleReportsCalculateJob extends Thread {
        private String key;

        public SaleReportsCalculateJob(String key) {
            this.key = key;
        }

        public void run() {
            long begin = System.currentTimeMillis();
            List<SaleReportDTO> dtos = new ArrayList<SaleReportDTO>();
            synchronized(key.intern()) {
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
                Cache.add(key, dtos);
            }
            LogUtils.JOBLOG.info(String.format("SaleReportPost execute with key: %s, Calculate time: %s", key,
                    System.currentTimeMillis() - begin));
        }
    }
}
