package models.view.post;

import controllers.Login;
import models.User;
import models.market.Listing;
import models.market.M;
import models.product.Category;
import models.view.dto.SaleReportDTO;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;
import services.MetricSaleReportService;

import java.util.ArrayList;
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
public class SaleReportPost extends Post<SaleReportDTO> {
    private MetricSaleReportService service = new MetricSaleReportService();

    /**
     * 匹配 SKU
     */
    public final Pattern SKU = Pattern.compile("^sku:(\\w*)$");

    /**
     * 匹配 Selling
     */
    public final Pattern SELLING = Pattern.compile("^selling:(\\w*)$");

    /**
     * 匹配 Category
     */
    public final Pattern CATEGORY = Pattern.compile("^category:(\\w*)$");


    public Date from;
    public Date to;
    public M market;
    public String search;

    public SaleReportPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.from = now.toDate();
        this.to = now.toDate();

    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        String sku = isSearchSKU();
        if(StringUtils.isNotBlank(sku)) {
            sbd.append("sku=?");
            params.add(sku);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }

        String sellingId = isSearchSKU();
        if(StringUtils.isNotBlank(sellingId)) {
            sbd.append("sellingId=?");
            params.add(sellingId);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }

        String categoryId = isSearchSKU();
        if(StringUtils.isNotBlank(categoryId)) {
            sbd.append("categoryId=?");
            params.add(categoryId);
            return new F.T2<String, List<Object>>(sbd.toString(), params);
        }

        if(this.market != null) {
            sbd.append("");
        }
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }

    public List<SaleReportDTO> query() {
        F.T2<String, List<Object>> params = params();

        return new ArrayList<SaleReportDTO>();
    }

    /**
     * 当前用户拥有的 Selling 集合
     */
    private List<String> sellings() {
        List<String> sellings = new ArrayList<String>();

        User user = Login.current();
        //当前用户的 Category 权限
        List<String> cates = User.getTeamCategorys(user);
        //sku 权限
        List<String> skus = Category.getSKUs(cates);
        //Listing  权限
        List<String> listings = new ArrayList<String>();
        for(String sku : skus) {
            listings.addAll(Listing.getAllListingBySKU(sku));
        }

        //
        return sellings;
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
            if(matcher.find()) return matcher.group(1);
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
}
