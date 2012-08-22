package controllers;

import helper.J;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 控制器
 * User: wyattpan
 * Date: 7/16/12
 * Time: 12:12 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Cooperators extends Controller {

    public static void index() {
        List<Cooperator> coopers = Cooperator.findAll();
        render(coopers);
    }

    public static void show(long id, Boolean full) {
        Cooperator coper = Cooperator.findById(id);
        if(coper == null || !coper.isPersistent()) notFound();
        if(full == null) {
            redirect("/cooperators/index#" + id);
        } else {
            render(coper, full);
        }
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

    /**
     * 创建新的 Cooperator
     *
     * @param cop
     */
    public static void newCooper(Cooperator cop) {
        if(cop == null) cop = new Cooperator();
        render(cop);
    }

    public static void saveCooper(Cooperator cop) {
        validation.valid(cop);
        if(Validation.hasErrors())
            render("Cooperators/newCooper.html", cop);
        cop.checkAndUpdate();
        // 这样编写, 是因为前台这个页面有自动处理 hash 值
        redirect("/cooperators/index#" + cop.id);
    }

    /**
     * 添加新的 CooperItem
     *
     * @param copItem
     */
    public static void newCooperItem(CooperItem copItem, long cooperId) {
        if(copItem == null) copItem = new CooperItem();
        Cooperator cop = Cooperator.findById(cooperId);
        if(cop == null || !cop.isPersistent()) error("不正确的合作者参数");
        renderArgs.put("skus", J.json(cop.frontSkuHelper()));
        render(copItem, cop);
    }

    public static void editCooperItem(CooperItem copItem) {
        renderArgs.put("cop", copItem.cooperator);
        renderArgs.put("skus", J.json(copItem.cooperator.frontSkuHelper()));
        render("Cooperators/newCooperItem.html", copItem);
    }

    public static void saveCooperItem(CooperItem copItem, long cooperId) {
        checkAuthenticity();
        validation.valid(copItem);
        Cooperator cop = Cooperator.findById(cooperId);
        renderArgs.put("skus", J.json(cop.frontSkuHelper()));
        if(Validation.hasErrors())
            render("Cooperators/newCooperItem.html", copItem, cop);
        copItem.checkAndSave(cop);
        flash.success("创建成功.");
        show(cop.id, true);
    }

    public static void updateCooperItem(CooperItem copItem) {
        checkAuthenticity();
        validation.valid(copItem);
        if(Validation.hasErrors()) {
            renderArgs.put("cop", copItem.cooperator);
            renderArgs.put("skus", J.json(copItem.cooperator.frontSkuHelper()));
            render("Cooperators/newCooperItem.html", copItem);
        }
        copItem.checkAndUpdate();
        flash.success("CooperItem %s, %s 修改成功", copItem.id, copItem.sku);
        redirect("/cooperators/index#" + copItem.cooperator.id);
    }

    public static void removeCooperItem(CooperItem copItem) {
        copItem.checkAndRemove();
        renderJSON(new Ret());
    }
}
