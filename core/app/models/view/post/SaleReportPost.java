package models.view.post;

import controllers.Login;
import helper.Caches;
import jobs.SaleReportsJob;
import models.User;
import models.market.M;
import models.market.Selling;
import models.product.Category;
import models.view.dto.SaleReportDTO;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.utils.FastRuntimeException;

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
                (this.search != null ? this.search : ""), user.username, "SaleReports");

        List<SaleReportDTO> dtos = Cache.get(key, List.class);
        if(dtos == null) {
            if(!SaleReportsJob.isRunning(key)) {
                List<String> sellingIds = params();
                if(this.market != null) {
                    org.apache.commons.collections.CollectionUtils.filter(sellingIds, new SellingIdPredicate());
                }
                new SaleReportsJob(this.from, this.to, this.market, this.search, user.username, sellingIds).now();
            }
            throw new FastRuntimeException("已经在后台计算中，请于 10min 后再来查看结果~");
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

    private class SellingIdPredicate implements org.apache.commons.collections.Predicate {
        @Override
        public boolean evaluate(Object o) {
            String sellingId = (String) o;
            return StringUtils.endsWithIgnoreCase(Selling.sidToMarket(sellingId).name(), market.name());
        }
    }
}
