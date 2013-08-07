package query;

import helper.Currency;
import helper.DBUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 7/31/13
 * Time: 5:16 PM
 */
public class PaymentUnitQuery {

    /**
     * 查询 skus 的平均运输运费(三个月)
     *
     * @param skus
     * @return
     */
    public Map<String, Float> avgSkuTransportshippingFee(String... skus) {
        DateTime now = DateTime.now();
        return avgSkuTransportshippingFee(now.minusMonths(3).toDate(), now.toDate(), skus);
    }

    public Map<String, Float> avgSkuTransportshippingFee(Date from, Date to, String... skus) {
        /**
         * 1. 找到所有币种
         * 2. 每个币种进行统计总额
         * 3. 统一为 CNY 币种
         */
        // 1
        SqlSelect currencySql = new SqlSelect()
                .select("distinct p.currency")
                .from("PaymentUnit p")
                .leftJoin("ShipItem si ON si.id=p.shipItem_id")
                .leftJoin("ProcureUnit u ON u.id=si.unit_id")
                .where("p.createdAt>=?").param(from)
                .where("p.createdAt<=?").param(to)
                .where(SqlSelect.whereIn("u.sku", skus))
                .where("p.feeType_name='transportshipping'");

        List<Map<String, Object>> currencies = DBUtils.rows(currencySql.toString(), currencySql.getParams().toArray());
        Map<Currency, Map<String, Float>> currencyAvgFeeMap = new HashMap<Currency, Map<String, Float>>();

        for(Map<String, Object> crcy : currencies) {
            Currency currency = Currency.valueOf(crcy.get("currency").toString());
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
                    .groupBy("u.sku", "p.currency");

            List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

            for(Map<String, Object> row : rows) {
                currencyAvgFeeMap.get(crcy)
                        .put(row.get("sku").toString(), NumberUtils.toFloat(row.get("avgPrice").toString()));
            }
        }


        // 3
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
