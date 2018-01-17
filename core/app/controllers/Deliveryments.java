package controllers;

import controllers.api.SystemOperation;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.finance.ProcureApply;
import models.procure.*;
import models.product.Category;
import models.view.Ret;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import models.view.post.DeliveryPost;
import models.view.post.ProcurePost;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Deliveryments extends Controller {

    @Before(only = {"show", "update", "addunits", "delunits", "cancel", "confirm"})
    public static void showPageSetUp() {
        String deliverymentId = request.params.get("id");
        if(StringUtils.isBlank(deliverymentId)) deliverymentId = request.params.get("dmt.id");
        Deliveryment dmt = Deliveryment.findById(deliverymentId);
        if(dmt != null)
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
        renderArgs.put("records", ElcukRecord.records(deliverymentId));
        renderArgs.put("shippers", Cooperator.shippers());
        renderArgs.put("buyers", User.openUsers());
    }

    @Before(only = {"index", "deliverymentToApply"})
    public static void beforeIndex(DeliveryPost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        List<ProcureApply> avaliableApplies = ProcureApply
                .unPaidApplies(p == null ? null : p.cooperId);
        renderArgs.put("suppliers", suppliers);
        renderArgs.put("avaliableApplies", avaliableApplies);
    }

    @Check("deliveryments.index")
    public static void index(DeliveryPost p, List<String> deliverymentIds) {
        List<Deliveryment> deliveryments = null;
        if(deliverymentIds == null) deliverymentIds = new ArrayList<>();
        if(p == null) p = new DeliveryPost();
        deliveryments = p.query();
        render(deliveryments, p, deliverymentIds);
    }

    public static void indexByCooperId(Long id) {
        DeliveryPost p = new DeliveryPost();
        p.cooperId = id;
        DateTime now = DateTime.now(Dates.timeZone(null));
        p.from = now.withYear(2011).toDate();
        p.to = now.toDate();
        p.dateType = DeliveryPost.DateType.CREATE;
        List<Deliveryment> deliveryments = p.query();
        List<Cooperator> suppliers = Cooperator.suppliers();
        render("Deliveryments/index.html", suppliers, deliveryments, p);
    }

    public static void show(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        if(dmt.units.size() > 0) {
            String username = Login.currentUserName();
            List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                    .collect(Collectors.toList());
            if(dmt.units.stream().noneMatch(unit -> categoryList.contains(unit.product.category.categoryId))) {
                renderText("对不起，您没有查看此采购单的权限，如要查看请联系管理员！");
            }
        }
        notFoundIfNull(dmt);
        List<Long> expressUnitIds = dmt.units.stream()
                .filter(unit -> unit.shipType == Shipment.T.EXPRESS)
                .map(unit -> unit.id)
                .collect(Collectors.toList());
        String expressid = StringUtils.join(expressUnitIds, ",");
        double total = dmt.units.stream().filter(unit -> unit.type != ProcureUnit.T.StockSplit)
                .mapToDouble(ProcureUnit::totalAmountToCNY).sum();
        String applyMsg = "";
        List<Deliveryment> deliveryments = new ArrayList<>();
        if(dmt.state == Deliveryment.S.PENDING_REVIEW) {
            applyMsg = dmt.validDmtIsNeedApply().message;
            deliveryments = dmt.getRelateDelivery();
        }
        boolean isB2b = Objects.equals(dmt.handler.projectName, User.COR.MengTop);
        render(dmt, expressid, total, applyMsg, deliveryments, isB2b);
    }

    public static void showPayment(String id, Long paymentId) {
        Deliveryment dmt = Deliveryment.findById(id);
        if(dmt.units.size() > 0) {
            String username = Login.currentUserName();
            List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                    .collect(Collectors.toList());
            if(dmt.units.stream().noneMatch(unit -> categoryList.contains(unit.product.category.categoryId))) {
                renderText("对不起，您没有查看此采购单的权限，如要查看请联系管理员！");
            }
        }
        notFoundIfNull(dmt);
        List<Long> expressUnitIds = dmt.units.stream()
                .filter(unit -> unit.shipType == Shipment.T.EXPRESS)
                .map(unit -> unit.id)
                .collect(Collectors.toList());
        String expressid = StringUtils.join(expressUnitIds, ",");
        double total = dmt.units.stream().filter(unit -> unit.type != ProcureUnit.T.StockSplit)
                .mapToDouble(ProcureUnit::totalAmountToCNY).sum();
        String applyMsg = "";
        List<Deliveryment> deliveryments = new ArrayList<>();
        if(dmt.state == Deliveryment.S.PENDING_REVIEW) {
            applyMsg = dmt.validDmtIsNeedApply().message;
            deliveryments = dmt.getRelateDelivery();
        }
        boolean isB2b = Objects.equals(dmt.handler.projectName, User.COR.MengTop);
        render("Deliveryments/show.html", dmt, expressid, total, applyMsg, deliveryments, isB2b, paymentId);
    }


    public static void showProcureUnitList(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        List<ProcureUnit> units = dmt.units;
        if(units.size() > 0) {
            String username = Login.currentUserName();
            List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                    .collect(Collectors.toList());
            if(dmt.units.stream().noneMatch(unit -> categoryList.contains(unit.product.category.categoryId))) {
                renderText("对不起，您没有查看此采购单的权限，如要查看请联系管理员！");
            }
        }
        boolean isB2B = (boolean) renderArgs.get("isB2B");
        renderArgs.put("norecord", true);
        if(isB2B || dmt.deliveryType == Deliveryment.T.MOVE) {
            render("/ProcureUnits/_b2b_unit_list.html", units);
        } else {
            render("/ProcureUnits/_unit_list_v3.html", units);
        }
    }

    public static void update(Deliveryment dmt) {
        validation.valid(dmt);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
        dmt.save();
        flash.success("更新成功.");
        show(dmt.id);
    }

    public static void confirmUnit(String id, List<Long> pids) {
        if(pids.size() > 0) {
            for(Long unit_id : pids) {
                ProcureUnit unit = ProcureUnit.findById(unit_id);
                unit.isConfirm = true;
                unit.save();
            }
        }
        show(id);
    }

    /**
     * 从 Procrues#index 页面, 通过选择 ProcureUnit 创建 Deliveryment
     */
    @Check("procures.createdeliveryment")
    public static void create(List<Long> pids, String name) {
        Validation.required("procrues.createDeliveryment.name", name);
        Validation.required("deliveryments.addunits", pids);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcureUnits.index(new ProcurePost(ProcureUnit.STAGE.PLAN));
        }

        Deliveryment deliveryment = Deliveryment.createFromProcures(pids, name, Login.current());

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcureUnits.index(new ProcurePost(ProcureUnit.STAGE.PLAN));
        }

        flash.success("Deliveryment %s 创建成功.", deliveryment.id);
        Deliveryments.show(deliveryment.id);
    }

    /**
     * 向 Deliveryment 添加 ProcureUnit
     */
    public static void addunits(String id, List<Long> pids) {
        Deliveryment dmt = Deliveryment.findById(id);
        Validation.required("deliveryments.addunits", pids);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
        dmt.assignUnitToDeliveryment(pids);

        // 再一次检查, 是否有错误
        if(Validation.hasErrors()) {
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
            render("Deliveryments/show.html", dmt);
        }
        flash.success("成功将 %s 采购计划添加到当前采购单.", StringUtils.join(pids, ","));
        show(dmt.id);
    }

    /**
     * 将 ProcureUnit 从 Deliveryment 中解除
     */
    public static void deleteUnits(String id, List<Long> pids) {
        Deliveryment dmt = Deliveryment.findById(id);
        Validation.required("deliveryments.delunits", pids);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
        dmt.unAssignUnitInDeliveryment(pids);

        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);

        flash.success("成功将 %s 采购计划从当前采购单中移除.", StringUtils.join(pids, ","));
        show(dmt.id);
    }

    public static void validDmtIsNeedApply(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        if(Arrays.asList("CONFIRM", "APPROVE", "DONE").contains(dmt.state.name()))
            renderJSON(new Ret(false));
        if(dmt.state == Deliveryment.S.PENDING_REVIEW)
            renderJSON(new Ret(true, "采购单正在审核中！"));
        renderJSON(dmt.validDmtIsNeedApply());
    }

    /**
     * 确认采购单, 这样才能进入运输单进行挑选
     */
    public static void confirm(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        dmt.confirm();
        if(Validation.hasErrors()) {
            double total = dmt.units.stream().mapToDouble(ProcureUnit::totalAmountToCNY).sum();
            render("Deliveryments/show.html", dmt, total);
        }
        new ElcukRecord(Messages.get("deliveryment.confirm"), String.format("确认[采购单] %s", id), id).save();
        show(id);
    }

    /**
     * 取消采购单
     */
    @Check("deliveryments.cancel")
    public static void cancel(String id, String msg) {
        Validation.required("deliveryments.cancel", msg);
        Deliveryment dmt = Deliveryment.findById(id);
        dmt.cancel(msg);
        if(Validation.hasErrors()) {
            render("Deliveryments/show.html", dmt, msg);
        }
        show(dmt.id);
    }

    public static void review(String id, String msg, Boolean result) {
        Deliveryment dmt = Deliveryment.findById(id);
        Validation.required("deliveryments.review", result);
        dmt.review(result, msg);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt, msg);
        show(id);
    }


    /**
     * 为采购单提交请款单申请
     */
    @Check("deliveryments.deliverymenttoapply")
    public static void deliverymentToApply(List<String> deliverymentIds, DeliveryPost p, Long procureApplyId) {
        if(deliverymentIds == null) deliverymentIds = new ArrayList<>();
        if(deliverymentIds.size() <= 0) {
            flash.error("请选择需纳入请款的采购单(相同供应商).");
            index(p, deliverymentIds);
        }
        ProcureApply.initApplyStatus();
        ProcureApply apply = ProcureApply.findById(procureApplyId);
        if(apply == null)
            apply = ProcureApply.buildProcureApply(deliverymentIds);
        else
            apply.appendDelivery(deliverymentIds);

        if(apply == null || Validation.hasErrors()) {
            for(Error error : Validation.errors()) {
                flash.error(error.message());
            }
            index(p, deliverymentIds);
        } else {
            flash.success("请款单 %s 申请成功.", apply.serialNumber);
            Applys.procure(apply.id);
        }
    }

    public static void departProcureApply(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        long applyId = dmt.apply.id;
        dmt.departFromProcureApply();

        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 剥离成功.", id);
        Applys.procure(applyId);
    }

    @Check("deliveryments.manual")
    public static void manual() {
        Deliveryment dmt = new Deliveryment();
        ProcureUnit unit = new ProcureUnit();
        List<ProcureUnit> units = dmt.units;
        String project = Login.current().projectName.name();
        render(dmt, unit, units, project);
    }

    /**
     * 创建手动单
     *
     * @param dmt
     * @param units
     */
    @Check("deliveryments.manual")
    public static void createManual(Deliveryment dmt, List<ProcureUnit> units) {
        Validation.required("供应商", dmt.cooperator);
        Validation.required("采购单别名", dmt.name);
        dmt.id = Deliveryment.id();
        dmt.handler = Login.current();
        dmt.state = Deliveryment.S.PENDING;
        dmt.name = dmt.name.trim();
        dmt.deliveryType = Deliveryment.T.MANUAL;
        dmt.projectName = Login.current().projectName;
        dmt.createDate = new Date();
        boolean containTax = units.get(0).containTax;
        units.stream().filter(unit -> unit.product != null).forEach(unit -> {
            unit.cooperator = dmt.cooperator;
            unit.handler = Login.current();
            unit.deliveryment = dmt;
            unit.stage = ProcureUnit.STAGE.DELIVERY;
            unit.containTax = containTax;
            if(containTax) {
                CooperItem item = CooperItem.find("product.sku=? AND cooperator.id=?",
                        unit.product.sku, unit.cooperator.id).first();
                unit.taxPoint = item.taxPoint;
            }
            unit.validateManual();
            if(Validation.hasErrors()) {
                render("Deliveryments/manual.html", dmt, units);
            }
            unit.originQty = unit.attrs.planQty;
            unit.save();
        });
        dmt.save();
        flash.success("Deliveryment %s 创建成功.", dmt.id);
        Deliveryments.show(dmt.id);
    }

    public static void indexPer() {
        HighChart lineChart = new HighChart(Series.LINE);
        lineChart.series(DeliveryPost.queryProcureNumPerDay());
        lineChart.series(DeliveryPost.queryDeliveryNumPerDay());
        renderJSON(J.json(lineChart));
    }

    public static void perCreateTotalNum() {
        HighChart lineChart = ProcurePost.perCreateTotalNum();
        renderJSON(J.json(lineChart));
    }

}
