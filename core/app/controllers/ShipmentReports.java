package controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import controllers.api.SystemOperation;
import helper.*;
import models.ReportRecord;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.procure.ShipmentMonthly;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import models.view.dto.CostReportDTO;
import models.view.dto.MonthlyShipmentDTO;
import models.view.dto.ShipmentWeight;
import models.view.highchart.HighChart;
import models.view.post.ArrivalRatePost;
import models.view.post.LossRatePost;
import models.view.post.MonthlyShipmentPost;
import models.view.post.ReportPost;
import models.view.report.ArrivalRate;
import models.view.report.LossRate;
import org.apache.commons.lang.StringUtils;
import play.libs.F;
import play.modules.excel.RenderExcel;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;
import query.ShipmentReportESQuery;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 物流模块报表控制器
 * User: mac
 * Date: 14-6-3
 * Time: PM3:27
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class ShipmentReports extends Controller {

    @Before(only = {"cost"})
    public static void setUpIndexPage() {
        F.T2<List<String>, List<String>> categorysToJson = Category.fetchCategorysJson();
        renderArgs.put("categorys", J.json(categorysToJson._2));
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
        ShipmentWeight excel = new ShipmentWeight();
        renderArgs.put("excel", excel);
    }

    public static void index(ReportPost p) {
        if(p == null) p = new ReportPost();
        p.reportTypes = ReportPost.shipmentMonthlyTypes();
        List<ReportRecord> reports = p.query();
        render(p, reports);
    }

    public static void downloadShipmentMonthlyReport(int year, int month) {
        List<ShipmentMonthly> seaList = ShipmentMonthly.find("year=? AND month=? AND type=?",
                year, month, Shipment.T.SEA).fetch();
        List<ShipmentMonthly> expressList = ShipmentMonthly.find("year=? AND month=? AND type=?",
                year, month, Shipment.T.EXPRESS).fetch();
        List<ShipmentMonthly> airList = ShipmentMonthly.find("year=? AND month=? AND type=?",
                year, month, Shipment.T.AIR).fetch();
        List<ShipmentMonthly> dedicatedList = ShipmentMonthly.find("year=? AND month=? AND type=?",
                year, month, Shipment.T.DEDICATED).fetch();
        List<ShipmentMonthly> railWayList = ShipmentMonthly.find("year=? AND month=? AND type=?",
                year, month, Shipment.T.RAILWAY).fetch();
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("月度物流报表_%s年_%s月.xls", year, month));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(seaList, expressList, airList, dedicatedList, railWayList);
    }

    /**
     * 运输方式的重量与费用统计(成本)
     */
    @Check("shipmentreports.cost")
    public static void cost() {
        Date now = new Date();
        String from = Dates.date2Date(Dates.monthBegin(now));
        String to = Dates.date2Date(now);
        render(from, to);
    }

    public static void costReport(Date from, Date to) {
        List<CostReportDTO> dtos = CostReportDTO.setReportData(from, to);
        render("/ShipmentReports/costReport.html", from, to, dtos);
    }

    /**
     * 根据运输方式统计运输费用
     */
    public static void countShipFeeByType(Date from, Date to) {
        try {
            HighChart chart = ShipmentReportESQuery.shipFeeByTypeColumn(from, to);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 根据市场统计运输费用
     */
    public static void countShipFeeByMarket(Date from, Date to, String type) {
        try {
            if(StringUtils.equals(type, "专线")) {
                HighChart chart = ShipmentReportESQuery.shipFeeByMarketPieForDedicated(from, to, type);
                renderJSON(J.json(chart));
            } else {
                HighChart chart = ShipmentReportESQuery.shipFeeByMarketPie(from, to, type);
                renderJSON(J.json(chart));
            }
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 根据运输方式统计重量
     */
    public static void countShipWeightByType(Date from, Date to) {
        try {
            HighChart chart = ShipmentReportESQuery.shipWeightByTypeColumn(from, to);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 根据市场统计运输重量
     */
    public static void countShipWeightByMarket(Date from, Date to, String type) {
        try {
            Shipment.T market = Shipment.T.valueOf(type);
            HighChart chart = ShipmentReportESQuery.shipWeightByMarketPie(from, to, market);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 运输准时到货率报表
     */
    public static void arrivalRate(ArrivalRatePost p) {
        if(p == null) p = new ArrivalRatePost();
        List<ArrivalRate> arrivals = p.query();
        List<Shipment> shipments = p.queryOverTimeShipment();
        render(arrivals, shipments, p);
    }

    /**
     * 统计物流准时到货率
     * <p/>
     * 规则:
     * <p/>
     * 预计到库时间 < 签收时间, 超时抵达
     * 预计到库时间 = 签收时间, 准时抵达
     * 预计到库时间 > 签收时间, 提前抵达
     * <p/>
     * 准时到货率的公式:
     * 准时到货率 = (准时抵达数量 + 提前抵达数量) / (超时抵达数量 + 提前抵达数量 + 准时抵达数量)*100%
     * 注: 数量(采购计划的实际交货数量)
     */
    public static void countArrivalRate(int year, Shipment.T shipType, String countType) {
        try {
            HighChart chart = ShipmentReportESQuery.arrivalRateLine(year, shipType, countType);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    public static void lossRateReport(LossRatePost p) {
        if(p == null) p = new LossRatePost();
        List<LossRate> lossrates = new ArrayList<>();
        List<ShipItem> shipItems = new ArrayList<>();
        LossRate losstotal = new LossRate();
        try {
            Map<String, Object> map = p.queryDate();
            lossrates = (List<LossRate>) map.get("lossrate");
            shipItems = (List<ShipItem>) map.get("shipItems");
            losstotal = p.buildTotalLossRate(lossrates);
        } catch(FastRuntimeException e) {
            flash.error(Webs.e(e));
        }
        render(lossrates, losstotal, p, shipItems);
    }

    public static void monthlyShipmentReport(MonthlyShipmentPost p) {
        if(p == null) p = new MonthlyShipmentPost();
        List<MonthlyShipmentDTO> list = p.queryBySku();
        render(list, p);
    }

    public static void monthlyShipmentPrescription(ArrivalRatePost p) {
        if(p == null) p = new ArrivalRatePost();
        List<Shipment> list = p.queryMonthlyShipment();
        Map<String, F.T3<String, String, Double>> map = p.calAverageTime(list);
        render(list, map, p);
    }

    /**
     * @param p
     */
    public static void shipmentReport(LossRatePost p) {
        List<Map> list = new ArrayList<Map>();
        if(p == null) {
            p = new LossRatePost();
        } else {
            try {
                String beginDate = new SimpleDateFormat("yyyyMMdd").format(p.from);
                String endDate = new SimpleDateFormat("yyyyMMdd").format(p.to);
                String redisIngKey = String.format("shipmentSkuExecuting_%s_%s", beginDate, endDate);
                String redisKey = String.format("shipmentSku_%s_%s", beginDate, endDate);
                String postvalue = Caches.get(redisKey);
                /** 验证报表结果是否在缓存中生成 **/
                if(StringUtils.isBlank(postvalue)) {
                    /**  若报表结果未生成,那么验证是否已经在执行当前条件的 后台任务 **/
                    String ingValue = Caches.get(redisIngKey);
                    if(StringUtils.isBlank(ingValue)) {
                        //若redis没有 JRockend 正在执行任务的 标志位,那么通过 aws 发送消息队列调用任务执行
                        Map map = new HashMap();
                        map.put("jobName", "shipmentSkuJob");  //  任务ID
                        map.put("args", GTs.newMap("beginDate", beginDate).put("endDate", endDate).build());  //开始日期和结束日期
                        String message = JSONObject.toJSONString(map);
                        AmazonSQS.sendMessage(message);
                        throw new FastRuntimeException("单个sku物流费年均价报表已经在后台计算中，请于 2min 后再来查看结果~");
                    } else {
                        throw new FastRuntimeException("单个sku物流费年均价报表已经在后台计算中，请稍后后再来查看结果~");
                    }
                } else {
                    list = JSONArray.parseArray(postvalue, Map.class);
                }
            } catch(FastRuntimeException e) {
                flash.error(Webs.e(e));
            }
        }
        render(list, p);
    }

}
