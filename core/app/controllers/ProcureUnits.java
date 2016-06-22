package controllers;

import controllers.api.SystemOperation;
import exception.PaymentException;
import helper.*;
import models.ElcukRecord;
import models.User;
import models.activiti.ActivitiProcess;
import models.embedded.UnitAttrs;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.market.Selling;
import models.procure.Cooperator;
import models.procure.DeliverPlan;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Product;
import models.qc.CheckTask;
import models.view.Ret;
import models.view.post.AnalyzePost;
import models.view.post.ProcurePost;
import models.whouse.Whouse;
import models.whouse.WhouseItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Files;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/26/13
 * Time: 5:56 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class ProcureUnits extends Controller {

    @Before(only = {"index", "indexForMarket"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("whouses", Whouse.find("type=?", Whouse.T.FBA).fetch());
        renderArgs.put("logs", ElcukRecord.records(
                Arrays.asList("procureunit.save", "procureunit.update", "procureunit.remove", "procureunit.delivery",
                        "procureunit.revertdelivery", "procureunit.split", "procureunit.prepay",
                        "procureunit.tailpay"), 50));
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("categories", User.getTeamCategorys(User.current()));
    }

    @Before(only = {"index"})
    public static void beforeCooperatorJson() {
        String suppliersJson = J.json(Cooperator.supplierNames());
        renderArgs.put("suppliersJson", suppliersJson);
    }

    @Before(only = {"edit", "update"})
    public static void beforeLog(Long id) {
        List<ElcukRecord> logs = ElcukRecord
                .records(id.toString(), Arrays.asList("procureunit.update", "procureunit.deepUpdate"));
        renderArgs.put("logs", logs);
    }

    @Check("procures.index")
    public static void index(ProcurePost p) {
        if(p == null) p = new ProcurePost();
        List<ProcureUnit> units = p.query();
        render(p, units);
    }

    public static void indexForMarket(ProcurePost p) {
        if(p == null) p = new ProcurePost();
        List<ProcureUnit> units = p.query();
        render(p, units);
    }

    /**
     * 将搜索结果 打成ZIP包，进行下载
     */
    public static synchronized void downloadFBAZIP(ProcurePost p, List<Long> boxNumbers) throws Exception {
        List<ProcureUnit> procureUnitsList = p.query();
        if(procureUnitsList != null && procureUnitsList.size() != 0) {
            //创建FBA根目录，存放工厂FBA文件
            File dirfile = new File(Constant.TMP, "FBA");
            try {
                Files.delete(dirfile);
                dirfile.mkdir();
                for(int i = 0; i < procureUnitsList.size(); i++) {
                    ProcureUnit procureUnit = procureUnitsList.get(i);

                    String name = procureUnit.cooperator.name;
                    String date = Dates.date2Date(procureUnit.attrs.planDeliveryDate);
                    //生成工厂的文件夹. 格式：采购单ID-预计交货日期-工厂名称
                    File factoryDir = new File(dirfile, String.format("%s-%s-出货FBA", date, name));
                    factoryDir.mkdir();
                    //生成 PDF
                    procureUnit.fbaAsPDF(factoryDir, boxNumbers.get(i));
                }
                FileUtils.writeStringToFile(new File(dirfile, "采购计划ID列表.txt"),
                        java.net.URLDecoder.decode(p.unitIds, "UTF-8"), "UTF-8");
            } finally {
                File zip = new File(Constant.TMP + "/FBA.zip");
                Files.zip(dirfile, zip);
                zip.deleteOnExit();
                renderBinary(zip);
            }
        } else {
            renderText("没有数据无法生成zip文件！");
        }
    }

    /**
     * 明天 后天 大后天 计划视图
     */
    public static void planView(Date date) {
        ProcurePost p = new ProcurePost(ProcureUnit.STAGE.DELIVERY);
        p.dateType = "attrs.planDeliveryDate";
        p.from = date;
        p.to = date;
        ProcureUnits.index(p);
    }

    /**
     * 发货时间为当天, 同时货物还没有抵达货代的采购计划
     */
    public static void noPlaced() {
        ProcurePost p = new ProcurePost();
        p.dateType = "attrs.planArrivDate";
        p.from = new Date();
        p.to = new Date();
        p.isPlaced = ProcurePost.PLACEDSTATE.NOARRIVE;
        ProcureUnits.index(p);
    }


    public static void blank(String sid) {
        ProcureUnit unit = new ProcureUnit();
        List<Whouse> whouses = new ArrayList<>();
        if(StringUtils.isNotBlank(sid)) {
            unit.selling = Selling.findById(sid);
            whouses = Whouse.findByAccount(unit.selling.account);
        } else {
            whouses = Whouse.findByType(Whouse.T.FBA);
        }
        render(unit, whouses, sid);
    }


    /**
     * 某一个 ProcureUnit 交货
     */
    public static void deliveryUnit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.attrs.qty == null || unit.attrs.qty == 0)
            unit.attrs.qty = unit.attrs.planQty;
        if(unit.attrs.deliveryDate == null)
            unit.attrs.deliveryDate = new Date();
        renderArgs.put("attrs", unit.attrs);
        render(unit);
    }

    @Check("procures.delivery")
    public static void reverDelivery(long id, String msg) {
        ProcureUnit unit = ProcureUnit.findById(id);
        unit.revertDelivery(msg);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("撤销成功");
        }
        deliveryUnit(id);
    }

    /**
     * 交货更新
     *
     * @param attrs
     */
    @Check("procures.delivery")
    public static void delivery(UnitAttrs attrs, long id, String cmt) {
        ProcureUnit unit = ProcureUnit.findById(id);
        unit.deliveryValidate(attrs);
        if(Validation.hasErrors()) render("/ProcureUnits/deliveryUnit.html", unit, attrs);

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
            render("/ProcureUnits/deliveryUnit.html", unit, attrs);
        }

        //抵达货代
        if(unit.cooperator == null || unit.shipType == null) {
            Validation.addError("", "[合作者] 或者 [运输方式] 需要填写完整.");
        } else {
            unit.isPlaced = true;
            unit.save();
        }

        if(unit.deliverplan != null) {
            unit.deliverplan.delivery();
            DeliverPlans.show(unit.deliverplan.id);
        } else
            Deliveryments.show(unit.deliveryment.id);

    }

    public static void hasProcureUnitBySellings(String sellingId) {
        boolean flag = ProcureUnit.hasProcureUnitBySellings(sellingId);
        if(flag)
            renderJSON(new Ret(true, "存在采购计划"));
        renderJSON(new Ret(false, "当前selling不存在采购计划"));
    }

    public static void isNeedApprove(int total, float day, String sellingId) {
        List<ProcureUnit> procureUnits = ProcureUnit.find("sid = ? and stage = ? ", sellingId, ProcureUnit.STAGE.PLAN)
                .fetch();
        int plan_total = 0;
        if(procureUnits != null && procureUnits.size() > 0) {
            for(ProcureUnit unit : procureUnits) {
                plan_total += unit.attrs.planQty;
            }
        }
        int temp = total + plan_total;
        int needCompare = new BigDecimal(Double.valueOf(temp + "") / day).setScale(0, BigDecimal.ROUND_HALF_UP)
                .intValue();
        int returnValue = AnalyzePost.setOutDayColor(null, needCompare);
        if(returnValue > 0) {
            renderJSON(new Ret(true, "该selling当前库存加上采购量除以Day30，超过了标准断货期天数，需要走采购计划审批流程，确定吗？"));
        }
        renderJSON(new Ret(false, "可正常走采购流程，不需要审批"));
    }

    public static void create(ProcureUnit unit, String shipmentId, String isNeedApply, String to_page) {
        unit.handler = User.findByUserName(Secure.Security.connected());
        unit.creator = unit.handler;
        unit.clearanceType = DeliverPlan.CT.Self;
        if(unit.product != null) unit.sku = unit.product.sku;
        if(unit.selling != null)
            unit.validate();

        if(unit.shipType == Shipment.T.EXPRESS) {
            if(StringUtils.isNotBlank(shipmentId)) Validation.addError("", "快递运输方式, 不需要指定运输单");
        } else {
            if(unit.selling != null)
                Validation.required("运输单", shipmentId);
        }

        if(Validation.hasErrors()) {
            List<Whouse> whouses;
            if(StringUtils.isNotBlank(unit.sid)) {
                unit.selling = Selling.findById(unit.sid);
                whouses = Whouse.findByAccount(unit.selling.account);
            } else {
                whouses = Whouse.findByType(Whouse.T.FBA);
            }
            render("ProcureUnits/blank.html", unit, whouses);
        }

        if(unit.isCheck != 1) unit.isCheck = 0;
        if(isNeedApply != null && isNeedApply.equals("need")) {
            unit.stage = ProcureUnit.STAGE.APPROVE;
        }
        unit.save();

        if(unit.selling != null) {
            if(unit.shipType != Shipment.T.EXPRESS) {
                Shipment ship = Shipment.findById(shipmentId);
                ship.addToShip(unit);
            }

            if(Validation.hasErrors()) {
                List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
                unit.remove();
                render("ProcureUnits/blank.html", unit, whouses);
            }
            new ElcukRecord(Messages.get("procureunit.save"), Messages.get("action.base", unit.to_log()), unit.id + "")
                    .save();

            if(StringUtils.equals(isNeedApply, "need")) {
                unit.startActiviti(unit.handler.username);
                flash.success("提交审批成功, 并且采购计划同时被指派到运输单 %s", shipmentId);
                ProcureUnits.showactiviti(unit.id);
            } else {
                flash.success("创建成功, 并且采购计划同时被指派到运输单 %s", shipmentId);
            }
        }
        if(StringUtils.isNotBlank(to_page) && to_page.trim().equals("analysis")) {
            Analyzes.index();
        } else {
            ProcureUnits.indexForMarket(new ProcurePost());
        }
    }


    public static void edit(long id, String type) {
        ProcureUnit unit = ProcureUnit.findById(id);
        int oldPlanQty = unit.attrs.planQty;
        List<Whouse> whouses = new ArrayList<>();
        if(unit.selling != null) {
            whouses = Whouse.findByAccount(unit.selling.account);
        }
        unit.setPeriod();
        render(unit, oldPlanQty, whouses, type);
    }

    /**
     * TODO effect: 需要调整的采购计划的修改
     *
     * @param id
     * @param oldPlanQty
     */
    public static void update(Long id, Integer oldPlanQty, ProcureUnit unit, String shipmentId, String msg) {
        List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
        ProcureUnit managedUnit = ProcureUnit.findById(id);
        managedUnit.update(unit, shipmentId, msg);
        if(Validation.hasErrors()) {
            flash.error(Validation.errors().toString());
            unit.id = managedUnit.id;
            render("ProcureUnits/edit.html", unit, oldPlanQty, whouses);
        }
        flash.success("成功修改采购计划!", id);
        edit(id, "edit");

    }


    /**
     * @param unitid
     * @param oldPlanQty
     */
    public static void updateprocess(Long unitid, Long checkid, Integer oldPlanQty, ProcureUnit unit,
                                     String shipmentId, String msg) {
        List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
        ProcureUnit managedUnit = ProcureUnit.findById(unitid);
        managedUnit.update(unit, shipmentId, msg);
        if(Validation.hasErrors()) {
            flash.error(Validation.errors().toString());
            unit.id = managedUnit.id;
            CheckTasks.showactiviti(checkid);
        }
        flash.success("成功修改采购计划!!", unitid);
        CheckTasks.showactiviti(checkid);
    }

    public static void destroy(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        Set<User> users = unit.editToUsers();
        unit.remove();
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcurePost p = new ProcurePost();
            p.search = "id:" + id;
            index(p);
        }
        flash.success("删除成功, 所关联的运输项目也成功删除.");
        index(null);
    }

    /**
     * 分拆采购计划页面
     *
     * @param id
     */
    public static void splitUnit(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        ProcureUnit newUnit = new ProcureUnit();
        newUnit.comment(String.format("此采购计划由于 #%s 采购计划分拆创建.", unit.id));
        newUnit.attrs.qty = 0;
        F.T2<List<Selling>, List<String>> sellingAndSellingIds = Selling.sameFamilySellings(unit.sku);
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
        renderArgs.put("sids", J.json(sellingAndSellingIds._2));
        renderArgs.put("whouses", Whouse.findAll());
        render(unit, newUnit);
    }

    /**
     * 分拆操作
     *
     * @param id
     * @param newUnit
     */
    @Check("procures.dosplitunit")
    public static void doSplitUnit(long id, ProcureUnit newUnit) {
        checkAuthenticity();
        ProcureUnit unit = ProcureUnit.findById(id);
        newUnit.handler = User.current();
        ProcureUnit nUnit = unit.split(newUnit);
        if(Validation.hasErrors()) {
            List<Whouse> whouses = Whouse.findAll();
            render("ProcureUnits/splitUnit.html", unit, newUnit, whouses);
        }

        flash.success("采购计划 #%s 成功分拆出 #%s", id, nUnit.id);
        Deliveryments.show(unit.deliveryment.id);
    }

    /**
     * 抵达货代
     *
     * @param id
     */
    public static void markPlace(long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        if(unit.cooperator == null || unit.shipType == null) {
            Validation.addError("", "[合作者] 或者 [运输方式] 需要填写完整.");
        } else {
            unit.isPlaced = true;
            unit.save();
        }
        render(unit);
    }


    /**
     * 预付款申请
     *
     * @param id
     */
    @Check("procureunits.billingprepay")
    public static void billingPrePay(Long id, Long applyId) {
        ProcureUnit unit = ProcureUnit.findById(id);
        try {
            unit.billingPrePay();
        } catch(PaymentException e) {
            Validation.addError("", e.getMessage());
        }
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 请款成功", FeeType.cashpledge().nickName);
        Applys.procure(applyId);
    }


    /**
     * 预付款申请
     *
     * @param applyid
     */
    @Check("procureunits.billingprepay")
    public static void morebillingPrePay(Long applyid, List<Long> unitids) {
        if(unitids == null || unitids.size() <= 0) renderJSON(new Ret("请选择请款明细!"));
        for(Long unitid : unitids) {
            ProcureUnit unit = ProcureUnit.findById(unitid);
            if(!unit.isNeedPay)
                renderJSON(new Ret(false, "采购计划ID:" + unitid + "不可以请款!"));
            try {
                unit.billingPrePay();
            } catch(PaymentException e) {
                Validation.addError("", e.getMessage());
            }
            if(Validation.hasErrors())
                renderJSON(new Ret(Validation.errors().get(0).message()));
        }
        renderJSON(new Ret(true, "预付款请款成功"));
    }


    /**
     * 尾款申请
     *
     * @param id
     */
    @Check("procureunits.billingtailpay")
    public static void billingTailPay(Long id, Long applyId) {
        ProcureUnit unit = ProcureUnit.findById(id);
        try {
            unit.billingTailPay();
        } catch(PaymentException e) {
            Validation.addError("", e.getMessage());
        }
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 请款成功", FeeType.procurement().nickName);
        Applys.procure(applyId);
    }


    public static void morebillingTailPay(Long applyid, List<Long> unitids) {
        if(unitids == null || unitids.size() <= 0) renderJSON(new Ret("请选择请款明细!"));
        for(Long unitid : unitids) {
            ProcureUnit unit = ProcureUnit.findById(unitid);
            if(!unit.isNeedPay)
                renderJSON(new Ret(false, "采购计划ID:" + unitid + "不可以请款!"));
            try {
                unit.billingTailPay();
            } catch(PaymentException e) {
                Validation.addError("", e.getMessage());
            }
            if(Validation.hasErrors())
                renderJSON(new Ret(Validation.errors().toString()));
        }
        renderJSON(new Ret(true, "尾款请款成功"));
    }


    /**
     * 加载当前采购计划供应商下所有未申请的返工费用
     *
     * @param id
     */
    public static void loadCheckList(Long id) {
        ProcureUnit pro = ProcureUnit.findById(id);
        //首先查询出当前采购计划下所属的供应商下所有的采购计划ID
        SqlSelect sql = new SqlSelect().select("id").from("ProcureUnit").where("cooperator_id=?").param(pro
                .cooperator.id);
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        List<Long> unitIds = new ArrayList<Long>();
        for(Map<String, Object> row : rows) {
            unitIds.add(Long.parseLong(row.get("id").toString()));
        }
        //使用查询出来的采购计划ID去查询 所有未申请返工费用的质检任务(费用需要大于0)
        List<CheckTask> checks = CheckTask
                .find("reworkPay = null AND workfee > 0 AND units_id IN " + SqlSelect.inlineParam(unitIds) + "")
                .fetch();
        render("ProcureUnits/_reworkpay_modal.html", checks);
    }

    /**
     * 申请返工费用
     *
     * @param id
     * @param applyId
     */
    public static void billingReworkPay(Long id, Long applyId, String ids) {
        ProcureUnit unit = ProcureUnit.findById(id);
        try {
            List<CheckTask> checks = CheckTask.find("id IN " + SqlSelect.inlineParam(ids.split("_")) + "").fetch();
            float amount = 0f;
            PaymentUnit reworkPay = unit.billingReworkPay(amount);
            for(CheckTask check : checks) {
                amount += check.workfee;
                //将PaymentUnit对应到CheckTask
                check.reworkPay = reworkPay;
                check.save();
            }
            //返工费用是需要向工厂收取的费用 所以这里转成负数
            reworkPay.amount = -amount;
            reworkPay.save();
        } catch(PaymentException e) {
            Validation.addError("", e.getMessage());
        }
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("%s 请款成功", FeeType.rework().nickName);
        }
        Applys.procure(applyId);
    }

    public static void editManual(Long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        render("ProcureUnits/editManualProcureUnit.html", unit);
    }

    /**
     * 修改手动单采购计划
     */
    public static void updateManual(Long id, ProcureUnit unit) {
        ProcureUnit managedUnit = ProcureUnit.findById(id);
        managedUnit.updateManualData(unit);
        managedUnit.validateManual();
        if(Validation.hasErrors()) {
            render("ProcureUnits/editManualProcureUnit.html", unit);
        }
        managedUnit.save();
        flash.success("成功修改采购计划!", id);
        renderArgs.put("unit", managedUnit);
        render("ProcureUnits/editManualProcureUnit.html");
    }


    /**
     * 修改采购计划的是否付款
     *
     * @param pid
     * @param applyId
     */

    @Check("procureunits.billingprepay")
    public static void editPaySatus(Long pid, Long applyId, String reason) {
        ProcureUnit unit = ProcureUnit.findById(pid);
        try {
            unit.editPayStatus();
            new ElcukRecord(Messages.get("procureunit.editPaySatus"),
                    "采购计划id:" + pid + " 更改收款状态:" + !unit.isNeedPay + " " + reason, String.valueOf(applyId)).save();
        } catch(Exception e) {
            Validation.addError("", e.getMessage());
        }

        Applys.procure(applyId);
    }

    /**
     * 导出修改日志
     */
    public static void exportLogs(ProcurePost p) {
        if(p == null) p = new ProcurePost();
        render(p);
    }

    public static void showactiviti(Long id) {
        if(id == null) return;
        ProcureUnit unit = ProcureUnit.findById(id);
        int oldPlanQty = unit.attrs.planQty;
        List<Whouse> whouses = Whouse.findByAccount(unit.selling.account);
        Map<String, Object> map = unit.showInfo(id, Secure.Security.connected());
        ActivitiProcess ap = (ActivitiProcess) map.get("ap");
        int issubmit = (Integer) map.get("issubmit");
        List<Map<String, String>> infos = (List<Map<String, String>>) map.get("infos");
        String taskname = (String) map.get("taskname");
        //不显示菜单栏
        boolean isEnd = false;
        if(taskname != null && taskname.equals("运营专员查看审核结果")) {
            isEnd = true;
        }
        render(unit, ap, infos, issubmit, taskname, isEnd, whouses, oldPlanQty);
    }

    public static void submitactiviti(String processid, String submitstate, String opition, Long id,
                                      Integer oldPlanQty, ProcureUnit unit, String shipmentId, String msg) {
        ProcureUnit old_unit = ProcureUnit.findById(id);
        ActivitiProcess ap = ActivitiProcess.find("id=?", Long.parseLong(processid)).first();
        String taskName = ActivitiProcess.privilegeProcess(ap.processInstanceId, Secure.Security.connected());
        //如果主管打回，则专员可以修改采购计划
        if(taskName != null && taskName.equals("运营专员")) {
            ProcureUnit managedUnit = ProcureUnit.findById(id);
            managedUnit.update(unit, shipmentId, msg);
            if(Validation.hasErrors()) {
                flash.error(Validation.errors().toString());
                unit.id = managedUnit.id;
                ProcureUnits.showactiviti(id);
            }
            flash.success("成功修改采购计划,并提交审批!", id);
        }

        old_unit.submitActiviti(ap, submitstate, Secure.Security.connected(), opition);
        ProcureUnits.showactiviti(id);
    }

    /**
     * 终止流程
     *
     * @param processid
     * @param id
     */
    public static void terminateProcess(long processid, long id) {
        ProcureUnit unit = ProcureUnit.findById(id);
        ActivitiProcess.endTask(processid, "procureunit.stopactiviti");
        unit.resetUnitByTerminalProcess();
        redirect("/activitis/index");
    }

    /**
     * 展示库存
     *
     * @param name
     * @param type
     */
    public static void showStockBySellingOrSku(String name, String type) {
        HashMap<String, Integer> map = ProcureUnit.caluStockInProcureUnit(name, type);
        HashMap<String, Integer> stock_map = WhouseItem.caluStockInProcureUnit(name, type);
        render(map, stock_map);
    }

    /**
     * 批量创建FBA
     *
     * @param pids
     */
    public static void batchCreateFBA(ProcurePost p, List<Long> pids) {
        if(pids != null && pids.size() > 0) {
            ProcureUnit.postFbaShipments(pids);
        }
        ProcureUnits.indexForMarket(p);
    }

    /**
     * 更新单一属性
     *
     * @param attr
     * @param value
     */
    public static void updateAttr(Long id, String attr, String value) {
        try {
            ProcureUnit unit = ProcureUnit.findById(id);
            if(unit == null) throw new FastRuntimeException("未找到对应的采购计划");
            unit.update(attr, value);
            renderJSON(new Ret(true, String.format("成功修改采购计划[%s]", id)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
