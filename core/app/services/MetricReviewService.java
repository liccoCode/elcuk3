package services;

import com.alibaba.fastjson.JSONObject;
import helper.Dates;
import helper.ES;
import helper.Promises;
import models.market.AmazonListingReview;
import models.market.ListingStateRecord;
import models.market.M;
import models.product.Category;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import play.Logger;
import play.utils.FastRuntimeException;

import java.text.SimpleDateFormat;
import java.util.Collections;
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
     * 附 Aggregation API: (http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-aggregations.html)
     *
     * @return
     */
    public JSONObject countReviewRating() {
        Date firstReviewDate = AmazonListingReview.firstReviewDate();
        List<Date> sundays = Dates.getAllSunday(this.from, this.to);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        SearchSourceBuilder search = new SearchSourceBuilder().size(0); // search
        for(M m : Promises.MARKETS) {
            List<String> asins = Category.asins(this.category, m, true);
            List<String> listingIds = Category.listingIds(this.category, m, true);

            //市场的 aggregation 过滤
            FilterAggregationBuilder marketAggregation = AggregationBuilders
                    .filter(m.name(), QueryBuilders.termQuery("market", m));
            // 每一个时间点作为一个单独的 Aggregation
            for(Date sunday : sundays) {
                List<String> filteredAsins = filterAsinsByDateRange(asins, listingIds, sunday);
                //未找到合法的 ASIN 跳过此日期(取值时会取出 null,直接设置为 0 即可)
                if(filteredAsins == null || filteredAsins.isEmpty()) continue;
                marketAggregation.subAggregation(AggregationBuilders.filter(formatter.format(sunday), QueryBuilders
                        .boolQuery()
                        //ASIN 过滤
                        .must(QueryBuilders.termsQuery("listing_asin", filteredAsins))
                        //时间过滤
                        .must(QueryBuilders.rangeQuery("review_date")
                                .gte(formatter.format(firstReviewDate))
                                .lte(formatter.format(sunday)))
                ).subAggregation(
                        // 按照 Review 的 rating 分组
                        AggregationBuilders.terms("group_by_rating").field("rating")
                ));
            }
            search.aggregation(marketAggregation);
        }
        Logger.info(search.toString());
        JSONObject result = ES.searchOnEtrackerES("etracker", "review", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }

    /**
     * 根据时间范围与 asins 计算 Review中差评率
     *
     * @return
     */

    public JSONObject countPoorRatingByDateRange() {
        // search
        SearchSourceBuilder search = new SearchSourceBuilder().size(0);
        for(M m : Promises.MARKETS) {
            List<String> asins = Category.asins(this.category, m, true);
            if(asins == null || asins.isEmpty()) continue; //未找到合法的 ASIN
            search.aggregation(buildPoorRatingAggregation(m.name(), m, asins));
        }
        //还有一个 统计的 Aggregation
        search.aggregation(buildPoorRatingAggregation("SUM", null, Category.asins(category, null, true)));
        Logger.info(search.toString());

        JSONObject result = ES.searchOnEtrackerES("etracker", "review", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }

    public AggregationBuilder buildPoorRatingAggregation(String name, M m, List<String> asins) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //市场、ASIN、from、to aggregation
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termsQuery("listing_asin", asins))
                .must(QueryBuilders.rangeQuery("review_date").gte(formatter.format(this.from))
                        .lte(formatter.format(this.to)));
        if(m != null) boolQuery.must(QueryBuilders.termQuery("market", m));
        return AggregationBuilders.filter(name, boolQuery)
                //日期单位为 周 分组展现数据 aggregation
                .subAggregation(AggregationBuilders.dateHistogram("count_by_review_date")
                        .field("review_date")
                        .dateHistogramInterval(DateHistogramInterval.WEEK)
                        //负评分 aggregation
                        .subAggregation(AggregationBuilders.filter("poor_rating_review",
                                QueryBuilders.rangeQuery("rating").gte(1).lte(3))));
    }

    /**
     * 获取 Category 下在指定时间范围内未下架的 ASIN
     * 是否合法判断规则:
     * 取得时间范围内的最后一条记录, 如果该记录为下架状态,则该 Listing 不合法
     *
     * @return
     */
    public List<String> filterAsinsByDateRange(List<String> asins, List<String> listingIds, Date end) {
        for(int i = 0; i < listingIds.size(); i++) {
            List<ListingStateRecord> records = ListingStateRecord.getCacheByListingId(listingIds.get(i));//取到对应的缓存
            //排序
            Collections.sort(records, (o1, o2) -> o1.changedDate.compareTo(o2.changedDate));

            for(int j = records.size() - 1; j >= 0; j--) {//倒序循环
                ListingStateRecord record = records.get(j);
                //如果时间小于等于 end, 且 state 为 DOWN， 删除该 asin  找到时间小于 end 但是状态不为 SELLING 需要保留此 asin
                if(record.changedDate.getTime() <= end.getTime()) {
                    if(record.state == ListingStateRecord.S.DOWN) asins.remove(record.selling.asin.toLowerCase());
                    break;
                }
            }
        }
        return asins;
    }
}
