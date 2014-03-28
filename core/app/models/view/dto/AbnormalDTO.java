package models.view.dto;

import models.market.AmazonListingReview;
import models.market.Listing;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
         * 昨天销售额与同期对比
         */
        DAY1,

        /**
         * 上周销售额与上上周对比
         */
        BEFOREAMOUNT,

        /**
         * 上周利润率与上上周对比
         */
        BEFOREPROFIT
    }

    /**
     * 异常信息的类型
     */
    public T abnormalType;

    public AbnormalDTO() {
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

    public AbnormalDTO(float today, float before, float difference, String sku, T abnormalType) {
        this.today = today;
        this.before = before;
        this.difference = difference;
        this.sku = sku;
        this.abnormalType = abnormalType;
    }

    public AbnormalDTO(String sku, T abnormalType) {
        this.sku = sku;
        this.abnormalType = abnormalType;
    }
}
