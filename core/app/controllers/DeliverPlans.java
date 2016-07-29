package controllers;


import controllers.api.SystemOperation;
import helper.Constant;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.procure.Cooperator;
import models.procure.DeliverPlan;
import models.procure.ProcureUnit;
import models.view.post.DeliverPlanPost;
import models.view.post.DeliveryPost;
import models.view.post.ProcurePost;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Validation;
import play.libs.Files;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
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
        DeliverPlan dp = DeliverPlan.findById(deliverymentId);
        if(dp != null)
            renderArgs.put("plan_units", dp.availableInPlanStageProcureUnits());
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
    public static void index(DeliverPlanPost p) {
        List<DeliverPlan> deliverplans = null;
        if(p == null) p = new DeliverPlanPost();
        deliverplans = p.query();
        List<String> handlers = DeliverPlan.handlers();
        render(deliverplans, p, handlers);
    }


    public static void update(DeliverPlan dp) {
        if(dp.isLocked()) Validation.addError("", "已经确认发货的出货单不能再修改!");
        Validation.required("出货单名称", dp.name);
        Validation.required("报关类型", dp.clearanceType);
        validation.valid(dp);
        if(!Validation.hasErrors()) {
            dp.save();
            dp.syncClearanceTypeToUnits();
        }
        render("/DeliverPlans/show.html", dp);
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
    public static void triggerReceiveRecords(List<String> ids, DeliverPlanPost p) {
        if(ids != null && !ids.isEmpty()) {
            for(String id : ids) {
                DeliverPlan deliverPlan = DeliverPlan.findById(id);
                if(deliverPlan == null || deliverPlan.isLocked()) continue;
                deliverPlan.triggerReceiveRecords();
            }
        }
        flash.success("确认发货成功!");
        index(p);
    }

    @Check("fbas.deploytoamazon")
    public static void deploysToAmazon(String id, List<Long> pids) {
        if(pids == null || pids.size() == 0)
            Validation.addError("", "必须选择需要创建的采购计划");

        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            DeliverPlans.show(id);
        }

        ProcureUnit.postFbaShipments(pids);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {

            flash.success("选择的采购计划全部成功创建 FBA");
        }
        DeliverPlans.show(id);
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
}
