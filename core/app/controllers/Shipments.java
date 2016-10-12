package controllers;

import controllers.api.SystemOperation;
import ext.ShipmentsHelper;
import helper.Dates;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.finance.FeeType;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.view.Ret;
import models.view.post.ShipmentPost;
import models.whouse.Whouse;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.util.*;

import static play.modules.pdf.PDF.renderPDF;

/**
 * 货运单的控制器
 * User: wyattpan
 * Date: 6/20/12
 * Time: 3:09 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Shipments extends Controller {
    @Before(only = {"index", "blank", "save", "shipmentToApply"})
    public static void whouses() {
        List<Whouse> whouses = Whouse.findAll();
        List<Cooperator> cooperators = Cooperator.shippers();
        renderArgs.put("whouses", whouses);
        renderArgs.put("cooperators", cooperators);
    }

    @Check("shipments.index")
    public static void index(ShipmentPost p) {
        if(p == null) p = new ShipmentPost();
        List<Shipment> shipments = p.query();

        for(int i = 0; i < shipments.size(); i++) {
            Shipment ship = shipments.get(i);
            ship.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
            shipments.set(i, ship);
        }
        Shipment.handleQty1(shipments, null);
        renderArgs.put("dateTypes", ShipmentPost.DATE_TYPES);
        render(shipments, p);
    }

    public static void showProcureUnitList(String id) {
        Shipment shipment = Shipment.findById(id);
        List<ShipItem> items = shipment.items;
        render("Shipments/_shipitem.html", items);
    }

    public static void blank() {
        Shipment ship = new Shipment(Shipment.id());
        render(ship);
    }

    public static void save(Shipment ship) {
        checkAuthenticity();
        ship.creater = User.findByUserName(Secure.Security.connected());
        validation.valid(ship);
        Validation.required("shipment.whouse", ship.whouse);
        Validation.required("shipment.creater", ship.creater);
        if(Validation.hasErrors()) {
            render("Shipments/blank.html", ship);
        }
        ship.arryParamSetUP(Shipment.FLAG.ARRAY_TO_STR);
        Shipment shipment = new Shipment(ship).save();
        show(shipment.id);
    }

    /**
     * 通过采购计划创建运输单
     *
     * @param units
     */
    @Check("shipments.procureunittoshipment")
    public static void procureUnitToShipment(List<Long> units) {
        if(units == null || units.size() <= 0)
            Validation.addError("", "必须选择采购计划");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ShipItems.index(null);
        }

        Shipment shipment = new Shipment().buildFromProcureUnits(units);

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ShipItems.index(null);
        }
        flash.success("成功为 %s 个采购计划创建运输单 %s", units.size(), shipment.id);
        show(shipment.id);
    }

    @Before(only = {"show", "update", "beginShip", "refreshProcuress", "updateFba"})
    public static void setUpShowPage() {
        //TODO 需要添加 FeeType 的数据
        renderArgs.put("whouses", Whouse.findAll());
        renderArgs.put("shippers", Cooperator.shippers());
        String shipmentId = request.params.get("id");
        if(StringUtils.isBlank(shipmentId)) shipmentId = request.params.get("ship.id");
        if(StringUtils.isNotBlank(shipmentId)) {
            renderArgs.put("records", ElcukRecord.records(shipmentId));
            Shipment ship = Shipment.findById(shipmentId);
            renderArgs.put("feeTypes", feeTypes(ship.type));
        } else {
            renderArgs.put("feeTypes", feeTypes(null));
        }
    }

    @Util
    public static List<FeeType> feeTypes(Shipment.T shipType) {
        List<FeeType> feeTypes = FeeType.transports();
        if(shipType == Shipment.T.EXPRESS) {
            CollectionUtils.filter(feeTypes, new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    return !((FeeType) o).name.equals("transportshipping");
                }
            });
        }
        return feeTypes;
    }

    public static void show(String id) {
        Shipment ship = Shipment.findById(id);
        ship.endShipByComputer();
        List<Cooperator> cooperators = Cooperator.shippers();
        ship.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
        Shipment.handleQty1(null, ship);
        render(ship, cooperators);
    }

    public static void preview(String id) {
        Shipment ship = Shipment.findById(id);
        if(ship == null) {
            Validation.addError("", "ShipmentId 错误");
            Webs.errorToFlash(flash);
        }

        render(ship);
    }

    public static void confirm(String id, Boolean undo) {
        if(undo == null) {
            Validation.addError("", "缺少 undo 参数.");
            show(id);
        }

        Shipment shipment = Shipment.findById(id);
        shipment.state = undo ? Shipment.S.PLAN : Shipment.S.CONFIRM;
        shipment.save();
        show(id);
    }

    public static void update(Shipment ship, String shipid) {
        checkAuthenticity();
        Shipment dbship = Shipment.findById(shipid);
        dbship.update(ship);
        if(Validation.hasErrors()) {
            dbship.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
            renderArgs.put("ship", dbship);
            render("Shipments/show.html");
        }
        dbship.updateShipment();
        /**像采购计划负责人发送邮件**/
        dbship.sendMsgMail(ship.dates.planArrivDate, Secure.Security.connected());
        flash.success("更新成功.");

        new ElcukRecord(Messages.get("shipment.update"),
                Messages.get("shipment.update.msg", ship.to_log()), dbship.id).save();
        Shipments.show(shipid);
    }

    /**
     * 用来更新 Shipment 的 coment 与 trackNo
     */
    public static void comment(String id, String cmt, String track, String jobNumber, Float totalWeightShipment,
                               Float totalVolumeShipment, Float totalStockShipment, String reason) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.V(Validation.errors())));
        Shipment ship = Shipment.findById(id);
        ship.memo = cmt;
        if(StringUtils.isNotBlank(track))
            ship.trackNo = track;
        if(StringUtils.isNotBlank(jobNumber))
            ship.jobNumber = jobNumber;
        if(StringUtils.isNotBlank(reason))
            ship.reason = reason;
        ship.save();
        renderJSON(new Ret(true, Webs.V(Validation.errors())));
    }

    /**
     * 取消运输单
     */
    @Check("shipments.cancel")
    public static void cancel(String id) {
        Shipment ship = Shipment.findById(id);
        ship.destroy();
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        } else {
            flash.success("运输单取消成功.");
            index(null);
        }
    }


    @Check("shipments.beginship")
    public static void beginShip(String id, Date date) {
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.planArrivDate", ship.dates.planArrivDate);

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        }

        try {
            ship.beginShip(date);
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
        }
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        }

        new ElcukRecord(Messages.get("shipment.beginShip"),
                Messages.get("shipment.beginShip.msg", ship.id), ship.id).save();
        flash.success("运输单已经标记运输, FBA 已经标记 SHIPPED.");

        show(id);
    }

    /**
     * 到港
     */
    @Check("shipments.handleprocess")
    public static void landPort(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.landPort(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 到港!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 港口提货
     *
     * @param id
     * @param date
     */
    @Check("shipments.handleprocess")
    public static void pickGoods(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.pickGoods(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 开始提货!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 预约
     *
     * @param id
     */
    @Check("shipments.handleprocess")
    public static void booking(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.booking(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 已经预约!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 派送
     */
    @Check("shipments.handleprocess")
    public static void deliverying(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.beginDeliver(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 已经派送!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 签收
     *
     * @param id
     * @param date
     */
    @Check("shipments.handleprocess")
    public static void receipt(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.receipt(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 已经签收!", shipment.type.label(), id);
        show(id);
    }

    /**
     * 入库
     */
    @Check("shipments.handleprocess")
    public static void inbounding(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.inbounding(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 正在入库!", shipment.type.label(), id);
        show(id);
    }

    @Check("shipments.revertstate")
    public static void revertState(String id) {
        Shipment shipment = Shipment.findById(id);
        shipment.revertState();
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 成功返回到上一状态!", shipment.type.label(), id);
        show(id);
    }

    public static void log(String id, String msg) {
        Shipment shipment = Shipment.findById(id);
        shipment.logEvent(msg);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 成功记录一条事件!", shipment.type.label(), id);
        show(id);
    }

    public static void endShip(String id, Date date) {
        Shipment shipment = Shipment.findById(id);
        shipment.endShipByHand(date);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 运输单 %s 正在入库!", shipment.type.label(), id);
        show(id);
    }


    public static void refreshProcuress(final String id) {
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.trackNo", ship.trackNo);
        Validation.required("shipment.internationExpress", ship.internationExpress);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(ship.id);
        }
        ship.trackWebSite();
        ship.monitor();
        show(ship.id);
    }

    public static void track(String id, String track) {
        Shipment ship = Shipment.findById(id);
        if(StringUtils.isNotBlank(track)) ship.trackNo = track;
        renderText(ship.trackWebSite());
    }

    /**
     * 加载出来 Whouse 相关的可使用的运输单
     *
     * @param whouseId
     */
    public static void unitShipments(Long whouseId, Shipment.T shipType) {
        List<Shipment> unitRelateShipments = Shipment.findUnitRelateShipmentByWhouse(whouseId, shipType);
        render(unitRelateShipments);
    }

    /**
     * 生成运输单发票
     *
     * @param id
     */
    public static void invoice(String id) {
        Shipment ship = Shipment.findById(id);
        String shipType = ship.type.name();
        Map<String, List<ProcureUnit>> fbaGroupUnits = new HashMap<String, List<ProcureUnit>>();
        for(ShipItem item : ship.items) {
            String centerId = item.unit.fba.centerId;
            if(!fbaGroupUnits.containsKey(centerId))
                fbaGroupUnits.put(centerId, new ArrayList<ProcureUnit>());
            fbaGroupUnits.get(centerId).add(item.unit);
        }
        String invoiceNo = ship.buildInvoiceNO();//生成 InvoiceNO
        final PDF.Options options = new PDF.Options();
        options.filename = id;
        options.pageSize = IHtmlToPdfTransformer.A3P;
        renderPDF(options, fbaGroupUnits, invoiceNo, shipType);
    }

    public static void dates(String id) {
        Shipment shipment = Shipment.findById(id);
        Map<String, String> dates = new HashMap<String, String>();
        dates.put("begin", Dates.date2Date(shipment.dates.planBeginDate));
        dates.put("end", Dates.date2Date(ShipmentsHelper.predictArriveDate(shipment)));
        renderJSON(dates);
    }


    public static void planArriveDate(String shipType, String planShipDate, String warehouseid) {
        Shipment shipment = new Shipment();
        shipment.type = Shipment.T.valueOf(shipType);
        shipment.whouse = Whouse.findById(Long.parseLong(warehouseid));
        int day = shipment.shipDay();
        DateTime arrivedate = Dates.cn(planShipDate).plusDays(day);
        Map<String, String> dates = new HashMap<String, String>();
        dates.put("arrivedate", Dates.date2Date(arrivedate));
        renderJSON(dates);
    }

    /**
     * 创建出库
     */
    @Check("outboundrecords.index")
    public static void outbound(List<String> shipmentId) {
        if(shipmentId != null && !shipmentId.isEmpty()) {
            for(String sid : shipmentId) {
                Shipment shipment = Shipment.findById(sid);
                shipment.initOutbound();
            }
        }
        flash.success("创建出库成功!");
        redirect("/OutboundRecords/index");
    }
}
