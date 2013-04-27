package controllers;

import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.embedded.UnitAttrs;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.product.Whouse;
import models.view.Ret;
import models.view.post.ProcurePost;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/5/12
 * Time: 9:53 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Procures extends Controller {

    @Before(only = {"index", "createDeliveryment", "remove", "splitUnit", "doSplitUnit"})
    public static void index_sets() {
        renderArgs.put("cooperators", Cooperator.<Cooperator>findAll());
        renderArgs.put("dateTypes", ProcurePost.DATE_TYPES);
        renderArgs.put("whouses", Whouse.<Whouse>findAll());
        renderArgs.put("logs", ElcukRecord.fid("procures.remove").<ElcukRecord>fetch(50));
    }

    @Before(only = {"splitUnit", "deliveryUnit"}, priority = 0)
    public static void checkUnitValid() {
        String unitId = request.params.get("id");
        long uid = NumberUtils.toLong(unitId);
        ProcureUnit unit = ProcureUnit.findById(uid);
        if(unit.cooperator == null) {
            flash.error("请将 合作伙伴(供应商) 补充完整.");
            redirect("/procures/edit/" + unitId);
        }
        if(unit.shipType == null) {
            flash.error("请将 运输方式 补充完整.");
            redirect("/procures/edit/" + unitId);
        }
    }

    @Check("procures.index")
    public static void index(ProcurePost p) {
        if(p == null)
            p = new ProcurePost();
        render(p);
    }


    public static void remove(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        unit.remove();
        if(Validation.hasErrors()) {
            renderArgs.put("p", new ProcurePost());
            render("Procures/index");
        }
        flash.success("删除成功");
        redirect("/Procures/index");
    }


    /**
     * 抵达货代
     * TODO effect?
     *
     * @param id
     */
    public static void markPlace(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.cooperator == null || unit.shipType == null) {
            renderJSON(new Ret(false, "[合作者] 或者 [运输方式] 需要填写完整."));
        }
        unit.isPlaced = true;
        unit.save();
        renderJSON(new Ret());
    }

    /**
     * 某一个 ProcureUnit 交货
     */
    public static void deliveryUnit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        renderArgs.put("attrs", unit.attrs);
        render(unit);
    }

    /**
     * 交货更新
     *
     * @param attrs
     */
    @Check("procures.delivery")
    public static void delivery(UnitAttrs attrs, long id, String cmt) {
        attrs.validate();
        ProcureUnit unit = ProcureUnit.findById(id);
        if(Validation.hasErrors()) {
            render("Procures/deliveryUnit.html", unit, attrs);
        }
        unit.comment = cmt;
        try {
            Boolean isFullDelivery = unit.delivery(attrs);
            if(isFullDelivery) {
                flash.success("ProcureUnit %s 全部交货!", unit.id);
            } else {
                flash.success("ProcureUnits %s 超额交货, 预计交货 %s, 实际交货 %s",
                        unit.id, unit.attrs.planQty, unit.attrs.qty);
            }
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
            render("Procures/deliveryUnit.html", unit, attrs);
        }

        Deliveryments.show(unit.deliveryment.id);
    }

    public static void splitUnit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        ProcureUnit newUnit = new ProcureUnit(unit);
        newUnit.deliveryment = unit.deliveryment;
        newUnit.product = unit.product;
        render(unit, newUnit);
    }

    /**
     * 分拆操作
     *
     * @param id
     * @param newUnit
     */
    @Check("procures.dosplitunit")
    public static void doSplitUnit(long id, ProcureUnit newUnit) {
        checkAuthenticity();
        ProcureUnit unit = ProcureUnit.findById(id);
        newUnit.handler = User.current();
        unit.split(newUnit);
        if(Validation.hasErrors()) render("Procures/splitUnit.html", unit, newUnit);
        //TODO effect: 调整采购计划分拆, 取消周期型运输单
        /*
        if(unit.isHaveCycleShipment())
            flash.success("分拆成功, 并且成功保留对应的周期型运输单.");
        else
            flash.success("分拆成功, 并不处于周期型运输单中, 进入采购计划池中.");
        */
        flash.success("分拆成功, 并不处于周期型运输单中, 进入采购计划池中.");
        Deliveryments.show(unit.deliveryment.id);
    }

    public static void calculateBox(long coperId, String sku, int size) {
        validation.required(coperId);
        validation.required(sku);
        validation.required(size);

        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.V(Validation.errors())));

        CooperItem copi = CooperItem.find("cooperator.id=? AND product.sku=?", coperId, sku)
                .first();
        renderJSON(new Ret(true, copi.boxToSize(size) + ""));
    }
}
