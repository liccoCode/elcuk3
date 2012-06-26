package controllers;

import helper.Webs;
import models.User;
import models.procure.Payment;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import play.data.validation.Validation;
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
        List<Shipment> clear = Shipment.shipmentsByState(Shipment.S.CLEARGATE);
        List<Shipment> dones = Shipment.find("state=?", Shipment.S.DONE).fetch(1, 20); // 由更多再 Ajax 加载

        render(pendings, shippings, clear, dones);
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
        List<ProcureUnit> units = ProcureUnit.findWaitingForShip();
        render(s, units);
    }

    public static void shipProcureUnit(ProcureUnit unit, Integer qty, String shipmentId) {
        Shipment shipment = Shipment.findById(shipmentId);
        if(shipment == null || !shipment.isPersistent()) throw new FastRuntimeException("Shipment 不存在!");
        renderJSON(Webs.G(unit.transformToShipment(shipment, qty)));
    }

    public static void removeItemFromShipment(Long shipmentId) {
        ShipItem item = ShipItem.findById(shipmentId);
        if(item == null || !item.isPersistent()) throw new FastRuntimeException("ShipItem 不存在!");
        renderJSON(Webs.G(item.removeFromShipment()));
    }

    public static void confirmShipment(String shipmentId, String trckNo, Shipment.I iExpress) {
        Shipment shipment = Shipment.findById(shipmentId);
        if(shipment == null || !shipment.isPersistent())
            throw new FastRuntimeException("Shipment(" + shipmentId + ") 不存在!");
        renderJSON(Webs.G(shipment.fromPlanToShip(trckNo, iExpress)));
    }

    public static void payment(Payment pay, Shipment payObj) {
        pay.payer = User.findByUserName(Secure.Security.connected());
        renderJSON(Webs.G(payObj.payForShipment(pay)));
    }
}
