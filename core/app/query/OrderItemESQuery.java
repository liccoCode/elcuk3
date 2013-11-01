package query;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.Dates;
import helper.ES;
import models.market.M;
import models.view.highchart.Series;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import play.utils.FastRuntimeException;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 通过向 ElasticSearch 进行搜索的 OrderItem Query
 * User: wyatt
 * Date: 11/1/13
 * Time: 11:44 AM
 */
public class OrderItemESQuery {
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

    public Series.Line catSalesAndUnits(String cat, M market, Date from, Date to) {
        return base("\"" + cat + "\"", "cat", market, from, to);
    }

    public Series.Line allSalesAndUnits(M market, Date from, Date to) {
        return base("", "all", market, from, to);
    }

    /**
     * @param val
     * @param type sku/msku/cat
     * @return
     */
    private Series.Line base(String val, String type, M market, Date from, Date to) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");
        if(!Arrays.asList("sku", "msku", "cat", "all").contains(type))
            throw new FastRuntimeException("还不支持 " + type + " " + "类型");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        SearchSourceBuilder search = new SearchSourceBuilder()
                .facet(FacetBuilders.dateHistogramFacet("units")
                        .keyField("createDate")
                        .valueField("quantity")
                        .interval("day")
                        .preZone(Dates.timeZone(market).getShortName(System.currentTimeMillis()))
                        .facetFilter(FilterBuilders.boolFilter()
                                .must(FilterBuilders.termFilter("market", market.name().toLowerCase()))
                                .must(FilterBuilders.rangeFilter("createDate")
                                        .gte(fromD.toString(isoFormat))
                                        .lt(toD.toString(isoFormat))
                                )
                        )
                ).size(0);
        if(StringUtils.isBlank(val)) {
            search.query(QueryBuilders.queryString("*"));
        } else {
            search.query(QueryBuilders.queryString(val).defaultField(type));
        }

        JSONObject result = ES.search("elcuk2", "orderitem", search);
        JSONObject facets = result.getJSONObject("facets");
        JSONArray entries = facets.getJSONObject("units").getJSONArray("entries");

        Series.Line line = new Series.Line(market.label() + "销量");
        for(Object o : entries) {
            JSONObject entry = (JSONObject) o;
            line.add(Dates.date2JDate(entry.getDate("time")), entry.getFloat("total"));
        }
        long interval = TimeUnit.DAYS.toMillis(1);
        for(long i = from.getTime(); i <= to.getTime(); i += interval) {
            line.add(0f, i);
        }
        line.sort();
        return line;
    }

    public Series.Pie categoryPie(M market, Date from, Date to) {
        if(market == null) throw new FastRuntimeException("此方法 Market 必须指定");

        DateTime fromD = market.withTimeZone(from);
        DateTime toD = market.withTimeZone(to);
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(QueryBuilders.matchAllQuery())
                .facet(FacetBuilders.termsStatsFacet("units")
                        .keyField("cat")
                        .valueField("quantity")
                        .size(30) // category 数量
                        .facetFilter(FilterBuilders.boolFilter()
                                .must(FilterBuilders.termFilter("market", market.name().toLowerCase()))
                                .must(FilterBuilders.rangeFilter("createDate")
                                        .gte(fromD.toString(isoFormat))
                                        .lt(toD.toString(isoFormat))
                                )
                        )
                ).size(0);
        JSONObject result = ES.search("elcuk2", "orderitem", search);
        JSONObject facets = result.getJSONObject("facets");
        JSONArray terms = facets.getJSONObject("units").getJSONArray("terms");

        Series.Pie pie = new Series.Pie(market.label() + "销量百分比");

        for(Object o : terms) {
            JSONObject term = (JSONObject) o;
            pie.add(term.getFloat("total"), term.getString("term"));
        }

        return pie;
    }
}
