package jobs;

import models.market.Listing;

import java.util.concurrent.TimeUnit;

/**
 * @deprecated 只作为工具方法用
 */
public class ListingSchedulJob {

    /**
     * 根据 Listing 计算抓取的间隔
     */
    public static long calInterval(Listing listing) {
        /**
         * 编写计算下一次抓取的间隔的计算算法;
         * 主旨:
         * - 情况变好, 增大检查间隔
         * - 情况变坏, 减小检查间隔
         */
        // 默认周期为 15 分钟; 900s
        long defaultInterval = TimeUnit.MINUTES.toMillis(15);

        // saleRank, review rating, reviews, likes, offers

        // 排名的修正, 需要减去
        long saleRankFix = 0;
        if(listing.saleRank != null) { // Max 120s
            if(listing.saleRank < 5000) saleRankFix += TimeUnit.SECONDS.toMillis(20);
            if(listing.saleRank < 3000) saleRankFix += TimeUnit.SECONDS.toMillis(30);
            if(listing.saleRank < 1000) saleRankFix += TimeUnit.SECONDS.toMillis(40);
            if(listing.saleRank < 500) saleRankFix += TimeUnit.SECONDS.toMillis(50);
            if(listing.saleRank < 100) saleRankFix += TimeUnit.SECONDS.toMillis(60);
        }

        long reviewRatingFix = 0;
        if(listing.rating != null) { // max 120s
            // 评论很好了, 可以增加检查的间隔
            if(listing.rating > 4.5) reviewRatingFix -= TimeUnit.SECONDS.toMillis(20);
            if(listing.rating > 4) reviewRatingFix += TimeUnit.SECONDS.toMillis(20);
            if(listing.rating > 3) reviewRatingFix += TimeUnit.SECONDS.toMillis(30);
            // 比较严重了
            if(listing.rating > 1) reviewRatingFix += TimeUnit.SECONDS.toMillis(50);
        }

        long reviewsFix = 0;
        if(listing.reviews != null) { // max 30s
            if(listing.reviews > 100) reviewsFix += TimeUnit.SECONDS.toMillis(10);
            if(listing.reviews > 30) reviewsFix += TimeUnit.SECONDS.toMillis(10);
            if(listing.reviews > 5) reviewsFix += TimeUnit.SECONDS.toMillis(10);
        }

        long likesFix = 0;
        if(listing.likes != null) { // max 30s
            if(listing.likes > 50) likesFix += TimeUnit.SECONDS.toMillis(10);
            if(listing.likes > 30) likesFix += TimeUnit.SECONDS.toMillis(20);
        }
        long interval = (defaultInterval - saleRankFix - reviewRatingFix - reviewsFix - likesFix);
        return interval < 0 ? TimeUnit.SECONDS.toMillis(30) : interval;
    }
}
