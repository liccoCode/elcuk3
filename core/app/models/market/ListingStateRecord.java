package models.market;

import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
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
    private static final long serialVersionUID = -7279223067180390522L;

    @ManyToOne
    public Selling selling;

    public enum S {
        /**
         * 已经正常开始进行销售
         */
        SELLING,
        /**
         * 完全下架, 如果可以还能够重新上架
         */
        DOWN,
        /**
         * 新Listing
         */
        NEW
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

    public static List<ListingStateRecord> getCacheByListingId(String listingId) {
        String cacheKey = ListingStateRecord.cacheKey(listingId);
        List<ListingStateRecord> cachedRecords = Cache.get(cacheKey, List.class);
        if(cachedRecords != null) {
            return cachedRecords;
        } else {
            ListingStateRecord.initRecordsByAsinAndMarket(listingId); //初始化缓存
            return Cache.get(cacheKey, List.class);//重新获取缓存
        }
    }

    public static void initRecordsByAsinAndMarket(String listingId) {
        List<ListingStateRecord> records = ListingStateRecord.find("listing_listingId = ?", listingId).fetch();
        Cache.add(ListingStateRecord.cacheKey(listingId), records, "8h");
    }

    /**
     * 增加单个到缓存中
     */
    public void pushRecordToCache() {
        String cacheKey = ListingStateRecord.cacheKey(this.selling.sellingId);
        List<ListingStateRecord> cachedRecords = Cache.get(cacheKey, List.class);
        if(cachedRecords != null) {
            cachedRecords.add(this);
        } else {
            cachedRecords = new ArrayList<>();
        }
        Cache.delete(cacheKey);
        Cache.add(cacheKey, cachedRecords, "8h");
    }

    public static String cacheKey(String listingId) {
        return String.format("%s_listing_state_record_cache", listingId);
    }
}
