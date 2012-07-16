package controllers;

import helper.J;
import models.User;
import models.embedded.UnitDelivery;
import models.market.Selling;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.view.Ret;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/5/12
 * Time: 9:53 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Procures extends Controller {
    public static void index() {
        List<ProcureUnit> plan = ProcureUnit.findByStage(ProcureUnit.STAGE.PLAN);
        List<ProcureUnit> procure = ProcureUnit.findByStage(ProcureUnit.STAGE.DELIVERY);
        List<ProcureUnit> done = ProcureUnit.findByStage(ProcureUnit.STAGE.DONE);
        List<Deliveryment> dlmts = Deliveryment.openDeliveryments();
        List<Deliveryment> doneDlmts = Deliveryment.find("state=?", Deliveryment.S.DELIVERY).fetch();
        render(plan, procure, done, dlmts, doneDlmts);
    }

    public static void create() {
        renderArgs.put("suppliers", J.json(ProcureUnit.suppliers()));
        renderArgs.put("sids", J.json(Selling.allSid(true)));
        render();
    }

    public static void edit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        render(unit);
    }

    public static void update(ProcureUnit p) {
        if(!p.isPersistent()) throw new FastRuntimeException("此 ProcureUnti 不存在.");
        p.checkAndUpdate();
        renderJSON(J.G(p));
    }

    public static void sidSetUp(String sid) {
        Selling selling = Selling.findById(sid);
        renderJSON(J.json(new F.T4<String, String, String, String>(
                selling.listing.listingId,
                selling.sellingId,
                selling.listing.product.sku,
                selling.market.nickName()
        )));
    }

    public static void save(ProcureUnit p) {
        p.handler = User.findByUserName(Secure.Security.connected());
        renderJSON(J.G(p.checkAndCreate()));
    }

    public static void close(Long id, String msg) {
        ProcureUnit p = ProcureUnit.findById(id);
        if(p == null || !p.isPersistent()) throw new FastRuntimeException("不存在!");
        p.close(msg);
        renderJSON(new Ret(true, "ProcureUnit(" + id + ") 因 (" + msg + ") 关闭成功"));
    }

    // ------------- Plan Tab ---------------------
    public static void planDetail(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.stage != ProcureUnit.STAGE.PLAN)
            throw new FastRuntimeException("此采购单元已经不是 PLAN 阶段");
        List<Deliveryment> dlms = Deliveryment.find("state=?", Deliveryment.S.PENDING).fetch();
        render(unit, dlms);
    }

    public static void createDeliveryMent() {
        User user = User.findByUserName(Secure.Security.connected());
        renderJSON(J.G(Deliveryment.checkAndCreate(user)));
    }

    public static void procureUnitToDeliveryMent(ProcureUnit p, Deliveryment dlmt) {
        renderJSON(J.G(p.assignToDeliveryment(dlmt)));
    }

    public static void procureUnitDone(Long id, UnitDelivery d, String cmt) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit == null || !unit.isPersistent()) throw new FastRuntimeException("ProcureUnit 不存在!");
        renderJSON(J.G(unit.deliveryComplete(d, cmt)));
    }

    // ---------------- Delivery Tab ------------------
    public static void deliveryDetail(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.stage != ProcureUnit.STAGE.DELIVERY)
            throw new FastRuntimeException("此采购单元的不是 DELIVERY 阶段");
        List<Shipment> shipments = Shipment.shipmentsByState(Shipment.S.PLAN);
        render(unit, shipments);
    }
}
