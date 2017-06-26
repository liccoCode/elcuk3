package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.material.Material;
import models.material.MaterialPlan;
import models.material.MaterialPlanUnit;
import models.procure.Cooperator;
import models.view.Ret;
import models.view.post.MaterialPlanPost;
import models.view.post.MaterialPost;
import models.view.post.MaterialUnitPost;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * 物料出货单Controller
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/8
 * Time: PM4:57
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialPlans extends Controller {


    @Before(only = {"show", "blank"})
    public static void showPageSetUp() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        String id = request.params.get("id");
        renderArgs.put("records", ElcukRecord.records(id));
    }


    /**
     * 查询物料采购余量信息
     *
     * @param p
     */
    public static void indexMaterial(MaterialPost p) {
        if(p == null) p = new MaterialPost();
        List<Material> materials = p.query();
        render(p, materials);
    }


    /**
     * 跳转 到 创建物料出库单页面
     */
    public static void materialPlan(List<Long> pids, String planName) {
        if(StringUtils.isBlank(planName))
            Validation.addError("", "出货单名称必须填写!");
        if(pids == null || pids.size() <= 0)
            Validation.addError("", "必须选择物料信息!");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            MaterialPlans.indexMaterial(new MaterialPost());
        }

        MaterialPlan materialPlan = MaterialPlan
                .createMaterialPlan(pids, planName, User.findByUserName(Secure.Security.connected()));
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            MaterialUnits.index(new MaterialUnitPost());
        }
        flash.success("物料出货单 %s 创建成功.", pids.toString());
        MaterialPlans.show(materialPlan.id);
    }

    @Check("deliverplans.index")
    public static void index(MaterialPlanPost p) {
        List<MaterialPlan> materialPlans;
        if(p == null) p = new MaterialPlanPost();
        materialPlans = p.query();
        MaterialPlan.S financeState = MaterialPlan.S.PENDING_REVIEW;
        render(materialPlans, p, financeState);
    }

    public static void show(String id) {
        MaterialPlan dp = MaterialPlan.findById(id);
        List<MaterialPlanUnit> units = dp.units;
        boolean qtyEdit = false;
        if(dp.state == MaterialPlan.P.CREATE) {
            qtyEdit = true;
        }
        render(dp, units, qtyEdit);
    }

    /**
     * 修改物料出货单
     *
     * @param dp
     */
    public static void update(MaterialPlan dp) {
        validation.valid(dp);
        if(Validation.hasErrors())
            show(dp.id);
        dp.save();
        flash.success("更新成功.");
        show(dp.id);
    }

    /**
     * 更新出货单信息
     *
     * @param id
     * @param value
     */
    public static void updateUnit(String id, String value) {
        MaterialPlanUnit unit = MaterialPlanUnit.findById(Long.valueOf(id));
        //验证交货数量需判断不能大于采购余量
        if(unit.material.surplusConfirmQty() >= NumberUtils.toInt(value)) {
            unit.updateAttr(value);
            renderJSON(new Ret());
        } else {
            renderJSON(new Ret(false, "交货数量大于采购余量"));
        }
    }

    /**
     * 将 MaterialPlanUnit 从 MaterialPlan 中解除
     */
    public static void delunits(String id, List<Long> pids) {
        MaterialPlan dp = MaterialPlan.findById(id);
        notFoundIfNull(dp);

        Validation.required("materialPlans.delunits", pids);
        if(Validation.hasErrors()) render("MaterialPlans/show.html", dp);
        dp.unassignUnitToMaterialPlan(pids);
        if(Validation.hasErrors()) render("MaterialPlans/show.html", dp);

        flash.success("成功将 %s 出货单元从物料出货单 %s 中移除.", StringUtils.join(pids, ","), id);
        if(dp.units.isEmpty()) {
            dp.delete();
            index(null);
        } else {
            show(dp.id);
        }
    }

    /**
     * 物料出货单确认验证
     *
     * @param id
     */
    public static void confirmValidate(String id) {
        MaterialPlan dp = MaterialPlan.findById(id);
        //验证交货数量需判断不能大于采购余量
        long count = dp.units.stream().filter(unit -> unit.material.surplusPendingQty() > 0).count();
        if(count > 0) {
            renderJSON(new Ret(true, "【物料编码】存在未确认的采购数***，是否仍要交货？"));
        }
    }

    /**
     * 确认物料出货单
     */
    public static void confirm(String id) {
        MaterialPlan dp = MaterialPlan.findById(id);
        dp.confirm();

        if(Validation.hasErrors()) {
            show(id);
        } else {
            new ElcukRecord(Messages.get("materialPlans.confirm"), String.format("确认[物料出货单] %s", id), id).save();
            flash.success("物料出货单 %s 确认成功.", id);
            show(id);
        }
    }

    /**
     * 根据出货单ID查询出货计划集合
     *
     * @param id
     */
    public static void showMaterialPlanUnitList(String id) {
        MaterialPlan plan = MaterialPlan.findById(id);
        List<MaterialPlanUnit> units = plan.units;
        render("/MaterialPlans/_unit_list.html", units);
    }

    /**
     * 修改物料计划
     */
    public static void updateMaterialPlanUnit(MaterialPlanUnit unit) {
        MaterialPlanUnit materialPlanUnit = MaterialPlanUnit.findById(unit.id);
        materialPlanUnit.receiptQty = unit.receiptQty;
        materialPlanUnit.save();
        renderJSON(new Ret());
    }


    /**
     * MaterialPlan 添加 MaterialPlanUnit
     */
    public static void addunits(String id, String code) {
        Validation.required("materialPlans.addunits", code);
        if(Validation.hasErrors()) show(id);

        MaterialPlan materialPlan = MaterialPlan.addunits(id, code);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            MaterialPlans.show(id);
        }
        flash.success("物料 %s 添加成功.", code);
        MaterialPlans.show(materialPlan.id);
    }


    /**
     * 批量财务审核
     * @param pids
     */
    public static void approveBatch(List<String> pids) {
        MaterialPlan.approve(pids);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            index(new MaterialPlanPost());
        }
        flash.success("物料审核成功.");
        index(new MaterialPlanPost());
    }

    /**
     * 单个财务审核
     * @param id
     */
    public static void approve(String id) {
        List<String> pids = new ArrayList<>();
        pids.add(id);
        MaterialPlan.approve(pids);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            index(new MaterialPlanPost());
        }
        flash.success("物料审核成功.");
        index(new MaterialPlanPost());
    }
}
