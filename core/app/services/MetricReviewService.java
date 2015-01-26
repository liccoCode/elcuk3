package services;

import com.alibaba.fastjson.JSONObject;
import helper.Dates;
import helper.ES;
import models.market.AmazonListingReview;
import models.market.ListingStateRecord;
import models.market.M;
import models.product.Category;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import play.Logger;
import play.utils.FastRuntimeException;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-14
 * Time: AM11:44
 */
public class MetricReviewService {
    public Date from;
    public Date to;
    public String category;

    public MetricReviewService(Date from, Date to, String category) {
        this.from = from;
        this.to = to;
        this.category = category;
    }

    /**
     * 根据时间范围与 Listing ID 集合计算 Review 评分
     * <p>
     * 计算公式:
     * (5 * 所有5星的Review数量 +  4 * 所有4星的Review数量 +  3 * 所有3星的Review数量 + 2 * 所有2星的Review数量  +  1 * 所有1星的Review数量)
     * / 所有Review数量
     * </p>
     *
     * @return
     */
    public JSONObject countReviewRating() {
        if(ListingStateRecord.count() == 0) ListingStateRecord.initAllListingRecords(); // Listing 状态记录数据初始化

        Date firstReviewDate = AmazonListingReview.firstReviewDate();
        List<Date> sundays = Dates.getAllSunday(this.from, this.to);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        SearchSourceBuilder search = new SearchSourceBuilder().size(0); // search

        //使用 Aggregation 构造出一个巨大的查询 Json, 一次性查询出所有市场的数据
        for(M m : M.values()) {
            if(m.equals(M.EBAY_UK)) continue; // eBay 不参与计算
            List<String> asins = Category.asins(this.category, m);

            //市场的 aggregation 过滤
            FilterAggregationBuilder marketAggregation = AggregationBuilders.filter(
                    String.format("%s_docs", m.nickName())).filter(
                    // market 过滤
                    FilterBuilders.termFilter("market", m)
            );
            // 每一个时间点作为一个单独的 Aggregation
            for(Date sunday : sundays) {
                List<String> filteredAsins = filterAsinsByDateRange(asins, sunday, m);
                if(filteredAsins == null || filteredAsins.isEmpty()) {
                    continue; //未找到合法的 ASIN 跳过此日期(取值时会取出 null,直接设置为 0 即可)
                }
                //时间过滤 begin(firstReviewDate) end(sunday)、ASIN 过滤
                FilterAggregationBuilder dateAndAsinAggregation = AggregationBuilders.filter(
                        formatter.format(sunday)).filter(
                        FilterBuilders.andFilter(
                                FilterBuilders.termsFilter("listing_asin", filteredAsins),
                                FilterBuilders.rangeFilter("review_date")
                                        .gte(formatter.format(firstReviewDate))
                                        .lte(formatter.format(sunday))
                        )
                );
                // 按照 Review 的 rating 分组
                TermsBuilder groupByRatingAggregation = AggregationBuilders.terms("group_by_rating").field("rating");
                //groupByRatingAggregation 作为 dateAndAsinAggregation 的 子 Aggregation
                dateAndAsinAggregation.subAggregation(groupByRatingAggregation);
                //dateAndAsinAggregation 作为 marketAggregation 的 子 Aggregation
                marketAggregation.subAggregation(dateAndAsinAggregation);
            }
            search.aggregation(marketAggregation);
        }
        Logger.info("countReviewRating:::" + search.toString());
        JSONObject result = ES.search("etracker", "review", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }

    /**
     * 根据时间范围与 asins 计算 Review中差评率
     *
     * @return
     */
    public JSONObject countPoorRatingByDateRange() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        // search
        SearchSourceBuilder search = new SearchSourceBuilder().size(0);
        //使用 Aggregation 构造出一个巨大的查询 Json, 一次性查询出所有市场的数据
        for(M m : M.values()) {
            if(m.equals(M.EBAY_UK)) continue; // eBay 不参与计算
            List<String> asins = Category.asins(this.category, m);
            if(asins == null || asins.isEmpty()) continue; //未找到合法的 ASIN
            //市场、ASIN、from、to aggregation
            FilterAggregationBuilder marketAggregation = AggregationBuilders
                    .filter(String.format("%s_docs", m.nickName())).filter(
                            FilterBuilders.andFilter(
                                    FilterBuilders.termFilter("market", m),
                                    FilterBuilders.termsFilter("listing_asin", asins),
                                    FilterBuilders.rangeFilter("review_date")
                                            .gte(formatter.format(this.from))
                                            .lte(formatter.format(this.to))

                            )
                    );
            //进行日期单位为 周 分组展现数据 aggregation
            DateHistogramBuilder intervalAggregation = AggregationBuilders
                    .dateHistogram("count_by_review_date")
                    .field("review_date")
                    .interval(DateHistogram.Interval.WEEK);
            //负评分 aggregation
            FilterAggregationBuilder poorRatingAggregation = AggregationBuilders.filter("poor_rating_review").filter(
                    FilterBuilders.rangeFilter("rating").gte(1).lte(3)
            );
            intervalAggregation.subAggregation(poorRatingAggregation);
            marketAggregation.subAggregation(intervalAggregation);
            search.aggregation(marketAggregation);
        }
        Logger.info("PoorRatingByDateRange:::" + search.toString());

        JSONObject result = ES.search("etracker", "review", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }


    /**
     * 获取 Category 下在指定时间范围内未下架的 ASIN
     * 是否合法判断规则:
     * 取得时间范围内的最后一条记录, 如果该记录为下架状态,则该 Listing 不合法
     *
     * @return
     */
    public List<String> filterAsinsByDateRange(List<String> asins, Date end, M market) {
        for(int i = 0; i < asins.size(); i++) {
            List<ListingStateRecord> records = ListingStateRecord.getCacheByAsinAndMarket(
                    asins.get(i),
                    market
            );//取到对应的缓存

            //排序
            Collections.sort(records, new Comparator<ListingStateRecord>() {
                @Override
                public int compare(ListingStateRecord o1, ListingStateRecord o2) {
                    return o1.changedDate.compareTo(o2.changedDate);
                }
            });

            for(int j = records.size() - 1; j >= 0; j--) {//倒序循环
                ListingStateRecord record = records.get(j);
                //如果时间小于等于 end, 且 state 为 DOWN， 删除该 asin  找到时间小于 end 但是状态不为 SELLING 需要保留此 asin
                if(record.changedDate.getTime() <= end.getTime()) {
                    if(record.state == ListingStateRecord.S.DOWN) asins.remove(i);
                    break;
                }
            }
        }
        return asins;
    }
}
