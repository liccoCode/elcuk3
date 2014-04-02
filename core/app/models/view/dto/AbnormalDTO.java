package models.view.dto;

import jobs.PmDashboard.AbnormalFetchJob;
import jobs.driver.GJob;
import models.User;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.product.Category;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.utils.FastRuntimeException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PM 首页异常信息
 * <p/>
 * User: mac
 * Date: 14-3-21
 * Time: PM2:22
 */
public class AbnormalDTO implements Serializable {
    /**
     * 今天的数据
     */
    public float today = 0;

    /**
     * 上个周期的数据
     */
    public float before = 0;

    /**
     * 当前数据与上个周期内的数据的差值百分比
     */
    public float difference = 0;

    /**
     * sku
     */
    public String sku;

    public enum T {
        /**
         * review 信息异常
         */
        REVIEW,

        /**
         * 销量异常
         */
        SALESQTY,

        /**
         * 销售额异常
         */
        SALESAMOUNT,

        /**
         * 利润率异常
         */
        SALESPROFIT
    }

    /**
     * 异常信息的类型
     */
    public T abnormalType;

    public AbnormalDTO() {
    }

    public AbnormalDTO(T abnormalType) {
        this.abnormalType = abnormalType;
    }

    public AbnormalDTO(String sku, T abnormalType) {
        this.sku = sku;
        this.abnormalType = abnormalType;
    }

    public AbnormalDTO(float today, float before, float difference, String sku, T abnormalType) {
        this.today = today;
        this.before = before;
        this.difference = difference;
        this.sku = sku;
        this.abnormalType = abnormalType;
    }

    public List<AbnormalDTO> query(User user) {
        Map<String, List<AbnormalDTO>> dtoMap = Cache.get(AbnormalFetchJob.AbnormalDTO_CACHE, Map.class);
        if(dtoMap == null || dtoMap.size() == 0) {
            if(!AbnormalFetchJob.isRnning()) {
                GJob.perform(AbnormalFetchJob.class.getName(), new HashMap<String, Object>());
                Cache.add(AbnormalFetchJob.RUNNING, AbnormalFetchJob.RUNNING);
            }
            throw new FastRuntimeException("正在后台计算中, 请 10 mn 后再尝试");
        }
        List<String> categoryIds = new ArrayList<String>();
        for(Category category : User.getTeamCategorys(user)) {
            categoryIds.add(category.categoryId);
        }
        //skus 集合
        List<String> skus = Category.getSKUs(categoryIds);

        List<AbnormalDTO> dtos = dtoMap.get(this.abnormalType.toString());
        List<AbnormalDTO> filterResult = abnormalFilter(skus, dtos);
        return filterResult;
    }


    /**
     * 对异常对象进行过滤筛选
     *
     * @return
     */
    public List<AbnormalDTO> abnormalFilter(List<String> skus, List<AbnormalDTO> dtos) {
        List<AbnormalDTO> filterResult = new ArrayList<AbnormalDTO>();
        for(AbnormalDTO dto : dtos) {
            if(skus.contains(dto.sku) && dto.difference >= this.difference) filterResult.add(dto);
        }
        return filterResult;
    }

    /**
     * 获取昨天新增的 AmazonListingReview
     *
     * @return
     */
    public List<AmazonListingReview> reviews() {
        List<String> listingIds = Listing.getAllListingBySKU(this.sku);
        DateTime yesterday = DateTime.now().plusDays(-1);
        List<AmazonListingReview> listingReviews = new ArrayList<AmazonListingReview>();
        if(listingIds.size() > 0) {
            listingReviews = AmazonListingReview.find("rating <= 3 AND listingId IN" + SqlSelect.inlineParam
                    (listingIds)).fetch();// + "AND " + "reviewDate >=?", yesterday.toDate()
        }
        return listingReviews;
    }


}
