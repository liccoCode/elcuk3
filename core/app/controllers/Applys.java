package controllers;

import helper.Webs;
import models.finance.Apply;
import models.finance.FeeType;
import models.finance.ProcureApply;
import models.finance.TransportApply;
import models.procure.Shipment;
import models.view.post.ShipmentPost;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.List;

/**
 * 所有的请款单控制器
 * User: wyatt
 * Date: 3/26/13
 * Time: 3:53 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Applys extends Controller {

    @Check("applys.index")
    public static void index() {
        List<Apply> applyes = ProcureApply.find("ORDER BY createdAt DESC").fetch();
        render(applyes);
    }

    public static void transports() {
        List<Apply> applyes = TransportApply.find("ORDER BY createdAt DESC").fetch();
        render(applyes);
    }

    /**
     * 采购请款单
     */
    @Check("applys.procure")
    public static void procure(Long id) {
        ProcureApply apply = ProcureApply.findById(id);
        render(apply);
    }

    public static void transport(Long id) {
        List<FeeType> feeTypes = Shipments.feeTypes(null);
        TransportApply apply = TransportApply.findById(id);
        render(apply, feeTypes);
    }

    public static void procureConfirm(Long id) {
        ProcureApply apply = ProcureApply.findById(id);
        apply.confirm = true;
        apply.save();
        render();
    }

    public static void transportAddShipment(Long id, String shipmentId) {
        TransportApply apply = TransportApply.findById(id);
        Shipment ship = Shipment.findById(shipmentId);
        apply.appendShipment(Arrays.asList(ship.id));
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        transport(id);
    }

    /**
     * 通过运输单 创建 运输请款单 资源
     *
     * @param shipmentId
     * @param p
     */
    @Check("applys.shipmenttoapply")
    public static void shipmentToApply(List<String> shipmentId, ShipmentPost p) {
        if(shipmentId == null || shipmentId.size() == 0)
            Validation.addError("", "请选择需要创建请款单的运输单");

        TransportApply apply = null;
        if(!Validation.hasErrors())
            apply = TransportApply.buildTransportApply(shipmentId);

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Shipments.index(p);
        } else {
            if(apply != null) {
                Applys.transport(apply.id);
            } else {
                flash.error("请款单创建失败.");
                Shipments.index(p);
            }
        }
    }
}
