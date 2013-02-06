package controllers;

import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.Notification;
import models.User;
import models.procure.Cooperator;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Whouse;
import models.view.Ret;
import models.view.post.ShipmentPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.libs.F;
import play.modules.router.Get;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;

import java.util.List;

/**
 * 货运单的控制器
 * User: wyattpan
 * Date: 6/20/12
 * Time: 3:09 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Shipments extends Controller {
    @Before(only = {"index", "blank", "save"})
    public static void whouses() {
        List<Whouse> whouses = Whouse.findAll();
        List<Cooperator> cooperators = Cooperator.shippers();
        renderArgs.put("whouses", whouses);
        renderArgs.put("cooperators", cooperators);
    }

    @Check("shipments.index")
    @Get("/shipments")
    public static void index(ShipmentPost p) {
        List<Shipment> shipments = null;
        if(p == null)
            p = new ShipmentPost();
        shipments = p.query();
        renderArgs.put("dateTypes", ShipmentPost.DATE_TYPES);
        render(shipments, p);
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
        ship.save();
        ship.notifyWithMuchMoreShipmentCreate();
        redirect("/shipments/show/" + ship.id);
    }


    @Before(only = {"show", "update", "beginShip", "refreshProcuress", "updateFba"})
    public static void setUpShowPage() {
        renderArgs.put("whouses", Whouse.findAll());
        renderArgs.put("shippers", Cooperator.shippers());
        renderArgs.put("fbas", J.json(FBAShipment.uncloseFBAShipmentIds()));
        String shipmentId = request.params.get("id");
        if(StringUtils.isBlank(shipmentId)) shipmentId = request.params.get("ship.id");
        if(StringUtils.isNotBlank(shipmentId)) {
            renderArgs.put("records", ElcukRecord.records(shipmentId));
        }
    }

    @Get(value = "/shipments/{id}", priority = 100)
    public static void show(String id) {
        Shipment ship = Shipment.findById(id);
        render(ship);
    }

    @Util
    public static void checkShowError(Shipment ship) {
        if(Validation.hasErrors()) {
            renderArgs.put("ship", ship);
            render("Shipments/show.html");
        }
    }

    public static void update(Shipment ship) {
        checkAuthenticity();
        validation.valid(ship);
        ship.validate();
        checkShowError(ship);
        ship.updateShipment();
        checkShowError(ship);
        new ElcukRecord(Messages.get("shipment.update"),
                Messages.get("shipment.update.msg", ship.to_log()), ship.id).save();
        flash.success("更新成功.");
        redirect("/Shipments/show/" + ship.id);
    }

    /**
     * 用来更新 Shipment 的 coment 与 trackNo
     */
    public static void comment(String id, String cmt, String track) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.V(Validation.errors())));
        Shipment ship = Shipment.findById(id);
        ship.memo = cmt;
        if(StringUtils.isNotBlank(track))
            ship.trackNo = track;
        ship.save();
        renderJSON(new Ret(true, Webs.V(Validation.errors())));
    }

    @Before(only = {"shipItem", "ship", "cancelShip"})
    public static void setUpShipPage() {
        String shipmentId = request.params.get("id");
        Shipment ship = Shipment.findById(shipmentId);
        try {
            List<ProcureUnit> units = ProcureUnit.waitToShip(ship.whouse.id, ship.type);
            renderArgs.put("units", units);
        } catch(Exception e) {
            Validation.addError("shipments.setUpShipPage", "%s");
        }
    }

    public static void shipItem(String id) {
        Shipment ship = Shipment.findById(id);
        render(ship);
    }

    /**
     * 取消运输单
     */
    @Check("shipments.cancel")
    public static void cancel(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        try {
            ship.cancel();
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
            if(Validation.hasErrors()) render("Shipments/show.html", ship);
        }
        if(Validation.hasErrors()) render("Shipments/show.html", ship);
        new ElcukRecord(Messages.get("shipment.cancel"), Messages.get("action.base", id), id)
                .save();
        flash.success("运输单取消成功.");
        redirect("/Shipments/show/" + id);
    }

    @Check("shipments.ship")
    public static void ship(String id, List<Long> unitId) {
        Validation.required("shipments.ship.unitId", unitId);
        Validation.required("shipment.id", id);
        Shipment ship = Shipment.findById(id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);

        ship.addToShip(unitId);

        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);
        redirect("/shipments/shipitem/" + id);
    }

    /**
     * 取消运输单项
     *
     * @param shipItemId
     * @param id
     */
    @Check("shipments.cancelship")
    public static void cancelShip(List<Integer> shipItemId, String id) {
        Validation.required("shipments.ship.shipId", shipItemId);
        Validation.required("shipment.id", id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        Shipment ship = Shipment.findById(id);
        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);

        try {
            ship.cancelShip(shipItemId, true);
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
            if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);
        }

        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);
        redirect("/shipments/shipitem/" + id);
    }

    @Check("shipments.beginship")
    public static void beginShip(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.id", id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        Validation.required("shipment.planArrivDate", ship.planArrivDate);
        Validation.required("shipment.volumn", ship.volumn);
        Validation.required("shipment.weight", ship.weight);
        Validation.required("shipment.declaredValue", ship.declaredValue);
        Validation.required("shipment.deposit", ship.deposit);
        Validation.required("shipment.otherFee", ship.otherFee);
        Validation.required("shipment.shipFee", ship.shipFee);
        Validation.required("shipment.cooper", ship.cooper);
        Validation.min("shipment.items.size", ship.items.size(), 1);

        checkShowError(ship);

        try {
            ship.beginShip();
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
        }
        checkShowError(ship);
        new ElcukRecord(Messages.get("shipment.beginShip"),
                Messages.get("shipment.beginShip.msg", ship.id), ship.id).save();
        flash.success("运输单已经标记运输, FBA[%s] 已经标记 SHIPPED.", StringUtils.join(ship.fbas, ","));

        redirect("/shipments/show/" + id);
    }

    /**
     * 对 Shipment 进行 Confirm, Confirm 以后不再允许添加运输项目
     *
     * @param id
     */
    @Check("shipments.deploytoamazon")
    public static void deployToAmazon(final String id, final List<String> shipItemId) {
        validation.required(shipItemId);
        checkAuthenticity();

        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.whouse", ship.whouse);
        checkShowError(ship);
        F.Option<FBAShipment> fbaOpt = ship.deployFBA(shipItemId);
        if(!fbaOpt.isDefined()) checkShowError(Shipment.<Shipment>findById(id));
        new ElcukRecord(Messages.get("shipment.createFBA"),
                Messages.get("shipment.createFBA.msg", id, fbaOpt.get().shipmentId), id).save();
        Notification.notifies("FBA 创建成功",
                Messages.get("shipment.createFBA.msg", id, fbaOpt.get().shipmentId),
                Notification.PROCURE);
        flash.success("Amazon FBA %s (with %s items) 创建成功", fbaOpt.get().shipmentId,
                fbaOpt.get().shipItems.size());
        redirect("/shipments/show/" + id);
    }

    /**
     * 将某一个 FBA 从一个运输单移动到另外一个运输单
     */
    @Check("shipments.movefba")
    public static void moveFba(String shipmentId, String id, String lastShipment) {
        validation.required(id);
        if(Validation.hasErrors()) {
            flash.error("必须选择的一个运输单");
            show(lastShipment);
        }
        FBAShipment fba = FBAShipment.findByShipmentId(shipmentId);
        Shipment shipment = Shipment.findById(id);
        fba.moveTo(shipment);
        if(Validation.hasErrors()) {
            renderArgs.put("ship", fba.shipment);
            render("Shipments/show.html");
        }
        flash.success("FBA #%s 转移成功.", fba.shipmentId);
        show(id);
    }


    /**
     * 更新某个 FBAShipment
     *
     * @param fbaId FBAShipment 的 Id
     */
    public static void updateFba(final Long fbaId) {
//        action 参数是 play! 自己使用的保留字
        checkAuthenticity();
        FBAShipment fba = FBAShipment.findById(fbaId);
        try {
            fba.updateFBAShipment(null);
        } catch(Exception e) {
            Validation.addError("", e.getMessage());
        }
        checkShowError(fba.shipment);
        new ElcukRecord(Messages.get("shipment.updateFBA"),
                Messages.get("action.base", String.format("FBA [%s] 更新了 %s 个 Items", fba.shipmentId,
                        fba.shipItems.size())),
                fba.shipment.id).save();
        flash.success("更新 Amazon FBA %s 成功.", fba.shipmentId);
        show(fba.shipment.id);
        redirect("/shipments/show/" + fba.shipment.id);
    }

    /**
     * 删除一个 FBAShipment
     *
     * @param fbaId FBAShipment.id
     */
    @Check("shipments.deployfba")
    public static void deleteFba(final Long fbaId) {
        checkAuthenticity();
        FBAShipment fba = FBAShipment.findById(fbaId);
        try {
            fba.removeFBAShipment();
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
        }
        if(Validation.hasErrors())
            checkShowError(fba.shipment);
        flash.success("FBA %s 删除成功", fba.shipmentId);
        Notification.notifies(String.format("FC`s %s 的 FBA(%s) 被删除", fba.centerId, fba.shipmentId),
                String.format("FBA %s 从系统中删除, 请检查运输单 %s", fba.shipmentId, fba.shipment.id),
                Notification.PROCURE);
        show(fba.shipment.id);
    }

    public static void refreshProcuress(final String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.trackNo", ship.trackNo);
        Validation.required("shipment.internationExpress", ship.internationExpress);
        checkShowError(ship);
        // 通过 play status 查看这个方法执行平均在 3s, 所以让其放开 3s 线程时间
        await("3s", new F.Action0() {
            @Override
            public void invoke() {
                // 由于使用 await 后, 就与原来不是同一个线程, 所以无法使用 Validate
                Shipment ship = Shipment.findById(id);
                ship.trackWebSite();
                ship.monitor();
                redirect("/shipments/show/" + ship.id);
            }
        });
    }

    /**
     * 将 Shipment 进行分拆
     */
    @Check("shipments.splitshipment")
    public static void splitShipment(String id, List<String> shipItemId) {
        validation.required(shipItemId);
        Shipment ship = Shipment.findById(id);
        checkShowError(ship);
        F.Option<Shipment> newShipmentOpt = ship.splitShipment(shipItemId);
        checkShowError(ship);
        if(newShipmentOpt.isDefined())
            flash.success("成功分拆运输单, 创建了新运输单 %s", newShipmentOpt.get().id);
        show(newShipmentOpt.get().id);
    }

    /**
     * 加载出来 Whouse 相关的可使用的运输单
     *
     * @param whouseId
     */
    public static void unitShipments(Long whouseId, Shipment.T shipType) {
        List<Shipment> unitRelateShipments = Shipment
                .findUnitRelateShipmentByWhouse(whouseId, shipType);
        render(unitRelateShipments);
    }
}
