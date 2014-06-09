package controllers;

import helper.J;
import helper.Webs;
import models.procure.Shipment;
import models.view.Ret;
import models.view.highchart.HighChart;
import play.mvc.Controller;
import play.mvc.With;
import query.ShipmentReportESQuery;

/**
 * 物流模块报表控制器
 * User: mac
 * Date: 14-6-3
 * Time: PM3:27
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class ShipmentReports extends Controller {

    /**
     * 运输方式的重量与费用统计(成本)
     */
    @Check("shipmentreports.cost")
    public static void cost() {
        render();
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


    public static void arrivalRate() {
        render();
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
}
