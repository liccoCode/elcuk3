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
import java.util.Arrays;
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
        renderArgs.put("records", ElcukRecord.records(deliverymentId));
    }

    @Before(only = {"index", "indexWhouse"})
    public static void beforeIndex(DeliveryPost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        renderArgs.put("suppliers", suppliers);
    }

    /**
     * 从 Procrues#index 页面, 通过选择 ProcureUnit 创建 出货单
     * TODO effect: 需要调整权限
     */
    @Check("procures.createdeliveryment")
    public static void deliverplan(List<Long> pids, String deliverName) {
        if(StringUtils.isBlank(deliverName))
            Validation.addError("", "出货单名称必须填写!");
        if(pids == null || pids.size() <= 0)
            Validation.addError("", "必须选择采购计划单!");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcureUnits.index(new ProcurePost(ProcureUnit.STAGE.PLAN));
        }
        DeliverPlan deliverplan = DeliverPlan
                .createFromProcures(pids, deliverName, User.findByUserName(Secure.Security.connected()));
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcureUnits.index(new ProcurePost(ProcureUnit.STAGE.PLAN));
        }
        flash.success("出货单 %s 创建成功.", pids.toString());
        DeliverPlans.show(deliverplan.id);
    }

    public static void show(String id) {
        DeliverPlan dp = DeliverPlan.findById(id);
        boolean showAdd = false;
        if(dp.units.stream().anyMatch(unit -> Arrays.asList(ProcureUnit.STAGE.PLAN, ProcureUnit.STAGE.DELIVERY)
                .contains(unit.stage) && unit.attrs.planQty != 0)) {
            showAdd = true;
        }
        render(dp, showAdd);
    }

    @Check("deliverplans.index")
    public static void index(DeliverPlanPost p, List<String> deliverplanIds) {
        List<DeliverPlan> deliverplans;
        if(deliverplanIds == null) deliverplanIds = new ArrayList<>();
        if(p == null) p = new DeliverPlanPost();
        deliverplans = p.query();
        render(deliverplans, p, deliverplanIds);
    }

    public static void indexWhouse(DeliverPlanPost p) {
        if(p == null) p = new DeliverPlanPost();
        List<DeliverPlan> plans = p.query();
        render(p, plans);
    }


    public static void update(DeliverPlan dp) {
        try {
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
        Validation.required("deliverplans.addunits", pids);
        if(Validation.hasErrors())
            render("DeliverPlans/show.html", dp);
        dp.assignUnitToDeliverplan(pids);

        // 再一次检查, 是否有错误
        if(Validation.hasErrors()) {
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
        notFoundIfNull(dp);

        Validation.required("deliverplans.delunits", pids);
        if(Validation.hasErrors()) render("DeliverPlans/show.html", dp);
        dp.unassignUnitToDeliverplan(pids);
        if(Validation.hasErrors()) render("DeliverPlans/show.html", dp);

        flash.success("成功将 %s 采购计划从出货单 %s 中移除.", StringUtils.join(pids, ","), id);
        if(dp.units.isEmpty()) {
            dp.delete();
            index(null, null);
        } else {
            show(dp.id);
        }
    }
}
