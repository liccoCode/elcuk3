package controllers;

import helper.Webs;
import models.User;
import models.procure.Deliveryment;
import models.view.Ret;
import models.view.post.DeliveryPost;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;

import java.util.List;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Deliveryments extends Controller {

    public static void index(DeliveryPost p) {
        List<Deliveryment> deliveryments = null;
        if(p == null) {
            p = new DeliveryPost();
            deliveryments = Deliveryment.openDeliveryments();
        } else {
            deliveryments = p.query();
        }
        render(deliveryments, p);
    }

    public static void show(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
        render(dmt);
    }

    public static void update(Deliveryment dmt) {
        validation.valid(dmt);
        Validation.valid("d", "sdf");
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        dmt.save();
        flash.success("更新成功.");
        redirect("/Deliveryments/show/" + dmt.id);
    }

    /**
     * 向 Deliveryment 添加 ProcureUnit
     *
     * @param pids
     * @param dmt
     */
    public static void addunits(List<Long> pids, Deliveryment dmt) {
        Validation.required("deliveryments.addunits", pids);
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        dmt.assignUnitToDeliveryment(pids);

        // 再一次检查, 是否有错误
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }

        redirect("/Deliveryments/show/" + dmt.id);
    }

    /**
     * 将 ProcureUnit 从 Deliveryment 中解除
     *
     * @param pids
     * @param dmt
     */
    public static void delunits(List<Long> pids, Deliveryment dmt) {
        Validation.required("deliveryments.delunits", pids);
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        dmt.unAssignUnitInDeliveryment(pids);

        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        redirect("/Deliveryments/show/" + dmt.id);
    }

    /**
     * 取消采购单
     */
    public static void cancel(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        dmt.cancel();
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        redirect("/Deliveryments/show/" + dmt.id);
    }
}
