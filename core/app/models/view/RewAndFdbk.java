package models.view;

import helper.Cached;
import helper.Caches;
import helper.Dates;
import models.market.AmazonListingReview;
import models.market.Feedback;
import models.support.Ticket;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.utils.FastRuntimeException;
import query.OrderItemQuery;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 给前台使用一个 DTO , 这个里面的数据不需要修改, 只需要读取读取展示即可.
 * User: wyattpan
 * Date: 8/9/12
 * Time: 10:28 AM
 */
public class RewAndFdbk {

    public RewAndFdbk() {
    }

    /**
     * 可以是 SKU 或者也可以是 sid
     */
    public String fid;

    /**
     * 负评比例 负评个数/销量
     */
    public Float negtiveRatio;

    /**
     * 销量
     */
    public Float sales;

    /*
   一星到五星的 Review 数量
    */

    public int s1;
    public int s2;
    public int s3;
    public int s4;
    public int s5;

    /**
     * 当前还是负评的个数
     */
    public int negtive;

    /**
     * 无论现在还是以前, 只有是是负评就统计进来
     */
    public int historyNegtive;

    /**
     * rating 的分数降低了的
     */
    public int failed;

    /**
     * 成功处理的 Ticket
     */
    public int success;

    /**
     * 成功比率
     */
    public float successRatio;

    public int total;


    /**
     * 处理成功的 Review 数量
     */
    public int updateSucc;

    public RewAndFdbk review(AmazonListingReview review) {
        if(review.ticket != null) {
            if(review.ticket.type != Ticket.T.REVIEW)
                throw new FastRuntimeException("此方法只处理 Review.");

            // 被成功处理的
            if(review.ticket.isSuccess) this.success++;

            // 记录历史的负评
            if(review.lastRating < review.rating && review.ticket.isSuccess)
                this.historyNegtive++;
        }

        // 现在还是负评价的
        if(review.rating < 4) {
            this.negtive++;
            this.historyNegtive++;
        }

        // 分值被降低了的
        if(review.lastRating > review.rating)
            this.failed++;

        // 几星?
        switch(review.rating.intValue()) {
            case 1:
                this.s1++;
                break;
            case 2:
                this.s2++;
                break;
            case 3:
                this.s3++;
                break;
            case 4:
                this.s4++;
                break;
            case 5:
                this.s5++;
                break;
        }


        return this;
    }

    private RewAndFdbk feedback(Feedback fdbk) {
        if(fdbk.ticket != null) {
            if(fdbk.ticket.type != Ticket.T.FEEDBACK)
                throw new FastRuntimeException("此方法只处理 Feedback.");

            // 被成功处理的
            if(fdbk.ticket.isSuccess) this.success++;

            // 记录历史的负评
            if(fdbk.ticket.isSuccess)
                this.historyNegtive++;

        }

        // 现在还是负评价的
        if(fdbk.score < 4) {
            this.negtive++;
            this.historyNegtive++;
        }

        // 几星?
        switch(fdbk.score.intValue()) {
            case 1:
                this.s1++;
                break;
            case 2:
                this.s2++;
                break;
            case 3:
                this.s3++;
                break;
            case 4:
                this.s4++;
                break;
            case 5:
                this.s5++;
                break;
        }
        return this;
    }

    /**
     * 统计 Review 所需要的数据
     *
     * @param from
     * @param to
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("30mn")
    public static List<RewAndFdbk> reviews(Date from, Date to) {
        String cackeKey = String.format("RewAndFdbk.review.%s", Caches.Q.cacheKey(from, to));
        List<RewAndFdbk> reviews = Cache.get(cackeKey, List.class);
        if(reviews != null) return reviews;
        synchronized(RewAndFdbk.class) {
            reviews = Cache.get(cackeKey, List.class);
            if(reviews != null) return reviews;
            reviews = new ArrayList<RewAndFdbk>();

            Map<String, RewAndFdbk> groupBySku = new HashMap<String, RewAndFdbk>();

            Date begin = Dates.morning(from);
            Date end = Dates.night(to);

            List<AmazonListingReview> calReviews = AmazonListingReview.find("createDate>=? AND createDate<=? AND listing.product IS NOT NULL", begin, end).fetch();
            for(AmazonListingReview r : calReviews) {
                String fid = null;
                if(r.ticket != null) fid = r.ticket.fid;
                else fid = r.listing.product.sku;
                if(!groupBySku.containsKey(fid))
                    groupBySku.put(fid, new RewAndFdbk().review(r));
                else
                    groupBySku.get(fid).review(r);
            }

            // 销量...
            Map<String, AtomicInteger> skuSales = OrderItemQuery.skuSales();

            // 总数
            for(String sku : groupBySku.keySet()) {
                RewAndFdbk rvw = groupBySku.get(sku);
                rvw.fid = sku;
                rvw.total = (int) AmazonListingReview.count("listing.product.sku=?", sku);

                Number number = skuSales.get(sku);
                if(number == null) {
                    Logger.warn("SKU (%s) no sales.", sku);
                } else
                    rvw.sales = number.floatValue();
                if(rvw.sales == null) rvw.sales = 100000f;

                rvw.negtiveRatio = rvw.historyNegtive / rvw.sales;
                rvw.successRatio = rvw.historyNegtive > 0 ? (rvw.success / (rvw.historyNegtive * 1f)) : 0;
                reviews.add(rvw);
            }
            Cache.add(cackeKey, reviews, "30mn");
        }
        return reviews;
    }

    @SuppressWarnings("unchecked")
    @Cached("30mn")
    public static List<RewAndFdbk> feedbacks(Date from, Date to) {
        String cacheKey = String.format("RewAndFdbk.feedback.%s", Caches.Q.cacheKey(from, to));
        List<RewAndFdbk> feedbacks = Cache.get(cacheKey, List.class);
        if(feedbacks != null) return feedbacks;

        synchronized(RewAndFdbk.class) {
            feedbacks = Cache.get(cacheKey, List.class);
            if(feedbacks != null) return feedbacks;

            feedbacks = new ArrayList<RewAndFdbk>();
            Date begin = Dates.morning(from);
            Date end = Dates.night(to);

            Map<String, RewAndFdbk> groupBySku = new HashMap<String, RewAndFdbk>();

            List<Feedback> feedbackList = Feedback.find("createDate>=? AND createDate<=?", begin, end).fetch();
            for(Feedback fdbk : feedbackList) {
                String fid = OrderItemQuery.feedbackSKU(fdbk);
                if(StringUtils.isBlank(fid)) continue;

                if(!groupBySku.containsKey(fid))
                    groupBySku.put(fid, new RewAndFdbk().feedback(fdbk));
                else
                    groupBySku.get(fid).feedback(fdbk);
            }

            // 销量...
            Map<String, AtomicInteger> skuSales = OrderItemQuery.skuSales();

            // 总数
            for(String sku : groupBySku.keySet()) {
                RewAndFdbk rvw = groupBySku.get(sku);
                rvw.fid = sku;
                rvw.total = OrderItemQuery.skuFeedbackCount(sku);

                Number number = skuSales.get(sku);
                if(number == null) {
                    Logger.warn("SKU (%s) no sales.", sku);
                } else
                    rvw.sales = number.floatValue();
                if(rvw.sales == null) rvw.sales = 100000f;

                rvw.negtiveRatio = rvw.historyNegtive / rvw.sales;
                rvw.successRatio = rvw.historyNegtive > 0 ? (rvw.success / (rvw.historyNegtive * 1f)) : 0;
                feedbacks.add(rvw);
            }
            Cache.add(cacheKey, feedbacks, "30mn");
        }

        return feedbacks;
    }


    public static void sortByColumn(List<RewAndFdbk> rewAndFdbks, String c) {
        String column = c;
        if(StringUtils.isBlank(c)) column = "negtiveRatio";

        final String finalColumn = column;
        Collections.sort(rewAndFdbks, new Comparator<RewAndFdbk>() {
            @Override
            public int compare(RewAndFdbk r1, RewAndFdbk r2) {
                if(finalColumn.equals("negtiveRatio"))
                    return (int) ((r2.negtiveRatio - r1.negtiveRatio) * 100000/*为了精确排序*/);
                else if(finalColumn.equals("total"))
                    return (r2.total - r1.total);
                else if(finalColumn.equals("historyNegtive"))
                    return (r2.historyNegtive - r1.historyNegtive);
                else if(finalColumn.equals("negtive"))
                    return (r2.negtive - r1.negtive);
                else if(finalColumn.equals("success"))
                    return (r2.success - r1.success);
                else if(finalColumn.equals("successRatio"))
                    return (int) ((r2.successRatio - r1.successRatio) * 1000);
                else if(finalColumn.equals("sales"))
                    return (int) (r2.sales - r1.sales);
                else if(finalColumn.equals("s1"))
                    return (r2.s1 - r1.s1);
                else if(finalColumn.equals("s2"))
                    return (r2.s2 - r1.s2);
                else if(finalColumn.equals("s3"))
                    return (r2.s3 - r1.s3);
                else if(finalColumn.equals("s4"))
                    return (r2.s4 - r1.s4);
                else if(finalColumn.equals("s5"))
                    return (r2.s5 - r1.s5);
                else
                    return (int) ((r2.negtiveRatio - r1.negtiveRatio) * 100000/*为了精确排序*/);
            }
        });
    }


}
