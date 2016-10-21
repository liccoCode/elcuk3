package models.view.dto;

import helper.Dates;
import jobs.PmDashboard.AbnormalFetchJob;
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
import java.util.Date;
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
    private static final long serialVersionUID = 6545835762456678553L;

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

    public AbnormalDTO(T abnormalType, float difference) {
        this.abnormalType = abnormalType;
        this.difference = difference;
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
                //这里会存在一次创建几个任务的情况，但是不会影响后台 Job 的执行次数，当 Job 发现缓存中存在 Running 这个 Key 时，会直接返回然后将当前任务从 DB 内删除，并不会执行计算
                new AbnormalFetchJob().now();
            }
            throw new FastRuntimeException("正在后台计算中, 请 10 mn 后再尝试");
        }
        List<String> categoryIds = User.getTeamCategorys(user);
        //skus 集合
        List<String> skus = Category.getSKUs(categoryIds);

        List<AbnormalDTO> dtos = dtoMap.get(this.abnormalType.toString());
        return AbnormalDTO.abnormalFilter(skus, dtos, this);
    }

    /**
     * 返回所有异常的 总和size
     *
     * @param user
     * @return
     */
    public static long queryAbnormalDTOListSize(User user) {
        List<String> categoryIds = User.getTeamCategorys(user);
        //skus 集合
        List<String> skus = Category.getSKUs(categoryIds);

        Map<String, List<AbnormalDTO>> dtoMap = Cache.get(AbnormalFetchJob.AbnormalDTO_CACHE, Map.class);
        if(dtoMap != null) {
            long abnormalSize = 0;
            for(String key : dtoMap.keySet()) {
                List<AbnormalDTO> abnormalDTOs = dtoMap.get(key);
                //执行过滤
                abnormalDTOs = AbnormalDTO.abnormalFilter(skus, abnormalDTOs, new AbnormalDTO());

                abnormalSize += abnormalDTOs == null ? 0 : abnormalDTOs.size();
            }
            return abnormalSize;
        }
        return 0;
    }

    /**
     * 对异常对象进行过滤筛选
     *
     * @return
     */
    public static List<AbnormalDTO> abnormalFilter(List<String> skus, List<AbnormalDTO> dtos, AbnormalDTO arg) {
        List<AbnormalDTO> filterResult = new ArrayList<>();
        for(AbnormalDTO dto : dtos) {
            if(skus.contains(dto.sku) && dto.difference >= (arg.difference / 100)) filterResult.add(dto);

        }
        return filterResult;
    }

    /**
     * 获取昨天新增的 AmazonListingReview
     *
     * @return
     */
    public List<AmazonListingReview> reviews() {
        DateTime day1 = DateTime.now().plusDays(-1);
        Date day1begin = Dates.morning(day1.toDate());
        Date day1end = Dates.night(day1.toDate());

        List<String> listingIds = Listing.getAllListingBySKU(this.sku);
        List<AmazonListingReview> listingReviews = new ArrayList<>();
        if(listingIds.size() > 0) {
            listingReviews = AmazonListingReview
                    .find("rating <= 3 AND listingId IN " + SqlSelect.inlineParam(listingIds) + " AND reviewDate >=? " +
                            "AND " + "reviewDate <=?", day1begin, day1end).fetch();
        }
        return listingReviews;
    }


}
