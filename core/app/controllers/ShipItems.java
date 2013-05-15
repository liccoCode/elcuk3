package controllers;

import helper.Webs;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.view.post.ProcureUnitShipPost;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With({GlobalExceptionHandler.class, Secure.class})
public class ShipItems extends Controller {

    public static void index(ProcureUnitShipPost p) {
        if(p == null)
            p = new ProcureUnitShipPost();
        List<ProcureUnit> units = p.query();
        render(p, units);
    }

    /**
     * 将运输项目从一个运输单调整到另外一个运输单
     *
     * @param shipItemId 所在的原始运输单
     * @param shipmentId 需要调整的运输项目 id
     * @param targetId   目标运输单
     */
    public static void adjust(String shipmentId, String targetId, List<Long> shipItemId) {
        Shipment shipment = Shipment.findById(targetId);
        if(shipment == null)
            Validation.addError("", "需要指定一个正确的目标调整运输单.");
        if(shipItemId == null || shipItemId.size() == 0)
            Validation.addError("", "请选择需要调整的运输项目");

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Shipments.show(shipmentId);
        }

        ShipItem.adjustShipment(shipItemId, shipment);

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("成功调整 %s 个运输项目到 %s 运输单", shipItemId.size(), targetId);
        }

        Shipments.show(shipmentId);
    }

    /**
     * 修改运输计划的接收数量
     *
     * @param id
     * @param qty
     */
    public static void received(Long id, Integer qty) {
        if(qty == null) qty = 0;
        ShipItem itm = ShipItem.findById(id);
        itm.receviedQty(qty);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("修改成功.");

        render(itm);
    }
}
