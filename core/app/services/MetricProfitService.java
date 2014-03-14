package services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.ES;
import models.market.M;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.product.Product;
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
    public String breoreSku;

    public MetricProfitService(Date begin, Date end, M market,
                               String sku, String sellingId) {
        this.begin = begin;
        this.end = end;
        this.market = market;
        //计算中需要查询SKU的MODEL
        this.breoreSku = sku;
        //特殊字符需要转换
        this.sku = ES.parseEsString(sku);
        this.sellingId = ES.parseEsString(sellingId);
        checkParam();
    }


    /**
     * SKU总销售额
     */
    public Float sellingAmazonTotalFee() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("cost_in_usd")
                        .facetFilter(this.filterbuilder(true)
                                //销售费用项目
                                .must(FilterBuilders.termFilter("fee_type", "productcharges")))
                ).size(0);

        return getEsTermsTotal(search, "salefee");
    }

    /**
     * SKU亚马逊费用
     */
    public Float sellingAmazonFee() {
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(querybuilder())
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("cost_in_usd")
                        .facetFilter(this.filterbuilder(true)
                                //FBA亚马逊项目
                                .must(FilterBuilders.termFilter("fee_type", "commission")))
                ).size(0);
        float x = getEsTermsTotal(search, "salefee");
        return x;
    }

    /**
     * SKUFBA费用
     */
    public Float sellingAmazonFBAFee() {
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
    public Float sellingAmazonQty() {
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
    public Float payPrice() {
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
    public Float shipPrice() {
        /**
         * 从ES同时查出符合sku条件的列表，以及按照ship_type分组的求和
         */
        TermsFilterBuilder orfilter = FilterBuilders.termsFilter("ship_type", "sea", "express", "air");
        SearchSourceBuilder search = new SearchSourceBuilder()
                .filter(FilterBuilders.boolFilter().must(FilterBuilders.termFilter("sku",
                        this.sku.toLowerCase()))).size(Integer.MAX_VALUE / 10 - 100000000)
                .facet(FacetBuilders.termsStatsFacet("units")
                        .keyField("ship_type")
                        .valueField("cost_in_usd")
                        .facetFilter(this.filterbuilder(false).must(orfilter
                        ).must(FilterBuilders.termFilter("sku", this.sku.toLowerCase()))
                        ));
        //总运费
        F.T2<JSONArray, JSONArray> esresult = getEsShipTerms(search, "shippayunit");
        JSONArray feearray = esresult._1;
        if(feearray == null)
            return 0f;
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
        //海运总运费乘以重量比例
        float expressfee = getshipfee(expressvolume, expresstotalfee);

        //运费的平均单价=运费/SKu的数量
        Integer skuqty = seavolume._3 + airvolume._3 + expressvolume._3;
        float price = 0;
        if(skuqty != 0) {
            price = (seafee + airfee + expressfee) / skuqty;
        }
        return price;
    }

    /**
     * 关税和VAT单价
     */
    public Float vatPrice() {
        //关税
        SearchSourceBuilder search = new SearchSourceBuilder()
                .facet(FacetBuilders.statisticalFacet("units")
                        .field("cost_in_usd")
                        .facetFilter(this.filterbuilder(false).must(
                                FilterBuilders.orFilter().add(FilterBuilders.termFilter("fee_type", "sea"))
                                        .add(FilterBuilders.termFilter("fee_type", "express"))
                                        .add(FilterBuilders.termFilter("fee_type", "air")
                                        ).add(FilterBuilders.termFilter("skus", this.sku.toLowerCase()))
                        )
                        )).size(0);
        //总关税和VAT
        float fee = getEsTermsTotal(search, "shippayunit");
        //获取与关税相关的运输单
        Set<String> mentids = getVatMentIds();

        /**
         * 获取VAT的系数 所有涉及SKU的运输单总关税 / (sku1数量*申报价格1+sku2数量*申报价格2+....)
         */
        Float totalprice = getVatTotalPrice(mentids);
        float param = 0f;
        if(totalprice != 0f) {
            param = fee / totalprice;
        }
        Product product = Product.findById(this.breoreSku);
        float vatprice = product.declaredValue * param;
        return vatprice;
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
    private Set<String> getVatMentIds() {
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        //包含SKU
        boolFilter.must(FilterBuilders.termFilter("sku", this.sku.toLowerCase()));
        boolFilter.must(FilterBuilders.termFilter("fee_type", "sea"));
        builder.query(QueryBuilders
                .queryString("")
                .field("shipment_id").field("fee_type")
        ).filter(boolFilter);

        JSONObject result = ES.search("elcuk2", "shippayunit", builder);
        JSONObject hits = result.getJSONObject("hits");
        Set<String> vatMentIds = new HashSet<String>();
        if(hits != null) {
            for(Object obj : hits.getJSONArray("hits")) {
                JSONObject hit = (JSONObject) obj;
                JSONObject source = hit.getJSONObject("_source");
                if(source.getString("fee_type").equals("sea")) {
                    vatMentIds.add(hit.getJSONObject("_source").getString("shipment_id"));
                }
            }
        }
        return vatMentIds;
    }

    /**
     * 计算Vat的 sku1数量*申报价格1+sku2数量*申报价格2+....
     *
     * @param mentIds
     * @param shiptype
     * @return
     */
    private Float getVatTotalPrice(Set<String> mentIds) {
        /**
         * 获取所有运输单
         */
        List<Shipment> shipment = Shipment.find(SqlSelect.whereIn("id", mentIds)).fetch();
        //所有SKU的申报价格总和
        float totalprice = 0f;
        for(Shipment ment : shipment) {
            List<ShipItem> items = ment.items;
            for(ShipItem item : items) {
                //单个SKU数量
                Integer itemqty = item.unit.attrs.qty;
                if(itemqty == null)
                    itemqty = 0;
                totalprice = totalprice + itemqty * item.unit.product.declaredValue;
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
        String sql = SqlSelect.whereIn("id", mentIds);
        if(sql == null || sql.length() <= 0) {
            return new F.T3<Float, Float, Integer>(0f, 0f, 0);
        }
        /**
         * 获取所有运输单
         */
        List<Shipment> shipment = Shipment.find(sql).fetch();

        //单个SKU的数量
        Integer qty = 0;
        //所有SKU的体积
        float totalvolume = 0f;
        //单个SKU的体积
        float volume = 0f;
        for(Shipment ment : shipment) {
            List<ShipItem> items = ment.items;
            for(ShipItem item : items) {
                Integer unitqty = item.unit.attrs.qty;
                if(unitqty == null)
                    unitqty = 0;
                Product p = item.unit.product;
                float siglevolume = 0f;
                //快递用重量计算比例
                if(shiptype.equals("express")) {
                    //重量
                    siglevolume = p.weight * unitqty;
                } else {
                    //体积
                    siglevolume = p.lengths * p.width * p.heigh * unitqty;
                }
                totalvolume = totalvolume + siglevolume;
                if(this.breoreSku.equals(p.sku)) {
                    //SKU的数量
                    qty = qty + unitqty;
                    volume = volume + siglevolume;
                }
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
    private F.T2<JSONArray, JSONArray> getEsShipTerms(SearchSourceBuilder search, String estype) {
        JSONObject result = ES.search("elcuk2", estype, search);
        if(result == null) {
            throw new FastRuntimeException("ES连接异常!");
        }
        JSONObject facets = result.getJSONObject("facets");
        JSONArray terms = null;
        if(facets != null) {
            JSONObject units = facets.getJSONObject("units");
            terms = units.getJSONArray("terms");
        }
        JSONObject hits = result.getJSONObject("hits");
        JSONArray hitmentids = null;
        if(facets != null) {
            hitmentids = hits.getJSONArray("hits");
        }
        return new F.T2<JSONArray, JSONArray>(terms, hitmentids);
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
                .boolQuery().must(QueryBuilders.fieldQuery("sku", this.sku));

        if(!StringUtils.isBlank(this.sellingId)) {
            qb.must(QueryBuilders.fieldQuery("sellingid", this.sellingId));
        }

        return qb;
    }


    /**
     * 过滤条件
     *
     * @return
     */
    private BoolFilterBuilder filterbuilder(boolean dateFilter) {
        DateTime fromD = M.AMAZON_US.withTimeZone(begin);
        DateTime toD = M.AMAZON_US.withTimeZone(end);
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
