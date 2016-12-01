package controllers;

import controllers.api.SystemOperation;
import models.procure.Cooperator;
import models.view.post.RefundPost;
import models.whouse.Refund;
import models.whouse.Whouse;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by licco on 2016/11/28.
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Refunds extends Controller {

    @Before(only = {"index", "edit"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("whouses", Whouse.selfWhouses(false));
    }

    public static void index(RefundPost p) {
        if(p == null) p = new RefundPost();
        List<Refund> refunds = p.query();
        render(p, refunds);
    }

    public static void edit(Long id) {
        Refund refund = Refund.findById(id);
        render(refund);
    }

    public static void update(Refund refund) {


    }

}
