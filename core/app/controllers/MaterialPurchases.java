package controllers;

import controllers.api.SystemOperation;
import models.finance.ProcureApply;
import models.material.MaterialPurchase;
import models.procure.Cooperator;
import models.view.post.DeliveryPost;
import models.view.post.MaterialPurchasePost;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/31
 * Time: 下午5:10
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialPurchases extends Controller {

    @Before(only = {"index"})
    public static void beforeIndex(DeliveryPost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        List<ProcureApply> availableApplies = ProcureApply.unPaidApplies(p == null ? null : p.cooperId);
        renderArgs.put("suppliers", suppliers);
        renderArgs.put("availableApplies", availableApplies);
    }

    public static void index(MaterialPurchasePost p) {
        if(p == null) p = new MaterialPurchasePost();

        render(p);
    }

    public static void blank() {


       render();
    }

    public static void create(MaterialPurchase purchase) {



    }

}
