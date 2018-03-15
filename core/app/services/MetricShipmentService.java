package services;

import com.alibaba.fastjson.JSONObject;
import helper.*;
import models.market.M;
import models.procure.Shipment;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import play.db.helper.SqlSelect;
import play.utils.FastRuntimeException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物流报表相关的 ES 计算
 * User: mac
 * Date: 14-6-3
 * Time: PM4:01
 */
public class MetricShipmentService {

    /**
     * 开始
     */
    public Date from;

    /**
     * 结束
     */
    public Date to;

    /**
     * 市场
     */
    public M market;

    /**
     * 运输方式
     */
    public Shipment.T type;

    public MetricShipmentService(Date from, Date to, Shipment.T type, M market) {
        this.from = Dates.morning(from);
        this.to = Dates.night(to);
        this.market = market;
        this.type = type;
    }

    public MetricShipmentService(Date from, Date to, Shipment.T type) {
        this.from = Dates.morning(from);
        this.to = Dates.night(to);
        this.type = type;
    }

    /**
     * 准备过滤参数
     *
     * @return
     */
    private BoolQueryBuilder filterbuilder() {
        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTimeNoMillis().withZoneUTC();
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        DateTime fromD = new DateTime(this.from);
        DateTime toD = new DateTime(this.to);
        if(this.market != null) {
            qb.must(QueryBuilders.termQuery("market", this.market.name().toLowerCase()));
        }
        //日期过滤
        qb.must(QueryBuilders.rangeQuery("ship_date").gte(fromD.toString(isoFormat)).lt(toD.toString(isoFormat)));
        // 运输方式不为空时做 type 过滤
        if(this.type != null) qb.must(QueryBuilders.termQuery("ship_type", this.type.name()));
        return qb;
    }

    /**
     * 统计运输费用(市场 或者 运输方式)
     */
    public Float countShipFee() {
        SumAggregationBuilder builder = AggregationBuilders.sum("cost_in_usd").field("cost_in_usd");
        SearchSourceBuilder search = new SearchSourceBuilder()
                .query(filterbuilder())
                .aggregation(builder)
                .size(0);
        JSONObject result = ES.search(System.getenv(Constant.ES_INDEX), "shippayunit", search);
        if(result == null) throw new FastRuntimeException("ES 连接异常!");
        JSONObject cost = result.getJSONObject("aggregations").getJSONObject("cost_in_usd");
        return cost.getFloat("value");
    }


    /**
     * 统计运输重量(市场 或者 运输方式)
     */
    public Float countShipWeight() {
        //由于ES的 shippayunit 与查询要求不符 故放弃使用ES而采用直接查询DB(考虑到数据量不是很大且查询语句为 SUM 统计函数)
        SqlSelect sql = new SqlSelect().select("SUM(CASE WHEN pro.weight IS NULL THEN 0 * si.qty "
                + "WHEN pro.weight >= 0 THEN pro.weight * si.qty END"
                + ") weight")
                .from("ShipItem si")
                .leftJoin("Shipment s ON si.shipment_id=s.id")
                .leftJoin("ProcureUnit pu ON si.unit_id=pu.id")
                .leftJoin("Product pro on pu.product_sku = pro.sku")
                .leftJoin("Whouse w ON w.id=s.whouse_id")
                .where("s.planBeginDate>=?").param(this.from)
                .andWhere("s.planBeginDate<=?").param(this.to);
        if(this.type != null) sql.andWhere("s.type=?").param(this.type.toString());
        if(this.market != null) sql.andWhere("w.name IN (" + this.market.marketTransferEUR() + ")");
        Object result = DBUtils.row(sql.toString(), sql.getParams().toArray()).get("weight");
        return result == null ? 0 : NumberUtils.toFloat(result.toString());
    }

    /**
     * 统计运输关税和VAT(市场 或者 运输方式)
     */
    public Map<String, Float> countVAT() {
        SqlSelect sql = new SqlSelect().select("w.name, s.type, p.currency, round(sum(p.unitPrice), 2) as vatPrice")
                .from("Shipment s")
                .leftJoin("Whouse w ON w.id=s.whouse_id")
                .innerJoin("PaymentUnit p ON p.shipment_id = s.id AND p.feeType_name = 'dutyandvat'")
                .where("s.planBeginDate>=?").param(this.from)
                .andWhere("s.planBeginDate<=?").param(this.to);
        if(this.type != null) sql.andWhere("s.type=?").param(this.type.toString());
        if(this.market != null) sql.andWhere("w.name= IN (" + this.market.marketTransferEUR() + ")");
        sql.groupBy("w.name, s.type, p.currency");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, Float> vat = new HashMap<>();
        for(Map<String, Object> objectMap : rows) {
            String row_market = objectMap.get("name").toString().split("_")[1];
            String row_type = objectMap.get("type").toString();
            Currency currency = Currency.valueOf(objectMap.get("currency").toString());
            Float vatPrice = Float.valueOf(objectMap.get("vatPrice").toString());
            String key = row_market + "_" + row_type;
            if(vat.containsKey(key)) {
                Float existVatPrice = vat.get(key);
                vat.put(key, currency.toUSD(vatPrice) + existVatPrice);
            } else {
                vat.put(key, currency.toUSD(vatPrice));
            }
        }
        return vat;
    }
}
