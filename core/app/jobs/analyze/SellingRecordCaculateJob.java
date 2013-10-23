package jobs.analyze;

import helper.Caches;
import helper.Webs;
import models.market.Selling;
import models.market.SellingRecord;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.On;
import play.libs.F;
import services.MetricAmazonFeeService;
import services.MetricProcureCostService;
import services.MetricSalesService;
import services.MetricShipCostService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private MetricShipCostService shipCostService = new MetricShipCostService();
    private MetricAmazonFeeService amzService = new MetricAmazonFeeService();
    private MetricProcureCostService pcCostService = new MetricProcureCostService();
    private MetricSalesService salesService = new MetricSalesService();

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
            Cache.add(Caches.SELLINGRECORD_RUNNING, "running");
            // 当天产生的数据
            // 需要计算的所有数据
            List<Selling> sellings = null;
            if(Play.mode.isProd()) sellings = Selling.findAll();
            else sellings = Selling.find("sellingId like '80%'").fetch();

            Map<String, Integer> sellingUnits = salesService.sellingUnits(dateTime.toDate());
            Map<String, Integer> sellOrders = salesService.sellingOrders(dateTime.toDate());
            Map<String, Float> sellingSales = salesService.sellingSales(dateTime.toDate(), sellings, sellingUnits);

            Map<String, Float> sellingVATFee = shipCostService.sellingVATFee(dateTime.toDate());
            Map<String, Float> sellingAmzFee = amzService.sellingAmazonFee(dateTime.toDate(), sellings, sellOrders);
            Map<String, Float> sellingFBAFee = amzService.sellingAmazonFBAFee(dateTime.toDate(), sellings, sellOrders);

            List<SellingRecord> sellingRecords = new ArrayList<SellingRecord>();

            Map<String, Float> seaCost = shipCostService.seaCost(dateTime.toDate());
            Map<String, Float> airCost = shipCostService.airCost(dateTime.toDate());


            for(Selling selling : sellings) {
                try {
                    String sid = selling.sellingId;
                    SellingRecord record = SellingRecord.oneDay(sid, dateTime.toDate());
                    SellingRecord yesterdayRcd = SellingRecord.oneDay(sid, dateTime.minusDays(1).toDate());
                    // 销售价格
                    record.salePrice = selling.aps.salePrice == null ? 0 : selling.aps.salePrice;
                    // amz 扣费
                    record.amzFee = sellingAmzFee.get(sid) == null ? 0 : Math.abs(sellingAmzFee.get(sid));
                    // amzFba 扣费
                    record.fbaFee = sellingFBAFee.get(sid) == null ? 0 : Math.abs(sellingFBAFee.get(sid));
                    // 销量
                    record.units = sellingUnits.get(sid) == null ? 0 : sellingUnits.get(sid);
                    // 销售额
                    record.sales = sellingSales.get(sid) == null ? 0 : sellingSales.get(sid);
                    // 订单量
                    record.orders = sellOrders.get(sid) == null ? 0 : sellOrders.get(sid);
                    // 实际收入 = 销量 - amazon 扣费
                    record.income = record.sales - record.amzFee;

                    F.T2<Float, Integer> procureCostAndQty = pcCostService.sellingProcreCost(selling, dateTime.toDate());
                    // 采购成本
                    record.procureCost = procureCostAndQty._1;
                    record.procureNumberSum = procureCostAndQty._2;

                    // 海运运输成本
                    Float seaCostPrice = seaCost.get(sid);
                    record.seaCost = seaCostPrice == null ? yesterdayRcd.seaCost : seaCostPrice;

                    // 空运运输成本
                    Float airCostPrice = airCost.get(sid);
                    record.airCost = airCostPrice == null ? yesterdayRcd.airCost : airCostPrice;

                    /*
                    // 快递运输成本
                    costAndKg = shipCostService.seaCost(selling, dateTime.toDate());
                    record.seaCost = costAndKg._1;
                    record.seaCubicMeter = costAndKg._2;
                    */

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
            Cache.delete(Caches.SELLINGRECORD_RUNNING);
        }
    }


    public static boolean isRunning() {
        return StringUtils.isNotBlank(Cache.get(Caches.SELLINGRECORD_RUNNING, String.class));
    }
}
