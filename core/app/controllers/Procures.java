package controllers;

import helper.Webs;
import models.User;
import models.embedded.UnitAttrs;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.product.Whouse;
import models.view.Ret;
import models.view.post.ProcurePost;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
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
    @Before(only = {"blank", "save", "index", "createDeliveryment", "edit", "update"}, priority = 0)
    public static void whouses() {
        renderArgs.put("whouses", Whouse.<Whouse>findAll());
    }

    @Before(only = {"index", "createDeliveryment"})
    public static void cooperators() {
        renderArgs.put("cooperators", Cooperator.<Cooperator>findAll());
        renderArgs.put("dateTypes", ProcurePost.DATE_TYPES);
    }

    public static void index(ProcurePost p) {
        List<ProcureUnit> units = null;
        if(p == null) {
            p = new ProcurePost();
            units = ProcureUnit.unitsFilterByStage(ProcureUnit.STAGE.PLAN);
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

    /**
     * 从 Procrues#index 页面, 通过选择 ProcureUnit 创建 Deliveryment
     *
     * @param pids
     * @param name
     */
    public static void createDeliveryment(List<Long> pids, String name) {
        Validation.required("procrues.createDeliveryment.name", name);
        Validation.required("deliveryments.addunits", pids);
        if(Validation.hasErrors()) {
            renderArgs.put("units", ProcureUnit.unitsFilterByStage(ProcureUnit.STAGE.PLAN));
            renderArgs.put("p", new ProcurePost());
            render("Procures/index.html", name);
        }
        Deliveryment deliveryment = Deliveryment.createFromProcures(pids, name, User.findByUserName(Secure.Security.connected()));
        if(Validation.hasErrors()) {
            renderArgs.put("units", ProcureUnit.unitsFilterByStage(ProcureUnit.STAGE.PLAN));
            renderArgs.put("p", new ProcurePost());
            render("Procures/index.html", name);
        }
        flash.success("Deliveryment %s 创建成功.", deliveryment.id);
        redirect("/Deliveryments/show/" + deliveryment.id);
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
    public static void delivery(UnitAttrs attrs, long id, String cmt) {
        attrs.validate();
        ProcureUnit unit = ProcureUnit.findById(id);
        if(Validation.hasErrors()) {
            render("Procures/deliveryUnit.html", unit, attrs);
        }
        unit.comment = cmt;
        F.T2<Boolean, ProcureUnit> isFullDelivery = unit.delivery(attrs);
        if(Validation.hasErrors()) {
            render("Procures/deliveryUnit.html", unit, attrs);
        }
        if(isFullDelivery._1) {
            flash.success("ProcureUnit %s 全部交货!", unit.id);
        } else if(!isFullDelivery._1 && isFullDelivery._2 != null) {
            flash.success("ProcureUnits %s 部分交货, 剩余部分将自动创建一个新的[采购单元(%s)]! (%s / %s)",
                    unit.id, isFullDelivery._2.id, attrs.qty, attrs.planQty);
        }
        redirect("/Deliveryments/show/" + unit.deliveryment.id);
    }

    public static void calculateBox(long coperId, String sku, int size) {
        validation.required(coperId);
        validation.required(sku);
        validation.required(size);

        if(Validation.hasErrors()) renderJSON(new Ret(false, Webs.V(Validation.errors())));

        CooperItem copi = CooperItem.find("cooperator.id=? AND product.sku=?", coperId, sku).first();
        renderJSON(new Ret(true, copi.boxToSize(size) + ""));
    }
}
