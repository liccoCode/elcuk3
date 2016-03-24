package services;

import com.alibaba.fastjson.JSONObject;
import helper.*;
import models.finance.FeeType;
import models.market.M;
import models.market.Orderr;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import play.Logger;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/12/13
 * Time: 1:51 PM
 */
public class MetricAmazonFeeService {

    public Date from;
    public Date to;
    public M market;

    public MetricAmazonFeeService() {
    }

    public MetricAmazonFeeService(Date from, Date to, M market) {
        this.from = from;
        this.to = to;
        this.market = market;
    }

    /**
     * Selling 的 Amazon 消耗的费用;
     * <p/>
     * 因为 Amazon 收费的不及时, 最近 10 天, 手动模拟计算一个典型:
     * 固定:
     * US: fbaweightbasedfee, fbaweighthandlingfee, fbapickpackfeeperunit => 2.46 USD
     * UK: fbaweighthandlingfee, fbapickpackfeeperunit => 2.49 USD
     * DE: fbaweighthandlingfee, fbapickpackfeeperunit => 有高有低, 取 3.1 USD
     * 统一起来取  2.8 USD
     * Commission: 按照销售价 13% 取
     */
    public Map<String, Float> sellingAmazonFee(Date date, List<Selling> sellings, Map<String, Integer> sellingUnits) {
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            Map<String, Float> sellingAmzFeeMap = new HashMap<String, Float>();
            for(Selling sell : sellings) {
                if(sell.aps.salePrice == null) sell.aps.salePrice = 0f;
                Integer units = sellingUnits.get(sell.sellingId);
                if(units == null) units = 0;
                sellingAmzFeeMap.put(sell.sellingId, ((2.6f + (sell.aps.salePrice * 0.13f)) * units));
                /*无论 Amazon 还是 FBA 费用都是按照个数计算*/
            }
            return sellingAmzFeeMap;
        } else {
            List<FeeType> fees = FeeType.amazon().children;
            List<String> feesTypeName = new ArrayList<String>();
            for(FeeType feeType : fees) {
                if(feeType == FeeType.productCharger()) continue;
                if("shipping".equals(feeType.name)) continue;
                feesTypeName.add(feeType.name);
            }
            return sellingFeeTypesCost(date, feesTypeName);
        }

    }

    /**
     * Selling 的 FBA 销售的费用
     * <p/>
     * 因为 Amazon 收费的不及时, 最近 10 天, 手动模拟计算一个典型:
     * 固定:
     * US: fbaweightbasedfee, fbaweighthandlingfee, fbapickpackfeeperunit => 2.46 USD
     * UK: fbaweighthandlingfee, fbapickpackfeeperunit => 2.49 USD
     * DE: fbaweighthandlingfee, fbapickpackfeeperunit => 有高有低, 取 3.1 USD
     * 统一起来取  2.8 USD
     */
    public Map<String, Float> sellingAmazonFBAFee(Date date, List<Selling> sellings,
                                                  Map<String, Integer> sellingUnits) {
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            Map<String, Float> sellingAmzFbaFeeMap = new HashMap<String, Float>();
            for(Selling sell : sellings) {
                Integer units = sellingUnits.get(sell.sellingId);
                if(units == null) units = 0;
                sellingAmzFbaFeeMap.put(sell.sellingId, 2.6f * units);
            }
            return sellingAmzFbaFeeMap;
        } else {
            List<FeeType> fees = FeeType.fbaFees();
            List<String> feesTypeName = new ArrayList<String>();
            for(FeeType feeType : fees) {
                if(feeType == FeeType.productCharger()) continue;
                feesTypeName.add(feeType.name);
            }
            return sellingFeeTypesCost(date, feesTypeName);
        }
    }

    /**
     * 查询某天销售中, 每个 Selling 所涉及的 OrderId 是哪些;
     * 不同的市场需要拥有不同的时间段
     */
    @SuppressWarnings("unchecked")
    public Map<String, List<String>> oneDaySellingOrderIds(Date date) {
        String cacheKey = Caches.Q.cacheKey("oneDaySellingOrderIds", date);
        Map<String, List<String>> sellingOrders = Cache.get(cacheKey, Map.class);
        if(sellingOrders != null) return sellingOrders;
        synchronized(cacheKey.intern()) {
            sellingOrders = Cache.get(cacheKey, Map.class);
            if(sellingOrders != null) return sellingOrders;

            sellingOrders = new HashMap<String, List<String>>();
            for(M m : M.values()) {
                if(m.isEbay()) continue;
                sellingOrders.putAll(oneDaySellingOrderIds(date, m));
            }
            Cache.add(cacheKey, sellingOrders, "3mn");
        }
        return Cache.get(cacheKey, Map.class);
    }

    public Map<String, List<String>> oneDaySellingOrderIds(Date date, M market) {
        // 设置 group_concat_max_len 最大为 20M
        F.T2<DateTime, DateTime> actualDatePair = market.withTimeZone(Dates.morning(date), Dates.night(date));
        DBUtils.execute("set group_concat_max_len=20971520");
        SqlSelect sellingOdsSql = new SqlSelect()
                .select("selling_sellingId as sellingId", "group_concat(order_orderId) as orderIds")
                .from("OrderItem")
                .where("market=?").param(market.name())
                .where("createDate>=?").param(actualDatePair._1.toDate())
                .where("createDate<=?").param(actualDatePair._2.toDate())
                .groupBy("sellingId");
        Map<String, List<String>> sellingOrders = new HashMap<String, List<String>>();
        List<Map<String, Object>> rows = DBUtils.rows(sellingOdsSql.toString(), sellingOdsSql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            String sellingId = row.get("sellingId").toString();
            if(StringUtils.isBlank(sellingId)) continue;
            sellingOrders.put(sellingId, Arrays.asList(StringUtils.split(row.get("orderIds").toString(), ",")));
        }
        return sellingOrders;
    }

    /**
     * 指定 amazon 费用类型, 返回当天所有 Selling 这些费用类型的总费用
     */
    public Map<String, Float> sellingFeeTypesCost(Date date, List<String> feeTypes) {
        Map<String, List<String>> sellingOrders = oneDaySellingOrderIds(date);
        SqlSelect sellFeesTemplate = new SqlSelect()
                .select("sum(usdCost) as cost")
                .from("SaleFee")
                        // 需要统计 productcharges 销售价格, 和 shipping 加快快递(这个会在 amazon 中减去)
                .where(SqlSelect.whereIn("type_name", feeTypes));
        Map<String, Float> sellingSales = new HashMap<String, Float>();
        for(String sellingId : sellingOrders.keySet()) {
            SqlSelect sellFees = new SqlSelect(sellFeesTemplate)
                    .where(SqlSelect.whereIn("order_orderId", sellingOrders.get(sellingId)));
            Map<String, Object> row = DBUtils.row(sellFees.toString());
            Object costObj = row.get("cost");
            sellingSales.put(sellingId, costObj == null ? 0 : NumberUtils.toFloat(costObj.toString()));
        }
        return sellingSales;
    }

    public String stateToFeeCategory(Orderr.S state) {
        switch(state) {
            case SHIPPED:
                return "order";
            case REFUNDED:
                return "refunds";
            default:
                return "order";
        }
    }

    public Map<String, Map<String, BigDecimal>> orderFeesCost() {
        SearchSourceBuilder search = new SearchSourceBuilder().size(0);
        FilterAggregationBuilder dateAndMarketAggregation = AggregationBuilders.filter("date_and_market_filters")
                .filter(dateAndmarketFilters());
        //Orders & Refunds Fees
        for(Orderr.S state : Arrays.asList(Orderr.S.SHIPPED, Orderr.S.REFUNDED)) {
            FilterAggregationBuilder feeCategoryAggregation = AggregationBuilders.filter(stateToFeeCategory(state));

            //套上一层便于区分
            feeCategoryAggregation.filter(FilterBuilders.matchAllFilter());

            for(String feeType : Arrays.asList("productcharges", "principal", "promorebates", "commission")) {
                //使用 Cost 的正负来判断是属于 Order 还是 Refunds
                FilterAggregationBuilder feeTypeAggregation = AggregationBuilders.filter(feeType);
                feeTypeAggregation.filter(
                        FilterBuilders.boolFilter()
                                .must(FilterBuilders.termFilter("fee_type", feeType))
                                .must(costRangeFilter(state, feeType))
                );

                feeTypeAggregation.subAggregation(AggregationBuilders.sum("order_fees_cost").field("cost"));
                feeCategoryAggregation.subAggregation(feeTypeAggregation);
            }

            FilterAggregationBuilder otherAggregation = AggregationBuilders.filter("other");
            otherAggregation.filter(
                    FilterBuilders.boolFilter()
                            .mustNot(
                                    FilterBuilders.termsFilter("fee_type", Arrays.asList("productcharges", "principal",
                                            "promorebates", "commission", "fbaweightbasedfee",
                                            "fbaperorderfulfilmentfee", "fbaperunitfulfillmentfee"))
                            ).must(costRangeFilter(state, "other"))
            );
            otherAggregation.subAggregation(AggregationBuilders.sum("order_fees_cost").field("cost"));
            feeCategoryAggregation.subAggregation(otherAggregation);
            dateAndMarketAggregation.subAggregation(feeCategoryAggregation);
        }

        //Selling Fees
        FilterAggregationBuilder fbaFeeAggregation = AggregationBuilders.filter("selling_fees");
        fbaFeeAggregation.filter(
                FilterBuilders.termsFilter("fee_type",
                        Arrays.asList("fbaweightbasedfee", "fbaperorderfulfilmentfee", "fbaperunitfulfillmentfee"))
        );
        fbaFeeAggregation.subAggregation(AggregationBuilders.sum("order_fees_cost").field("cost"));
        dateAndMarketAggregation.subAggregation(fbaFeeAggregation);

        //Other Transactions
        search.aggregation(dateAndMarketAggregation);
        Logger.info("orderFeesCost:::" + search.toString());

        JSONObject result = ES.search("elcuk2", "salefee", search);
        if(result == null) throw new FastRuntimeException("ES 连接异常!");
        return readFeesCostInESResult(result);
    }

    public BoolFilterBuilder dateAndmarketFilters() {
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
        return FilterBuilders.boolFilter()
                .must(FilterBuilders.termFilter("market", this.market.name().toLowerCase()))
                .must(FilterBuilders.rangeFilter("date")
                        .gte(this.market.withTimeZone(Dates.morning(this.from)).toString(isoFormat))
                        .lt(this.market.withTimeZone(Dates.night(this.to)).toString(isoFormat))
                );
    }

    public RangeFilterBuilder costRangeFilter(Orderr.S state, String feeType) {
        RangeFilterBuilder costRangeFilter = FilterBuilders.rangeFilter("cost");
        if(state == Orderr.S.SHIPPED) {
            if("productcharges".equalsIgnoreCase(feeType) || "principal".equals(feeType) ||
                    "other".equalsIgnoreCase(feeType)) {
                costRangeFilter.gt(0);
            } else {
                costRangeFilter.lt(0);
            }
        } else if(state == Orderr.S.REFUNDED) {
            if("productcharges".equalsIgnoreCase(feeType) || "principal".equals(feeType) ||
                    "other".equalsIgnoreCase(feeType)) {
                costRangeFilter.lt(0);
            } else {
                costRangeFilter.gt(0);
            }
        }
        return costRangeFilter;
    }

    public Map<String, Map<String, BigDecimal>> readFeesCostInESResult(JSONObject esResult) {
        Map<String, Map<String, BigDecimal>> feesCost = new HashMap<String, Map<String, BigDecimal>>();
        JSONObject dateAndMarket = esResult.getJSONObject("aggregations").getJSONObject("date_and_market_filters");

        //Orders & Refunds Fees
        for(Orderr.S state : Arrays.asList(Orderr.S.SHIPPED, Orderr.S.REFUNDED)) {
            String feeCategory = stateToFeeCategory(state);
            JSONObject feeCategoryObj = dateAndMarket.getJSONObject(feeCategory);

            Map<String, BigDecimal> feeCategoryMap = new HashMap<String, BigDecimal>();
            for(String feeType : Arrays.asList("productcharges", "principal", "promorebates", "commission", "other")) {
                JSONObject feeTypeObj = feeCategoryObj.getJSONObject(feeType);
                BigDecimal cost = feeTypeObj.getJSONObject("order_fees_cost").getBigDecimal("value");

                if(feeType.equalsIgnoreCase("principal")) {
                    feeCategoryMap.put("productcharges",
                            feeCategoryMap.get("productcharges").add(cost.setScale(2, BigDecimal.ROUND_HALF_UP)));
                } else {
                    feeCategoryMap.put(feeType, cost.setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }
            feesCost.put(feeCategory, feeCategoryMap);
        }

        //Selling Fees
        JSONObject sellingFees = dateAndMarket.getJSONObject("selling_fees");
        BigDecimal cost = sellingFees.getJSONObject("order_fees_cost").getBigDecimal("value");
        feesCost.put("selling_fees", GTs.MapBuilder.map("fba_fee", cost.setScale(2, BigDecimal.ROUND_HALF_UP)).build());
        return feesCost;
    }
}
