package controllers;

import helper.Webs;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Whouse;
import models.view.Ret;
import models.view.post.ShipmentPost;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 货运单的控制器
 * User: wyattpan
 * Date: 6/20/12
 * Time: 3:09 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Shipments extends Controller {
    @Before(only = {"index", "show", "update", "beginShip", "blank"})
    public static void whouses() {
        List<Whouse> whouses = Whouse.findAll();
        renderArgs.put("whouses", whouses);
    }

    public static void index(ShipmentPost p) {
        List<Shipment> shipments = null;
        if(p == null) {
            p = new ShipmentPost();
            shipments = Shipment.find("state=? ORDER By createDate DESC", Shipment.S.PLAN).fetch();
        } else {
            shipments = p.query();
        }
        renderArgs.put("dateTypes", ShipmentPost.DATE_TYPES);
        render(shipments, p);
    }

    public static void blank() {
        Shipment ship = new Shipment(Shipment.id());
        render(ship);
    }

    public static void save(Shipment ship) {
        checkAuthenticity();
        validation.valid(ship);
        Validation.required("shipment.whouse", ship.whouse);
        if(Validation.hasErrors()) {
            render("Shipments/blank.html", ship);
        }
        ship.save();
        redirect("/shipments/show/" + ship.id);
    }


    @Before(only = {"show", "update", "beginShip"})
    public static void setUpShowPage() {
        List<Cooperator> shippers = Cooperator.shipper();
        renderArgs.put("shippers", shippers);
    }

    public static void show(String id) {
        Shipment ship = Shipment.findById(id);
        render(ship);
    }

    public static void update(Shipment ship) {
        checkAuthenticity();
        validation.valid(ship);
        ship.validate();
        if(Validation.hasErrors()) {
            render("Shipments/show.html", ship);
        }
        ship.save();
        flash.success("更新成功.");
        redirect("/Shipments/show/" + ship.id);
    }

    public static void comment(String id, String cmt) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.V(Validation.errors())));
        Shipment ship = Shipment.findById(id);
        ship.memo = cmt;
        ship.save();
        renderJSON(new Ret(true, Webs.V(Validation.errors())));
    }

    @Before(only = {"shipItem", "ship", "cancelShip"})
    public static void setUpShipPage() {
        String shipmentId = request.params.get("id");
        Shipment ship = Shipment.findById(shipmentId);
        List<ProcureUnit> units = ProcureUnit.waitToShip(ship.whouse.id);
        renderArgs.put("units", units);
    }

    public static void shipItem(String id) {
        Shipment ship = Shipment.findById(id);
        render(ship);
    }

    public static void ship(String id, List<Long> unitId, List<Integer> shipQty) {
        Validation.required("shipments.ship.unitId", unitId);
        Validation.required("shipments.ship.shipQty", shipQty);
        Validation.required("shipment.id", id);
        Validation.equals("shipments.ship.equal", unitId.size(), "", shipQty.size());
        Shipment ship = Shipment.findById(id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);

        ship.addToShip(unitId, shipQty);

        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);

        redirect("/shipments/shipitem/" + id);
    }

    public static void cancelShip(List<Integer> shipItemId, String id) {
        Validation.required("shipments.ship.shipId", shipItemId);
        Validation.required("shipment.id", id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        Shipment ship = Shipment.findById(id);
        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);

        ship.cancelShip(shipItemId);

        if(Validation.hasErrors()) render("Shipments/shipItem.html", ship);
        redirect("/shipments/shipitem/" + id);
    }

    public static void beginShip(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.id", id);
        if(Validation.hasError("shipment.id")) redirect("/shipments/index");
        Validation.required("shipment.trackNo", ship.trackNo);
        Validation.required("shipment.planArrivDate", ship.planArrivDate);
        Validation.required("shipment.volumn", ship.volumn);
        Validation.required("shipment.weight", ship.weight);
        Validation.required("shipment.declaredValue", ship.declaredValue);
        Validation.required("shipment.deposit", ship.deposit);
        Validation.required("shipment.otherFee", ship.otherFee);
        Validation.required("shipment.shipFee", ship.shipFee);
        Validation.min("shipment.items.size", ship.items.size(), 1);

        if(Validation.hasErrors()) render("Shipments/show.html", ship);

        ship.beginShip();
        if(Validation.hasErrors()) render("Shipments/show.html", ship);
        flash.success("运输单已经标记运输, FBA[%s] 已经标记 SHIPPED.", ship.fbaShipment.shipmentId);

        redirect("/shipments/show/" + id);
    }

    /**
     * 对 Shipment 进行 Confirm, Confirm 以后不再允许添加运输项目
     *
     * @param id
     */
    public static void confirm(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        Validation.equals("shipments.confirm.state", ship.state, "", Shipment.S.PLAN);
        if(Validation.hasErrors()) render("Shipments/show.html", ship);
        ship.confirmAndSyncTOAmazon();
        if(Validation.hasErrors()) render("Shipments/show.html", ship);
        flash.success("成功确认, 运输项目已经固定, Amazon 成功创建 Shipment 并且已经 confirm.");
        redirect("/shipments/show/" + id);
    }

    /**
     * 确认运输单已经到库
     *
     * @param id
     */
    public static void ensureDone(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        ship.ensureDone();
        if(Validation.hasErrors()) render("Shipments/show.html", ship);
        flash.success("成功确认, 运输单已经确认运输完毕.");
        redirect("/shipments/show/" + id);
    }

    public static void refreshProcuress(String id) {
        checkAuthenticity();
        Shipment ship = Shipment.findById(id);
        Validation.required("shipment.trackNo", ship.trackNo);
        Validation.required("shipment.internationExpress", ship.internationExpress);
        if(Validation.hasErrors()) render("Shipments/show.html", ship);
        ship.refreshIExpressHTML();
        redirect("/shipments/show/" + id);
    }
}
