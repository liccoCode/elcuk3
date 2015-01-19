package models.market;

import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 记录 Listing 状态的变化过程
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-8
 * Time: AM10:17
 */
@Entity
public class ListingStateRecord extends Model {

    @ManyToOne(fetch = FetchType.LAZY)
    public Listing listing;

    public enum S {
        /**
         * 已经正常开始进行销售
         */
        SELLING,
        /**
         * 完全下架, 如果可以还能够重新上架
         */
        DOWN
    }

    /**
     * Listing 的状态
     */
    @Enumerated(EnumType.STRING)
    public S state;

    /**
     * Listing 状态变更日期
     */
    public Date changedDate = DateTime.now().toDate();

    /**
     * 为所有 Listing 做一个状态的变化过程记录的初始化
     */
    public static void initAllListingRecords() {
        Date firstReviewDate = AmazonListingReview.firstReviewDate();
        for(Listing listing : Listing.<Listing>findAll()) {
            listing.recordingListingState(firstReviewDate);
        }
    }

    public static List<ListingStateRecord> getCacheByAsinAndMarket(String asin, M market) {
        String cacheKey = ListingStateRecord.cacheKey(asin, market);
        List<ListingStateRecord> cachedRecords = Cache.get(cacheKey, List.class);
        if(cachedRecords != null) {
            return cachedRecords;
        } else {
            ListingStateRecord.initRecordsByAsinAndMarket(asin, market); //初始化缓存
            return Cache.get(cacheKey, List.class);//重新获取缓存
        }
    }

    public static void initRecordsByAsinAndMarket(String asin, M market) {
        List<ListingStateRecord> records = ListingStateRecord
                .find("SELECT DISTINCT ls FROM ListingStateRecord ls LEFT JOIN ls.listing li WHERE 1=1 AND li.asin = ?" +
                        " AND li.market = ?", asin.toUpperCase(), market).fetch();
        Cache.add(ListingStateRecord.cacheKey(asin, market), records, "8h");
    }

    /**
     * 增加单个到缓存中
     */
    public void pushRecordToCache() {
        String cacheKey = ListingStateRecord.cacheKey(this.listing.asin, this.listing.market);
        List<ListingStateRecord> cachedRecords = Cache.get(cacheKey, List.class);
        if(cachedRecords != null) {
            cachedRecords.add(this);
        } else {
            cachedRecords = new ArrayList<ListingStateRecord>();
        }
        Cache.delete(cacheKey);
        Cache.add(cacheKey, cachedRecords, "8h");
    }

    public static String cacheKey(String asin, M market) {
        return String.format("%s_%s_listing_state_record_cache",
                asin, market.nickName().toLowerCase()
        );
    }
}
