package controllers;

import controllers.api.SystemOperation;
import models.OperatorConfig;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.post.OutboundPost;
import models.whouse.Outbound;
import models.whouse.Whouse;
import play.db.helper.JpqlSelect;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by licco on 2016/11/30.
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Outbounds extends Controller {

    @Before(only = {"index", "edit", "blank"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("whouses", Whouse.selfWhouses(false));
        renderArgs.put("shippers", Cooperator.shippers());
        renderArgs.put("suppliers", Cooperator.suppliers());
        renderArgs.put("users", User.findAll());
    }

    public static void index(OutboundPost p) {
        if(p == null) p = new OutboundPost();
        List<Outbound> outbounds = p.query();

        render(p, outbounds);
    }

    public static void edit(String id) {
        Outbound outbound = Outbound.findById(id);

        render(outbound);
    }

    public static void blank(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        ProcureUnit proUnit = units.get(0);
        render(units, proUnit);
    }

    public static void create(Outbound outbound, List<Long> pids) {
        outbound.create(pids);
        index(new OutboundPost());
    }

}
