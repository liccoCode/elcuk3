package services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.*;
import helper.Currency;
import models.market.M;
import models.view.report.Profit;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import play.Logger;
import play.db.helper.SqlSelect;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.vo.OrderrVO;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-3-11
 * Time: 下午4:48
 */
public class MetricProfitService {

    public Date begin;
    public Date end;
    public M market;
    public String sku;
    public String sellingId;

    public String category;

    public MetricProfitService(Date begin, Date end, String category) {
        this.begin = begin;
        this.end = end;
        this.category = category;
    }

    public MetricProfitService(Date begin, Date end, M market,
                               String sku, String sellingId) {
        this.begin = Dates.morning(begin);
        this.end = Dates.night(end);
        this.market = market;
        this.sku = sku;
        this.sellingId = ES.parseEsString(sellingId);
        checkParam();
    }

    public MetricProfitService(Date begin, Date end, M market,
                               String sku, String sellingId, String category) {
        this.begin = Dates.morning(begin);
        this.end = Dates.night(end);
        this.market = market;
        this.sku = sku;
        this.sellingId = ES.parseEsString(sellingId);
        this.category = category;
    }


    public String parseEsSku() {
        return ES.parseEsString(sku).toLowerCase();
    }

    public String parseEsSellingId() {
        return ES.parseEsString(sellingId).toLowerCase();
    }

    /**
     * 获得利润对象
     */
    public Profit calProfit() {
        Profit profit = new Profit();
        profit.sku = this.sku;
        profit.sellingId = sellingId;
        profit.market = market;
        //总销售额
        profit.totalfee = this.esSaleFee();
        profit.totalfee = Webs.scale2Double(profit.totalfee);
        //亚马逊费用
        profit.amazonfee = this.esAmazonFee();
        profit.amazonfee = Webs.scale2Double(profit.amazonfee);
        //fba费用
        profit.fbafee = this.esFBAFee();
        profit.fbafee = Webs.scale2Double(profit.fbafee);
        //总销量
        profit.quantity = this.esSaleQty();
        //采购价格
        profit.procureprice = this.esProcurePrice();
        profit.procureprice = Webs.scale2Double(profit.procureprice);
        //运输价格
        profit.shipprice = this.esShipPrice();
        profit.shipprice = Webs.scale2Double(profit.shipprice);
        //vat价格
        profit.vatprice = this.esVatPrice();
        profit.vatprice = Webs.scale2Double(profit.vatprice);
        //利润
        profit.totalprofit = this.totalProfit(profit);
        profit.totalprofit = Webs.scale2Double(profit.totalprofit);
        //利润率
        profit.profitrate = this.profitRate(profit);
        profit.profitrate = Webs.scale2Double(profit.profitrate);
        return profit;
    }

    /**
     * SKU总销售额
     */
    public Float esSaleFee() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(QueryBuilders.boolQuery()
                                //销售费用项目
                                .must(QueryBuilders.termQuery("fee_type", "productcharges")))
                        .subAggregation(AggregationBuilders.stats("units").field("cost_in_usd"))
                ).size(0);
        return getEsTermsTotal(search, "salefee");
    }

    /**
     * SKU亚马逊费用
     */
    public Float esAmazonFee() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(QueryBuilders.boolQuery()
                                //销售费用项目
                                .must(QueryBuilders.termQuery("fee_type", "commission")))
                        .subAggregation(AggregationBuilders.stats("units").field("cost_in_usd"))
                ).size(0);
        return getEsTermsTotal(search, "salefee");
    }

    /**
     * SKUFBA费用
     */
    public Float esFBAFee() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(QueryBuilders.boolQuery()
                                //销售费用项目
                                .must(QueryBuilders.prefixQuery("fee_type", "fba")))
                        .subAggregation(AggregationBuilders.stats("units").field("cost_in_usd"))
                ).size(0);
        return getEsTermsTotal(search, "salefee");
    }

    /**
     * 总销量
     */
    public Float esSaleQty() {
        BoolQueryBuilder qb = (BoolQueryBuilder) querybuilder();
        qb = qb.mustNot(QueryBuilders.termQuery("state", "cancel"));

        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(qb)
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(this.filterbuilder(true))
                        .subAggregation(AggregationBuilders.stats("units").field("quantity"))
                ).size(0);
        return getEsTermsTotal(search, "orderitem");
    }

    /**
     * 平均采购价
     */
    public Float esProcurePrice() {
        float avgprice = 0f;
        avgprice = getPrice(this.sku, "CNY", this.market.nickName());
        if(avgprice <= 0) {
            avgprice = getPrice(this.sku, "USD", this.market.nickName());
        }
        return avgprice;
    }


    private float getPrice(String fieldValue, String currency, String market) {
        float avgprice = 0;
        String sql = "select sum(price*qty)/sum(qty) as price From ProcureUnit "
                + " where product_sku='" + fieldValue + "' "
                + " and upper(selling_sellingid) like '%" + market + "%'"
                + " and qty!='' and currency='" + currency + "' ";
        List<Map<String, Object>> rows = DBUtils.rows(sql);
        if(rows != null && rows.size() > 0) {
            Double price = (Double) rows.get(0).get("price");
            if(price != null) {
                if(currency.equals("CNY"))
                    avgprice = Currency.valueOf("CNY").toUSD(price.floatValue());
                else
                    avgprice = price.floatValue();
            }
        }
        return avgprice;
    }


    /**
     * 平均运价
     */
    public Float esShipPrice() {
        F.T3<F.T2<Float, Integer>, F.T2<Float, Integer>, F.T2<Float, Integer>> feeinfo = shipTypePrice();
        F.T2<Float, Integer> seainfo = feeinfo._1;
        F.T2<Float, Integer> airinfo = feeinfo._2;
        F.T2<Float, Integer> expressinfo = feeinfo._3;
        //运费的平均单价=运费/SKu的数量
        Integer skuqty = seainfo._2 + airinfo._2 + expressinfo._2;
        float price = 0;
        if(skuqty != 0) price = (seainfo._1 + airinfo._1 + expressinfo._1) / skuqty;
        //计算标准运价
        if(price == 0) {
            return calDefaultPrice();
        }
        return price;
    }

    /**
     * 默认的运价
     *
     * @return
     */
    public Float calDefaultPrice() {
        Map<String, Float> pricemap = new HashMap<>();
        //默认快递价格urrent
        pricemap.put(M.AMAZON_US.toString(), helper.Currency.CNY.toUSD(32f));
        pricemap.put(M.AMAZON_UK.toString(), helper.Currency.CNY.toUSD(33f));
        pricemap.put(M.AMAZON_DE.toString(), helper.Currency.CNY.toUSD(34f));

        SqlSelect itemsql = new SqlSelect()
                .select("pd.lengths", "pd.width",
                        "pd.heigh", "pd.weight")
                .from("Product pd")
                .where("pd.sku='" + this.sku + "'");
        List<Map<String, Object>> rows = DBUtils.rows(itemsql.toString());
        float weight = 0, lengths = 0, width = 0, heigh = 0;
        float price = 0;
        if(rows.size() > 0) {
            //（长*宽*高）厘米/5000 与重量相比，哪个大取哪个
            Object w = rows.get(0).get("weight");
            if(w != null)
                weight = (Float) w;
            Object l = rows.get(0).get("lengths");
            if(l != null)
                lengths = (Float) l;
            Object wid = rows.get(0).get("width");
            if(wid != null)
                width = (Float) wid;
            Object h = rows.get(0).get("heigh");
            if(h != null)
                heigh = (Float) h;
            float volume = lengths * width * heigh / 5000 / 1000;
            if(volume > weight) {
                weight = volume;
            }
            if(this.market == null || (this.market != M.AMAZON_US
                    && this.market != M.AMAZON_UK && this.market != M.AMAZON_DE)) {
                price = pricemap.get(M.AMAZON_US.toString());
            } else {
                Object shipobject = pricemap.get(this.market.toString());
                if(shipobject != null) {
                    price = (Float) shipobject;
                } else
                    price = 0;
            }

        }
        return weight * price;
    }

    /**
     * 不同运输方式运价
     * <p>
     * 从ES同时查出符合sku条件的列表，以及按照ship_type分组的求和
     */
    public F.T3<F.T2<Float, Integer>, F.T2<Float, Integer>, F.T2<Float, Integer>> shipTypePrice() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .postFilter(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("sku", this.parseEsSku().toLowerCase()))
                        .must(this.filterbuilder(false)))
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(this.filterbuilder(false)
                                .must(QueryBuilders.termsQuery("ship_type", "sea", "express", "air"))
                                .must(QueryBuilders.termQuery("sku", this.parseEsSku().toLowerCase()))
                        ).subAggregation(AggregationBuilders.terms("units").field("ship_type")
                                .subAggregation(AggregationBuilders.stats("cost_sum").field("cost_in_usd"))
                        )
                ).size(10000);
        //总运费
        F.T2<JSONObject, JSONArray> esresult = getEsShipTerms(search, "shippayunit");
        if(esresult._1 == null) {
            return new F.T3<>(new F.T2<>(0f, 0), new F.T2<>(0f, 0), new F.T2<>(0f, 0));
        }
        JSONArray feearray = esresult._1.getJSONArray("buckets");
        float seatotalfee = 0f;
        float airtotalfee = 0f;
        float expresstotalfee = 0f;
        for(Object o : feearray) {
            JSONObject term = (JSONObject) o;
            float costSum = Optional.ofNullable(term.getJSONObject("cost_sum"))
                    .map(cost -> cost.getFloat("sum"))
                    .orElse(0f);
            if(term.getString("key").equals("sea")) {
                //海运总运费
                seatotalfee = costSum;
            } else if(term.getString("key").equals("air")) {
                //空运总运费
                airtotalfee = costSum;
            } else if(term.getString("key").equals("express")) {
                //快递总运费
                expresstotalfee = costSum;
            }
        }
        /**
         * 获取运输单ID
         */
        F.T3<Set<String>, Set<String>, Set<String>> mentids = getMentIds(esresult._2);
        Set<String> seaMentIds = mentids._1;
        Set<String> airMentIds = mentids._2;
        Set<String> expressMentIds = mentids._3;

        F.T3<Float, Float, Integer> seavolume = getShipmentInfo(seaMentIds, "sea");
        //海运总运费乘以体积比例
        float seafee = getshipfee(seavolume, seatotalfee);

        F.T3<Float, Float, Integer> airvolume = getShipmentInfo(airMentIds, "air");
        //空运总运费乘以体积比例
        float airfee = getshipfee(airvolume, airtotalfee);

        F.T3<Float, Float, Integer> expressvolume = getShipmentInfo(expressMentIds, "express");
        //快递总运费乘以重量比例
        float expressfee = getshipfee(expressvolume, expresstotalfee);

        //运费的平均单价=运费/SKu的数量
        Integer skuqty = seavolume._3 + airvolume._3 + expressvolume._3;

        /**
         * 返回三种运输方式的运费和数量
         */
        F.T2<Float, Integer> seainfo = new F.T2<>(seafee, seavolume._3);
        F.T2<Float, Integer> airinfo = new F.T2<>(airfee, airvolume._3);
        F.T2<Float, Integer> expressinfo = new F.T2<>(expressfee, expressvolume._3);
        return new F.T3<>
                (seainfo, airinfo, expressinfo);
    }

    /**
     * 关税和VAT单价
     */
    public Float esVatPrice() {
        //关税
        SearchSourceBuilder search = new SearchSourceBuilder()
                .postFilter(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termsQuery("fee_type", "banlancedutyandvat", "dutyandvat")))
                .aggregation(AggregationBuilders.filter("aggs_filters")
                        .filter(this.filterbuilder(false)
                                .must(QueryBuilders.termsQuery("fee_type", "banlancedutyandvat", "dutyandvat"))
                        ).subAggregation(AggregationBuilders.stats("units").field("cost_in_usd"))
                ).size(10000);
        //总关税和VAT
        F.T2<JSONObject, JSONArray> esresult = getEsShipTerms(search, "shippayunit");
        if(esresult._1 == null)
            return 0f;
        float fee = esresult._1.getFloat("total");
        //获取与关税相关的运输单
        Set<String> mentids = getVatMentIds(esresult._2);
        /**
         * 获取VAT的系数 所有涉及SKU的运输单总关税 / (sku1数量*申报价格1+sku2数量*申报价格2+....)
         */
        Float totalprice = getVatTotalPrice(mentids);
        float param = 0f;
        if(totalprice != 0f) {
            param = fee / totalprice;
        }

        String sql = "select declaredvalue From Product "
                + " where sku='" + this.sku + "' ";
        List<Map<String, Object>> rows = DBUtils.rows(sql);
        Float price = 0f;
        if(rows != null && rows.size() > 0) {
            price = (Float) rows.get(0).get("declaredvalue");
        }
        if(price == null) price = 0f;
        return price * param;
    }

    /**
     * 总利润
     */
    public double totalProfit(Profit profit) {
        /**
         *  SKU总实际利润[A] = SKU总销售额[B] - SKU总亚马逊费用[C] - SKU总FBA费用[D]
         *  - SKU总销量[E] * (SKU平均采购单价[F] + SKU平均运费单价[G] + 关税和VAT单价[H])
         *  amazonfee,fbafee 数据为负数,所以用加
         */
        return profit.totalfee + profit.amazonfee + profit.fbafee
                - profit.quantity * (profit.procureprice + profit.shipprice + profit.vatprice);
    }

    /**
     * 利润率
     */
    public double profitRate(Profit profit) {
        if(profit.totalfee > 0f) {
            //利润率
            return profit.totalprofit / profit.totalfee * 100;
        } else {
            return 0f;
        }
    }

    /**
     * 计算运费: 总运费*比例
     *
     * @return
     */
    private float getshipfee(F.T3<Float, Float, Integer> calvolume, float totalfee) {
        float fee = 0f;
        if(!(calvolume._2 == 0f || totalfee == 0f)) {
            //总运费乘以比例
            fee = totalfee * calvolume._1 / calvolume._2;
        }
        return fee;
    }

    /**
     * 获取VAT的运输单ID
     *
     * @return
     */
    private Set<String> getVatMentIds(JSONArray hits) {
        Set<String> vatMentIds = new HashSet<>();
        if(hits != null) {
            for(Object obj : hits) {
                JSONObject hit = (JSONObject) obj;
                JSONObject source = hit.getJSONObject("_source");
                vatMentIds.add(source.getString("shipment_id"));
            }
        }
        return vatMentIds;
    }

    /**
     * 计算Vat的 sku1数量*申报价格1+sku2数量*申报价格2+....
     *
     * @param mentIds
     * @return
     */
    private Float getVatTotalPrice(Set<String> mentIds) {
        String insql = SqlSelect.whereIn("sm.id", mentIds);
        if(insql == null || insql.length() <= 0) {
            return 0f;
        }
        //所有SKU的申报价格总和
        float totalprice = 0f;
        /**
         * sql查询符合条件的shipmentid
         */
        SqlSelect itemsql = new SqlSelect()
                .select("pu.qty", "pd.declaredValue")
                .from("Shipment sm")
                .leftJoin(" ShipItem si on si.shipment_id=sm.id ")
                .leftJoin(" ProcureUnit pu on si.unit_id=pu.id ")
                .leftJoin(" Product pd on pd.sku=pu.product_sku")
                .where(insql);
        List<Map<String, Object>> rows = DBUtils.rows(itemsql.toString());
        for(Map<String, Object> row : rows) {
            Object rowobject = row.get("qty");
            if(rowobject != null) {
                int rowqty = ((Number) rowobject).intValue();
                float declaredValue = ((Number) row.get("declaredValue")).floatValue();
                totalprice = totalprice + rowqty * declaredValue;
            }
        }
        return totalprice;
    }

    /**
     * 获取海运，空运，快递的运输单ID
     *
     * @return
     */
    private F.T3<Set<String>, Set<String>, Set<String>> getMentIds(JSONArray hits) {
        Set<String> airMentIds = new HashSet<>();
        Set<String> seaMentIds = new HashSet<>();
        Set<String> expressMentIds = new HashSet<>();
        if(hits != null && hits.size() > 0) {
            for(Object obj : hits) {
                JSONObject hit = (JSONObject) obj;
                JSONObject source = hit.getJSONObject("_source");
                if(source.getString("ship_type").toLowerCase().equals("sea")) {
                    seaMentIds.add(source.getString("shipment_id"));
                }
                if(source.getString("ship_type").toLowerCase().equals("air")) {
                    airMentIds.add(source.getString("shipment_id"));
                }
                if(source.getString("ship_type").toLowerCase().equals("express")) {
                    expressMentIds.add(source.getString("shipment_id"));
                }
            }
        }
        return new F.T3<>(seaMentIds, airMentIds, expressMentIds);
    }

    /**
     * 计算运输单的单个SKU所占比例
     *
     * @param mentIds
     * @param shiptype
     * @return
     */
    private F.T3<Float, Float, Integer> getShipmentInfo(Set<String> mentIds, String shiptype) {

        if(mentIds.size() <= 0) {
            return new F.T3<>(0f, 0f, 0);
        }
        String insql = SqlSelect.whereIn("sm.id", mentIds);
        if(insql == null || insql.length() <= 0) {
            return new F.T3<>(0f, 0f, 0);
        }

        //单个SKU的数量
        Integer qty = 0;
        //所有SKU的体积
        float totalvolume = 0f;
        //单个SKU的体积
        float volume = 0f;
        /**
         * sql查询符合条件的shipmentid
         */
        SqlSelect itemsql = new SqlSelect()
                .select("pu.sku", "pu.qty", "pd.lengths",
                        "pd.width", "pd.heigh", "pd.weight")
                .from("Shipment sm")
                .leftJoin(" ShipItem si on si.shipment_id=sm.id ")
                .leftJoin(" ProcureUnit pu on si.unit_id=pu.id ")
                .leftJoin(" Product pd on pd.sku=pu.product_sku")
                .where(insql);
        List<Map<String, Object>> rows = DBUtils.rows(itemsql.toString());
        List<OrderrVO> vos = new ArrayList<>();
        for(Map<String, Object> row : rows) {
            Object rowobject = row.get("sku");
            if(rowobject != null) {
                String rowsku = "";
                rowsku = rowobject.toString();
                rowobject = row.get("qty");
                if(rowobject != null) {
                    float rowlengths = 0, rowwidth = 0, rowheigh = 0, weight = 0;
                    int rowqty = ((Number) rowobject).intValue();

                    if(row.get("lengths") != null)
                        rowlengths = ((Number) row.get("lengths")).floatValue();
                    if(row.get("width") != null)
                        rowwidth = ((Number) row.get("width")).floatValue();
                    if(row.get("heigh") != null)
                        rowheigh = ((Number) row.get("heigh")).floatValue();

                    if(row.get("weight") != null)
                        weight = ((Number) row.get("weight")).floatValue();

                    float siglevolume = 0f;
                    //快递用重量计算比例
                    if(shiptype.equals("express")) {
                        //重量
                        siglevolume = weight * rowqty;
                    } else {
                        //体积
                        siglevolume = rowlengths * rowwidth * rowheigh * rowqty;
                    }
                    totalvolume = totalvolume + siglevolume;
                    if(this.sku.equals(rowsku)) {
                        //SKU的数量
                        qty = qty + rowqty;
                        volume = volume + siglevolume;
                    }
                }
            }
        }

        return new F.T3<>(volume, totalvolume, qty);
    }

    /**
     * 获取 ES 查询结果
     *
     * @param search
     * @param estype
     * @return
     */
    private float getEsTermsTotal(SearchSourceBuilder search, String estype) {
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), estype, search);
        return Optional.of(J.dig(result, "aggregations.aggs_filters.units"))
                .map(units -> units.getFloat("sum"))
                .orElse(0f);
    }

    /**
     * 获取查询 ES 运费的结果
     *
     * @param search
     * @param estype
     * @return
     */
    private F.T2<JSONObject, JSONArray> getEsShipTerms(SearchSourceBuilder search, String estype) {
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), estype, search);
        return new F.T2(Optional.of(J.dig(result, "aggregations.aggs_filters.units")),
                Optional.of(result.getJSONObject("hits")).map(hits -> hits.getJSONArray("hits")));
    }

    /**
     * 检查参数
     */
    private void checkParam() {
        //if(this.begin == null) throw new FastRuntimeException("此方法 开始时间 必须指定");
        //if(this.end == null) throw new FastRuntimeException("此方法 结束时间 必须指定");
        if(this.sku == null) throw new FastRuntimeException("此方法 sku 必须指定");
    }

    /**
     * query
     *
     * @return
     */
    private QueryBuilder querybuilder() {

        BoolQueryBuilder qb = QueryBuilders
                .boolQuery();
        if(StringUtils.isNotBlank(this.sku)) {
            qb.must(QueryBuilders.termQuery("sku", this.parseEsSku()));
        }

        if(StringUtils.isNotBlank(this.category)) {
            qb.must(QueryBuilders.prefixQuery("sku", this.category));
        }

        if(StringUtils.isNotBlank(this.sellingId)) {
            qb.must(QueryBuilders.termQuery("sellingid", this.parseEsSellingId()));
        }
        return qb;
    }


    /**
     * 过滤条件
     *
     * @return
     */
    private BoolQueryBuilder filterbuilder(boolean dateFilter) {
        DateTime fromD;
        DateTime toD;
        if(this.market == null) {
            fromD = new DateTime(begin);
            toD = new DateTime(end);
        } else {
            fromD = this.market.withTimeZone(begin);
            toD = this.market.withTimeZone(end);
        }
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if(this.market != null) {
            boolQuery.must(QueryBuilders.termQuery("market", this.market.name().toLowerCase()));
        }
        if(dateFilter) {
            boolQuery.must(QueryBuilders.rangeQuery("date")
                    .gte(fromD.toString(isoFormat))
                    .lt(toD.toString(isoFormat)));
        }
        return boolQuery;
    }


    /**
     * 计算PM的DASHBoard的Category 周平均销售额，周平均销量
     */
    public JSONArray dashboardDateAvg(String tablename, String fieldname, boolean categoryFilter) {

        DateHistogramBuilder builder = AggregationBuilders.
                dateHistogram("units")
                .field("date")
                .interval(DateHistogramInterval.WEEK)
                .subAggregation(AggregationBuilders.sum("fieldvalue").field(fieldname));

        BoolQueryBuilder qb = querybuilder(true, categoryFilter);
        //销售费用项目
        if(tablename.equals("salefee"))
            qb = qb.must(QueryBuilders.termQuery("fee_type", "productcharges"));


        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(qb)
                .aggregation(builder
                )
                .size(0);

        Logger.info("salefeeline:::" + search.toString());

        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), tablename, search);
        if(result == null) {
            throw new FastRuntimeException("ES连接异常!");
        }
        JSONObject aggregations = result.getJSONObject("aggregations");
        JSONObject units = null;
        JSONArray buckets = null;
        if(aggregations != null) {
            units = aggregations.getJSONObject("units");
            buckets = units.getJSONArray("buckets");
        }
        return buckets;
    }


    /**
     * 过滤条件
     *
     * @return
     */
    private BoolQueryBuilder querybuilder(boolean dateFilter, boolean categoryFilter) {
        DateTime fromD = null;
        DateTime toD = null;
        if(this.market == null) {
            fromD = new DateTime(begin);
            toD = new DateTime(end);
        } else {
            fromD = this.market.withTimeZone(begin);
            toD = this.market.withTimeZone(end);
        }
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
        BoolQueryBuilder qb = null;
        if(categoryFilter) {
            qb = QueryBuilders.boolQuery().must(QueryBuilders.prefixQuery("sku", this.category.toLowerCase()));

        } else {
            qb = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("sku", this.parseEsSku()));
        }

        if(this.market != null) {
            qb.must(QueryBuilders.termQuery("market", this.market.name().toLowerCase()));
        }
        if(dateFilter) {
            qb.must(QueryBuilders.rangeQuery("date").gte(fromD.toString(isoFormat))
                    .lt(toD.toString(isoFormat)));
        }
        return qb;
    }


    /**
     * 计算SKU的采购价格和数量
     * 采用Aggregation方式查询
     */
    public JSONArray skuProcureDate(String tablename, String fieldname, String caltype) {
        BoolQueryBuilder qb = QueryBuilders
                .boolQuery().must(QueryBuilders.termQuery("sku", this.parseEsSku()));

        DateHistogramBuilder builder = AggregationBuilders.
                dateHistogram("units")
                .field("date")
                .interval(DateHistogramInterval.DAY);
        /**
         * 求平均值
         */
        if(caltype.equals("avg")) {
            builder.subAggregation(AggregationBuilders
                    .avg("fieldvalue")
                    .script(new Script("doc['cost_in_usd'].value/doc['quantity'].value")));
        }
        /**
         * 求和
         */
        if(caltype.equals("sum")) {
            builder.subAggregation(AggregationBuilders.sum("fieldvalue").field(fieldname));
        }
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(qb)
                .aggregation(builder
                ).size(0);
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), tablename, search);
        if(result == null) {
            throw new FastRuntimeException("ES连接异常!");
        }
        JSONObject aggregations = result.getJSONObject("aggregations");
        JSONObject units = null;
        JSONArray buckets = null;
        if(aggregations != null) {
            units = aggregations.getJSONObject("units");
            buckets = units.getJSONArray("buckets");
        }
        return buckets;
    }


    /**
     * SKU总采购数量
     */
    public Float esProcureQty() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .aggregation(AggregationBuilders.stats("units").field("quantity"))
                .size(0);
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "procurepayunit", search);
        return Optional.of(J.dig(result, "aggregations.units"))
                .map(units -> units.getFloat("sum"))
                .orElse(0f);
    }


    /**
     * 获得利润对象
     */
    public Profit getMapProfit() {
        Profit profit = new Profit();
        profit.sku = this.sku;
        profit.sellingId = sellingId;
        profit.market = market;
        //总销售额
        profit.totalfee = this.esSaleFee();
        profit.totalfee = Webs.scale2Double(profit.totalfee);
        //亚马逊费用
        profit.amazonfee = this.esAmazonFee();
        profit.amazonfee = Webs.scale2Double(profit.amazonfee);
        //fba费用
        profit.fbafee = this.esFBAFee();
        profit.fbafee = Webs.scale2Double(profit.fbafee);
        //总销量
        profit.quantity = this.esSaleQty();
        //采购价格
        profit.procureprice = this.esProcurePrice();
        profit.procureprice = Webs.scale2Double(profit.procureprice);
        //运输价格
        profit.shipprice = this.esShipPrice();
        profit.shipprice = Webs.scale2Double(profit.shipprice);
        //vat价格
        profit.vatprice = this.esVatPrice();
        profit.vatprice = Webs.scale2Double(profit.vatprice);
        //利润
        profit.totalprofit = this.totalProfit(profit);
        profit.totalprofit = Webs.scale2Double(profit.totalprofit);
        //利润率
        profit.profitrate = this.profitRate(profit);
        profit.profitrate = Webs.scale2Double(profit.profitrate);
        return profit;
    }


}
