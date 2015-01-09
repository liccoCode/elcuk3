package models.market;

import org.joda.time.DateTime;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

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
}
