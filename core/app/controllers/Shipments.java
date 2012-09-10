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
}
