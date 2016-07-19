package controllers;

import controllers.api.SystemOperation;
import models.ElcukRecord;
import models.User;
import models.procure.Shipment;
import models.view.post.ShipPlanPost;
import models.whouse.ShipPlan;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 出货计划控制器
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 7/4/16
 * Time: 3:40 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class ShipPlans extends Controller {
    @Before(only = {"index", "blank", "create"})
    public static void beforeArgs() {
        renderArgs.put("whouses", Whouse.find("type=?", Whouse.T.FBA).fetch());
        renderArgs.put("logs", ElcukRecord.records(
                Arrays.asList("shipplan.save", "shipplan.update", "shipplan.remove", "shipplan.delivery"),
                50));
    }

    public static void index(ShipPlanPost p) {
        List<ShipPlan> plans = p.query();
        render(plans);
    }

    public static void blank(String sid) {
        ShipPlan plan = new ShipPlan(sid);
        render(plan);
    }

    public static void create(ShipPlan plan, String shipmentId) {
        plan.creator = User.findByUserName(Secure.Security.connected());
        plan.createDate = new Date();
        plan.state = ShipPlan.S.Pending;
        if(plan.shipType == Shipment.T.EXPRESS && StringUtils.isNotBlank(shipmentId)) {
            Validation.addError("", "快递运输方式, 不需要指定运输单");
        }
        plan.valid();
        if(Validation.hasErrors()) {
            render("ShipPlans/blank.html", plan);
        }
        plan.doCreate();
        flash.success("创建成功!");
        redirect("ShipPlans/index");
    }

    public static void batchCreateFBA(ShipPlanPost p, List<Long> pids, String redirectTarget) {
        if(pids != null && pids.size() > 0) {
            ShipPlan.postFbaShipments(pids);
        }
        if(StringUtils.isNotBlank(redirectTarget)) {
            redirect(redirectTarget);//如果需要参数请自行加到地址中去
        }
        redirect("ShipPlans/index");
    }
}
