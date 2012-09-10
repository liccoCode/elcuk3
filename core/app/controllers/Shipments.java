package controllers;

import helper.J;
import models.User;
import models.procure.Payment;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.view.Ret;
import models.view.post.ShipmentPost;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;

/**
 * 货运单的控制器
 * User: wyattpan
 * Date: 6/20/12
 * Time: 3:09 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Shipments extends Controller {
    public static void index(ShipmentPost p) {
        List<Shipment> shipments = null;
        if(p == null) {
            p = new ShipmentPost();
            shipments = Shipment.find("state=?", Shipment.S.PLAN).fetch();
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
        if(Validation.hasErrors()) {
            render("Shipments/blank.html", ship);
        }
        ship.save();
        redirect("/shipments/show/" + ship.id);
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

    @Before(only = {"shipItem", "ship", "cancelShip"})
    public static void setUpShipPage() {
        List<ProcureUnit> units = ProcureUnit.find("stage IN (?,?)", ProcureUnit.STAGE.DONE, ProcureUnit.STAGE.PART_SHIPPING).fetch();
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
}
