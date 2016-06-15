package controllers;


import controllers.api.SystemOperation;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.procure.Cooperator;
import models.procure.DeliverPlan;
import models.procure.ProcureUnit;
import models.view.Ret;
import models.view.post.DeliverPlanPost;
import models.view.post.DeliveryPost;
import models.view.post.ProcurePost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 16-1-21
 * Time: 上午10:40
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class DeliverPlans extends Controller {


    @Before(only = {"show", "update", "addunits", "delunits"})
    public static void showPageSetUp() {
        String deliverymentId = request.params.get("id");
        if(StringUtils.isBlank(deliverymentId)) deliverymentId = request.params.get("dp.id");
        DeliverPlan dmt = DeliverPlan.findById(deliverymentId);
        if(dmt != null)
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
        renderArgs.put("cooperators", Cooperator.suppliers());
        renderArgs.put("records", ElcukRecord.records(deliverymentId));
    }

    @Before(only = {"index"})
    public static void beforeIndex(DeliveryPost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        renderArgs.put("suppliers", suppliers);
    }

    @Check("procures.createdeliveryment")
    public static void deliverplan(List<Long> pids) {
        if(pids == null || pids.size() <= 0)
            Validation.addError("", "必须选择采购计划!");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcureUnits.index(new ProcurePost(ProcureUnit.STAGE.DELIVERY));
        }
        DeliverPlan deliverplan = DeliverPlan
                .createFromProcures(pids, User.findByUserName(Secure.Security.connected()));
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcureUnits.index(new ProcurePost(ProcureUnit.STAGE.DELIVERY));
        }
        flash.success("出货单 %s 创建成功.", pids.toString());
        DeliverPlans.show(deliverplan.id);
    }

    public static void show(String id) {
        DeliverPlan dp = DeliverPlan.findById(id);
        render(dp);
    }

    @Check("deliverplans.index")
    public static void index(DeliverPlanPost p, List<String> deliverplanIds) {
        List<DeliverPlan> deliverplans = null;
        if(deliverplanIds == null) deliverplanIds = new ArrayList<>();
        if(p == null) p = new DeliverPlanPost();
        deliverplans = p.query();
        List<String> handlers = DeliverPlan.handlers();
        render(deliverplans, p, deliverplanIds, handlers);
    }


    public static void update(DeliverPlan dp) {
        try {
            Validation.required("供应商", dp.cooperator);
            Validation.required("出货单名称", dp.name);
            validation.valid(dp);
            if(Validation.hasErrors())
                renderJSON(new Ret(Validation.errors().toString()));
            dp.save();
            renderJSON(new Ret(true, ""));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }


    /**
     * 向 Deliverplan 添加 ProcureUnit
     */
    public static void addunits(String id, List<Long> pids) {
        DeliverPlan dp = DeliverPlan.findById(id);
        if(dp.isLocked()) Validation.addError("", "出库单已经确认,不允许再添加采购计划!");
        Validation.required("deliverplans.addunits", pids);
        if(Validation.hasErrors())
            render("DeliverPlans/show.html", dp);
        dp.assignUnitToDeliverplan(pids);

        // 再一次检查, 是否有错误
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dp.availableInPlanStageProcureUnits());
            render("DeliverPlans/show.html", dp);
        }
        flash.success("成功将 %s 采购计划添加到当前采购单.", StringUtils.join(pids, ","));
        show(dp.id);
    }

    /**
     * 将 ProcureUnit 从 Deliverplan 中解除
     */
    public static void delunits(String id, List<Long> pids) {
        DeliverPlan dp = DeliverPlan.findById(id);
        if(dp.isLocked()) Validation.addError("", "出库单已经确认,不允许再解除采购计划!");
        Validation.required("deliverplans.delunits", pids);
        if(Validation.hasErrors())
            render("DeliverPlans/show.html", dp);
        dp.unassignUnitToDeliverplan(pids);

        if(Validation.hasErrors())
            render("DeliverPlans/show.html", dp);

        flash.success("成功将 %s 采购计划从当前采购单中移除.", StringUtils.join(pids, ","));
        show(dp.id);
    }

    /**
     * 确认发货
     *
     * @param ids
     */
    public static void triggerReceiveRecords(List<String> ids) {
        if(ids != null && !ids.isEmpty()) {
            for(String id : ids) {
                DeliverPlan deliverPlan = DeliverPlan.findById(id);
                if(deliverPlan == null || deliverPlan.isLocked()) continue;
                deliverPlan.triggerReceiveRecords();
            }
        }
        flash.success("确认发货成功!");
        redirect("/ReceiveRecords/index");
    }
}
