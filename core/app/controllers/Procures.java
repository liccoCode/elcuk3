package controllers;

import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.product.Whouse;
import models.view.ProcurePost;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/5/12
 * Time: 9:53 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Procures extends Controller {
    @Before(only = {"blank", "save", "index"}, priority = 0)
    public static void whouses() {
        renderArgs.put("whouses", Whouse.<Whouse>findAll());
    }

    @Before(only = {"index"}, priority = 1)
    public static void cooperators() {
        renderArgs.put("cooperators", Cooperator.<Cooperator>findAll());
    }

    public static void index(ProcurePost p) {
        List<ProcureUnit> units = null;
        if(p == null) {
            p = new ProcurePost();
            units = ProcureUnit.find("stage=?", ProcureUnit.STAGE.PLAN).fetch();
        } else {
            units = p.search();
        }
        render(p, units);
    }

    public static void blank(ProcureUnit unit) {
        if(unit == null || unit.selling == null) {
            flash.error("请通过 SellingId 进行, 没有执行合法的 SellingId 无法创建 ProcureUnit!");
            render(unit);
        }
        render(unit);
    }

    public static void save(ProcureUnit unit) {
        validation.valid(unit);
        validation.valid(unit.attrs);
        if(Validation.hasErrors()) {
            render("Procures/blank.html", unit);
        }
        unit.handler = User.findByUserName(Secure.Security.connected());
        unit.checkAndCreate();
        flash.success("创建成功");
        redirect("/Procures/index");
    }

}
