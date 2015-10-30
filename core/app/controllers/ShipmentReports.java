package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import models.view.dto.CostReportDTO;
import models.view.dto.ShipmentWeight;
import models.view.highchart.HighChart;
import models.view.post.ArrivalRatePost;
import models.view.report.AreaGoodsAnalyze;
import models.view.report.ArrivalRate;
import org.joda.time.DateTime;
import org.jsoup.helper.StringUtil;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import query.ShipmentReportESQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.view.post.LossRatePost;
import models.view.report.LossRate;
import play.utils.FastRuntimeException;

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

    /**
     * 运输方式的重量与费用统计(成本)
     */
    @Check("shipmentreports.cost")
    public static void cost() {
        render();
    }

    public static void costReport(int year, int month) {
        List<CostReportDTO> dtos = CostReportDTO.setReportData(year, month);
        render("/ShipmentReports/costReport.html", year, month, dtos);
    }

    /**
     * 根据运输方式统计运输费用
     */
    public static void countShipFeeByType(int year, int month) {
        try {
            HighChart chart = ShipmentReportESQuery.shipFeeByTypeColumn(year, month);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 根据市场统计运输费用
     */
    public static void countShipFeeByMarket(int year, int month, Shipment.T type) {
        try {
            HighChart chart = ShipmentReportESQuery.shipFeeByMarketPie(year, month, type);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 根据运输方式统计重量
     */
    public static void countShipWeightByType(int year, int month) {
        try {
            HighChart chart = ShipmentReportESQuery.shipWeightByTypeColumn(year, month);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 根据市场统计运输重量
     */
    public static void countShipWeightByMarket(int year, int month, Shipment.T type) {
        try {
            HighChart chart = ShipmentReportESQuery.shipWeightByMarketPie(year, month, type);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
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
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void lossRateReport(LossRatePost p) {
        if(p == null) p = new LossRatePost();
        List<LossRate> lossrates = new ArrayList<LossRate>();
        List<ShipItem> shipItems = new ArrayList<ShipItem>();
        LossRate losstotal = new LossRate();
        try {
            Map<String, Object> map = p.queryDate();
            lossrates = (List<LossRate>) map.get("lossrate");
            shipItems = (List<ShipItem>) map.get("shipItems");
            losstotal = p.buildTotalLossRate(lossrates);
        } catch(FastRuntimeException e) {
            flash.error(Webs.E(e));
        }
        render(lossrates, losstotal, p, shipItems);
    }

    public static void areaGoodsAnalyze(AreaGoodsAnalyze a) {
        if(a == null) {
            a = new AreaGoodsAnalyze();
            a.from = DateTime.now().minusMonths(1).plusDays(1).toDate();
            a.to = DateTime.now().toDate();
        }
        List<AreaGoodsAnalyze> analyzes = a.query();
        a.queryTotalShipmentAnalyze();
        render(analyzes, a);
    }

    public static void queryCenterIdByCountryCode(AreaGoodsAnalyze a){
        if(StringUtil.isBlank(a.countryCode)){
            renderJSON(new Ret());
        }else{
            List<String> list = a.queryCenterIdByCountryCode(a.countryCode);
            renderJSON(J.json(list));
        }
    }

}
