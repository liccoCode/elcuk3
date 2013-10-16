package services;

import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import models.finance.Payment;
import models.procure.Shipment;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import play.Logger;
import play.db.helper.SqlSelect;

import java.util.*;

/**
 * 用来计算运输方面的运费的业务
 * User: wyatt
 * Date: 9/11/13
 * Time: 3:46 PM
 */
public class MetricShipCostService {

    /**
     * 计算某一天所有 Selling 的海运运费
     */
    public Map<String, Float> seaCost(Date oneDay) {
        /**
         * 1. 通过 Payment 找到当天付款的运输单 ids, 以及总费用(排除 VAT关税)
         * 2. 通过 Payment 找到支付的总体积
         * 3. 根据 Selling 的 Product 找出产品重量, 并根据总费用计算出单位体积的费用.
         * 4. 根据 Selling 记录的体积, 计算出此 Selling 的海运费用
         */

        // 1. 寻找所涉及的 运输单 和 总费用
        SqlSelect shipIdsAndFee = new SqlSelect()
                .select("group_concat(distinct(s.id)) shipmentIds", "pu.currency",
                        "sum(pu.unitPrice * pu.unitQty + pu.fixValue) cost")
                .from("PaymentUnit pu")
                .leftJoin("Shipment s ON s.id=pu.shipment_id")
                .leftJoin("Payment p ON p.id=pu.payment_id")
                .leftJoin("FeeType fy ON pu.feeType_name=fy.name")
                .where("date_format(p.paymentDate, '%Y-%m-%d')=?").param(Dates.date2Date(oneDay))
                .where("p.state=?").param(Payment.S.PAID.name())
                .where("fy.parent_name=?").param("transport")
                .where("pu.feeType_name!=?").param("dutyandvat")
                .where("s.type=?").param(Shipment.T.SEA.name())
                .groupBy("pu.currency");

        Set<String> shipmentIds = new HashSet<String>();
        // USD
        float totalSeaFee = 0;

        List<Map<String, Object>> rows = DBUtils.rows(shipIdsAndFee.toString(), shipIdsAndFee.getParams().toArray());
        for(Map<String, Object> row : rows) {
            Currency currency = Currency.valueOf(row.get("currency").toString());
            totalSeaFee += row.get("cost") == null ? 0 : currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
            if(row.get("shipmentIds") != null) {
                shipmentIds.addAll(Arrays.asList(StringUtils.split(row.get("shipmentIds").toString(), ",")));
            }
        }


        // 2. 寻找付费的总体积 m3
        float totalWeight = 0;
        SqlSelect totalWeightSql = new SqlSelect()
                .select("sum(pu.unitQty) weight")
                .from("PaymentUnit pu")
                .where("pu.feeType_name=?").param("oceanfreight")
                .where(SqlSelect.whereIn("pu.shipment_id", shipmentIds));
        Map<String, Object> weightRow = DBUtils.row(totalWeightSql.toString(), totalWeightSql.getParams().toArray());
        totalWeight = weightRow.get("weight") == null ? 0 : NumberUtils.toFloat(weightRow.get("weight").toString());

        // 3. 找出 Selling 的体积 m3/数量
        Map<String, Float> sellingCubicMeters = new HashMap<String, Float>();
        Map<String, Float> sellingSeaShipQty = new HashMap<String, Float>();
        Map<String, Float> sellingSeaCost = new HashMap<String, Float>();
        SqlSelect sellingCubicMeterSql = new SqlSelect()
                // cm3 -> m3 (/1000*1000*1000)
                .select("u.selling_sellingId sellingId", "sum(si.qty) qty",
                        "((p.width * p.heigh * p.lengths) / (1000*1000*1000)) m3")
                .from("ShipItem si")
                .leftJoin("Shipment s ON s.id=si.shipment_id")
                .leftJoin("ProcureUnit u ON u.id=si.unit_id")
                .leftJoin("Product p ON p.sku=u.product_sku")
                .where(SqlSelect.whereIn("s.id", shipmentIds))
                .groupBy("u.selling_sellingId");
        rows = DBUtils.rows(sellingCubicMeterSql.toString(), sellingCubicMeterSql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            if(row.get("sellingId") == null) continue;
            sellingCubicMeters.put(row.get("sellingId").toString(),
                    row.get("m3") == null ? 0 : NumberUtils.toFloat(row.get("m3").toString()));
            sellingSeaShipQty.put(row.get("sellingId").toString(),
                    row.get("qty") == null ? 0 : NumberUtils.toFloat(row.get("qty").toString()));
        }

        float perCubicMeter = totalWeight == 0 ? 0 : totalSeaFee / totalWeight;
        if(totalWeight == 0) Logger.warn("运输单 ['%s'] 中没有 oceanfreight 费用?", StringUtils.join(shipmentIds, "','"));
        for(String sid : sellingCubicMeters.keySet()) {
            sellingSeaCost.put(sid, sellingCubicMeters.get(sid) * perCubicMeter * sellingSeaShipQty.get(sid));
        }

        return sellingSeaCost;
    }


    /**
     * 计算出 oneDay 所有涉及到的 Selling 的 VAT 费用
     * <p/>
     * 如果 oneDay 没有, 则为 0
     *
     * @return
     */
    public Map<String, Float> sellingVATFee(Date oneDay) {
        /**
         * 1. 找出当天已经支付的 VAT 的费用类型的总费用和涉及的运输单
         * 2. 通过运输单找出相关的 Selling
         */

        SqlSelect vatSql = new SqlSelect()
                .select("group_concat(pu.shipment_id) shipmentIds", "pu.currency"
                        , "sum(pu.unitPrice * pu.unitQty + pu.fixValue) cost")
                .from("PaymentUnit pu")
                .leftJoin("Payment p ON p.id=pu.payment_id")
                .where("pu.feeType_name=?").param("dutyandvat")/*固定费用类型*/
                .where("date_format(p.paymentDate, '%Y-%m-%d')=?").param(Dates.date2Date(oneDay))
                .where("p.state=?").param(Payment.S.PAID.name())
                .groupBy("pu.currency");
        List<Map<String, Object>> rows = DBUtils.rows(vatSql.toString(), vatSql.getParams().toArray());

        // 所有关税 USD
        float totalVATFee = 0;
        Set<String> shipmentIds = new HashSet<String>();
        for(Map<String, Object> row : rows) {
            Currency currency = Currency.valueOf(row.get("currency").toString());
            totalVATFee += row.get("cost") == null ? 0 : currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
            if(row.get("shipmentIds") != null) {
                shipmentIds.addAll(Arrays.asList(StringUtils.split(row.get("shipmentIds").toString(), ",")));
            }
        }
        // 产出 shipmentIds 与 totalVATFee

        SqlSelect effectSellingSql = new SqlSelect()
                .select("s.sellingId", "sum(si.qty) qty", "round(sum(si.qty * p.declaredValue), 2) x")
                .from("Selling s")
                .leftJoin("ProcureUnit u ON u.selling_sellingId=s.sellingId")
                .leftJoin("Product p ON p.sku=u.product_sku")
                .leftJoin("ShipItem si ON si.unit_id=u.id")
                .leftJoin("Shipment t ON t.id=si.shipment_id")
                .where(SqlSelect.whereIn("t.id", shipmentIds))
                .groupBy("s.sellingId");

        Map<String, Float> sellingsVAT = new HashMap<String, Float>();
        Map<String, Float> sellingsX = new HashMap<String, Float>();
        rows = DBUtils.rows(effectSellingSql.toString(), effectSellingSql.getParams().toArray());

        // (440x + 220x + 124x)[sumX] = (2000)[totalVATFee] -> x = 2.55
        // 440 * 2.55 / 400[qty] = 2.805 [单个 VAT]
        float sumX = 0;
        for(Map<String, Object> row : rows) {
            if(row.get("sellingId") == null || StringUtils.isBlank(row.get("sellingId").toString())) continue;
            float nX = row.get("x") == null ? 0 : NumberUtils.toFloat(row.get("x").toString());
            sumX += nX;
            sellingsX.put(row.get("sellingId").toString(), nX);
        }
        float x = sumX == 0 ? 0 : (totalVATFee / sumX);

        for(String sid : sellingsX.keySet()) {
            sellingsVAT.put(sid, x * sellingsX.get(sid));
        }

        return sellingsVAT;
    }
}
