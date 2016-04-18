package query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.Constant;
import helper.Dates;
import helper.ES;
import helper.Promises;
import models.market.M;
import models.view.highchart.Series;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.OrFilterBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogram;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.range.RangeFacetBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import play.Logger;
import play.utils.FastRuntimeException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        return base("\"" + sku + "\"", "sku", market, from, to);
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
        return base("\"" + sid + "\"", "msku", market, from, to);
    }

    /**
     * 某个市场上某个类别的销量汇总
     *
     * @param market
     */
    public Series.Line catSalesAndUnits(String cat, M market, Date from, Date to) {
        return base("\"" + cat + "\"", "category_id", market, from, to);
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
        return baseMoveingAve("\"" + sku + "\"", "sku", market, from, to);
    }

    public Series.Line mskuSalesMovingAvg(String sid, M market, Date from, Date to) {
        String[] args = StringUtils.split(sid, "|");
        if(args.length == 3) sid = args[0];
        return baseMoveingAve("\"" + sid + "\"", "msku", market, from, to);
    }

    public Series.Line catSalesMovingAvg(String cat, M market, Date from, Date to) {
        return baseMoveingAve("\"" + cat + "\"", "category_id", market, from, to);
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
     * @deprecated facetFilter 查询需要替换
     */
    private Series.Line base(String val, String type, M market, Date from, Date to) {


        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "category_id", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        SearchSourceBuilder search = new SearchSourceBuilder()
                .facet(FacetBuilders.dateHistogramFacet("units")
                                .keyField("date")
                                .valueField("quantity")
                                .interval("day")
                                .preZone(Dates.timeZone(market).getShortName(System.currentTimeMillis()))
                                .facetFilter(FilterBuilders.boolFilter()
                                                .must(FilterBuilders.termFilter("market", market.name().toLowerCase()))
                                                .must(FilterBuilders.rangeFilter("date")
                                                                .gte(fromD.toString(isoFormat))
                                                                .lt(toD.toString(isoFormat))
                                                ).mustNot(FilterBuilders.termFilter("state", "cancel"))
                                )
                ).size(0);

        if(StringUtils.isBlank(val)) {
            search.query(QueryBuilders.matchAllQuery());
        } else {
            search.query(QueryBuilders.queryString(val).defaultField(type));
        }

        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        Logger.info(result.toString());
        JSONObject facets = result.getJSONObject("facets");
        if(facets != null && facets.getJSONObject("units") != null) {
            JSONArray entries = facets.getJSONObject("units").getJSONArray("entries");
            Series.Line line = new Series.Line(market.label() + "销量");
            for(Object o : entries) {
                JSONObject entry = (JSONObject) o;
                line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
            }

            DateTime datePointer = new DateTime(from);
            while(datePointer.getMillis() <= to.getTime()) {
                line.add(0f, Dates.date2JDate(from));
                datePointer = datePointer.plusDays(1);
            }
            line.sort();
            return line;
        } else {
            Series.Line line = new Series.Line(market.label() + "销量");
            return line;
        }
    }

    /**
     * 计算滑动平均
     *
     * @deprecated facetFilter 查询需要替换
     */
    public Series.Line baseMoveingAve(String val, String type, M market, Date from, Date to) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "category_id", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        RangeFacetBuilder facetBuilder = FacetBuilders.rangeFacet("moving_ave")
                .keyField("date").valueField("quantity")
                .facetFilter(FilterBuilders.termFilter("market", market.name().toLowerCase()));
        DateTime datePointer = new DateTime(fromD);
        while(datePointer.getMillis() <= toD.getMillis()) {
            facetBuilder.addRange(datePointer.minusDays(7).toString(isoFormat), datePointer.toString(isoFormat));
            // 以天为单位, 指针向前移动
            datePointer = datePointer.plusDays(1);
        }

        SearchSourceBuilder search = new SearchSourceBuilder()
                .facet(facetBuilder)
                .size(0);
        if(StringUtils.isBlank(val)) {
            search.query(QueryBuilders.matchAllQuery());
        } else {
            search.query(QueryBuilders.queryString(val).defaultField(type));
        }

        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        JSONObject facets = result.getJSONObject("facets");
        JSONArray movingAveRanges = facets.getJSONObject("moving_ave").getJSONArray("ranges");

        Series.Line line = new Series.Line(market.label() + " 滑动平均");
        for(Object o : movingAveRanges) {
            JSONObject range = (JSONObject) o;
            line.add(Dates.date2JDate(range.getDate("to")), range.getFloat("total") / 7);
        }
        return line;
    }

    /**
     * @param market
     * @param from
     * @param to
     * @deprecated facetFilter 查询需要替换
     */
    public Series.Pie categoryPie(M market, Date from, Date to) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .facet(FacetBuilders.termsStatsFacet("units")
                                .keyField("category_id")
                                .valueField("quantity")
                                .size(30) // category 数量
                                .facetFilter(FilterBuilders.boolFilter()
                                                .must(FilterBuilders.termFilter("market", market.name().toLowerCase()))
                                                .must(FilterBuilders.rangeFilter("date")
                                                                .gte(fromD.toString(isoFormat))
                                                                .lt(toD.toString(isoFormat))
                                                )
                                )
                ).size(0);
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        JSONObject facets = result.getJSONObject("facets");
        JSONArray terms = facets.getJSONObject("units").getJSONArray("terms");

        Series.Pie pie = new Series.Pie(market.label() + " 销量百分比");

        for(Object o : terms) {
            JSONObject term = (JSONObject) o;
            pie.add(term.getFloat("total"), term.getString("term"));
        }

        return pie;
    }

    public OrFilterBuilder skusfilter(String type, String val) {
        String[] skus = val.replace("\"", "").split(",");
        OrFilterBuilder builders = FilterBuilders.orFilter();
        for(int i = 0; i < skus.length; i++) {
            if(StringUtils.isNotBlank(skus[i])) {
                builders.add(FilterBuilders.termFilter(type,
                        ES.parseEsString(skus[i]).toLowerCase().trim()));
            }
        }
        return builders;
    }


    /**
     * @param val
     * @param type sku/msku/cat
     * @deprecated facetFilter 查询需要替换
     */
    public Series.Line skusSearch(String type, String val, M market, Date from, Date to, boolean issku) {


        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "category_id", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();


        SearchSourceBuilder search = new SearchSourceBuilder()
                .facet(FacetBuilders.dateHistogramFacet("units")
                                .keyField("date")
                                .valueField("quantity")
                                .interval("day")
                                .preZone(Dates.timeZone(market).getShortName(System.currentTimeMillis()))
                                .facetFilter(FilterBuilders.boolFilter()
                                                .must(FilterBuilders.termFilter("market", market.name().toLowerCase()))
                                                .must(FilterBuilders.rangeFilter("date")
                                                                .gte(fromD.toString(isoFormat))
                                                                .lt(toD.toString(isoFormat))
                                                ).must(skusfilter(type, val))
                                )
                ).size(0);

        Logger.info(search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        Logger.info(result.toString());
        JSONObject facets = result.getJSONObject("facets");
        if(facets != null && facets.getJSONObject("units") != null) {
            JSONArray entries = facets.getJSONObject("units").getJSONArray("entries");
            Series.Line line = new Series.Line(market.label() + "销量");
            if(issku)
                line = new Series.Line(market.label() + val + "销量");
            for(Object o : entries) {
                JSONObject entry = (JSONObject) o;
                line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
            }

            DateTime datePointer = new DateTime(from);
            while(datePointer.getMillis() <= to.getTime()) {
                line.add(0f, Dates.date2JDate(from));
                datePointer = datePointer.plusDays(1);
            }
            line.sort();
            return line;
        } else {
            Series.Line line = new Series.Line(market.label() + "销量");
            if(issku)
                line = new Series.Line(market.label() + val + "销量");
            return line;
        }
    }


    /**
     * 计算滑动平均
     *
     * @deprecated facetFilter 查询需要替换
     */
    public Series.Line skusMoveingAve(String type, String val, M market, Date from, Date to, boolean issku) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "category_id", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        RangeFacetBuilder facetBuilder = FacetBuilders.rangeFacet("moving_ave")
                .keyField("date").valueField("quantity")
                .facetFilter(FilterBuilders.boolFilter()
                                .must(FilterBuilders.termFilter("market", market.name().toLowerCase()))
                                .must(skusfilter(type, val))
                );
        DateTime datePointer = new DateTime(fromD);
        while(datePointer.getMillis() <= toD.getMillis()) {
            facetBuilder.addRange(datePointer.minusDays(7).toString(isoFormat), datePointer.toString(isoFormat));
            // 以天为单位, 指针向前移动
            datePointer = datePointer.plusDays(1);
        }

        SearchSourceBuilder search = new SearchSourceBuilder()
                .facet(facetBuilder)
                .size(0);

        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        JSONObject facets = result.getJSONObject("facets");
        JSONArray movingAveRanges = facets.getJSONObject("moving_ave").getJSONArray("ranges");

        Series.Line line = new Series.Line(market.label() + " 滑动平均");
        if(issku)
            line = new Series.Line(market.label() + val + " 滑动平均");
        for(Object o : movingAveRanges) {
            JSONObject range = (JSONObject) o;
            line.add(Dates.date2JDate(range.getDate("to")), range.getFloat("total") / 7);
        }
        return line;
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
        Logger.info("countSkuSales:::" + search.toString());
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

        BoolFilterBuilder filter = FilterBuilders.boolFilter()
                //市场
                .must(FilterBuilders.termFilter("market", market.name().toLowerCase()))
                        //日期
                .must(FilterBuilders.rangeFilter("date")
                        .gte(fromD.toString(isoFormat))
                        .lt(toD.toString(isoFormat)))
                        //SKU
                .must(FilterBuilders.termsFilter(type, params))
                .mustNot(FilterBuilders.termFilter("state", "cancel"));
        aggregation.filter(filter);
        aggregation.subAggregation(AggregationBuilders.sum("sum_sales").field("quantity"));
        return aggregation;
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
            FilterAggregationBuilder marketAggregation = AggregationBuilders.filter(m.name());
            BoolFilterBuilder filter = FilterBuilders.boolFilter()
                    //市场
                    .must(FilterBuilders.termFilter("market", m.name().toLowerCase()))
                            //日期间隔
                    .must(FilterBuilders.rangeFilter("date")
                                    .gte(m.withTimeZone(Dates.monthBegin(from)).toString(isoFormat))
                                    .lt(m.withTimeZone(Dates.monthEnd(to)).toString(isoFormat))
                    ).mustNot(FilterBuilders.termFilter("state", "cancel"));
            marketAggregation.filter(filter);
            for(String sku : skus) {
                if(StringUtils.isBlank(sku)) continue;
                //SKU
                String prettySku = ES.parseEsString(sku).toLowerCase();
                FilterAggregationBuilder skuAggregation = AggregationBuilders.filter(prettySku);
                skuAggregation.filter(FilterBuilders.termFilter("sku", prettySku));

                //时间间隔为每个月
                DateHistogramBuilder monthAvgAggregation = AggregationBuilders.dateHistogram("monthly_avg")
                        .field("date")
                        .preZone(Dates.timeZone(m).toString())
                        .interval(DateHistogram.Interval.MONTH);

                //求和 quantity 的数量
                monthAvgAggregation.subAggregation(AggregationBuilders.sum("sum_sales").field("quantity"));
                skuAggregation.subAggregation(monthAvgAggregation);
                marketAggregation.subAggregation(skuAggregation);
            }
            search.aggregation(marketAggregation);
        }
        Logger.info("SkusMonthlyDailySale:::" + search.toString());
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "orderitem", search);
        if(result == null) throw new FastRuntimeException("ES连接异常!");
        return result.getJSONObject("aggregations");
    }
}
