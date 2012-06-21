package controllers;

import models.procure.Shipment;
import play.data.validation.Validation;
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
    public static void index() {
        List<Shipment> pendings = Shipment.shipmentsByState(Shipment.S.PEDING);
        List<Shipment> shippings = Shipment.shipmentsByState(Shipment.S.SHIPPING);
        List<Shipment> clearAndReciving = Shipment.shipmentsByState(Shipment.S.CLEARGATE);
        clearAndReciving.addAll(Shipment.shipmentsByState(Shipment.S.RECIVING));
        List<Shipment> dones = Shipment.find("state=?", Shipment.S.DONE).fetch(1, 20); // 由更多再 Ajax 加载

        render(pendings, shippings, clearAndReciving, dones);
    }

    public static void blank() {
        Shipment s = new Shipment();
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

    /**
     * 查看一个 Pending 状态的 Shipment
     */
    public static void pending(String id) {
        Shipment s = Shipment.findById(id);
        render(s);
    }
}
