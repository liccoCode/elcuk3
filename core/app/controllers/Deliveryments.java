package controllers;

import models.ElcukRecord;
import models.procure.Deliveryment;
import models.view.post.DeliveryPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
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

    @Before(only = {"show", "update", "addunits", "delunits", "cancel", "confirm"})
    public static void showPageSetUp() {
        String deliverymentId = request.params.get("id");
        if(StringUtils.isBlank(deliverymentId)) deliverymentId = request.params.get("dmt.id");
        Deliveryment dmt = Deliveryment.findById(deliverymentId);
        renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
        renderArgs.put("records", ElcukRecord.records(deliverymentId));
    }

    public static void index(DeliveryPost p) {
        List<Deliveryment> deliveryments = null;
        if(p == null) {
            p = new DeliveryPost();
            deliveryments = Deliveryment.openDeliveryments(Deliveryment.S.PENDING);
        } else {
            deliveryments = p.query();
        }
        render(deliveryments, p);
    }

    public static void show(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        render(dmt);
    }

    public static void update(Deliveryment dmt) {
        validation.valid(dmt);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
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
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
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
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
        dmt.unAssignUnitInDeliveryment(pids);

        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);

        redirect("/Deliveryments/show/" + dmt.id);
    }

    /**
     * 确认采购单, 这样才能进入运输单进行挑选
     */
    public static void confirm(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        Validation.equals("deliveryments.confirm", dmt.state, "", Deliveryment.S.PENDING);
        if(Validation.hasErrors()) render("Deliveryments/show.html", dmt);

        dmt.state = Deliveryment.S.CONFIRM;
        dmt.save();
        redirect("/Deliveryments/show/" + id);
    }

    /**
     * 取消采购单
     */
    public static void cancel(String id, String msg) {
        Validation.required("deliveryments.cancel", msg);
        Deliveryment dmt = Deliveryment.findById(id);
        dmt.cancel(msg);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt, msg);

        redirect("/Deliveryments/show/" + dmt.id);
    }
}
