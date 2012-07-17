package controllers;

import helper.J;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.product.Product;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Before;
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

    /**
     * 创建新的 Cooperator
     *
     * @param cop
     */
    public static void newCooper(Cooperator cop) {

    }

    public static void saveCooper(Cooperator cop) {

    }

    @Before(only = {"newCooperItem", "saveCooperItem"})
    public static void setUpCooperItemSavePage() {
        renderArgs.put("skus", J.json(Product.skus(false)));
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
        render(copItem, cop);
    }

    public static void editCooperItem(CooperItem copItem) {
        renderArgs.put("cop", copItem.cooperator);
        render("Cooperators/newCooperItem.html", copItem);
    }

    public static void saveCooperItem(CooperItem copItem, long cooperId) {
        checkAuthenticity();
        validation.valid(copItem);
        Cooperator cop = Cooperator.findById(cooperId);
        if(Validation.hasErrors())
            render("Cooperators/newCooperItem.html", copItem, cop);
        copItem.checkAndSave(cop);
        flash.success("创建成功.");
        show(cop.id, true);
    }

    public static void updateCooperItem(CooperItem copItem) {
        checkAuthenticity();
        validation.valid(copItem);
        if(Validation.hasErrors())
            render("Cooperators/editCooperItem.html", copItem);
        copItem.checkAndUpdate();
        flash.success("修改成功");
        show(copItem.cooperator.id, true);
    }

    public static void removeCooperItem(CooperItem copItem) {
        copItem.checkAndRemove();
        renderJSON(new Ret());
    }
}
