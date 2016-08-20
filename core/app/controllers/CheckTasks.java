package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.activiti.ActivitiProcess;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.qc.CheckTask;
import models.view.Ret;
import models.view.post.CheckTaskPost;
import models.whouse.Whouse;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.jsoup.helper.StringUtil;
import play.data.binding.As;
import play.data.validation.Validation;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static play.modules.pdf.PDF.renderPDF;

/**
 * sku_check列表
 * User: cary
 * Date: 5/8/14
 * Time: 3:53 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class CheckTasks extends Controller {

    @Before(only = {"checklist", "checkerList"})
    public static void beforIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("whouses", Whouse.find("type !=?", Whouse.T.FORWARD).fetch());
        renderArgs.put("shipwhouses", Whouse.find("type =?", Whouse.T.FORWARD).fetch());
        renderArgs.put("cooperators", cooperators);

        List<User> users = User.find("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE 1=1 AND r.roleName " +
                "like ?", "%质检%").fetch();
        renderArgs.put("users", users);
    }


    @Check("checktasks.checklist")
    public static void checklist(CheckTaskPost p, int day) {
        if(p == null) {
            p = new CheckTaskPost();
            p.initDateRange(day);
        }
        List<CheckTask> tasklist = p.query();
        render(tasklist, p);
    }

    /**
     * 质检员任务列表
     */
    @Check("checktasks.checkerList")
    public static void checkerList(CheckTaskPost p, int day) {
        String username = Secure.Security.connected();
        if(p == null) {
            p = new CheckTaskPost();
            p.initDateRange(day);
        }
        List<ElcukRecord> records = ElcukRecord.find("action like '[CheckTask%' ORDER BY createAt DESC").fetch(50);
        List<CheckTask> checks = p.check();
        List<CheckTask> checkeds = p.checked();
        List<CheckTask> checkRepeats = p.checkRepeat();
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
        check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
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

    public static void showactiviti(Long id) {
        if(id == null) return;
        CheckTask check = CheckTask.findById(id);
        check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
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
    public static void update(Long id, CheckTask check, @As("yyyy-MM-dd HH:mm") Date from,
                              @As("yyyy-MM-dd HH:mm") Date to) {
        CheckTask old = CheckTask.findById(id);
        old.arryParamSetUPForQtInfo(CheckTask.FLAG.STR_TO_ARRAY);
        check.startTime = from;
        check.endTime = to;
        check.checkor = old.checkor;

        check.validateRight();
        if(Validation.hasErrors()) {
            check = old;
            render("CheckTasks/show.html", check);
        }
        check.arryParamSetUP(CheckTask.FLAG.ARRAY_TO_STR);
        old.update(check);
        flash.success("更新成功");
        redirect("/CheckTasks/show/" + id);
    }

    @Check("checktasks.update")
    public static void fullUpdate(Long id, CheckTask check, @As("yyyy-MM-dd HH:mm") Date from,
                                  @As("yyyy-MM-dd HH:mm") Date to) {
        CheckTask old = CheckTask.findById(id);
        check.startTime = from;
        check.endTime = to;
        check.checkor = old.checkor;
        check.validateRequired();
        check.validateRight();
        if(old.units == null || old.units.id == null) Validation.addError("", "没有关联的采购单！");
        if(Validation.hasErrors()) {
            check = old;
            check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
            render("CheckTasks/show.html", check);
        }
        check.arryParamSetUP(CheckTask.FLAG.ARRAY_TO_STR);
        old.fullUpdate(check, Secure.Security.connected());
        flash.success("更新成功");
        show(id);
    }


    public static void submitactiviti(CheckTask check, long checkid, long processid, @As("yyyy-MM-dd HH:mm") Date from,
                                      @As("yyyy-MM-dd HH:mm") Date to) {
        CheckTask c = CheckTask.findById(checkid);
        ActivitiProcess ap = ActivitiProcess.findById(processid);
        String taskname = ActivitiProcess.privilegeProcess(ap.processInstanceId, Secure.Security.connected());

        if(taskname.equals("采购员")) {
            c.dealway = check.dealway;
            c.planDeliveryDate = check.planDeliveryDate;
            c.workfee = check.workfee;
        } else if(taskname.equals("质检确认")) {
            //退回工厂或者到仓库返工
            //结束
            if(!(check.dealway == CheckTask.DealType.RETURN ||
                    check.dealway == CheckTask.DealType.WAREHOUSE)) {
                c.checknote = check.checknote;
                c.qty = check.qty;
                c.pickqty = check.pickqty;
                c.startTime = from;
                c.endTime = to;
                c.result = check.result;
                c.isship = check.isship;
                c.checknote = check.checknote;
                c.workers = check.workers;
                c.workhour = check.workhour;
            }
        }
        c.opition = check.opition;
        c.submitActiviti(ap, taskname, check.workfee, 1, Secure.Security.connected());

        flash.success("更新成功");
        CheckTasks.showactiviti(checkid);
    }


    /**
     * 质检管理取消费用并 回滚到采购
     *
     * @param check
     * @param checkid
     * @param processid
     * @param from
     * @param to
     */
    public static void rollactiviti(CheckTask check, long checkid, long processid, @As("yyyy-MM-dd HH:mm") Date from,
                                    @As("yyyy-MM-dd HH:mm") Date to) {
        CheckTask c = CheckTask.findById(checkid);
        ActivitiProcess ap = ActivitiProcess.findById(processid);
        String taskname = ActivitiProcess.privilegeProcess(ap.processInstanceId, Secure.Security.connected());

        //退回工厂或者到仓库返工
        //结束
        if(!(check.dealway == CheckTask.DealType.RETURN ||
                check.dealway == CheckTask.DealType.WAREHOUSE)) {
            c.checknote = check.checknote;
            c.qty = check.qty;
            c.pickqty = check.pickqty;
            c.startTime = from;
            c.endTime = to;
            c.result = check.result;
            c.isship = check.isship;
            c.checknote = check.checknote;
            c.workers = check.workers;
            c.workhour = check.workhour;
        }
        c.opition = check.opition;
        c.submitActiviti(ap, taskname, check.workfee, 2, Secure.Security.connected());

        flash.success("更新成功");
        CheckTasks.showactiviti(checkid);
    }


    /**
     * 调整运营的数据并提交流程
     *
     * @param check
     * @param processid
     * @param unitid
     * @param checkid
     * @param oldPlanQty
     * @param unit
     * @param shipmentId
     */
    public static void operateupdateprocess(CheckTask check, long processid, Long unitid, Long checkid,
                                            Integer oldPlanQty, ProcureUnit unit, String shipmentId, String msg) {

        ActivitiProcess ap = ActivitiProcess.findById(processid);
        String taskname = ActivitiProcess.privilegeProcess(ap.processInstanceId, Secure.Security.connected());

        ProcureUnit managedUnit = ProcureUnit.findById(unitid);

        if(managedUnit.stage != ProcureUnit.STAGE.CLOSE) {
            managedUnit.update(unit, shipmentId, msg);
            if(Validation.hasErrors()) {
                flash.error(Validation.errors().toString());
                unit.id = managedUnit.id;
                CheckTasks.showactiviti(checkid);
            }
        }

        CheckTask c = CheckTask.findById(checkid);
        c.opition = check.opition;
        c.submitActiviti(ap, taskname, check.workfee, 1, Secure.Security.connected());
        flash.success("更新成功");
        CheckTasks.showactiviti(checkid);
    }


    /**
     * 还原结束流程
     *
     * @param check
     * @param checkid
     */
    public static void endactiviti(CheckTask check, long checkid, long processid) {
        ActivitiProcess ap = ActivitiProcess.findById(processid);
        String taskname = ActivitiProcess.privilegeProcess(ap.processInstanceId, Secure.Security.connected());

        CheckTask c = CheckTask.findById(checkid);
        c.isship = CheckTask.ShipType.SHIP;
        c.result = CheckTask.ResultType.AGREE;
        c.opition = "[还原]" + check.opition;

        //提交流程
        c.submitActiviti(ap, taskname, check.workfee, 2, Secure.Security.connected());

        flash.success("更新成功");
        CheckTasks.showactiviti(checkid);
    }


    public static void updateactiviti(CheckTask check, long checkid, @As("yyyy-MM-dd HH:mm") Date from,
                                      @As("yyyy-MM-dd HH:mm") Date to) {
        CheckTask c = CheckTask.findById(checkid);
        c.checknote = check.checknote;
        c.planDeliveryDate = check.planDeliveryDate;
        c.qty = check.qty;
        c.pickqty = check.pickqty;
        c.startTime = from;
        c.endTime = to;
        c.result = check.result;
        c.isship = check.isship;
        c.checknote = check.checknote;
        c.workers = check.workers;
        c.workhour = check.workhour;

        c.save();
        flash.success("更新成功");
        CheckTasks.showactiviti(checkid);
    }

    public static void prints(Long id) {
        CheckTask check = CheckTask.findById(id);
        check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
        check.fetchSkucheck();
        render(check);
    }

    public static void addRequireAndWay(String require, String way, Long id) {
        if(StringUtil.isBlank(require)) {
            require = " ";
        }
        if(StringUtil.isBlank(way)) {
            way = " ";
        }
        CheckTask check = CheckTask.findById(id);
        check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
        check.qcRequire.add(require);
        check.qcWay.add(way);
        check.arryParamSetUP(CheckTask.FLAG.ARRAY_TO_STR);
        check.save();
        flash.success("添加成功!");
        prints(id);
    }

    public static void doPrints(Long id) {
        CheckTask check = CheckTask.findById(id);
        check.arryParamSetUP(CheckTask.FLAG.STR_TO_ARRAY);
        check.printNumber++;
        check.save();
        check.fetchSkucheck();
        //log
        new ERecordBuilder("checktask.doPrints").fid(id).msgArgs(id).save();
        renderArgs.put("check", check);
        final PDF.Options options = new PDF.Options();
        options.pageSize = IHtmlToPdfTransformer.A4P;
        renderPDF(options);
    }

    /**
     * 质检信息查看列表
     * <p/>
     * 当采购计划ID下存在1个以上的质检任务
     */
    public static void showList(Long id) {
        List<CheckTask> checks = CheckTask.find("units_id=?", id).fetch();
        render(checks);
    }

    /**
     * 允许质检任务重新编辑
     */
    @Check("checktasks.resetedit")
    public static void resetEdit(Long id) {
        try {
            ActivitiProcess p = ActivitiProcess.find("objectid=? and definition.menuCode='checktasks.fullupdate'",
                    id).first();
            if(p != null) {
                renderJSON(new Ret(false, "已经在不发货流程处理中."));
            }

            CheckTask check = CheckTask.findById(id);
            check.checkstat = CheckTask.StatType.UNCHECK;
            check.finishStat = CheckTask.ConfirmType.UNCONFIRM;
            check.save();
            renderJSON(new Ret(true, "操作成功."));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}

