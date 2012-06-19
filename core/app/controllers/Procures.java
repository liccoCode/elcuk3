package controllers;

import com.alibaba.fastjson.JSON;
import helper.Webs;
import models.Ret;
import models.User;
import models.market.Selling;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import play.libs.F;
import play.mvc.Controller;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/5/12
 * Time: 9:53 AM
 */
public class Procures extends Controller {
    public static void index() {
        List<ProcureUnit> plan = ProcureUnit.findByState(ProcureUnit.STAGE.PLAN, 1, 20);
        List<ProcureUnit> procure = ProcureUnit.findByState(ProcureUnit.STAGE.DELIVERY, 1, 20);
        List<ProcureUnit> shipped = ProcureUnit.findByState(ProcureUnit.STAGE.SHIP, 1, 20);
        render(plan, procure, shipped);
    }

    public static void create() {
        renderArgs.put("suppliers", JSON.toJSONString(ProcureUnit.suppliers()));
        renderArgs.put("sids", JSON.toJSONString(Selling.allSID()));
        render();
    }

    public static void sidSetUp(String sid) {
        Selling selling = Selling.findById(sid);
        renderJSON(JSON.toJSONString(new F.T3<String, String, String>(selling.listing.listingId, selling.sellingId, selling.listing.product.sku)));
    }

    public static void save(ProcureUnit p) {
        try {
            p.handler = User.findByUserName(Secure.Security.connected());
            renderJSON(Webs.exposeGson(p.checkAndCreate()));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void planDetail(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.stage() != ProcureUnit.STAGE.PLAN)
            renderJSON(new Ret("此采购单元已经不是 PLAN 状态."));
        List<Deliveryment> dlms = Deliveryment.openDeliveryments();
        render(unit, dlms);
    }

    public static void createDeliveryMent() {
        try {
            renderJSON(Webs.exposeGson(Deliveryment.checkAndCreate()));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void procureUnitToDeliveryMent(ProcureUnit p) {
        System.out.println(p.deliveryment);
    }
}
