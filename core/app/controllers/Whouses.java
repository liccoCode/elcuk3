package controllers;

import models.market.Account;
import models.procure.FBACenter;
import models.product.Whouse;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 9/26/12
 * Time: 11:34 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Whouses extends Controller {

    @Before(only = {"index", "blank", "create", "edit", "update"})
    public static void setUpAccs() {
        renderArgs.put("accs", Account.openedSaleAcc());
        renderArgs.put("fbaCenters", FBACenter.all().<FBACenter>fetch());
    }


    @Check("whouses.index")
    public static void index() {
        List<Whouse> whs = Whouse.all().fetch();
        render(whs);
    }

    public static void blank() {
        Whouse wh = new Whouse();
        render(wh);
    }

    public static void create(Whouse wh) {
        validation.valid(wh);
        wh.validate();
        if(Validation.hasErrors()) render("Whouses/blank.html", wh);
        wh.save();
        flash.success("创建成功");
        redirect("/Whouses/index");
    }

    public static void edit(long id) {
        Whouse wh = Whouse.findById(id);
        render(wh);
    }

    public static void update(Whouse wh) {
        validation.valid(wh);
        wh.validate();
        if(Validation.hasErrors()) render("Whouses/edit.html", wh);
        wh.save();
        flash.success("更新成功");
        redirect("/Whouses/index");
    }
}
