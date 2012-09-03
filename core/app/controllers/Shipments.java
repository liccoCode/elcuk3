package controllers;

import helper.J;
import models.User;
import models.procure.Payment;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.view.Ret;
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
    public static void index() {
        List<Shipment> pendings = Shipment.shipmentsByState(Shipment.S.PLAN);
        List<Shipment> shippings = Shipment.shipmentsByState(Shipment.S.SHIPPING);
        List<Shipment> clear = Shipment.shipmentsByState(Shipment.S.CLEARANCE);
        List<Shipment> dones = Shipment.find("state=?", Shipment.S.DONE).fetch(1, 20); // 由更多再 Ajax 加载

        render(pendings, shippings, clear, dones);
    }

    public static void blank() {
        Shipment s = new Shipment(Shipment.id());
        render(s);
    }

    public static void save(Shipment s) {
        checkAuthenticity();
        validation.valid(s);
        if(Validation.hasErrors()) {
            render("Shipments/blank.html", s);
        }
        s.checkAndCreate();
        index();
    }
}
