package controllers;

import com.alibaba.fastjson.JSON;
import helper.Webs;
import models.User;
import models.market.Selling;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
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
@With({FastRunTimeExceptionCatch.class, Secure.class, GzipFilter.class})
public class Procures extends Controller {
    public static void index() {
        List<ProcureUnit> plan = ProcureUnit.findByStage(ProcureUnit.STAGE.PLAN, 1, 20);
        List<ProcureUnit> procure = ProcureUnit.findByStage(ProcureUnit.STAGE.DELIVERY, 1, 20);
        List<ProcureUnit> shipped = ProcureUnit.findByStage(ProcureUnit.STAGE.SHIP, 1, 20);
        render(plan, procure, shipped);
    }

    public static void create() {
        renderArgs.put("suppliers", JSON.toJSONString(ProcureUnit.suppliers()));
        renderArgs.put("sids", JSON.toJSONString(Selling.allSID()));
        render();
    }

    public static void edit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        render(unit);
    }

    public static void update(ProcureUnit p) {
        if(!p.isPersistent()) throw new FastRuntimeException("此 ProcureUnti 不存在.");
        p.checkAndUpdate();
        renderJSON(Webs.exposeGson(p));
    }

    public static void sidSetUp(String sid) {
        Selling selling = Selling.findById(sid);
        renderJSON(JSON.toJSONString(new F.T3<String, String, String>(selling.listing.listingId, selling.sellingId, selling.listing.product.sku)));
    }

    public static void save(ProcureUnit p) {
        p.handler = User.findByUserName(Secure.Security.connected());
        renderJSON(Webs.exposeGson(p.checkAndCreate()));
    }

    public static void planDetail(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.stage != ProcureUnit.STAGE.PLAN)
            throw new FastRuntimeException("此采购单元已经不是 PLAN 阶段");
        List<Deliveryment> dlms = Deliveryment.openDeliveryments();
        render(unit, dlms);
    }

    public static void deliveryDetail(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.stage != ProcureUnit.STAGE.DELIVERY)
            throw new FastRuntimeException("此采购单元的不是 DELIVERY 阶段");
        List<Shipment> shipments = Shipment.openShipments();
        render(unit, shipments);
    }

    public static void createDeliveryMent() {
        User user = User.findByUserName(Secure.Security.connected());
        renderJSON(Webs.exposeGson(Deliveryment.checkAndCreate(user)));
    }

    public static void procureUnitToDeliveryMent(ProcureUnit p) {
        renderJSON(Webs.exposeGson(p.stageFromPlanToDelivery()));
    }

    public static void procureUnitDeliveryInfoUpdate(ProcureUnit p) {
        renderJSON(Webs.exposeGson(p.deliveryInfoUpdate()));
    }
}
