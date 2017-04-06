package controllers;

import controllers.api.SystemOperation;
import helper.Dates;
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
import models.view.post.LossRatePost;
import models.view.report.AreaGoodsAnalyze;
import models.view.report.ArrivalRate;
import models.view.report.LossRate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.helper.StringUtil;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;
import query.ShipmentReportESQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 根据市场统计运输费用
     */
    public static void countShipFeeByMarket(Date from, Date to, Shipment.T type) {
        try {
            HighChart chart = ShipmentReportESQuery.shipFeeByMarketPie(from, to, type);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
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
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 根据市场统计运输重量
     */
    public static void countShipWeightByMarket(Date from, Date to, String type) {
        try {
            if(StringUtils.equals(type, "专线")) {
                HighChart chart = ShipmentReportESQuery.shipWeightByMarketPieForDedicated(from, to);
                renderJSON(J.json(chart));
            } else {
                Shipment.T market = Shipment.T.valueOf(type);
                HighChart chart = ShipmentReportESQuery.shipWeightByMarketPie(from, to, market);
                renderJSON(J.json(chart));
            }
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
        List<LossRate> lossrates = new ArrayList<>();
        List<ShipItem> shipItems = new ArrayList<>();
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

    public static void queryCenterIdByCountryCode(AreaGoodsAnalyze a) {
        if(StringUtil.isBlank(a.countryCode)) {
            renderJSON(new Ret());
        } else {
            List<String> list = a.queryCenterIdByCountryCode(a.countryCode);
            renderJSON(J.json(list));
        }
    }

}
