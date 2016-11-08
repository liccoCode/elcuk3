package query;

import com.alibaba.fastjson.JSONObject;
import helper.*;
import models.market.M;
import models.view.highchart.Series;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.range.date.DateRangeBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import play.Logger;
import play.utils.FastRuntimeException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 通过向 ElasticSearch 进行搜索的 OrderItem Query
 * User: wyatt
 * Date: 11/1/13
 * Time: 11:44 AM
 */
public class OrderItemESQuery {

    public Series.Line salesFade(String type, String val, M m, Date from, Date to) {
        try {
            if("all".equals(val)) {
                return this.allSalesAndUnits(m, from, to);
            } else if(val.matches("^\\d{2}$")) {
                return this.catSalesAndUnits(val, m, from, to);
            } else if("sid".equals(type)) {
                return this.mskuSalesAndUnits(val, m, from, to);
            } else if("sku".equals(type)) {
                return this.skuSalesAndUnits(val, m, from, to);
            } else {
                throw new FastRuntimeException("不支持的类型!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过 ES 计算出指定日期段的销量和销售额.
     * PS: 时间为北京时间, 当传入 market 后, 会计算成为对应市场的那个时间.
     * 例: 2013-10-24 00:00:00 (+8)  ,  AMAZON_US 会自动计算为 2013-10-24 00:00:00 (-7) 计算.
     *
     * @param from JVM 上跑的都是 +8 时间
     * @param to   JVM 上跑的都是 +8 时间
     * @return
     */
    public Series.Line skuSalesAndUnits(String sku, M market, Date from, Date to) {
        return base(sku, "sku", market, from, to);
    }

    /**
     * 通过 ES 计算出指定日期段的销量和销售额.
     * PS: 时间为北京时间, 当传入 market 后, 会计算成为对应市场的那个时间.
     * 例: 2013-10-24 00:00:00 (+8)  ,  AMAZON_US 会自动计算为 2013-10-24 00:00:00 (-7) 计算.
     *
     * @param from JVM 上跑的都是 +8 时间
     * @param to   JVM 上跑的都是 +8 时间
     * @return
     */
    public Series.Line mskuSalesAndUnits(String sid, M market, Date from, Date to) {
        String[] args = StringUtils.split(sid, "|");
        if(args.length == 3) sid = args[0];
        return base(sid, "msku", market, from, to);
    }

    /**
     * 某个市场上某个类别的销量汇总
     *
     * @param market
     */
    public Series.Line catSalesAndUnits(String cat, M market, Date from, Date to) {
        return base(cat, "category_id", market, from, to);
    }

    /**
     * 某个市场上的所有销量汇总
     *
     * @param market
     */
    public Series.Line allSalesAndUnits(M market, Date from, Date to) {
        return base("", "all", market, from, to);
    }

    // ---------------------- 滑动平均 --------------------------
    public Series.Line movingAvgFade(String type, String val, M m, Date from, Date to) {
        if("all".equals(val)) {
            return this.allSalesMovingAvg(m, from, to);
        } else if(val.matches("^\\d{2}$")) {
            return this.catSalesMovingAvg(val, m, from, to);
        } else if("sid".equals(type)) {
            return this.mskuSalesMovingAvg(val, m, from, to);
        } else if("sku".equals(type)) {
            return this.skuSalesMovingAvg(val, m, from, to);
        } else {
            throw new FastRuntimeException("不支持的类型!");
        }
    }

    public Series.Line skuSalesMovingAvg(String sku, M market, Date from, Date to) {
        return baseMoveingAve(sku, "sku", market, from, to);
    }

    public Series.Line mskuSalesMovingAvg(String sid, M market, Date from, Date to) {
        String[] args = StringUtils.split(sid, "|");
        if(args.length == 3) sid = args[0];
        return baseMoveingAve(sid, "msku", market, from, to);
    }

    public Series.Line catSalesMovingAvg(String cat, M market, Date from, Date to) {
        return baseMoveingAve(cat, "category_id", market, from, to);
    }

    /**
     * 某个市场上的滑动平均销量
     *
     * @param market
     */
    public Series.Line allSalesMovingAvg(M market, Date from, Date to) {
        return baseMoveingAve("", "all", market, from, to);
    }


    /**
     * @param val
     * @param type sku/msku/cat
     */
    private Series.Line base(String val, String type, M market, Date from, Date to) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "category_id", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        SearchSourceBuilder search = new SearchSourceBuilder()
                .aggregation(AggregationBuilders.filter("aggs_filters").filter(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("market", market.name().toLowerCase()))
                                .must(QueryBuilders.rangeQuery("date")
                                        .gte(fromD.toString(isoFormat)).lt(toD.toString(isoFormat)))
                                .mustNot(QueryBuilders.termQuery("state", "cancel"))
                        ).subAggregation(AggregationBuilders.dateHistogram("units")
                                .field("date")
                                .interval(DateHistogramInterval.DAY)
                                .timeZone(Dates.timeZone(market).getShortName(System.currentTimeMillis()))
                                .subAggregation(AggregationBuilders.sum("quantity").field("quantity"))
                        )
                ).size(0);
        if(StringUtils.isBlank(val)) {
            search.query(QueryBuilders.matchAllQuery());
        } else {
            search.query(QueryBuilders.queryStringQuery(val)
                    .defaultField(type)
                    .defaultOperator(QueryStringQueryBuilder.Operator.AND));
        }

        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);

        Series.Line line = new Series.Line(market.label() + "销量");
        Optional.of(J.dig(result, "aggregations.aggs_filters.units"))
                .map(units -> units.getJSONArray("buckets"))
                .ifPresent(buckets -> buckets.stream()
                        .map(bucket -> (JSONObject) bucket)
                        .forEach(bucket -> line.add(Dates.date2JDate(bucket.getDate("key")),
                                NumberUtils.toFloat(J.dig(bucket, "quantity.value").toString()))
                        )
                );
        return line.sort();
    }

    /**
     * 计算滑动平均
     */
    public Series.Line baseMoveingAve(String val, String type, M market, Date from, Date to) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "category_id", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        DateRangeBuilder dateRangeBuilder = AggregationBuilders.dateRange("moving_ave").field("date").subAggregation
                (AggregationBuilders.sum("quantity_sum").field("quantity"));
        DateTime datePointer = new DateTime(fromD);
        while(datePointer.getMillis() <= toD.getMillis()) {
            dateRangeBuilder.addRange("quantity", datePointer.minusDays(7).toString(isoFormat),
                    datePointer.toString(isoFormat));
            // 以天为单位, 指针向前移动
            datePointer = datePointer.plusDays(1);
        }
        SearchSourceBuilder search = new SearchSourceBuilder()
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(QueryBuilders.termQuery("market", market.name().toLowerCase()))
                        .subAggregation(dateRangeBuilder)
                ).size(0);
        if(StringUtils.isBlank(val)) {
            search.query(QueryBuilders.matchAllQuery());
        } else {
            search.query(QueryBuilders.queryStringQuery(val)
                    .defaultField(type)
                    .defaultOperator(QueryStringQueryBuilder.Operator.AND)
            );
        }
        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);

        Series.Line line = new Series.Line(market.label() + " 滑动平均");
        Optional.of(J.dig(result, "aggregations.aggs_filters.moving_ave"))
                .map(units -> units.getJSONArray("buckets"))
                .ifPresent(buckets -> buckets.stream()
                        .map(bucket -> (JSONObject) bucket)
                        .forEach(bucket -> line.add(Dates.date2JDate(bucket.getDate("key")),
                                (bucket.getFloat("total") / 7))
                        )
                );
        return line;
    }

    /**
     * @param market
     * @param from
     * @param to
     */
    public Series.Pie categoryPie(M market, Date from, Date to) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("market", market.name().toLowerCase()))
                                .must(QueryBuilders.rangeQuery("date")
                                        .gte(fromD.toString(isoFormat))
                                        .lt(toD.toString(isoFormat))))
                        .subAggregation(AggregationBuilders.terms("units").field("category_id")
                                .subAggregation(AggregationBuilders.stats("quantity_stats").field("quantity"))
                                .size(30)
                        )
                ).size(0);
        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);

        Series.Pie pie = new Series.Pie(market.label() + " 销量百分比");
        Optional.of(J.dig(result, "aggregations.aggs_filters.units"))
                .map(units -> units.getJSONArray("buckets"))
                .ifPresent(buckets -> buckets.stream()
                        .map(bucket -> (JSONObject) bucket)
                        .forEach(bucket -> pie.add(bucket.getFloat("total"), bucket.getString("term")))
                );
        return pie;
    }

    private TermsQueryBuilder skusfilter(String type, String val) {
        String[] skus = val.replace("\"", "").split(",");
        return QueryBuilders.termsQuery("type", Arrays.asList(skus));
    }

    /**
     * @param val
     * @param type sku/msku/cat
     */
    public Series.Line skusSearch(String type, String val, M market, Date from, Date to, boolean issku) {


        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "category_id", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();


        SearchSourceBuilder search = new SearchSourceBuilder().aggregation(
                AggregationBuilders.filter("aggs_filters")
                        .filter(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("market", market.name().toLowerCase()))
                                .must(QueryBuilders.rangeQuery("date")
                                        .gte(fromD.toString(isoFormat))
                                        .lt(toD.toString(isoFormat)))
                                .must(skusfilter(type, val))
                        ).subAggregation(AggregationBuilders.dateHistogram("units")
                        .field("date")
                        .interval(DateHistogramInterval.DAY)
                        .timeZone(Dates.timeZone(market).getShortName(System.currentTimeMillis()))
                        .subAggregation(AggregationBuilders.sum("quantity").field("quantity")
                        )
                )
        ).size(0);

        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);

        Series.Line line = new Series.Line(String.format("%s %s 销量", market.label(), issku ? val : ""));
        Optional.of(J.dig(result, "aggregations.aggs_filters.units"))
                .map(units -> units.getJSONArray("buckets"))
                .ifPresent(buckets -> buckets.stream()
                        .map(bucket -> (JSONObject) bucket)
                        .forEach(bucket -> line.add(Dates.date2JDate(bucket.getDate("key")), bucket.getFloat("total")))
                );
        return line.sort();
    }


    /**
     * 计算滑动平均
     */
    public Series.Line skusMoveingAve(String type, String val, M market, Date from, Date to, boolean issku) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "category_id", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        DateRangeBuilder dateRangeBuilder = AggregationBuilders.dateRange("moving_ave").field("date");
        DateTime datePointer = new DateTime(fromD);
        while(datePointer.getMillis() <= toD.getMillis()) {
            dateRangeBuilder.addRange("quantity",
                    datePointer.minusDays(7).toString(isoFormat),
                    datePointer.toString(isoFormat));
            // 以天为单位, 指针向前移动
            datePointer = datePointer.plusDays(1);
        }
        SearchSourceBuilder search = new SearchSourceBuilder()
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("market", market.name().toLowerCase()))
                                .must(skusfilter(type, val))
                        ).subAggregation(dateRangeBuilder)
                ).size(0);
        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        Series.Line line = new Series.Line(String.format("%s %s 滑动平均", market.label(), issku ? val : ""));

        Optional.of(J.dig(result, "aggregations.aggs_filters.moving_ave.range"))
                .map(units -> units.getJSONArray("buckets"))
                .ifPresent(buckets -> buckets.stream()
                        .map(bucket -> (JSONObject) bucket)
                        .forEach(bucket -> line
                                .add(Dates.date2JDate(bucket.getDate("key")), (bucket.getFloat("total") / 7)))
                );
        return line.sort();
    }

    /**
     * 计算 SKU 的销量
     *
     * @param from
     * @param to
     * @param params
     * @return
     */
    public JSONObject skuSales(Date from, Date to, List<String> params, String type) {
        SearchSourceBuilder search = new SearchSourceBuilder().size(0);
        for(M m : Promises.MARKETS) {
            search.aggregation(skuSalesBaseSalesAggregation(m, from, to, params, type));
        }
        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }

    public AggregationBuilder skuSalesBaseSalesAggregation(M market, Date from, Date to, List<String> params,
                                                           String type) {
        FilterAggregationBuilder aggregation = AggregationBuilders.filter(market.name());

        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
        //不同的市场需要考虑到时区的问题
        DateTime fromD = market.withTimeZone(Dates.morning(from));
        DateTime toD = market.withTimeZone(Dates.night(to));

        return AggregationBuilders.filter(market.name())
                .filter(QueryBuilders.boolQuery()
                        //市场
                        .must(QueryBuilders.termQuery("market", market.name().toLowerCase()))
                        //日期
                        .must(QueryBuilders.rangeQuery("date")
                                .gte(fromD.toString(isoFormat))
                                .lt(toD.toString(isoFormat)))
                        //SKU
                        .must(QueryBuilders.termsQuery(type, params))
                        .mustNot(QueryBuilders.termQuery("state", "cancel"))
                ).subAggregation(AggregationBuilders.sum("sum_sales").field("quantity"));
    }

    /**
     * SKU 月度总销量
     *
     * @return
     */
    public JSONObject skusMonthlyDailySale(Date from, Date to, List<String> skus, List<M> markets) {
        SearchSourceBuilder search = new SearchSourceBuilder().size(0);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        for(M m : markets) {
            FilterAggregationBuilder marketAggregation = AggregationBuilders.filter(m.name())
                    .filter(QueryBuilders.boolQuery()
                            //市场
                            .must(QueryBuilders.termQuery("market", m.name().toLowerCase()))
                            //日期间隔
                            .must(QueryBuilders.rangeQuery("date")
                                    .gte(m.withTimeZone(Dates.monthBegin(from)).toString(isoFormat))
                                    .lt(m.withTimeZone(Dates.monthEnd(to)).toString(isoFormat))
                            ).mustNot(QueryBuilders.termQuery("state", "cancel"))
                    );
            for(String sku : skus) {
                if(StringUtils.isBlank(sku)) continue;
                String prettySku = ES.parseEsString(sku).toLowerCase();

                marketAggregation.subAggregation(
                        AggregationBuilders.filter(prettySku)
                                //SKU
                                .filter(QueryBuilders.termQuery("sku", prettySku))
                                .subAggregation(AggregationBuilders.dateHistogram("monthly_avg")
                                        .field("date")
                                        .timeZone(Dates.timeZone(m).getShortName(System.currentTimeMillis()))
                                        //时间间隔为每个月
                                        .interval(DateHistogramInterval.MONTH)
                                        //求和 quantity 的数量
                                        .subAggregation(AggregationBuilders.sum("sum_sales").field("quantity"))
                                )
                );
            }
            search.aggregation(marketAggregation);
        }
        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }
}
