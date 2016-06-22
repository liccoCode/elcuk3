package controllers;

import controllers.api.SystemOperation;
import helper.Constant;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.finance.ProcureApply;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.product.Product;
import models.view.post.DeliveryPost;
import models.view.post.ProcurePost;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.libs.F;
import play.libs.Files;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Deliveryments extends Controller {

    @Before(only = {"show", "update", "addunits", "delunits", "cancel"})
    public static void showPageSetUp() {
        String deliverymentId = request.params.get("id");
        if(StringUtils.isBlank(deliverymentId)) deliverymentId = request.params.get("dmt.id");
        Deliveryment dmt = Deliveryment.findById(deliverymentId);
        if(dmt != null)
            renderArgs.put("plan_units", dmt.availableInPlanStageProcureUnits());
        renderArgs.put("records", ElcukRecord.records(deliverymentId));
        renderArgs.put("shippers", Cooperator.shippers());
        renderArgs.put("buyers", User.openUsers());
        renderArgs.put("cooperators", Cooperator.suppliers());
    }

    @Before(only = {"index", "deliverymentToApply"})
    public static void beforeIndex(DeliveryPost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        List<ProcureApply> avaliableApplies = ProcureApply
                .unPaidApplies(p == null ? null : p.cooperId);
        renderArgs.put("suppliers", suppliers);
        renderArgs.put("avaliableApplies", avaliableApplies);
    }

    @Before(only = {"index"})
    public static void beforeCooperatorJson() {
        String suppliersJson = J.json(Cooperator.supplierNames());
        renderArgs.put("suppliersJson", suppliersJson);
        renderArgs.put("records",
                ElcukRecord.records(Arrays.asList("procureapply.save", "deliveryment.createFromProcures",
                        "deliveryment.confirm"), 50));
    }

    @Check("deliveryments.index")
    public static void index(DeliveryPost p, List<String> deliverymentIds) {
        List<Deliveryment> deliveryments = null;
        if(deliverymentIds == null) deliverymentIds = new ArrayList<>();
        if(p == null) p = new DeliveryPost();
        deliveryments = p.query();
        List<String> handlers = Deliveryment.handlers();
        render(deliveryments, p, deliverymentIds, handlers);
    }

    public static void show(String id) {
        Deliveryment dmt = Deliveryment.findById(id);
        String expressid = ",,";
        for(ProcureUnit unit : dmt.units) {
            if(unit.shipType == Shipment.T.EXPRESS) {
                expressid = expressid + unit.id + ",";
            }
        }
        render(dmt, expressid);
    }

    public static void update(Deliveryment dmt) {
        if(!dmt.canBeEdit()) Validation.addError("", "已确认的采购单不能再修改!");
        validation.valid(dmt);
        if(Validation.hasErrors())
            render("Deliveryments/show.html", dmt);
        dmt.save();
        dmt.syncCooperatorToUnits();
        flash.success("更新成功.");
        show(dmt.id);
    }

    /**
     * 从 Procrues#index 页面, 通过选择 ProcureUnit 创建 Deliveryment
     */
    @Check("procures.createdeliveryment")
    public static void create(List<Long> pids) {
        Validation.required("deliveryments.addunits", pids);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            ProcureUnits.index(new ProcurePost(ProcureUnit.STAGE.PLAN));
        }
        Deliveryment deliveryment = Deliveryment
                .createFromProcures(pids, User.findByUserName(Secure.Security.connected()));
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
    public static void delunits(String id, List<Long> pids) {
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

    /**
     * 确认采购单, 这样才能进入运输单进行挑选
     */
    public static void confirm(List<String> deliverymentIds) {
        if(deliverymentIds != null && !deliverymentIds.isEmpty()) {
            List<String> errors = Deliveryment.batchConfirm(deliverymentIds);
            if(errors.isEmpty()) {
                flash.success("确认成功!");
            } else {
                flash.error(StringUtils.join(errors, "<br/>"));
            }
        }
        redirect("/Deliveryments/index");
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
        flash.success("取消成功");
        show(dmt.id);
    }


    /**
     * 为采购单提交请款单申请
     */
    @Check("deliveryments.deliverymenttoapply")
    public static void deliverymentToApply(List<String> deliverymentIds, DeliveryPost p,
                                           Long procureApplyId) {
        if(deliverymentIds == null) deliverymentIds = new ArrayList<String>();
        if(deliverymentIds.size() <= 0) {
            flash.error("请选择需纳入请款的采购单(相同供应商).");
            index(p, deliverymentIds);
        }

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


    /**
     * 将选定的采购单的 出货FBA 打成ZIP包，进行下载
     */
    public static synchronized void downloadFBAZIP(String id, List<Long> pids, List<Long> boxNumbers)
            throws Exception {
        if(pids == null || pids.size() == 0)
            Validation.addError("", "必须选择需要下载的采购计划");
        if(boxNumbers == null || boxNumbers.size() == 0 || pids.size() != boxNumbers.size())
            Validation.addError("", "采购单元箱数填写错误");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        }
        //创建FBA根目录，存放工厂FBA文件
        File dirfile = new File(Constant.TMP, "FBA");
        try {
            Files.delete(dirfile);
            dirfile.mkdir();

            //生成工厂的文件夹. 格式：选中的采购单的id的组合a,b,c
            File factoryDir = new File(dirfile, String.format("采购单元-%s-出货FBA", StringUtils.join(pids.toArray(), ",")));
            factoryDir.mkdir();
            for(int i = 0; i < pids.size(); i++) {
                ProcureUnit procureunit = ProcureUnit.findById(pids.get(i));

                procureunit.fbaAsPDF(factoryDir, boxNumbers.get(i));
            }

        } catch(Exception e) {
            e.printStackTrace();
            Logger.warn("downloadFBAZIP %s:%s", id, e.getMessage());
        } finally {
            File zip = new File(Constant.TMP + "/FBA.zip");
            Files.zip(dirfile, zip);
            zip.deleteOnExit();
            renderBinary(zip);
        }
    }

    @Check("deliveryments.manual")
    public static void manual() {
        Deliveryment dmt = new Deliveryment();
        ProcureUnit unit = new ProcureUnit();
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
        render(dmt, unit);
    }

    /**
     * 创建手动单
     *
     * @param dmt  Deliveryment
     * @param unit ProcureUnit
     * @deprecated
     */
    @Check("deliveryments.manual")
    public static void createManual(Deliveryment dmt, ProcureUnit unit) {
        Validation.required("供应商", dmt.cooperator);
        Validation.required("采购单别名", dmt.name);
        dmt.id = Deliveryment.id();
        User user = User.findByUserName(Secure.Security.connected());
        unit.cooperator = dmt.cooperator;
        unit.handler = user;
        unit.deliveryment = dmt;
        unit.stage = ProcureUnit.STAGE.DELIVERY;

        dmt.handler = user;
        dmt.state = Deliveryment.S.PENDING;
        dmt.name = dmt.name.trim();
        dmt.units.add(unit);
        dmt.deliveryType = Deliveryment.T.MANUAL;
        unit.validateManual();
        if(Validation.hasErrors()) {
            render("Deliveryments/manual.html", dmt, unit);
        }
        unit.save();

        dmt.save();
        flash.success("Deliveryment %s 创建成功.", dmt.id);
        Deliveryments.show(dmt.id);
    }
}
