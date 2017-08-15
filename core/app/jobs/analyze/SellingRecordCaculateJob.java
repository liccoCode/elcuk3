package jobs.analyze;

import models.market.SellingRecord;
import org.joda.time.DateTime;
import play.Logger;
import play.jobs.Job;
import services.MetricAmazonFeeService;
import services.MetricProcureCostService;
import services.MetricSalesService;
import services.MetricShipCostService;

import java.util.List;

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
@Deprecated
//@On("0 0 0,8,16,23 * * ?")
public class SellingRecordCaculateJob extends Job {

    private MetricShipCostService shipCostService = new MetricShipCostService();
    private MetricAmazonFeeService amzService = new MetricAmazonFeeService();
    private MetricProcureCostService pcCostService = new MetricProcureCostService();
    private MetricSalesService salesService = new MetricSalesService();

    private DateTime dateTime = DateTime.now();

    public SellingRecordCaculateJob() {
        this.dateTime = DateTime.now().withTimeAtStartOfDay();
    }

    public SellingRecordCaculateJob(DateTime dateTime) {
        this.dateTime = dateTime.withTimeAtStartOfDay();
    }

    @Override
    public void doJob() {
        Logger.info("SellingRecordCaculateJob begin %s (nocached)", dateTime.toString("yyyy-MM-dd"));
        //metric(dateTime);
        //metric(dateTime.plusDays(-1));
    }

    public List<SellingRecord> metric(DateTime dt) {
//        /**
//         * 计算出每个 Selling 的:
//         * 销售价格: 这个需要对 Listing 进行抓取获取. (或者每天保持每个 Listing 的更新)
//         * 销量: 通过订单中的订单项目进行统计
//         * 销售额: 通过订单项目的数量与销售价格进行统计
//         * 实际收入: 销售额 - amazon 扣费(包含 fba 费用)
//         * 利润: 实际收入 - 采购成本 - 运输成本 - VAT
//         * 成本利润率: 利润 / (采购成本 + 运输成本 + VAT)
//         * 销售利润率: 利润 / 销售额
//         * 采购成本: 统计记录产品的所有花费/所有采购数量计算当前采购成本
//         * 运输成本: --- 需要区分出 快递/空运/海运 三个运输成本, 还需要对三种进行一个统一.
//         * 关税和VAT成本: 找出当天所有的 VAT, 按照产品的申报价值进行分摊
//         * 月销售额趋势:
//         * 月利润趋势
//         * 月成本利润率趋势
//         * 历史总销售额: 每天计算都会累加一份
//         * 历史总利润额: 每天计算都会累加一份
//         */
//        // 当天产生的数据
//        // 需要计算的所有数据
//        List<Selling> sellings = null;
//        if(Play.mode.isProd()) sellings = Selling.findAll();
//        else sellings = Selling.find("sellingId like '80%'").fetch();
//
//        long begin = System.currentTimeMillis();
//        Map<String, Integer> sellingUnits = salesService.sellingUnits(dt.toDate());
//        Logger.info("SellingUnit: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//        Map<String, Integer> sellOrders = salesService.sellingOrders(dt.toDate());
//        Logger.info("SellingOrders: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//        Map<String, Float> sellingSales = salesService.sellingSales(dt.toDate(), sellings, sellingUnits);
//        Logger.info("SellingSales: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//        Map<String, Float> sellingVATFee = shipCostService.sellingVATFee(dt.toDate());
//        Logger.info("SellingVAT: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//        Map<String, Float> sellingAmzFee = amzService.sellingAmazonFee(dt.toDate(), sellings, sellingUnits);
//        Logger.info("SellingAmazon: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//        Map<String, Float> sellingFBAFee = amzService.sellingAmazonFBAFee(dt.toDate(), sellings, sellingUnits);
//        Logger.info("SellingFBA: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//        List<SellingRecord> sellingRecords = new ArrayList<SellingRecord>();
//
//        Map<String, Float> seaCost = shipCostService.seaCost(dt.toDate());
//        Logger.info("SellingSeaCost: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//        Map<String, Float> airCost = shipCostService.airCost(dt.toDate());
//        Logger.info("SellingAirCost: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//        Map<String, Float> expressCost = shipCostService.expressCost(dt.toDate());
//        Logger.info("SellingExpressCost: %s ms", System.currentTimeMillis() - begin);
//        begin = System.currentTimeMillis();
//
//
//        for(Selling selling : sellings) {
//            try {
//                String sid = selling.sellingId;
//                SellingRecord record = SellingRecord.oneDay(sid, dt.toDate());
//                SellingRecord yesterdayRcd = SellingRecord.oneDay(sid, dt.minusDays(1).toDate());
//                // 销量
//                record.units = sellingUnits.get(sid) == null ? 0 : sellingUnits.get(sid);
//                // 销售额
//                record.sales = sellingSales.get(sid) == null ? 0 : sellingSales.get(sid);
//                // 销售价格
//                record.salePrice = selling.aps.salePrice == null ?
//                        (record.units == 0 ? yesterdayRcd.salePrice : record.sales / record.units)
//                        : selling.salePriceWithCurrency();
//                // 订单量
//                record.orders = sellOrders.get(sid) == null ? 0 : sellOrders.get(sid);
//                // amz 扣费
//                Float amzFeeF = sellingAmzFee.get(sid);
//                record.amzFee = record.totalToSingle(Math.abs(amzFeeF == null ? 0 : amzFeeF));
//                // amzFba 扣费
//                Float fbaFeeF = sellingFBAFee.get(sid);
//                record.fbaFee = record.totalToSingle(Math.abs(fbaFeeF == null ? 0 : fbaFeeF));
//                // 实际收入 = 销量 - amazon 扣费
//                record.income = record.totalToSingle(record.sales) - record.amzFee;
//
//                F.T2<Float, Integer> procureCostAndQty = pcCostService.sellingProcreCost(selling, dt.toDate());
//                // 采购成本
//                record.procureCost = procureCostAndQty._1;
//                record.procureNumberSum = procureCostAndQty._2;
//
//                //  运输成本的思考: 真的需要每天记录一个值吗? 这样记录的曲线有意义吗?
//
//                // 海运运输成本
//                record.seaCost = record.mergeWithLatest(seaCost.get(sid), "seaCost");
//
//
//                // 空运运输成本
//                record.airCost = record.mergeWithLatest(airCost.get(sid), "airCost");
//
//                // 快递运输成本
//                record.expressCost = record.mergeWithLatest(expressCost.get(sid), "expressCost");
//
//
//                // VAT 的费用
//                record.dutyAndVAT = record.mergeWithLatest(sellingVATFee.get(sid), "dutyandvat");
//
//                // 单个利润 = 实际收入 - 采购成本 - 运输成本 - VAT
//                record.profit = record.income - record.procureAndShipCost();
//                // 成本利润率
//                record.costProfitRatio = record.costProfitRatio();
//                // 销售利润率
//                record.saleProfitRatio = record.saleProfitRatio();
//                record.save();
//
//                //  还有总销售额和总利润
//                sellingRecords.add(record);
//            } catch(Exception e) {
//                Logger.error("SellingRecordCaculateJob Error:" + Webs.s(e));
//            }
//        }
//        Logger.info("Selling Loop: %s ms", System.currentTimeMillis() - begin);
//        return sellingRecords;
        return null;
    }

}
