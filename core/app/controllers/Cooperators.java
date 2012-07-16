package controllers;

import models.procure.CooperItem;
import models.procure.Cooperator;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;

import java.util.List;

/**
 * 控制器
 * User: wyattpan
 * Date: 7/16/12
 * Time: 12:12 PM
 */
public class Cooperators extends Controller {

    public static void index() {
        List<Cooperator> coopers = Cooperator.findAll();
        render(coopers);
    }

    public static void show(long id, boolean full) {
        Cooperator coper = Cooperator.findById(id);
        if(coper == null || !coper.isPersistent()) notFound();
        render(coper, full);
    }

    /**
     * 修改 Cooperator 对象
     *
     * @param cop
     */
    public static void edit(Cooperator cop) {
        validation.valid(cop);
        if(Validation.hasErrors())
            renderJSON(new Ret(validation.errorsMap()));
        cop.checkAndUpdate();
        renderJSON(new Ret());
    }

    public static void itemEdit(CooperItem copItem) {
        validation.valid(copItem);
        if(Validation.hasErrors())
            renderJSON(new Ret(validation.errorsMap()));
        copItem.checkAndUpdate();
        renderJSON(new Ret());
    }
}
