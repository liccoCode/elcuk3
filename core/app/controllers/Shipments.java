package controllers;

import controllers.api.SystemOperation;
import ext.ShipmentsHelper;
import helper.Dates;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.embedded.ShipmentDates;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.procure.*;
import models.view.Ret;
import models.view.post.ProcureUnitShipPost;
import models.view.post.ShipmentPost;
import models.whouse.Outbound;
import models.whouse.Whouse;
import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.Upload;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.modules.excel.RenderExcel;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.io.*;
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
        List<Whouse> whouses = Whouse.find("type=?", Whouse.T.FBA).fetch();
        List<Cooperator> cooperators = Cooperator.shippers();
        renderArgs.put("whouses", whouses);
        renderArgs.put("cooperators", cooperators);
    }

    @Check("shipments.index")
    public static void index(ShipmentPost p) {
        if(p == null) p = new ShipmentPost();
        p.pagination = false;
        List<Shipment> shipments = p.query();

        for(int i = 0; i < shipments.size(); i++) {
            Shipment ship = shipments.get(i);
            ship.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
            shipments.set(i, ship);
        }
        //Shipment.handleQty1(shipments, null);
        renderArgs.put("dateTypes", ShipmentPost.DATE_TYPES);
        render(shipments, p);
    }

    public static void indexByCooperId(Long cooperId) {
        ShipmentPost p = new ShipmentPost();
        p.pagination = false;
        p.cooperId = cooperId;
        p.dateType = "createDate";
        List<Shipment> shipments = p.query();
        renderArgs.put("dateTypes", ShipmentPost.DATE_TYPES);
        List<Whouse> whouses = Whouse.findAll();
        List<Cooperator> cooperators = Cooperator.shippers();
        render("Shipments/index.html", shipments, p, whouses, cooperators);
    }

    public static void indexB2B(ShipmentPost p) {
        if(p == null) p = new ShipmentPost();
        p.projectName = User.COR.MengTop;
        p.pagination = false;
        List<Shipment> shipments = p.query();

        for(int i = 0; i < shipments.size(); i++) {
            Shipment ship = shipments.get(i);
            ship.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
            shipments.set(i, ship);
        }
        //Shipment.handleQty1(shipments, null);
        renderArgs.put("dateTypes", ShipmentPost.DATE_TYPES);
        render(shipments, p);
    }

    public static void showProcureUnitList(String id) {
        Shipment shipment = Shipment.findById(id);
        List<ShipItem> items = shipment.items;
        if(Objects.equals(shipment.projectName, User.COR.MengTop)) {
            render("Shipments/_b2b_shipitem.html", items);
        } else {
            render("Shipments/_shipitem.html", items);
        }
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
    public static void procureUnitToShipment(List<Long> units, String shipmentId) {
        if(units == null || units.size() <= 0)
            Validation.addError("", "必须选择采购计划");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ShipItems.index(null);
        }
        Shipment shipment;
        if(StringUtils.isNotBlank(shipmentId)) {
            shipment = Shipment.findById(shipmentId);
        } else {
            shipment = new Shipment();
        }
        shipment.buildFromProcureUnits(units);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ShipItems.index(null);
        }
        flash.success("成功为 %s 个采购计划创建运输单 %s", units.size(), shipment.id);
        show(shipment.id);
    }

    public static void buildB2BFromProcureUnits(List<Long> units, String shipmentId) {
        Shipment shipment;
        if(StringUtils.isNotBlank(shipmentId)) {
            shipment = Shipment.findById(shipmentId);
            if(!Objects.equals(shipment.projectName, User.COR.MengTop)) {
                flash.error("非B2B的运输单，不能添加");
                ShipItems.indexB2B(new ProcureUnitShipPost());
            }
        } else {
            shipment = new Shipment();
        }
        shipment.buildB2BFromProcureUnits(units);
        flash.success("成功为 %s 个采购计划创建B2B运输单 %s", units.size(), shipment.id);
        show(shipment.id);
    }

    @Before(only = {"show", "update", "beginShip", "refreshProcuress", "updateFba"})
    public static void setUpShowPage() {
        renderArgs.put("whouses", Whouse.find("type=?", Whouse.T.FBA).fetch());
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
            CollectionUtils.filter(feeTypes, o -> !((FeeType) o).name.equals("transportshipping"));
        }
        return feeTypes;
    }

    public static void show(String id) {
        Shipment ship = Shipment.findById(id);
        ship.dates = ship.dates == null ? new ShipmentDates() : ship.dates;
        ship.endShipByComputer();
        List<Cooperator> cooperators = Cooperator.shippers();
        ship.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
        Shipment.handleQty1(null, ship);
        List<BtbCustom> customs = BtbCustom.find(" isDel=?", false).fetch();
        render(ship, cooperators, customs);
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
        Shipment old = Shipment.findById(shipid);
        Date realPlanArrivDate = old.dates != null ? old.dates.planArrivDate : null;
        old.update(ship);
        if(Validation.hasErrors()) {
            old.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
            renderArgs.put("ship", old);
            render("Shipments/show.html");
        }
        old.updateShipment();
        //向采购计划负责人发送邮件
        old.sendMsgMail(realPlanArrivDate, Secure.Security.connected());
        flash.success("更新成功.");
        new ElcukRecord(Messages.get("shipment.update"), Messages.get("shipment.update.msg", ship.toLog()), old.id)
                .save();
        Shipments.show(shipid);
    }

    /**
     * 用来更新 Shipment 的 coment 与 trackNo
     */
    public static void comment(String id, String cmt, String track, String jobNumber, Float totalWeightShipment,
                               Float totalVolumeShipment, Float totalStockShipment, String reason) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.v(Validation.errors())));
        Shipment ship = Shipment.findById(id);
        ship.memo = cmt;
        if(StringUtils.isNotBlank(track))
            ship.trackNo = track;
        if(StringUtils.isNotBlank(jobNumber))
            ship.jobNumber = jobNumber;
        if(StringUtils.isNotBlank(reason))
            ship.reason = reason;
        ship.save();
        renderJSON(new Ret(true, Webs.v(Validation.errors())));
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
    public static void beginShip(String id, Date date, boolean sync) {
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.planArrivDate", ship.dates.planArrivDate);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        }
        try {
            ship.beginShip(date, sync);
        } catch(Exception e) {
            Validation.addError("", Webs.e(e));

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

    public static void endShipByItem(Long id) {
        ShipItem item = ShipItem.findById(id);
        item.endShipByHand();
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success(" 运输项目 %s 完成运输!", id);
        show(item.shipment.id);
    }

    public static void syncReceiveQty(Long itemId) {
        ShipItem item = ShipItem.findById(itemId);
        item.syncReceiveQty(item.unit.id);
        flash.success(" 运输项目 %s 同步receiveQty!", item.unit.id);
        show(item.shipment.id);
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
    public static void unitShipments(Long whouseId, Shipment.T shipType, Date planDeliveryDate) {
        List<Shipment> unitRelateShipments = Shipment
                .findUnitRelateShipmentByWhouse(whouseId, shipType, planDeliveryDate);
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
        Map<String, List<ProcureUnit>> fbaGroupUnits = new HashMap<>();
        for(ShipItem item : ship.items) {
            String centerId = item.unit.fba.centerId;
            if(!fbaGroupUnits.containsKey(centerId))
                fbaGroupUnits.put(centerId, new ArrayList<>());
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
        Map<String, String> dates = new HashMap<>();
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
        Map<String, String> dates = new HashMap<>();
        dates.put("arrivedate", Dates.date2Date(arrivedate));
        renderJSON(dates);
    }

    /**
     * 创建出库
     */
    @Check("outbounds.index")
    public static void outbound(List<String> shipmentId) {
        Outbound.initCreateByShipItem(shipmentId);
        flash.success("已成功创建出库单!");
        index(new ShipmentPost());
    }

    public static void validCreateOutbound(String[] shipmentIds) {
        String msg = "";
        if(ArrayUtils.isNotEmpty(shipmentIds)) {
            List<Shipment> shipments = Shipment.find("SELECT s FROM Shipment s LEFT JOIN s.items i "
                    + "LEFT JOIN i.unit u WHERE s.id IN " + SqlSelect.inlineParam(shipmentIds)
                    + " AND u.stage <> ? ", ProcureUnit.STAGE.IN_STORAGE).fetch();
            if(shipments.size() > 0) {
                msg += "【" + shipments.get(0).id + "】";
                renderJSON(new Ret(false, "运输单：" + msg + " " + "下的采购计划还不是【已入仓】状态，是否继续？"));
            }
        }
        renderJSON(new Ret(true, ""));
    }

    public static void arns(String shipmentId) {
        Shipment shipment = Shipment.findById(shipmentId);
        notFoundIfNull(shipment);
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME, String.format("%s-AmazonReferenceID.xls", shipmentId));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        render(shipment);
    }

    /**
     * 导入运输费用
     *
     * @param xlsx
     * @param shipId
     */
    public static void importPayment(String xlsx, String shipId) {
        try {
            /** 第一步上传excel文件 **/
            List<Upload> files = (List<Upload>) request.args.get("__UPLOADS");
            Upload upload = files.get(0);
            List<ShipmentPayment> shipmentPaymentList = new ArrayList<>();


            /** 第二步根据定义好xml进行jxls映射 **/
            File directory = new File("");
            String courseFile = directory.getCanonicalPath();
            String xmlPath = courseFile + "/app/views/Shipments/shipmentPayment.xml";
            InputStream inputXML = new BufferedInputStream(new FileInputStream(xmlPath));
            XLSReader mainReader = ReaderBuilder.buildFromXML(inputXML);
            InputStream inputXLS = new BufferedInputStream(new FileInputStream(upload.asFile()));
            Map<String, Object> beans = new HashMap<>();
            beans.put("shipmentPaymentList", shipmentPaymentList);
            XLSReadStatus readStatus = mainReader.read(inputXLS, beans);
            if(readStatus.isStatusOK()) {
                StringBuilder error = new StringBuilder();
                List<PaymentUnit> paymentUnitList = new ArrayList<>();

                if(shipmentPaymentList.size() == 0) {
                    error.append("未读取道费用信息,请确保Excel第一个Sheet页的名字为'Sheet1'");
                } else {
                    /** 第三步 把excel对象解析成PaymentUnit  **/
                    Date date = new Date();
                    Shipment shipment = Shipment.findById(shipId);
                    notFoundIfNull(shipment);

                    for(int i = 0; i < shipmentPaymentList.size(); i++) {
                        ShipmentPayment sp = shipmentPaymentList.get(i);
                        PaymentUnit unit = new PaymentUnit();
                        StringBuilder rowError = new StringBuilder();
                        unit.currency = helper.Currency.valueOf(sp.getCurrency().toUpperCase());
                        unit.createdAt = date;
                        unit.memo = sp.getMeno();
                        unit.state = PaymentUnit.S.PAID;
                        FeeType feeType = FeeType.find(" nickName = ?", sp.getType()).first();
                        if(feeType == null) rowError.append("费用类型未找到,");
                        unit.feeType = feeType;
                        unit.payee = Login.current();
                        unit.shipment = shipment;
                        unit.unitPrice = sp.getPrice();
                        unit.unitQty = sp.getQty();
                        unit.amount = unit.unitQty * unit.unitPrice;
                        Cooperator cooperator = Cooperator.find("type=? and name=?",
                                Cooperator.T.SHIPPER, sp.getFullName()).first();
                        if(cooperator == null) rowError.append("费用关系人未找到,");
                        unit.cooperator = cooperator;
                        paymentUnitList.add(unit);
                        if(rowError.length() > 0)
                            error.append(String.format("第%s行 ", i + 2)).append(rowError.toString());
                    }
                }
                if(error.length() > 0) {
                    flash.error(error.substring(0, error.length() - 1));
                } else {
                    /** 第四步 数据插入PaymentUnit **/
                    paymentUnitList.forEach(unit -> {
                        unit.save();
                    });
                    flash.success(String.format("已成功上传 %s 条运输费用", paymentUnitList.size()));
                }
            } else {
                flash.error("上传失败!");
            }
        } catch(Exception e) {
            flash.error(String.format("上传失败!原因:[%s]", e.toString()));
        }
        show(shipId);
    }

}
