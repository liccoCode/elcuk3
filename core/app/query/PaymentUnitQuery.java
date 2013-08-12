package query;

import helper.Currency;
import helper.DBUtils;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.db.helper.SqlSelect;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 7/31/13
 * Time: 5:16 PM
 */
public class PaymentUnitQuery {

    /**
     * 运输运费的所有币种
     *
     * @return
     */
    private List<Currency> transportshippingCurrencies(Date from, Date to, String... skus) {
        SqlSelect currencySql = new SqlSelect()
                .select("distinct p.currency")
                .from("PaymentUnit p")
                .leftJoin("ShipItem si ON si.id=p.shipItem_id")
                .leftJoin("ProcureUnit u ON u.id=si.unit_id")
                .where("p.createdAt>=?").param(from)
                .where("p.createdAt<=?").param(to)
                .where(SqlSelect.whereIn("u.sku", skus))
                .where("p.feeType_name='transportshipping'");
        List<Map<String, Object>> rows = DBUtils.rows(currencySql.toString(), currencySql.getParams().toArray());
        List<Currency> currencies = new ArrayList<Currency>();
        for(Map<String, Object> row : rows) {
            try {
                Currency currency = Currency.valueOf(row.get("currency").toString());
                currencies.add(currency);
            } catch(Exception e) {
                //ignore
            }
        }
        return currencies;
    }

    public Float avgShipmentTransportshippingFee(Shipment.T shipType, String feeTypeName,
                                                 Date from, Date to, String... skus) {
        /**
         * 1. 找到所有币种
         * 2. 每个币种进行统计总额
         * 3. 统一为 CNY 币种
         */
        // 这里不区分 SKU 了, 统计运输总数量的平均值
        SqlSelect sql = new SqlSelect()
                .select("sum(p.amount + p.fixValue) / sum(p.unitQty) as avgPrice", "p.currency as currency")
                .from("PaymentUnit p")
                .leftJoin("ShipItem si ON si.id=p.shipItem_id")
                .leftJoin("Shipment s ON si.shipment_id=s.id")
                .where("p.createdAt>=?").param(from)
                .where("p.createdAt<=?").param(to)
                .where("p.feeType_name=?").param(feeTypeName)
                .where("s.type=?").param(shipType.name())
                .groupBy("p.currency");
        // 没有指定 sku 则查询全部
        if(skus != null && skus.length > 0) {
            sql.leftJoin("ProcureUnit u ON u.id=si.unit_id").where(SqlSelect.whereIn("u.sku", skus));
        }

        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

        float cnyAveFee = 0;
        for(Map<String, Object> row : rows) {
            Currency currency = Currency.valueOf(row.get("currency").toString());
            cnyAveFee += currency.toCNY(NumberUtils.toFloat(row.get("avgPrice").toString()));
        }

        return cnyAveFee / (rows.size() <= 0 ? 1 : rows.size());
    }

    /**
     * 所涉及的 SKU 在一段时间内的空运运输单的平均运输运费
     *
     * @param from
     * @param to
     * @param skus
     * @return
     */
    public Float avgSkuAIRTransportshippingFee(Date from, Date to, String... skus) {
        return avgShipmentTransportshippingFee(Shipment.T.AIR, "transportshipping", from, to, skus);
    }

    /**
     * 所涉及的 SKU 在一段时间内的海运运输单的平均运输运费
     *
     * @param from
     * @param to
     * @param skus
     * @return
     */
    public Float avgSkuSEATransportshippingFee(Date from, Date to, String... skus) {
        return avgShipmentTransportshippingFee(Shipment.T.SEA, "transportshipping", from, to, skus);
    }

    /**
     * 快递平均费用
     *
     * @param from
     * @param to
     * @param skus
     * @return
     */
    public Map<String, Float> avgSkuExpressTransportshippingFee(Date from, Date to, String... skus) {
        /**
         * 1. 找到所有币种
         * 2. 每个币种进行统计总额
         * 3. 统一为 CNY 币种
         */
        Map<Currency, Map<String, Float>> currencyAvgFeeMap = new HashMap<Currency, Map<String, Float>>();
        List<Currency> currencies = transportshippingCurrencies(from, to, skus);
        for(Currency currency : currencies) {
            if(!currencyAvgFeeMap.containsKey(currency))
                currencyAvgFeeMap.put(currency, new HashMap<String, Float>());
        }

        // 2
        for(Currency crcy : currencyAvgFeeMap.keySet()) {
            SqlSelect sql = new SqlSelect()
                    .select("sum(p.amount + p.fixValue) / sum(p.unitQty) as avgPrice", "u.sku", "p.currency")
                    .from("PaymentUnit p")
                    .leftJoin("ShipItem si ON si.id=p.shipItem_id")
                    .leftJoin("ProcureUnit u ON u.id=si.unit_id")
                    .where("p.createdAt>=?").param(from)
                    .where("p.createdAt<=?").param(to)
                    .where(SqlSelect.whereIn("u.sku", skus))
                    .where("p.feeType_name='transportshipping'")
                    .where("p.currency=?").param(crcy.name())
                    .groupBy("u.sku");

            List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

            for(Map<String, Object> row : rows) {
                if(row.get("sku") == null || StringUtils.isBlank(row.get("sku").toString())) continue;

                currencyAvgFeeMap.get(crcy)
                        .put(row.get("sku").toString(), NumberUtils.toFloat(row.get("avgPrice").toString()));
            }
        }

        // 3
        return mergeSkuDiffCurrencyToCNY(currencyAvgFeeMap);
    }

    /**
     * 将 Sku 不同币种的费用统一成为 CNY 币种
     *
     * @param currencyAvgFeeMap 不同币种下, 不同 sku 所产生的费用
     * @return
     */
    private Map<String, Float> mergeSkuDiffCurrencyToCNY(Map<Currency, Map<String, Float>> currencyAvgFeeMap) {
        Map<String, Float> cnyMap = new HashMap<String, Float>();
        Map<String, AtomicInteger> crcyChangeTimes = new HashMap<String, AtomicInteger>();

        for(Currency crcy : currencyAvgFeeMap.keySet()) {
            Map<String, Float> crcyMap = currencyAvgFeeMap.get(crcy);
            for(String sku : crcyMap.keySet()) {
                if(cnyMap.containsKey(sku)) {
                    crcyChangeTimes.get(sku).incrementAndGet();
                    cnyMap.put(sku, cnyMap.get(sku) + crcy.toCNY(crcyMap.get(sku)));
                } else {
                    crcyChangeTimes.put(sku, new AtomicInteger(1));
                    cnyMap.put(sku, crcy.toCNY(crcyMap.get(sku)));
                }
            }
        }

        for(String sku : cnyMap.keySet()) {
            int times = crcyChangeTimes.get(sku).get();
            cnyMap.put(sku, cnyMap.get(sku) / times);
        }
        return cnyMap;
    }

}
