package controllers;

import helper.Webs;
import models.ElcukRecord;
import models.embedded.UnitAttrs;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.product.Whouse;
import models.view.Ret;
import models.view.post.ProcurePost;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
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


    /**
     * 某一个 ProcureUnit 交货
     */
    public static void deliveryUnit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        List<ElcukRecord> records = ElcukRecord.records(unit.id + "", Messages.get("procureunit.delivery"));
        renderArgs.put("attrs", unit.attrs);
        render(unit, records);
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

    /**
     * 选择供应商
     *
     * @param coperId
     * @param sku
     * @param size
     */
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
