package services;

import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import models.finance.FeeType;
import models.market.Selling;
import models.market.SellingRecord;
import models.procure.Shipment;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.*;

/**
 * 用来计算运输方面的运费的业务
 * User: wyatt
 * Date: 9/11/13
 * Time: 3:46 PM
 */
public class MetricShipCostService {

    /**
     * 计算某一个 Selling 某一天的快递运费
     *
     * @return 单个快递运费, 总重量, 这一天的总费用
     */
    public F.T3<Float, Float, Float> expressCost(Selling selling, Date oneDay) {
        return baseCost(selling, oneDay, Shipment.T.EXPRESS, FeeType.expressFee());
    }


    /**
     * 计算某一个 Selling 某一天的空运运费
     *
     * @return 单个空运运费, 总重量, 这一天的总费用
     */
    public F.T3<Float, Float, Float> airCost(Selling selling, Date oneDay) {
        return baseCost(selling, oneDay, Shipment.T.AIR, FeeType.airFee());
    }


    /**
     * 计算某一个 Selling 某一天的海运运费
     *
     * @return 单个海运运费, 总重量, 这一天的总费用
     */
    public F.T3<Float, Float, Float> seaCost(Selling selling, Date oneDay) {
        return baseCost(selling, oneDay, Shipment.T.SEA, FeeType.oceanfreight());
    }

    public F.T3<Float, Float, Float> baseCost(Selling selling, Date oneDay, Shipment.T type, FeeType feeType) {
        /**
         * 1. 找到昨天的 SellingRecord 的数据, 用于统计计算今天的值.
         * 2. 从今天支付完成的运输付款单出发, 找出当天 selling 所涉及的所有快递/空运/海运运输单的总费用(不包括 VAT和关税)与总运输重量.
         * 3. 计算出昨天和今天的总费用与总重量, 然后通过 总费用/总重量 计算出 $N/kg(m3) 的值
         * 4. 根据 selling 自己所涉及的 sku 的重量 * $N/kg(m3) 计算出当前 selling 的运输成本.
         */
        SellingRecord oneDayRecord = SellingRecord.oneDay(selling.sellingId, oneDay);
        // 通过 PaymentUnit 找涉及的运输单
        SqlSelect effectShipmentSql = new SqlSelect()
                .select("group_concat(s.id) as shipmentIds")
                .from("PaymentUnit pu")
                .leftJoin("Payment p ON p.id=pu.payment_id")
                .leftJoin("Shipment s ON s.id=pu.shipment_id")
                .leftJoin("ShipItem si ON s.id=si.shipment_id")
                .leftJoin("ProcureUnit u ON u.id=si.unit_id")
                .where("u.selling_sellingId=?").param(selling.sellingId)
                .where("s.type=?").param(type.name())
                .where("date_format(p.paymentDate, '%Y-%m-%d')=?").param(Dates.date2Date(oneDay))
                .groupBy("s.id");

        Map<String, Object> idsRs = DBUtils.row(effectShipmentSql.toString(), effectShipmentSql.getParams().toArray());

        List<String> effectShipmentIds = new ArrayList<String>();
        if(idsRs.size() > 0)
            effectShipmentIds.addAll(Arrays.asList(StringUtils.split(idsRs.get("shipmentIds").toString(), ",")));


        SqlSelect sumBase = new SqlSelect().from("PaymentUnit pu")
                .leftJoin("Shipment s ON s.id=pu.shipment_id")
                .where(SqlSelect.whereIn("pu.shipment_id", effectShipmentIds))
                .where("s.type=?").param(type.name()); // 这里重复检查运输单类型, 是避免 effectShipmentIds 为空
        // 通过运输单找他们的总运输费用
        SqlSelect sumFeeSql = new SqlSelect(sumBase)
                .select("sum(pu.unitPrice * pu.unitQty + pu.fixValue) cost", "pu.currency")
                .where("pu.feeType_name!=?").param("dutyandvat")/*固定的 VAT 和关税的费用类型*/
                .groupBy("pu.currency");
        // 通过运输单找他们的总重量
        SqlSelect sumKilogram = new SqlSelect(sumBase)
                .select("sum(unitQty) kg")
                .where("feeType_name=?").param(feeType.name);


        float currentFee = 0;
        float totalKilogram = oneDayRecord.expressKilogram;

        List<Map<String, Object>> rows = DBUtils.rows(sumFeeSql.toString(), sumFeeSql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            if(row.get("currency") == null) continue;
            helper.Currency currency = helper.Currency.valueOf(row.get("currency").toString());
            currentFee += currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
        }
        Map<String, Object> totalKg = DBUtils.row(sumKilogram.toString(), sumKilogram.getParams().toArray());
        totalKilogram += (totalKg.get("kg") == null ? 0 : NumberUtils.toFloat(totalKg.get("kg").toString()));

        return new F.T3<Float, Float, Float>(
                totalKilogram == 0 ? 0 : (oneDayRecord.expressCost + currentFee) / totalKilogram,
                totalKilogram,
                currentFee);
    }

    /**
     * 计算出 oneDay 所有涉及到的 Selling 的 VAT 费用
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
                .groupBy("pu.shipment_id", "pu.currency");
        List<Map<String, Object>> rows = DBUtils.rows(vatSql.toString(), vatSql.getParams().toArray());

        // 所有关税 USD
        float totalVATFee = 0;
        Set<String> shipmentIds = new HashSet<String>();
        for(Map<String, Object> row : rows) {
            helper.Currency currency = Currency.valueOf(row.get("currency").toString());
            totalVATFee += row.get("cost") == null ? 0 : currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
            if(row.get("shipmentIds") != null) {
                shipmentIds.addAll(Arrays.asList(StringUtils.split(row.get("shipmentIds").toString(), ",")));
            }
        }

        SqlSelect effectSellingSql = new SqlSelect()
                .select("s.sellingId", "sum(si.qty) qty", "round(sum(si.qty * p.declaredValue), 2) x")
                .from("Selling s")
                .leftJoin("ProcureUnit u ON u.selling_sellingId=s.sellingId")
                .leftJoin("Product p ON p.sku=u.product_sku")
                .leftJoin("ShipItem si ON si.unit_id=u.id")
                .leftJoin("Shipment t ON t.id=si.shipment_id")
                .where(SqlSelect.whereIn("t.id", shipmentIds));

        Map<String, Float> sellingsVAT = new HashMap<String, Float>();
        Map<String, Integer> sellingsQty = new HashMap<String, Integer>();
        rows = DBUtils.rows(effectSellingSql.toString(), effectSellingSql.getParams().toArray());

        // (440x + 220x + 124x)[sumX] = (2000)[totalVATFee] -> x = 2.55
        // 440 * 2.55 / 400[qty] = 2.805 [单个 VAT]
        float sumX = 0;
        for(Map<String, Object> row : rows) {
            if(row.get("sellingId") == null || StringUtils.isBlank(row.get("sellingId").toString())) continue;
            float nX = row.get("x") == null ? 0 : NumberUtils.toFloat(row.get("x").toString());
            sumX += nX;
            sellingsQty.put(row.get("sellingId").toString(),
                    row.get("qty") == null ? 0 : NumberUtils.toInt(row.get("qty").toString()));
            sellingsVAT.put(row.get("sellingId").toString(), nX);
        }
        float x = sumX == 0 ? 0 : (totalVATFee / sumX);
        for(Map.Entry<String, Float> entry : sellingsVAT.entrySet()) {
            int qty = sellingsQty.get(entry.getKey());
            entry.setValue(qty == 0 ? 0 : (entry.getValue() * x) / qty);
        }

        return sellingsVAT;
    }
}
