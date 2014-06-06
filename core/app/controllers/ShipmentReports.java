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
}
