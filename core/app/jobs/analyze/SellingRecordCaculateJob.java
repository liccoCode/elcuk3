package jobs.analyze;

import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import models.finance.FeeType;
import models.market.Selling;
import models.market.SellingRecord;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Play;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.jobs.Job;
import play.libs.F;

import java.util.*;

/**
 * 周期:
 * 轮询: 7, 15, 23 三个时间点执行三次
 * 每天用于计算 SellingRecord 的后台任务.
 * <p/>
 * 每天需要计算三次, 但由于数据越到后面越准确, 所以一天内的后一次计算可以覆盖前一次计算的结果
 * User: wyatt
 * Date: 8/14/13
 * Time: 4:54 PM
 */
public class SellingRecordCaculateJob extends Job {
    private DateTime dateTime = DateTime.now();

    public SellingRecordCaculateJob() {
    }

    public SellingRecordCaculateJob(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public void doJob() {
        try {
            Cache.add("sellingRecordCaculateJobRunning", "running");
            // 当天产生的数据
            Map<String, Integer> sellingUnits = sellingUnits(dateTime.toDate());
            Map<String, Float> sellingSales = sellingSales(dateTime.toDate());
            Map<String, Float> sellingAmzFee = sellingAmazonFee(dateTime.toDate());

            List<SellingRecord> sellingRecords = new ArrayList<SellingRecord>();
            // 需要计算的所有数据
            List<Selling> sellings = null;
            if(Play.mode.isProd()) sellings = Selling.findAll();
            else sellings = Selling.find("sellingId like '80%'").fetch();

            for(Selling selling : sellings) {
                String sid = selling.sellingId;
                SellingRecord record = SellingRecord.oneDay(sid, dateTime.toDate());
                // amz 扣费
                float amzfee = sellingAmzFee.get(sid) == null ? 0 : sellingAmzFee.get(sid);
                // 销量
                record.units = sellingUnits.get(sid) == null ? 0 : sellingUnits.get(sid);
                // 销售额
                record.sales = sellingSales.get(sid) == null ? 0 : sellingSales.get(sid);
                // 实际收入 = 销量 - amazon 扣费
                record.income = record.sales - Math.abs(amzfee);

                F.T2<Float, Integer> procureCostAndQty = sellingProcreCost(selling, dateTime.toDate());
                // 采购成本
                record.procureCost = procureCostAndQty._1;
                record.procureNumberSum = procureCostAndQty._2;

                F.T2<Float, Integer> shipCostAndQty = sellingShipCost(selling, dateTime.toDate());
                // 运输成本
                record.shipCost = shipCostAndQty._1;
                record.shipNumberSum = shipCostAndQty._2;

                float procureAndShipCost = (record.shipCost * record.units) + (record.procureCost * record.units);
                // 利润 = 实际收入 - 采购成本 - 运输成本
                record.profit = record.income - procureAndShipCost;
                // 成本利润率 = 利润 / (采购成本 + 运输成本)
                record.costProfitRatio = procureAndShipCost == 0 ? 0 : (record.profit / procureAndShipCost);
                // 销售利润率 = 利润 / 销售额
                record.saleProfitRatio = record.sales == 0 ? 0 : (record.profit / record.sales);
                record.save();

                // TODO: 还有总销售额和总利润
                sellingRecords.add(record);
            }
            Cache.add("sellingRecordCaculateJob", sellingRecords);
        } finally {
            Cache.delete("sellingRecordCaculateJobRunning");
        }
    }

    // -------------------- 这里是根据业务, 将 SellingRecord 的计算方法全部组织到了当前这个类 ------------------

    /**
     * Selling 的销量数据
     *
     * @return
     */
    public Map<String, Integer> sellingUnits(Date date) {
        SqlSelect sql = new SqlSelect()
                .select("selling_sellingId as sellingId", "sum(quantity) as qty")
                .from("OrderItem")
                .where("createDate>=?").param(Dates.morning(date))
                .where("createDate<=?").param(Dates.night(date))
                .groupBy("sellingId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, Integer> sellingUnits = new HashMap<String, Integer>();
        for(Map<String, Object> row : rows) {
            String sellingId = row.get("sellingId").toString();
            if(StringUtils.isBlank(sellingId)) continue;
            sellingUnits.put(sellingId, NumberUtils.toInt(row.get("qty").toString()));
        }
        return sellingUnits;
    }

    /**
     * Selling 的销售额数据;
     *
     * @return
     */
    public Map<String, Float> sellingSales(Date date) {
        /**
         * 1. 找到某天 OrderItem 中所有涉及的 Selling 与每个 Selling 涉及的 Order.id
         * 2. 根据每个 selling 所涉及的 id 与费用类型, 计算处每个 Selling 的销售额
         */
        return sellingFeeTypesCost(date, Arrays.asList("productcharges", "shipping"));
    }

    /**
     * Selling 的 Amazon 消耗的费用;
     *
     * @param date
     * @return
     */
    public Map<String, Float> sellingAmazonFee(Date date) {
        List<FeeType> fees = FeeType.amazon().children;
        List<String> feesTypeName = new ArrayList<String>();
        for(FeeType fee : fees) {
            if("shipping".equals(fee.name)) continue;
            feesTypeName.add(fee.name);
        }
        return sellingFeeTypesCost(date, feesTypeName);
    }

    /**
     * 指定 amazon 费用类型, 返回当天所有 Selling 这些费用类型的总费用
     *
     * @param date
     * @param feeTypes
     * @return
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

    /**
     * 查询某天销售中, 每个 Selling 所涉及的 OrderId 是哪些
     *
     * @return
     */
    public Map<String, List<String>> oneDaySellingOrderIds(Date date) {
        // 设置 group_concat_max_len 最大为 20M
        DBUtils.execute("set group_concat_max_len=20971520");
        SqlSelect sellingOdsSql = new SqlSelect()
                .select("selling_sellingId as sellingId", "group_concat(order_orderId) as orderIds")
                .from("OrderItem")
                .where("createDate>=?").param(Dates.morning(date))
                .where("createDate<=?").param(Dates.night(date))
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
     * 某一个 Selling 的采购成本(发货时间与指定日期相同); 币种统一为 USD
     *
     * @param selling
     * @param date
     * @return
     */
    public F.T2<Float, Integer> sellingProcreCost(Selling selling, Date date) {
        /**
         * 1. 确定昨天的采购成本与到昨天为之的所有采购数量
         * 2. 从今天的所有采购单中寻找, 找出今天 selling 采购的所有数量与各自的单价(统一币种为 USD)
         * 3. 根据 1, 2 通过计算出 总价格 / 总数量 得出今天的平均单价
         */
        DateTime oneDay = new DateTime(date);
        SellingRecord record = SellingRecord.oneDay(selling.sellingId, oneDay.minusDays(1).toDate());

        SqlSelect sql = new SqlSelect()
                .select("currency", "sum(qty) as qty", "sum(price * qty) as cost")
                .from("ProcureUnit")
                .where("selling_sellingId=?").param(selling.sellingId)
                .where("deliveryDate>=?").param(Dates.morning(date))
                .where("deliveryDate<=?").param(Dates.night(date))
                .groupBy("currency");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

        float toDayTotalProcureCost = 0;
        int toDayProcureNumberSum = 0;
        for(Map<String, Object> row : rows) {
            if(row.get("currency") == null) continue;
            Currency currency = Currency.valueOf(row.get("currency").toString());
            toDayTotalProcureCost += currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
            toDayProcureNumberSum += NumberUtils.toInt(row.get("qty").toString());
        }

        float toDayProcureCost = 0;
        int procureNumberSum = record.procureNumberSum + toDayProcureNumberSum;
        if(procureNumberSum != 0) {
            toDayProcureCost = (record.procureCost * record.procureNumberSum + toDayTotalProcureCost) / procureNumberSum;
        }
        return new F.T2<Float, Integer>(toDayProcureCost, procureNumberSum);
    }

    /**
     * 某一个 Selling 的运输成本; 币种统一为 USD
     *
     * @param selling
     * @param date
     * @return
     */
    public F.T2<Float, Integer> sellingShipCost(Selling selling, Date date) {
        /**
         * 1. 确定昨天的运输成本与到昨天位置的运输数量
         * 2. 从今天所有的快递运输单中寻找, 找出今天 selling 快递的平均运输价格与数量
         * 3. 找到今天所有的运输单, 计算出海运/空运平均运输价格与这个 selling 的数量
         * 4. 根据 1,2,3 通过 总价格 / 总数量 得出今天的平均运输成本
         */
        DateTime oneDay = new DateTime(date);
        SellingRecord record = SellingRecord.oneDay(selling.sellingId, oneDay.minusDays(1).toDate());

        List<FeeType> transportFees = FeeType.transports();
        List<String> nonTransportshippingTypeFees = new ArrayList<String>();
        for(FeeType typeName : transportFees) {
            if("transportshipping".equals(typeName.name)) continue;
            nonTransportshippingTypeFees.add(typeName.name);
        }

        SqlSelect expressFeeTemplate = new SqlSelect()
                .from("PaymentUnit p")
                .leftJoin("Shipment s ON p.shipment_id=s.id")
                .leftJoin("ShipItem si ON p.shipItem_id=si.id")
                .leftJoin("ProcureUnit pi ON si.unit_id=pi.id")
                .where("p.createdAt>=?").param(Dates.morning(oneDay.toDate()))
                .where("p.createdAt<=?").param(Dates.night(oneDay.toDate()))
                .where("s.type=?").param(Shipment.T.EXPRESS.name());

        // 快递运输总运费
        float expressShipCost = 0;
        // 快递运输总数量
        int expressShipNumberSum = 0;
        // 1. 找出某个 Selling 快递的运输费用
        SqlSelect sellingTransportShippingFeeSql = new SqlSelect(expressFeeTemplate)
                .select("p.currency as currency", "sum(p.unitPrice * p.unitQty + p.fixValue) as cost",
                        "sum(si.qty) as qty")
                .where("pi.selling_sellingId=?").param(selling.sellingId)
                        // 先只计算快递的运输运费
                .where("p.feeType_name=?").param("transportshipping");

        List<Map<String, Object>> rows = DBUtils.rows(sellingTransportShippingFeeSql.toString(),
                sellingTransportShippingFeeSql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            if(row.get("currency") == null) continue;
            Currency currency = Currency.valueOf(row.get("currency").toString());
            expressShipCost += currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
            expressShipNumberSum += NumberUtils.toInt(row.get("qty").toString());
        }

        // 2. 找出某个 Selling 快递运输单的其他费用并均摊
        F.T2<Float, Integer> expressNonShippingFeeCostAndNumber = shipmentTotalCostAndTotalNumber(
                oneDay.toDate(), nonTransportshippingTypeFees, Shipment.T.EXPRESS);
        if(expressNonShippingFeeCostAndNumber._2 != 0)
            expressShipCost += (expressNonShippingFeeCostAndNumber._1 / expressNonShippingFeeCostAndNumber._2) *
                    expressShipNumberSum;


        // 海运/空运费用
        /**
         * 1. 找到当前产生了费用所涉及的 Shipment, 统计总费用
         * 2. 统计这些 Shipment 总运输的产品的数量
         */
        List<String> transportshippingTypeFees = new ArrayList<String>(nonTransportshippingTypeFees);
        transportshippingTypeFees.add("transportshipping");
        F.T2<Float, Integer> airAndSeaCostAndNumber = shipmentTotalCostAndTotalNumber(
                oneDay.toDate(), transportshippingTypeFees, Shipment.T.AIR, Shipment.T.SEA);

        F.T3<Float, Float, Integer> t3 = avgFee(
                Arrays.asList(record.shipCost * record.shipNumberSum, expressShipCost, airAndSeaCostAndNumber._1),
                Arrays.asList(record.shipNumberSum, expressShipNumberSum, airAndSeaCostAndNumber._2));

        return new F.T2<Float, Integer>(t3._1, t3._3);
    }

    /**
     * 计算平均费用
     *
     * @param costs
     * @param numberSums
     * @return ._1: 平均价格, ._2: 总费用, ._3: 总数量
     */
    private F.T3<Float, Float, Integer> avgFee(List<Float> costs, List<Integer> numberSums) {
        float totalCost = 0;
        int totalNumber = 0;
        for(Float cost : costs) totalCost += cost;
        for(Integer number : numberSums) totalNumber += number;
        if(totalNumber == 0) return new F.T3<Float, Float, Integer>(0f, totalCost, totalNumber);
        return new F.T3<Float, Float, Integer>(totalCost / totalNumber, totalCost, totalNumber);
    }

    /**
     * 计算 PaymentUnit 中指定条件下涉及到的运输单的总费用与总数量
     *
     * @return
     */
    private F.T2<Float, Integer> shipmentTotalCostAndTotalNumber(Date date, List<String> feeTypes,
                                                                 Shipment.T... shipTypes) {
        /**
         * 1. 找出当前 PaymentUnit 涉及的 ShipmentIds 与总费用
         * 2. 根据 ShipmentIds 找出涉及的总数量
         */
        List<String> shipTypeNames = new ArrayList<String>();
        for(Shipment.T type : shipTypes) shipTypeNames.add(type.name());
        SqlSelect airAndSeaShipFeeSql = new SqlSelect()
                .select("p.currency as currency", "sum(p.unitPrice * p.unitQty + p.fixValue) as cost",
                        "group_concat(distinct p.shipment_id) as shipmentIds")
                .from("PaymentUnit p")
                .leftJoin("Shipment s ON p.shipment_id=s.id")
                .where(SqlSelect.whereIn("s.type", shipTypeNames))
                .where("p.createdAt>=?").param(Dates.morning(date))
                .where("p.createdAt<=?").param(Dates.night(date))
                .where(SqlSelect.whereIn("p.feeType_name", feeTypes))
                .groupBy("p.currency");

        Set<String> shipmentIds = new HashSet<String>();
        float totalCost = 0;
        int totalNumberSum = 0;

        List<Map<String, Object>> rows = DBUtils
                .rows(airAndSeaShipFeeSql.toString(), airAndSeaShipFeeSql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            Currency currency = Currency.valueOf(row.get("currency").toString());
            totalCost += currency.toUSD(NumberUtils.toFloat(row.get("cost").toString()));
            String shipmentIdStr = row.get("shipmentIds").toString();
            if(StringUtils.isNotBlank(shipmentIdStr)) {
                Collections.addAll(shipmentIds, StringUtils.split(shipmentIdStr, ","));
            }
        }

        if(shipmentIds.size() > 0) {
            SqlSelect airAndSeaShipNumberSumSql = new SqlSelect()
                    .select("sum(qty) as qty")
                    .from("ShipItem")
                    .where(SqlSelect.whereIn("shipment_id", shipmentIds));

            Map<String, Object> row = DBUtils.row(airAndSeaShipNumberSumSql.toString());
            if(StringUtils.isNotBlank(row.get("qty").toString()))
                totalNumberSum = NumberUtils.toInt(row.get("qty").toString());
        }

        return new F.T2<Float, Integer>(totalCost, totalNumberSum);
    }
}
