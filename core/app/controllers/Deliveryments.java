package controllers;

import helper.J;
import helper.Webs;
import models.User;
import models.procure.Deliveryment;
import models.procure.Payment;
import models.view.Ret;
import models.view.post.DeliveryPost;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Router;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Deliveryments extends Controller {

    /**
     * 从 Procrues#index 页面, 通过选择 ProcureUnit 创建 Deliveryment
     *
     * @param pids
     * @param name
     */
    public static void save(List<Long> pids, String name) {
        validation.required(name);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.V(Validation.errors())));
        }
        Deliveryment deliveryment = Deliveryment.createFromProcures(pids, name, User.findByUserName(Secure.Security.connected()));
        //TODO 修改为 Deliveryments.show 来查看具体的 Deliveryment
        flash.success("Deliveryment <a href='%s'>%s</a> 创建成功.", Router.getFullUrl("Procures.index"), deliveryment.id);
        renderJSON(new Ret(true, Router.getFullUrl("Procures.index")));
    }

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
        renderArgs.put("plan_units", dmt.unbindInPlanStageProcureUnits());
        render(dmt);
    }

    public static void update(Deliveryment dmt) {
        validation.valid(dmt);
        Validation.valid("d", "sdf");
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.unbindInPlanStageProcureUnits());
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
            renderArgs.put("plan_units", dmt.unbindInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        dmt.assignUnitToDeliveryment(pids);

        // 再一次检查, 是否有错误
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.unbindInPlanStageProcureUnits());
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
            renderArgs.put("plan_units", dmt.unbindInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        dmt.unAssignUnitInDeliveryment(pids);

        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.unbindInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        redirect("/Deliveryments/show/" + dmt.id);
    }
}
