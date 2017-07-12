package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import models.User;
import models.finance.Apply;
import models.finance.FeeType;
import models.finance.ProcureApply;
import models.finance.TransportApply;
import models.material.MaterialApply;
import models.procure.Cooperator;
import models.procure.Shipment;
import models.view.Ret;
import models.view.post.*;
import play.data.validation.Validation;
import play.mvc.Before;
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
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Applys extends Controller {

    @Before(only = {"procures", "transports", "materials"})
    public static void beforIndex() {
        List<Cooperator> suppliers = Cooperator.suppliers();
        renderArgs.put("suppliers", suppliers);
    }


    @Check("applys.index")
    public static void procures(ProcreApplyPost p) {
        if(p == null) p = new ProcreApplyPost();
        List<Apply> applyes = p.query();
        render(applyes, p);
    }

    /**
     * 物流请款列表
     */
    public static void transports(TransportApplyPost p) {
        if(p == null) p = new TransportApplyPost();
        List<User> users = User.findAll();
        List<TransportApply> applyes = p.query();
        render(applyes, p, users);
    }


    /**
     * 采购请款单
     */
    @Check("applys.procure")
    public static void procure(Long id) {
        ProcureApply apply = ProcureApply.findById(id);
        render(apply);
    }


    public static void transport(Long id, TransApplyShipPost p) {
        List<FeeType> feeTypes = Shipments.feeTypes(null);
        List<Cooperator> cooperators = Cooperator.shippers();
        TransportApply apply = TransportApply.findById(id);
        notFoundIfNull(apply);

        if(p == null) p = new TransApplyShipPost(id);
        List<Shipment> shipments = p.query();
        render(apply, feeTypes, cooperators, p, shipments);
    }

    public static void procureConfirm(Long id) {
        ProcureApply apply = ProcureApply.findById(id);
        apply.confirm = true;
        apply.save();
        render();
    }

    /**
     * 想运输请款单中添加运输单
     *
     * @param id
     * @param shipmentId
     */
    @Check("applys.handlshipment")
    public static void transportAddShipment(Long id, String shipmentId) {
        TransportApply apply = TransportApply.findById(id);
        Shipment ship = Shipment.findById(shipmentId);
        if(ship.apply != null)
            Validation.addError("", "已经存在请款单, 无法添加成功.");
        apply.appendShipment(Arrays.asList(ship.id));
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        transport(id, null);
    }

    /**
     * 将运输单从请款单中剥离
     *
     * @param id
     */
    @Check("applys.handlshipment")
    public static void departShipmentFromApply(String id) {
        Shipment ship = Shipment.findById(id);
        ship.departFromApply();
        if(Validation.hasErrors())
            renderJSON(Webs.vJson(Validation.errors()));
        renderJSON(new Ret(true, "运输单剥离成功"));
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
            Validation.addError("", "请选择需要创建请款单的运输单！");
        TransportApply apply = null;
        if(!Validation.hasErrors())
            apply = TransportApply.buildTransportApply(shipmentId);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            Shipments.index(p);
        } else {
            if(apply != null) {
                Applys.transport(apply.id, null);
            } else {
                flash.error("请款单创建失败.");
                Shipments.index(p);
            }
        }
    }

    /**
     * 输出给 typeahead 所使用的 source, 暂时只支持 TransportApply
     * <p>
     * 需要支持: 运输单 ID 、TrackNo、FBA
     */
    public static void source(Long applyId, String search) {
        TransportApply apply = TransportApply.findById(applyId);
        notFoundIfNull(apply);
        renderJSON(J.json(apply.pickSource(search)));
    }

    /**
     * 采购请款单
     */
    @Check("applys.procure")
    public static void material(Long id) {
        MaterialApply apply = MaterialApply.findById(id);
        render(apply);
    }

    @Check("applys.index")
    public static void materials(MaterialApplyPost p) {
        if(p == null) p = new MaterialApplyPost();
        List<Apply> applyes = p.query();
        render(applyes, p);
    }
}
