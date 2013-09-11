package jobs.analyze;

import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import models.finance.FeeType;
import models.market.M;
import models.market.Selling;
import models.market.SellingRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Play;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.jobs.Job;
import play.jobs.On;
import play.libs.F;
import services.MetricShipCostService;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static models.market.Orderr.S;

/**
 * 周期:
 * 轮询: 8, 16, 23 三个时间点执行三次(避开 SellingSaleAnalyzeJob 的计算)
 * 每天用于计算 SellingRecord 的后台任务.
 * <p/>
 * 每天需要计算三次, 但由于数据越到后面越准确, 所以一天内的后一次计算可以覆盖前一次计算的结果
 * User: wyatt
 * Date: 8/14/13
 * Time: 4:54 PM
 */
@On("0 0 0,8,16,23 * * ?")
public class SellingRecordCaculateJob extends Job {
    public static final String RUNNING = "sellingRecordCaculateJobRunning";

    /**
     * 使用类的成员变量, 便于当前 Job 进行计算
     */
    private Map<String, Integer> sellingUnits = new HashMap<String, Integer>();
    private Map<String, Float> sellingSales = new HashMap<String, Float>();
    private Map<String, Float> sellingAmzFee = new HashMap<String, Float>();
    private Map<String, Float> sellingFBAFee = new HashMap<String, Float>();

    private MetricShipCostService shipCostService = new MetricShipCostService();

    private DateTime dateTime = DateTime.now();

    public SellingRecordCaculateJob() {
    }

    public SellingRecordCaculateJob(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public void doJob() {
        try {
            Cache.add(RUNNING, RUNNING);
            // 当天产生的数据
            sellingUnits(dateTime.toDate());
            sellingSales(dateTime.toDate());
            sellingAmazonFee(dateTime.toDate());
            sellingAmazonFBAFee(dateTime.toDate());

            List<SellingRecord> sellingRecords = new ArrayList<SellingRecord>();
            // 需要计算的所有数据
            List<Selling> sellings = null;
            if(Play.mode.isProd()) sellings = Selling.findAll();
            else sellings = Selling.find("sellingId like '80%'").fetch();

            for(Selling selling : sellings) {
                String sid = selling.sellingId;
                SellingRecord record = SellingRecord.oneDay(sid, dateTime.toDate());
                // amz 扣费
                record.amzFee = sellingAmzFee.get(sid) == null ? 0 : Math.abs(sellingAmzFee.get(sid));
                // amzFba 扣费
                record.fbaFee = sellingFBAFee.get(sid) == null ? 0 : Math.abs(sellingFBAFee.get(sid));
                // 销量
                record.units = sellingUnits.get(sid) == null ? 0 : sellingUnits.get(sid);
                // 销售额
                record.sales = sellingSales.get(sid) == null ? 0 : sellingSales.get(sid);
                // 实际收入 = 销量 - amazon 扣费
                record.income = record.sales - record.amzFee;

                F.T2<Float, Integer> procureCostAndQty = sellingProcreCost(selling, dateTime.toDate());
                // 采购成本
                record.procureCost = procureCostAndQty._1;
                record.procureNumberSum = procureCostAndQty._2;

                // 快递运输成本
                F.T2<Float, Float> shipCostAndQty = shipCostService.expressCost(selling, dateTime.toDate());
                // TODO 调整

                float procureAndShipCost = /*(record.expressCost * record.units) 需要重新计算 +*/
                        (record.procureCost * record.units);
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
            Cache.delete(RUNNING);
        }
    }

    // -------------------- 这里是根据业务, 将 SellingRecord 的计算方法全部组织到了当前这个类 ------------------

    /**
     * Selling 的销量数据
     */
    public Map<String, Integer> sellingUnits(Date date) {
        Map<String, Integer> sellingUnits = new HashMap<String, Integer>();
        for(M m : M.values()) {
            if(m.isEbay()) continue;
            sellingUnits.putAll(sellingUnits(date, m));
        }
        this.sellingUnits = sellingUnits;
        return sellingUnits;
    }

    public Map<String, Integer> sellingUnits(Date date, M market) {
        F.T2<DateTime, DateTime> actualDatePair = market.withTimeZone(Dates.morning(date), Dates.night(date));
        SqlSelect sql = new SqlSelect()
                .select("oi.selling_sellingId as sellingId", "sum(oi.quantity) as qty")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON o.orderId=oi.order_orderId")
                .where("oi.market=?").param(market.name())
                .where("oi.createDate>=?").param(actualDatePair._1.toDate())
                .where("oi.createDate<=?").param(actualDatePair._2.toDate())
                .where(SqlSelect.whereIn("o.state", Arrays.asList(S.PENDING.name(), S.PAYMENT.name(), S.SHIPPED.name())))
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
     * <p/>
     * 因为 Amazon 收费的不及时, 所以对于离当天 10 天内的数据, 使用最近 10~40 天之间的平均数进行计算.
     */
    public Map<String, Float> sellingSales(Date date) {
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            DateTime now = DateTime.now();
            SqlSelect sql = new SqlSelect()
                    .select("selling_sellingId as sellingId", "(sum(sales) / sum(units)) as price")
                    .from("SellingRecord")
                    .where("date>=?").param(Dates.morning(now.minusDays(40).toDate()))
                    .where("date<=?").param(Dates.night(now.minusDays(10).toDate()))
                    .groupBy("selling_sellingId");
            List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
            Map<String, Float> sellingPrice = new HashMap<String, Float>();
            for(Map<String, Object> row : rows) {
                Object priceObj = row.get("price");
                if(priceObj == null) priceObj = "0";
                sellingPrice.put(row.get("sellingId").toString(), NumberUtils.toFloat(priceObj.toString()));
            }
            Map<String, Float> sellingSales = new HashMap<String, Float>();
            for(Map.Entry<String, Integer> entry : this.sellingUnits.entrySet()) {
                Float price = sellingPrice.get(entry.getKey());
                sellingSales.put(entry.getKey(), (price == null ? 0 : price) * entry.getValue());
            }
            this.sellingSales = sellingSales;
        } else {
            /**
             * 1. 找到某天 OrderItem 中所有涉及的 Selling 与每个 Selling 涉及的 Order.id
             * 2. 根据每个 selling 所涉及的 id 与费用类型, 计算处每个 Selling 的销售额
             */
            this.sellingSales = sellingFeeTypesCost(date, Arrays.asList("productcharges", "shipping"));
        }
        return this.sellingSales;
    }

    /**
     * Selling 的 Amazon 消耗的费用;
     * <p/>
     * 因为 Amazon 收费的不及时, 所以对于离当天 10 天内的数据, 使用最近 10~40 天之间的平均数进行计算.
     */
    public Map<String, Float> sellingAmazonFee(Date date) {
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            DateTime now = DateTime.now();
            SqlSelect sql = new SqlSelect()
                    .select("selling_sellingId as sellingId", "(sum(amzFee) / count(amzFee)) as amzFee")
                    .from("SellingRecord")
                    .where("date>=?").param(Dates.morning(now.minusDays(40).toDate()))
                    .where("date<=?").param(Dates.night(now.minusDays(10).toDate()))
                    .groupBy("selling_sellingId");
            List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

            Map<String, Float> sellingAmzFeeMap = new HashMap<String, Float>();
            for(Map<String, Object> row : rows) {
                sellingAmzFeeMap.put(row.get("sellingId").toString(), NumberUtils.toFloat(row.get("amzFee").toString()));
            }
            this.sellingAmzFee = sellingAmzFeeMap;
        } else {
            List<FeeType> fees = FeeType.amazon().children;
            List<String> feesTypeName = new ArrayList<String>();
            for(FeeType fee : fees) {
                if("shipping".equals(fee.name)) continue;
                feesTypeName.add(fee.name);
            }
            this.sellingAmzFee = sellingFeeTypesCost(date, feesTypeName);
        }
        return this.sellingAmzFee;
    }

    /**
     * Selling 的 FBA 销售的费用
     * <p/>
     * 因为 Amazon 收费的不及时, 所以对于离当天 10 天内的数据, 使用最近 10~40 天之间的平均数进行计算.
     */
    public Map<String, Float> sellingAmazonFBAFee(Date date) {
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            DateTime now = DateTime.now();
            SqlSelect sql = new SqlSelect()
                    .select("selling_sellingId as sellingId", "(sum(fbaFee) / count(fbaFee)) as fbaFee")
                    .from("SellingRecord")
                    .where("date>=?").param(Dates.morning(now.minusDays(40).toDate()))
                    .where("date<=?").param(Dates.night(now.minusDays(10).toDate()))
                    .groupBy("selling_sellingId");
            List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

            Map<String, Float> sellingAmzFeeMap = new HashMap<String, Float>();
            for(Map<String, Object> row : rows) {
                sellingAmzFeeMap.put(row.get("sellingId").toString(), NumberUtils.toFloat(row.get("fbaFee").toString()));
            }
            this.sellingFBAFee = sellingAmzFeeMap;
        } else {
            List<FeeType> fees = FeeType.fbaFees();
            List<String> feesTypeName = new ArrayList<String>();
            for(FeeType fee : fees) {
                feesTypeName.add(fee.name);
            }
            this.sellingFBAFee = sellingFeeTypesCost(date, feesTypeName);
        }
        return this.sellingFBAFee;
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

    /**
     * 查询某天销售中, 每个 Selling 所涉及的 OrderId 是哪些;
     * 不同的市场需要拥有不同的时间段
     */
    public Map<String, List<String>> oneDaySellingOrderIds(Date date) {
        Map<String, List<String>> sellingOrders = new HashMap<String, List<String>>();
        for(M m : M.values()) {
            if(m.isEbay()) continue;
            sellingOrders.putAll(oneDaySellingOrderIds(date, m));
        }
        return sellingOrders;
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
     * 某一个 Selling 的采购成本(发货时间与指定日期相同); 币种统一为 USD
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
     * 计算平均费用
     *
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

    public static boolean isRunning() {
        return StringUtils.isNotBlank(Cache.get(RUNNING, String.class));
    }
}
