package controllers;

import controllers.api.SystemOperation;
import helper.Constant;
import helper.Dates;
import helper.Reflects;
import models.ElcukRecord;
import models.market.Selling;
import models.procure.Cooperator;
import models.procure.Shipment;
import models.qc.CheckTaskDTO;
import models.view.post.ShipPlanPost;
import models.whouse.ShipPlan;
import models.whouse.Whouse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.libs.Files;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 出库计划控制器
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 7/4/16
 * Time: 3:40 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class ShipPlans extends Controller {
    @Before(only = {"index", "blank", "create"})
    public static void beforeIndexLogs() {
        renderArgs.put("logs", ElcukRecord.records(
                Arrays.asList("shipplan.save", "shipplan.update", "shipplan.remove", "shipplan.delivery"),
                50));
    }

    @Before(only = {"index", "create", "show", "update"})
    public static void beforeArgs() {
        renderArgs.put("whouses", Whouse.find("type=?", Whouse.T.FBA).fetch());
    }

    @Before(only = {"show", "update"})
    public static void beforeUpdateLogs() {
        String id = request.params.get("id");
        if(StringUtils.isNotBlank(id)) {
            renderArgs.put("logs", ElcukRecord.records(id));
        }
    }

    public static void index(ShipPlanPost p) {
        if(p == null) p = new ShipPlanPost();
        List<ShipPlan> plans = p.query();
        render(plans, p);
    }

    public static void blank(String sid) {
        ShipPlan plan = null;
        List<Whouse> whouses = null;
        if(StringUtils.isNotBlank(sid)) {
            Selling selling = Selling.findById(sid);
            plan = new ShipPlan(selling);
            whouses = Whouse.findByAccount(plan.selling.account);
        } else {
            plan = new ShipPlan();
            whouses = Whouse.findByType(Whouse.T.FBA);
        }
        render(plan, whouses);
    }

    public static void create(ShipPlan plan, String shipmentId) {
        if(plan.shipType == Shipment.T.EXPRESS && StringUtils.isNotBlank(shipmentId)) {
            Validation.addError("", "快递运输方式, 不需要指定运输单");
        }
        try {
            plan.createAndOutbound(shipmentId);
        } catch(FastRuntimeException e) {
            plan.remove();
            render("ShipPlans/blank.html", plan);
        }
        new ElcukRecord(Messages.get("shipplan.save"),
                Messages.get("action.base", plan.to_log()),
                plan.id + "").save();
        flash.success("创建成功!");
        redirect("/ShipPlans/index");
    }

    public static void show(Long id) {
        ShipPlan plan = ShipPlan.findById(id);
        render(plan);
    }

    public static void update(Long id, ShipPlan plan, String shipmentId) {
        ShipPlan manager = ShipPlan.findById(id);
        manager.update(plan, shipmentId);
        if(Validation.hasErrors()) {
            plan.id = manager.id;
            render("ShipPlans/show.html", plan);
        }
        flash.success("成功修改出库计划!", id);
        redirect("/ShipPlans/index");
    }

    /**
     * 批量创建 FBA
     *
     * @param p
     * @param pids
     * @param redirectTarget
     */
    public static void batchCreateFBA(ShipPlanPost p, List<Long> pids, String redirectTarget, List<CheckTaskDTO> dtos) {
        if(pids != null && pids.size() > 0) {
            ShipPlan.postFbaShipments(pids, dtos);
        }
        if(StringUtils.isNotBlank(redirectTarget)) {
            redirect(redirectTarget);//如果需要参数请自行加到地址中去
        }
        index(p);
    }

    /**
     * 批量下载 FBA zip
     *
     * @param pids
     * @param boxNumbers
     * @throws Exception
     */
    public static void downloadFBAZIP(List<Long> pids, List<Long> boxNumbers) throws Exception {
        List<ShipPlan> plans = ShipPlan.find("id IN ?", SqlSelect.inlineParam(pids)).fetch();
        if(plans == null || plans.isEmpty()) renderText("没有数据无法生成zip文件！");
        if(pids.size() != boxNumbers.size()) renderText("出库计划 ID 与 箱数的 Size 不一致!");

        synchronized(ShipPlans.class) {//加类锁
            //创建FBA根目录，存放工厂FBA文件
            File dirfile = new File(Constant.TMP, "FBA");
            try {
                Files.delete(dirfile);
                dirfile.mkdir();
                for(int i = 0; i < plans.size(); i++) {
                    ShipPlan plan = plans.get(i);
                    String name = plan.selling.sellingId;
                    String date = Dates.date2Date(plan.planShipDate);
                    File folder = new File(dirfile, String.format("%s-%s-FBA", date, name));
                    folder.mkdir();
                    //生成 PDF
                    plan.fbaAsPDF(folder, boxNumbers.get(i));
                }
                FileUtils.writeStringToFile(new File(dirfile, "出库计划 ID.txt"),
                        java.net.URLDecoder.decode(StringUtils.join(pids, "_"), "UTF-8"), "UTF-8");
            } finally {
                File zip = new File(Constant.TMP + "/FBA.zip");
                Files.zip(dirfile, zip);
                zip.deleteOnExit();
                renderBinary(zip);
            }
        }
    }
}
