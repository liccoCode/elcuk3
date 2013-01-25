package controllers;

import helper.Dates;
import helper.Webs;
import models.ElcukRecord;
import models.Notification;
import models.User;
import models.embedded.UnitAttrs;
import models.procure.*;
import models.product.Whouse;
import models.view.Ret;
import models.view.post.ProcurePost;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/5/12
 * Time: 9:53 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Procures extends Controller {
    @Before(only = {"blank", "save", "edit", "update"}, priority = 1)
    public static void whouses() {
        renderArgs.put("whouses", Whouse.<Whouse>findAll());
    }

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
        List<ProcureUnit> units = p.query();
        render(p, units);
    }

    public static void blank(ProcureUnit unit) {
        if(unit == null || unit.selling == null) {
            flash.error("请通过 SellingId 进行, 没有执行合法的 SellingId 无法创建 ProcureUnit!");
            render(unit);
        }
        render(unit);
    }

    public static void save(ProcureUnit unit, String shipmentId) {
        unit.handler = User.findByUserName(Secure.Security.connected());
        unit.validate();
        if(Validation.hasErrors()) {
            render("Procures/blank.html", unit);
        }
        unit.save();
        if(StringUtils.isNotBlank(shipmentId)) {
            Shipment ship = Shipment.findById(shipmentId);
            ship.addToShip(Arrays.asList(unit.id));
            flash.success("创建成功, 并且采购计划同时被指派到运输单 %s", shipmentId);
        } else {
            flash.success("创建成功");
        }
        new ElcukRecord(Messages.get("procureunit.save"),
                Messages.get("action.base", unit.to_log()), unit.id + "").save();
        redirect("/Shipments/show/" + shipmentId);
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

    public static void edit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        int oldPlanQty = unit.attrs.planQty;
        render(unit, oldPlanQty);
    }

    public static void update(ProcureUnit unit, int oldPlanQty, String shipmentId) {
        validation.required(oldPlanQty);
        unit.validate();
        if(Validation.hasErrors()) {
            render("Procures/edit.html", unit, oldPlanQty);
        }
        unit.updateWithShipment(Shipment.<Shipment>findById(shipmentId));
        new ElcukRecord(Messages.get("procureunit.update"),
                Messages.get("action.base", unit.to_log()), unit.id + "").save();
        if(oldPlanQty != unit.attrs.planQty) {
            String shipment_id = "";
            if(unit.shipItem != null)
                shipment_id = unit.shipItem.shipment.id;
            Notification.notifies(String.format("采购计划 #%s(%s) 变更", unit.id, unit.sku),
                    String.format("计划采购量从 %s 变更为 %s, 预计交货日期: %s, 请检查相关采购单,运输单 %s",
                            oldPlanQty, unit.attrs.planQty,
                            Dates.date2Date(unit.attrs.planDeliveryDate), shipment_id),
                    Notification.PROCURE, Notification.SHIPPER);
        }
        flash.success("ProcureUnit %s update success!", unit.id);
        redirect("/procures/index?p.search=id:" + unit.id);
    }

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
     * 从 Procrues#index 页面, 通过选择 ProcureUnit 创建 Deliveryment
     *
     * @param pids
     * @param name
     */
    @Check("procures.createdeliveryment")
    public static void createDeliveryment(List<Long> pids, String name) {
        Validation.required("procrues.createDeliveryment.name", name);
        Validation.required("deliveryments.addunits", pids);
        if(Validation.hasErrors()) {
            ProcurePost p = new ProcurePost(ProcureUnit.STAGE.PLAN);
            renderArgs.put("units", p.query());
            renderArgs.put("p", p);
            render("Procures/index.html", name);
        }
        Deliveryment deliveryment = Deliveryment
                .createFromProcures(pids, name, User.findByUserName(Secure.Security.connected()));
        if(Validation.hasErrors()) {
            ProcurePost p = new ProcurePost(ProcureUnit.STAGE.PLAN);
            renderArgs.put("units", p.query());
            renderArgs.put("p", p);
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

        redirect("/Deliveryments/show/" + unit.deliveryment.id);
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
        newUnit.handler = User.findByUserName(User.username());
        unit.split(newUnit);
        if(Validation.hasErrors()) render("Procures/splitUnit.html", unit, newUnit);
        if(unit.isHaveCycleShipment())
            flash.success("分拆成功, 并且成功保留对应的周期型运输单.");
        else
            flash.success("分拆成功, 并不处于周期型运输单中, 进入采购计划池中.");
        redirect("/Deliveryments/show/" + unit.deliveryment.id);
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
