package services;

import com.alibaba.fastjson.JSONObject;
import helper.Constant;
import helper.Dates;
import helper.ES;
import models.market.M;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import play.utils.FastRuntimeException;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-6-9
 * Time: PM2:02
 */
public class MetricSaleReportService {

    private BoolQueryBuilder filterbuilder(Date from, Date to, M market, String sellingId) {
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        DateTime fromD, toD = null;
        if(market == null) {
            fromD = new DateTime(from);
            toD = new DateTime(to);
        } else {
            fromD = market.withTimeZone(from);
            toD = market.withTimeZone(to);
            //market 不为空时做 market 过滤
            qb.must(QueryBuilders.termQuery("market", market.name().toLowerCase()));
        }
        //日期过滤
        qb.must(QueryBuilders.rangeQuery("date").gte(fromD.toString(isoFormat))
                .lt(toD.toString(isoFormat)));
        if(StringUtils.isNotBlank(sellingId)) qb.must(QueryBuilders.termQuery("selling_id",
                ES.parseEsString(sellingId).toLowerCase()));
        return qb;
    }

    /**
     * 统计销量数据
     *
     * @return
     */
    public Float countSales(Date from, Date to, M market, String sellingId) {
        SumAggregationBuilder builder = AggregationBuilders.sum("quantity").field("quantity");
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(filterbuilder(Dates.morning(from), Dates.night(to), market, sellingId))
                .aggregation(builder)
                .size(0);
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        if(result == null) throw new FastRuntimeException("ES 连接异常!");
        JSONObject cost = result.getJSONObject("aggregations").getJSONObject("quantity");
        return cost.getFloat("value");
    }

    /**
     * 销售额数据
     *
     * @return
     */
    public Float countSalesAmount(Date from, Date to, M market, String sellingId) {
        SumAggregationBuilder builder = AggregationBuilders.sum("cost_in_usd").field("cost_in_usd");
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query((filterbuilder(Dates.morning(from), Dates.night(to), market, sellingId))
                        .must(QueryBuilders.termQuery("fee_type", "productcharges")))
                .aggregation(builder)
                .size(0);
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "salefee", search);
        if(result == null) throw new FastRuntimeException("ES 连接异常!");
        JSONObject cost = result.getJSONObject("aggregations").getJSONObject("cost_in_usd");
        return cost.getFloat("value");
    }
}
