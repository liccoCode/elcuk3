package query;

import helper.Currency;
import helper.DBUtils;
import models.market.M;
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
        List<Currency> currencies = new ArrayList<>();
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

    public Float avgShipmentTransportshippingFee(Shipment.T shipType, String feeTypeName, Date from, Date to, M market) {
        /**
         * 1. 找到所有币种
         * 2. 每个币种进行统计总额
         *  2.1. 找到总金额
         *  2.2. 找到总数量
         *  2.3. 计算平均值
         * 3. 统一为 CNY 币种
         */
        // 这里不区分 SKU 了, 统计运输总数量的平均值
        SqlSelect totalAmount = new SqlSelect()
                .select("sum(p.amount + p.fixValue) as sumFee", "p.currency as currency",
                        "group_concat(s.id) as shipmentIds")
                .from("PaymentUnit p")
                .leftJoin("Shipment s ON p.shipment_id=s.id")
                .leftJoin("Whouse w ON w.id=s.whouse_id")
                .where("p.createdAt>=?").param(from)
                .where("p.createdAt<=?").param(to)
                .where("p.feeType_name=?").param(feeTypeName)
                .where("s.type=?").param(shipType.name())
                .where("w.market=?").param(market.name())
                .groupBy("p.currency");

        List<Map<String, Object>> rows = DBUtils.rows(totalAmount.toString(), totalAmount.getParams().toArray());

        float cnyAveFee = 0;
        int totalQty = 0;

        List<String> shipmentIds = new ArrayList<>();
        for(Map<String, Object> row : rows) {
            Currency currency = Currency.valueOf(row.get("currency").toString());
            cnyAveFee += currency.toCNY(NumberUtils.toFloat(row.get("sumFee").toString()));
            shipmentIds.addAll(Arrays.asList(StringUtils.split(row.get("shipmentIds").toString(), ",")));
        }

        SqlSelect totalQtySql = new SqlSelect()
                .select("sum(qty) as qty")
                .from("ShipItem")
                .where(SqlSelect.whereIn("shipment_id", shipmentIds));
        Map<String, Object> row = DBUtils.row(totalQtySql.toString());
        totalQty = row.get("qty") == null ? 0 : NumberUtils.toInt(row.get("qty").toString());

        return totalQty == 0 ? 0 : (cnyAveFee / totalQty);
    }

    /**
     * 所涉及的 SKU 在一段时间内的空运运输单的平均运输运费
     *
     * @param from
     * @param to
     * @return
     */
    public Float avgSkuAIRTransportshippingFee(Date from, Date to, M market) {
        //TODO transportshipping 的名称需要调整!
        return avgShipmentTransportshippingFee(Shipment.T.AIR, "transportshipping", from, to, market);
    }

    /**
     * 所涉及的 SKU 在一段时间内的海运运输单的平均运输运费
     *
     * @param from
     * @param to
     * @return
     */
    public Float avgSkuSEATransportshippingFee(Date from, Date to, M market) {
        return avgShipmentTransportshippingFee(Shipment.T.SEA, "transportshipping", from, to, market);
    }

    /**
     * 快递平均费用
     *
     * @param from
     * @param to
     * @param skus
     * @return
     */
    public Map<String, Float> avgSkuExpressTransportshippingFee(Date from, Date to, M market, String... skus) {
        /**
         * 1. 找到所有币种
         * 2. 每个币种进行统计总额
         * 3. 统一为 CNY 币种
         */
        Map<Currency, Map<String, Float>> currencyAvgFeeMap = new HashMap<>();
        List<Currency> currencies = transportshippingCurrencies(from, to, skus);
        currencies.stream().filter(currency -> !currencyAvgFeeMap.containsKey(currency))
                .forEach(currency -> currencyAvgFeeMap.put(currency, new HashMap<>()));

        // 2
        for(Currency crcy : currencyAvgFeeMap.keySet()) {
            SqlSelect sql = new SqlSelect();
            sql.select("sum(p.amount+p.fixValue)/sum(si.qty-IFNULL(u.purchaseSample,0)) as avgPrice,u.sku,p.currency")
                    .from("PaymentUnit p")
                    .leftJoin("ShipItem si ON si.id=p.shipItem_id")
                    .leftJoin("ProcureUnit u ON u.id=si.unit_id")
                    .leftJoin("Whouse w ON w.id=u.whouse_id")
                    .where("p.createdAt>=?").param(from)
                    .where("p.createdAt<=?").param(to)
                    .where(SqlSelect.whereIn("u.sku", skus))
                    .where("p.feeType_name='transportshipping'")
                    .where("p.currency=?").param(crcy.name())
                    .where("w.market=?").param(market.name())
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
        Map<String, Float> cnyMap = new HashMap<>();
        Map<String, AtomicInteger> crcyChangeTimes = new HashMap<>();

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
