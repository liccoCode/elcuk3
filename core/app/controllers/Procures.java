package controllers;

import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.product.Whouse;
import models.view.post.ProcurePost;
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
    @Before(only = {"blank", "save", "index", "edit", "update"}, priority = 0)
    public static void whouses() {
        renderArgs.put("whouses", Whouse.<Whouse>findAll());
    }

    @Before(only = {"index"})
    public static void cooperators() {
        renderArgs.put("cooperators", Cooperator.<Cooperator>findAll());
        renderArgs.put("dateTypes", ProcurePost.DATE_TYPES);
    }

    public static void index(ProcurePost p) {
        List<ProcureUnit> units = null;
        if(p == null) {
            p = new ProcurePost();
            units = ProcureUnit.find("stage=?", ProcureUnit.STAGE.PLAN).fetch();
        } else {
            units = p.query();
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
        unit.handler = User.findByUserName(Secure.Security.connected());
        unit.validate();
        if(Validation.hasErrors()) {
            render("Procures/blank.html", unit);
        }
        unit.save();
        flash.success("创建成功");
        redirect("/Procures/index");
    }

    public static void edit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        render(unit);
    }

    public static void update(ProcureUnit unit) {
        unit.validate();
        if(Validation.hasErrors()) {
            render("Procures/edit.html", unit);
        }
        unit.save();
        flash.success("ProcureUnit %s update success!", unit.id);
        redirect("/Procures/index?p.search=id:" + unit.id);
    }

}
