package services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.DBUtils;
import helper.ES;
import helper.Promises;
import models.market.M;
import models.market.Orderr;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.product.Product;
import models.view.report.Profit;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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

    public MetricProfitService(Date begin, Date end, M market,
                               String sku, String sellingId) {
        this.begin = begin;
        this.end = end;
        this.market = market;
        this.sku = sku;
        this.sellingId = ES.parseEsString(sellingId);
        checkParam();
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
        //亚马逊费用
        profit.amazonfee = this.esAmazonFee();
        //fba费用
        profit.fbafee = this.esFBAFee();
        //总销量
        profit.quantity = this.esSaleQty();
        //采购价格
        profit.procureprice = this.esProcurePrice();
        //运输价格
        profit.shipprice = this.esShipPrice();
        //vat价格
        profit.vatprice = this.esVatPrice();
        //利润
        profit.totalprofit = this.totalProfit(profit);
        //利润率
        profit.profitrate = this.profitRate(profit);
        return profit;
    }

    /**
     * SKU总销售额
     */
    public Float esSaleFee() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("cost_in_usd")
                        .facetFilter(this.filterbuilder(true)
                                //销售费用项目
                                .must(FilterBuilders.termFilter("fee_type", "productcharges")))
                ).size(0);

        System.out.println("xxx::::::::::::::::" + search.toString());
        return getEsTermsTotal(search, "salefee");
    }

    /**
     * SKU亚马逊费用
     */
    public Float esAmazonFee() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("cost_in_usd")
                        .facetFilter(this.filterbuilder(true)
                                //FBA亚马逊项目
                                .must(FilterBuilders.termFilter("fee_type", "commission")))
                ).size(0);
        return getEsTermsTotal(search, "salefee");
    }

    /**
     * SKUFBA费用
     */
    public Float esFBAFee() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("cost_in_usd")
                        .facetFilter(this.filterbuilder(true)
                                //FBAFBA项目
                                .must(FilterBuilders.prefixFilter("fee_type", "fba")))
                ).size(0);
        return getEsTermsTotal(search, "salefee");
    }

    /**
     * 总销量
     */
    public Float esSaleQty() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("quantity")
                        .facetFilter(this.filterbuilder(true))
                ).size(0);
        return getEsTermsTotal(search, "orderitem");
    }

    /**
     * 平均采购价
     */
    public Float esProcurePrice() {
        /*cashpledge 采购的预付款, 一般为 30%
          procurement 采购货物的货款
        */
        TermsFilterBuilder orfilter = FilterBuilders.termsFilter("fee_type", "cashpledge", "procurement");

        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("cost_in_usd")
                        .facetFilter(this.filterbuilder(false).must(orfilter))
                ).size(0);
        /**
         * 采购总金额（美元)
         */
        float fee = getEsTermsTotal(search, "procurepayunit");

        search = new SearchSourceBuilder()
                .query(querybuilder())
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("quantity")
                        .facetFilter(this.filterbuilder(false).must(orfilter))
                ).size(0);

        /**
         * 采购总数量
         */
        float qty = getEsTermsTotal(search, "procurepayunit");
        float avgprice = 0f;
        if(qty != 0f) {
            avgprice = fee / qty;
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
        if(skuqty != 0) {
            price = (seainfo._1 + airinfo._1 + expressinfo._1) / skuqty;
        }
        return price;
    }


    /**
     * 不用运输方式运价
     */
    public F.T3<F.T2<Float, Integer>, F.T2<Float, Integer>, F.T2<Float, Integer>> shipTypePrice() {
        /**
         * 从ES同时查出符合sku条件的列表，以及按照ship_type分组的求和
         */
        TermsFilterBuilder orfilter = FilterBuilders.termsFilter("ship_type", "sea", "express", "air");
        SearchSourceBuilder search = new SearchSourceBuilder()
                .postFilter(FilterBuilders.boolFilter().must(FilterBuilders.termFilter("sku",
                        this.parseEsSku().toLowerCase()))).size(100000)
                .facet(FacetBuilders.termsStatsFacet("units")
                        .keyField("ship_type")
                        .valueField("cost_in_usd")
                        .facetFilter(this.filterbuilder(false).must(orfilter
                        ).must(FilterBuilders.termFilter("sku", this.parseEsSku().toLowerCase()))
                        ));
        //总运费
        F.T2<JSONObject, JSONArray> esresult = getEsShipTerms(search, "shippayunit");
        if(esresult._1 == null) {
            return new F.T3<F.T2<Float, Integer>, F.T2<Float, Integer>, F.T2<Float, Integer>>
                    (new F.T2<Float, Integer>(0f, 0),
                            new F.T2<Float, Integer>(0f, 0),
                            new F.T2<Float, Integer>(0f, 0));
        }
        JSONArray feearray = esresult._1.getJSONArray("terms");
        float seatotalfee = 0f;
        float airtotalfee = 0f;
        float expresstotalfee = 0f;
        for(Object o : feearray) {
            JSONObject term = (JSONObject) o;
            if(term.getString("term").equals("sea")) {
                //海运总运费
                seatotalfee = term.getFloat("total");
            } else if(term.getString("term").equals("air")) {
                //空运总运费
                airtotalfee = term.getFloat("total");
            } else if(term.getString("term").equals("express")) {
                //快递总运费
                expresstotalfee = term.getFloat("total");
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
        F.T2<Float, Integer> seainfo = new F.T2<Float, Integer>(seafee, seavolume._3);
        F.T2<Float, Integer> airinfo = new F.T2<Float, Integer>(airfee, airvolume._3);
        F.T2<Float, Integer> expressinfo = new F.T2<Float, Integer>(expressfee, expressvolume._3);
        return new F.T3<F.T2<Float, Integer>, F.T2<Float, Integer>, F.T2<Float, Integer>>
                (seainfo, airinfo, expressinfo);
    }

    /**
     * 关税和VAT单价
     */
    public Float esVatPrice() {
        //关税
        SearchSourceBuilder search = new SearchSourceBuilder()
                .postFilter(FilterBuilders.boolFilter().must(FilterBuilders.termsFilter("fee_type",
                        "banlancedutyandvat", "dutyandvat"))).size(100000)
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("cost_in_usd")
                        .facetFilter(this.filterbuilder(false).must(
                                FilterBuilders.termsFilter("fee_type",
                                        "banlancedutyandvat", "dutyandvat")
                        )
                        ));
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
        Product product = Product.findById(this.sku);
        if(product == null) {
            return 0f;
        } else {
            float vatprice = product.declaredValue * param;
            return vatprice;
        }
    }

    /**
     * 总利润
     */
    public Float totalProfit(Profit profit) {
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
    public Float profitRate(Profit profit) {
        if(profit.totalfee != 0f) {
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
        Set<String> vatMentIds = new HashSet<String>();
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
                .leftJoin(" shipitem si on si.shipment_id=sm.id ")
                .leftJoin(" procureunit pu on si.unit_id=pu.id ")
                .leftJoin(" Product pd on pd.sku=pu.product_sku")
                .where(insql);
        List<Map<String, Object>> rows = DBUtils.rows(itemsql.toString());
        for(Map<String, Object> row : rows) {
            int rowqty = ((Number) row.get("qty")).intValue();
            float declaredValue = ((Number) row.get("declaredValue")).floatValue();
            totalprice = totalprice + rowqty * declaredValue;
        }
        return totalprice;
    }

    /**
     * 获取海运，空运，快递的运输单ID
     *
     * @return
     */
    private F.T3<Set<String>, Set<String>, Set<String>> getMentIds(JSONArray hits) {
        Set<String> airMentIds = new HashSet<String>();
        Set<String> seaMentIds = new HashSet<String>();
        Set<String> expressMentIds = new HashSet<String>();
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
        return new F.T3<Set<String>, Set<String>, Set<String>>(seaMentIds, airMentIds, expressMentIds);
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
            return new F.T3<Float, Float, Integer>(0f, 0f, 0);
        }
        String insql = SqlSelect.whereIn("sm.id", mentIds);
        if(insql == null || insql.length() <= 0) {
            return new F.T3<Float, Float, Integer>(0f, 0f, 0);
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
                .leftJoin(" shipitem si on si.shipment_id=sm.id ")
                .leftJoin(" procureunit pu on si.unit_id=pu.id ")
                .leftJoin(" Product pd on pd.sku=pu.product_sku")
                .where(insql);
        List<Map<String, Object>> rows = DBUtils.rows(itemsql.toString());
        List<OrderrVO> vos = new ArrayList<OrderrVO>();
        for(Map<String, Object> row : rows) {
            String rowsku = row.get("sku").toString();
            int rowqty = ((Number) row.get("qty")).intValue();
            float rowlengths = ((Number) row.get("lengths")).floatValue();
            float rowwidth = ((Number) row.get("width")).floatValue();
            float rowheigh = ((Number) row.get("heigh")).floatValue();

            float siglevolume = 0f;
            //快递用重量计算比例
            if(shiptype.equals("express")) {
                //重量
                siglevolume = rowheigh * rowqty;
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

        return new F.T3<Float, Float, Integer>(volume, totalvolume, qty);
    }

    /**
     * 查询ES的结果
     *
     * @param search
     * @param estype
     * @return
     */
    private float getEsTermsTotal(SearchSourceBuilder search, String estype) {
        JSONObject result = ES.search("elcuk2", estype, search);
        if(result == null) {
            throw new FastRuntimeException("ES连接异常!");
        }

        JSONObject facets = result.getJSONObject("facets");
        float totalcost = 0;
        if(facets != null) {
            JSONObject units = facets.getJSONObject("units");
            totalcost = units.getFloat("total");
        }
        return totalcost;
    }

    /**
     * 查询ES运费的结果
     *
     * @param search
     * @param estype
     * @return
     */
    private F.T2<JSONObject, JSONArray> getEsShipTerms(SearchSourceBuilder search, String estype) {
        JSONObject result = ES.search("elcuk2", estype, search);
        if(result == null) {
            throw new FastRuntimeException("ES连接异常!");
        }
        JSONObject facets = result.getJSONObject("facets");
        JSONObject units = null;
        if(facets != null) {
            units = facets.getJSONObject("units");
        }
        JSONObject hits = result.getJSONObject("hits");
        JSONArray hitmentids = null;
        if(facets != null) {
            hitmentids = hits.getJSONArray("hits");
        }
        return new F.T2<JSONObject, JSONArray>(units, hitmentids);
    }

    /**
     * 检查参数
     */
    private void checkParam() {
        if(this.begin == null) throw new FastRuntimeException("此方法 开始时间 必须指定");
        if(this.end == null) throw new FastRuntimeException("此方法 结束时间 必须指定");
        if(this.sku == null) throw new FastRuntimeException("此方法 sku 必须指定");
    }

    /**
     * query
     *
     * @return
     */
    private QueryBuilder querybuilder() {

        BoolQueryBuilder qb = QueryBuilders
                .boolQuery().must(QueryBuilders.termQuery("sku", this.parseEsSku()));

        if(!StringUtils.isBlank(this.sellingId)) {
            qb.must(QueryBuilders.termQuery("sellingid", this.parseEsSellingId()));
        }
        return qb;
    }


    /**
     * 过滤条件
     *
     * @return
     */
    private BoolFilterBuilder filterbuilder(boolean dateFilter) {
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

        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        if(this.market != null) {
            boolFilter.must(FilterBuilders.termFilter("market", this.market.name().toLowerCase()));
        }

        if(dateFilter) {
            boolFilter.must(FilterBuilders.rangeFilter("date").gte(fromD.toString(isoFormat))
                    .lt(toD.toString(isoFormat)));
        }
        return boolFilter;
    }

}
