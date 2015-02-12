package services;

import com.alibaba.fastjson.JSONObject;
import helper.Dates;
import helper.ES;
import models.User;
import models.market.AmazonListingReview;
import models.market.ListingStateRecord;
import models.market.M;
import models.product.Category;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import play.Logger;
import play.utils.FastRuntimeException;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-14
 * Time: AM11:44
 */
public class MetricReviewService {
    public Date from;
    public Date to;

    public MetricReviewService(Date from, Date to, String category) {
        this.from = from;
        this.to = to;
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
    public JSONObject countReviewRating(String category, User user) {
        List<String> categories = User.getTeamCategorys(user);
        if(!category.equalsIgnoreCase("all") && !categories.contains(category)) {
            return null; //没有该 Category 权限, 不允许查看数据
        }

        Date firstReviewDate = AmazonListingReview.firstReviewDate();
        List<Date> sundays = Dates.getAllSunday(this.from, this.to);
        SearchSourceBuilder search = new SearchSourceBuilder().size(0); // search
        //使用嵌套的 Aggregation 构造出一个巨大的查询 Json, 一次性查询出所有市场的数据(update: include 汇总数据) 具体参照 http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations.html
        for(M m : M.values()) {
            if(m.equals(M.EBAY_UK)) continue; // eBay 不参与计算
            List<String> asins = new ArrayList<String>();
            if(category.equalsIgnoreCase("all")) {
                asins = Category.asinsByCategories(categories, m);
            } else {
                asins = Category.asins(category, m);
            }
            //单独每个市场的 aggregation
            search.aggregation(buildReviewRatingAggregation(m.name(), m, firstReviewDate, category, asins, sundays));
        }
        //计算汇总的 aggregation
        search.aggregation(buildReviewRatingAggregation(
                "SUM",
                null,
                firstReviewDate,
                category,
                Category.asins(category, null),
                sundays));

        Logger.info("countReviewRating:::" + search.toString());
        JSONObject result = ES.search("etracker", "review", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }

    /**
     * @param name            用作最顶级 Aggregation 的名称
     * @param m               市场
     * @param firstReviewDate 第一个 Review 产生的时间
     * @param category        产品线(用作获取 Listing ID 来进行 ASIN 过滤 需要过滤掉那些下架的 ASIN)
     * @param asins
     * @param sundays
     * @return
     */
    public AggregationBuilder buildReviewRatingAggregation(String name, M m, Date firstReviewDate,
                                                           String category, List<String> asins,
                                                           List<Date> sundays) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        List<String> listingIds;
        if(name.equalsIgnoreCase("SUM")) {
            listingIds = Category.listingIds(category, null);
        } else {
            listingIds = Category.listingIds(category, m);
        }

        //市场的 aggregation 过滤
        FilterAggregationBuilder marketAggregation = AggregationBuilders.filter(name);
        if(m == null) {
            marketAggregation.filter(FilterBuilders.matchAllFilter());
        } else {
            marketAggregation.filter(FilterBuilders.termFilter("market", m));// market 过滤
        }
        // 每一个时间点作为一个单独的 Aggregation
        for(Date sunday : sundays) {
            List<String> filteredAsins = filterAsinsByDateRange(asins, listingIds, sunday, m);
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
        return marketAggregation;
    }

    /**
     * 根据时间范围与 asins 计算 Review中差评率
     *
     * @return
     */
    public JSONObject countPoorRatingByDateRange(String category, User user) {
        List<String> categories = User.getTeamCategorys(user);
        if(!category.equalsIgnoreCase("all") && !categories.contains(category)) {
            return null; //没有该 Category 权限, 不允许查看数据
        }
        // search
        SearchSourceBuilder search = new SearchSourceBuilder().size(0);
        //使用嵌套的 Aggregation 构造出一个巨大的查询 Json, 一次性查询出所有市场的数据(update: include 汇总数据) 具体参照 http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations.html
        for(M m : M.values()) {
            if(m.equals(M.EBAY_UK)) continue; // eBay 不参与计算
            List<String> asins = new ArrayList<String>();
            if(category.equalsIgnoreCase("all")) {
                asins = Category.asinsByCategories(categories, m);
            } else {
                asins = Category.asins(category, m);
            }
            if(asins == null || asins.isEmpty()) continue; //未找到合法的 ASIN

            search.aggregation(buildPoorRatingAggregation(m.name(), m, asins));
        }
        //还有一个 统计的 Aggregation
        search.aggregation(buildPoorRatingAggregation("SUM", null, Category.asins(category, null)));
        Logger.info("PoorRatingByDateRange:::" + search.toString());

        JSONObject result = ES.search("etracker", "review", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }

    public AggregationBuilder buildPoorRatingAggregation(String name, M m, List<String> asins) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //市场、ASIN、from、to aggregation
        AndFilterBuilder andFilter = FilterBuilders.andFilter(
                FilterBuilders.termsFilter("listing_asin", asins),
                FilterBuilders.rangeFilter("review_date")
                        .gte(formatter.format(this.from))
                        .lte(formatter.format(this.to))

        );
        if(m != null) andFilter.add(FilterBuilders.termFilter("market", m));

        FilterAggregationBuilder marketAggregation = AggregationBuilders
                .filter(name).filter(andFilter);
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
        return marketAggregation.subAggregation(intervalAggregation);
    }

    /**
     * 获取 Category 下在指定时间范围内未下架的 ASIN
     * 是否合法判断规则:
     * 取得时间范围内的最后一条记录, 如果该记录为下架状态,则该 Listing 不合法
     *
     * @return
     */
    public List<String> filterAsinsByDateRange(List<String> asins, List<String> listingIds, Date end, M market) {
        for(int i = 0; i < listingIds.size(); i++) {
            List<ListingStateRecord> records = ListingStateRecord.getCacheByListingId(listingIds.get(i));//取到对应的缓存
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
                    if(record.state == ListingStateRecord.S.DOWN) asins.remove(record.listing.asin);
                    break;
                }
            }
        }
        return asins;
    }
}
