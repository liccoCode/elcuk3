package services;

import helper.DBUtils;
import helper.Dates;
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
     * @return 单个快递运费, 总重量
     */
    public F.T2<Float, Float> expressCost(Selling selling, Date oneDay) {
        /**
         * 1. 找到昨天的 SellingRecord 的数据, 用于统计计算今天的值.
         * 2. 从今天支付完成的运输付款单出发, 找出当天 selling 所涉及的所有快递/空运运输单的总费用(不包括 VAT和关税)与总运输重量.
         * 3. 计算出昨天和今天的总费用与总重量, 然后通过 总费用/总重量 计算出 $N/kg 的值
         * 4. 根据 selling 自己所涉及的 sku 的重量 * $N/kg 计算出当前 selling 的运输成本.
         */
        SellingRecord oneDayRecord = SellingRecord.oneDay(selling.sellingId, oneDay);
        // 通过 PaymentUnit 找涉及的运输单
        SqlSelect effectShipmentSql = new SqlSelect()
                .select("group_concat(s.id) as shipmentIds")
                .from("PaymentUnit pu")
                .leftJoin("Payment p ON p.id=pu.payment_id")
                .leftJoin("Shipment s ON s.id=pu.shipment_id")
                .leftJoin("ShipItem si ON si.id=pu.shipItem_id")
                .leftJoin("ProcureUnit u ON u.id=si.unit_id")
                .where("u.selling_sellingId=?").param(selling.sellingId)
                .where("s.type=?").param(Shipment.T.EXPRESS.name())
                .where("date_format(p.paymentDate, '%Y-%m-%d')=?").param(Dates.date2Date(oneDay))
                .groupBy("s.id");

        Map<String, Object> idsRs = DBUtils.row(effectShipmentSql.toString(), effectShipmentSql.getParams().toArray());

        List<String> effectShipmentIds = new ArrayList<String>();
        effectShipmentIds.addAll(Arrays.asList(StringUtils.split(idsRs.get("shipmentIds").toString(), ",")));

        SqlSelect sumBase = new SqlSelect().from("PaymentUnit pu")
                .where(SqlSelect.whereIn("pu.shipment_id", effectShipmentIds));
        // 通过运输单找他们的总运输费用
        SqlSelect sumFeeSql = new SqlSelect(sumBase)
                .select("sum(pu.unitPrice * pu.unitQty + pu.fixValue) cost", "pu.currency")
                .where("pu.feeType_name!=?").param("dutyandvat")/*固定的 VAT 和关税的费用类型*/
                .groupBy("pu.currency");
        // 通过运输单找他们的总重量
        SqlSelect sumKilogram = new SqlSelect(sumBase)
                .select("sum(unitQty) kg")
                .where("feeType_name=?").param("transportshipping")/*固定的快递费名称*/;


        float totalFees = oneDayRecord.expressCost;
        float totalKilogram = oneDayRecord.expressKilogram;

        List<Map<String, Object>> rows = DBUtils.rows(sumFeeSql.toString(), sumFeeSql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            if(row.get("currency") == null) continue;
            helper.Currency currency = helper.Currency.valueOf(row.get("currency").toString());
            totalFees += currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
        }
        Map<String, Object> totalKg = DBUtils.row(sumKilogram.toString(), sumKilogram.getParams().toArray());
        totalKilogram += (totalKg.get("kg") == null ? 0 : NumberUtils.toFloat(totalKg.get("kg").toString()));

        return new F.T2<Float, Float>(
                totalKilogram == 0 ? 0 : totalFees / totalKilogram,
                totalKilogram);
    }


    /**
     * 计算某一个 Selling 某一天的空运运费
     */
    public F.T2<Float, Float> airCost(Selling selling, Date oneDay) {
        return null;
    }


    /**
     * 计算某一个 Selling 某一天的海运运费
     */
    public F.T2<Float, Float> seaCost(Selling selling, Date oneDay) {
        return null;
    }
}
