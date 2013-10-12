package jobs.analyze;

import helper.DBUtils;
import helper.Dates;
import helper.Webs;
import models.market.M;
import models.market.Selling;
import models.market.SellingRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.db.helper.SqlSelect;
import play.jobs.Job;
import play.jobs.On;
import play.libs.F;
import services.MetricAmazonFeeService;
import services.MetricProcureCostService;
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

    private MetricShipCostService shipCostService = new MetricShipCostService();
    private MetricAmazonFeeService amzFeeService = new MetricAmazonFeeService();
    private MetricProcureCostService pcCostService = new MetricProcureCostService();

    private DateTime dateTime = DateTime.now();

    public SellingRecordCaculateJob() {
    }

    public SellingRecordCaculateJob(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public void doJob() {
        try {
            /**
             * 计算出每个 Selling 的:
             * 销售价格: 这个需要对 Listing 进行抓取获取. (或者每天保持每个 Listing 的更新)
             * 销量: 通过订单中的订单项目进行统计
             * 销售额: 通过订单项目的数量与销售价格进行统计
             * 实际收入: 销售额 - amazon 扣费(包含 fba 费用)
             * 利润: 实际收入 - 采购成本 - 运输成本 - VAT
             * 成本利润率: 利润 / (采购成本 + 运输成本 + VAT)
             * 销售利润率: 利润 / 销售额
             * 采购成本: 统计记录产品的所有花费/所有采购数量计算当前采购成本
             * 运输成本: --- 需要区分出 快递/空运/海运 三个运输成本, 还需要对三种进行一个统一.
             * 关税和VAT成本: 找出当天所有的 VAT, 按照产品的申报价值进行分摊
             * 月销售额趋势:
             * 月利润趋势
             * 月成本利润率趋势
             * 历史总销售额: 每天计算都会累加一份
             * 历史总利润额: 每天计算都会累加一份
             */
            Cache.add(RUNNING, RUNNING);
            // 当天产生的数据
            // 需要计算的所有数据
            List<Selling> sellings = null;
            if(Play.mode.isProd()) sellings = Selling.findAll();
            else sellings = Selling.find("sellingId like '80%'").fetch();

            Map<String, Float> sellingVATFee = shipCostService.sellingVATFee(dateTime.toDate());
            sellingUnits(dateTime.toDate());
            sellingSales(dateTime.toDate(), sellings);
            Map<String, Float> sellingAmzFee = amzFeeService.sellingAmazonFee(dateTime.toDate(), sellings);
            Map<String, Float> sellingFBAFee = amzFeeService.sellingAmazonFBAFee(dateTime.toDate(), sellings);

            List<SellingRecord> sellingRecords = new ArrayList<SellingRecord>();


            for(Selling selling : sellings) {
                try {
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

                    F.T2<Float, Integer> procureCostAndQty = pcCostService.sellingProcreCost(selling, dateTime.toDate());
                    // 采购成本
                    record.procureCost = procureCostAndQty._1;
                    record.procureNumberSum = procureCostAndQty._2;

                    // 快递运输成本
                    F.T3<Float, Float, Float> costAndKg = shipCostService.expressCost(selling, dateTime.toDate());
                    record.expressCost = costAndKg._1;
                    record.expressKilogram = costAndKg._2;

                    // 空运运输成本
                    costAndKg = shipCostService.airCost(selling, dateTime.toDate());
                    record.airCost = costAndKg._1;
                    record.airKilogram = costAndKg._2;

                    // 海运运输成本
                    costAndKg = shipCostService.seaCost(selling, dateTime.toDate());
                    record.seaCost = costAndKg._1;
                    record.seaCubicMeter = costAndKg._2;

                    // VAT 的费用
                    record.dutyAndVAT = sellingVATFee.get(sid) == null ? 0 : sellingVATFee.get(sid);

                    // 利润 = 实际收入 - 采购成本 - 运输成本 - VAT
                    record.profit = record.income - record.procureAndShipCost();
                    // 成本利润率 = 利润 / (采购成本 + 运输成本 + VAT)
                    record.costProfitRatio =
                            record.procureAndShipCost() == 0 ? 0 : (record.profit / record.procureAndShipCost());
                    // 销售利润率 = 利润 / 销售额
                    record.saleProfitRatio = record.sales == 0 ? 0 : (record.profit / record.sales);
                    record.save();

                    // TODO: 还有总销售额和总利润
                    sellingRecords.add(record);
                } catch(Exception e) {
                    Logger.error(Webs.S(e));
                }
            }
            Cache.add("sellingRecordCaculateJob", sellingRecords);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            Cache.delete(RUNNING);
        }
    }

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
     * 因为 Amazon 收费的不及时, 所以对于离当天 10 天内的数据使用系统中的订单量进行计算
     */
    public Map<String, Float> sellingSales(Date date, List<Selling> sellings) {
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            this.sellingSales.clear();
            for(Selling sell : sellings) {
                int units = this.sellingUnits.get(sell.sellingId);
                this.sellingSales.put(sell.sellingId, units * (sell.aps.salePrice == null ? 0 : sell.aps.salePrice));
            }
        } else {
            /**
             * 1. 找到某天 OrderItem 中所有涉及的 Selling 与每个 Selling 涉及的 Order.id
             * 2. 根据每个 selling 所涉及的 id 与费用类型, 计算处每个 Selling 的销售额
             */
            this.sellingSales = amzFeeService.sellingFeeTypesCost(date, Arrays.asList("productcharges", "shipping"));
        }
        return this.sellingSales;
    }


    public static boolean isRunning() {
        return StringUtils.isNotBlank(Cache.get(RUNNING, String.class));
    }
}
