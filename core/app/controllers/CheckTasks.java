package controllers;

import models.ElcukRecord;
import models.activiti.ActivitiProcess;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.product.Whouse;
import models.qc.CheckTask;
import models.view.Ret;
import models.view.post.CheckTaskPost;
import org.joda.time.DateTime;
import play.data.binding.As;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * sku_check列表
 * User: cary
 * Date: 5/8/14
 * Time: 3:53 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class CheckTasks extends Controller {

    @Before(only = {"checklist", "checkerList"})
    public static void beforIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("whouses", Whouse.find("type !=?", Whouse.T.FORWARD).fetch());
        renderArgs.put("shipwhouses", Whouse.find("type =?", Whouse.T.FORWARD).fetch());
        renderArgs.put("cooperators", cooperators);
    }


    @Check("checktasks.checklist")
    public static void checklist(CheckTaskPost p, int day) {
        CheckTask.generateTask();
        List<CheckTask> tasklist = null;
        if(p == null) p = new CheckTaskPost();
        if(day == 3) {
            p.from = DateTime.now().minusDays(3).toDate();
            p.to = new Date();
        } else if(day == 2) {
            p.from = DateTime.now().minusDays(2).toDate();
            p.to = new Date();
        } else if(day == 1) {
            p.from = DateTime.now().minusDays(1).toDate();
            p.to = new Date();
        }
        tasklist = p.query();
        render(tasklist, p);
    }

    /**
     * 质检员任务列表
     */
    @Check("checktasks.checkerList")
    public static void checkerList(CheckTaskPost p, int day) {
        CheckTask.generateTask();
        String username = Secure.Security.connected();
        if(p == null) p = new CheckTaskPost(username);
        if(day == 3) {
            p.from = DateTime.now().minusDays(3).toDate();
            p.to = new Date();
        } else if(day == 2) {
            p.from = DateTime.now().minusDays(2).toDate();
            p.to = new Date();
        } else if(day == 1) {
            p.from = DateTime.now().minusDays(1).toDate();
            p.to = new Date();
        }
        List<CheckTask> checks = p.check();
        List<CheckTask> checkeds = p.checked();
        List<CheckTask> checkRepeats = p.checkRepeat();
        List<ElcukRecord> records = ElcukRecord.records("qcCheckRecords");

        render(p, checks, checkeds, checkRepeats, records);
    }

    /**
     * 更新质检方式
     */
    public static void updateQcType(long id, CheckTask.T qcType) {
        CheckTask check = CheckTask.findById(id);
        check.qcType = qcType;
        check.save();
        renderJSON(new Ret());
    }

    public static void show(Long id) {
        CheckTask check = CheckTask.findById(id);
        Map<String, Object> map = check.showInfo(id, Secure.Security.connected());

        ActivitiProcess ap = (ActivitiProcess) map.get("ap");
        int issubmit = (Integer) map.get("issubmit");
        String taskname = (String) map.get("taskname");
        int oldPlanQty = (Integer) map.get("oldPlanQty");
        List<Whouse> whouses = null;
        Object temp = map.get("whouses");
        if(temp != null) {
            whouses = (List<Whouse>) map.get("whouses");
        }
        ProcureUnit unit = null;
        temp = map.get("whouses");
        if(temp != null) {
            unit = (ProcureUnit) map.get("unit");
        }
        Date oldplanDeliveryDate = null;
        temp = map.get("oldplanDeliveryDate");
        if(temp != null) {
            oldplanDeliveryDate = (Date) map.get("oldplanDeliveryDate");
        }
        List<Map<String, String>> infos = (List<Map<String, String>>) map.get("infos");

        render(check, ap, issubmit, taskname, infos, unit, oldPlanQty, whouses, oldplanDeliveryDate);
    }

    public static void showActiviti(Long id) {
        if(id == null) return;
        CheckTask check = CheckTask.findById(id);
        Map<String, Object> map = check.showInfo(id, Secure.Security.connected());

        ActivitiProcess ap = (ActivitiProcess) map.get("ap");
        int issubmit = (Integer) map.get("issubmit");
        String taskname = "";
        Object temp = map.get("taskname");
        if(temp != null) {
            taskname = (String) temp;
        }

        int oldPlanQty = (Integer) map.get("oldPlanQty");
        List<Whouse> whouses = null;
        temp = map.get("whouses");
        if(temp != null) {
            whouses = (List<Whouse>) temp;
        }
        ProcureUnit unit = null;
        temp = map.get("unit");
        if(temp != null) {
            unit = (ProcureUnit) temp;
        }
        Date oldplanDeliveryDate = null;
        temp = map.get("oldplanDeliveryDate");
        if(temp != null) {
            oldplanDeliveryDate = (Date) temp;
        }
        List<Map<String, String>> infos = (List<Map<String, String>>) map.get("infos");

        render(check, ap, issubmit, taskname, infos, unit, oldPlanQty, whouses, oldplanDeliveryDate);
    }

    @Check("checktasks.update")
    public static void update(CheckTask check) {
        check.validateRight();
        if(Validation.hasErrors()) render("CheckTasks/show.html", check);
        check.save();
        flash.success("更新成功");
        redirect("/CheckTasks/show/" + check.id);
    }

    @Check("checktasks.update")
    public static void fullUpdate(CheckTask check, @As("yyyy-MM-dd HH:mm") Date from, @As("yyyy-MM-dd HH:mm") Date to) {
        check.startTime = from;
        check.endTime = to;
        validation.valid(check);
        check.validateRequired();
        check.validateRight();
        if(Validation.hasErrors()) render("CheckTasks/show.html", check);
        check.fullSave(Secure.Security.connected());
        flash.success("更新成功");
        show(check.id);
    }


    public static void submitactiviti(CheckTask check, long id, int acttype) {
        CheckTask c = CheckTask.findById(check.id);
        //2为运营,不执行保存
        if(acttype != 2) {
            c.dealway = check.dealway;
            c.workfee = check.workfee;
            c.checknote = check.checknote;
            c.planDeliveryDate = check.planDeliveryDate;
            c.qty = check.qty;
            c.pickqty = check.pickqty;
            c.endTime = check.endTime;
            c.startTime = check.startTime;
            c.result = check.result;
            c.isship = check.isship;
            c.checknote = check.checknote;
            c.save();
        }

        //提交流程
        c.submitActiviti(1, id, Secure.Security.connected());

        flash.success("更新成功");
        CheckTasks.showActiviti(check.id);
    }

    public static void endactiviti(CheckTask check, long id) {
        CheckTask c = CheckTask.findById(check.id);
        c.dealway = check.dealway;
        c.workfee = check.workfee;
        c.checknote = check.checknote;
        c.planDeliveryDate = check.planDeliveryDate;
        c.qty = check.qty;
        c.pickqty = check.pickqty;
        c.endTime = check.endTime;
        c.startTime = check.startTime;
        c.result = check.result;
        c.isship = check.isship;
        c.checknote = check.checknote;
        c.save();

        //提交流程
        c.submitActiviti(2, id, Secure.Security.connected());

        flash.success("更新成功");
        CheckTasks.showActiviti(check.id);
    }


    public static void updateactiviti(CheckTask check) {
        check.save();
        flash.success("更新成功");
        CheckTasks.showActiviti(check.id);
    }
}

