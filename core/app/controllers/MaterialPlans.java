package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.material.MaterialPlan;
import models.material.MaterialPlanUnit;
import models.procure.ProcureUnit;
import models.view.Ret;
import models.view.post.MaterialPlanPost;
import models.view.post.MaterialUnitPost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

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


    @Before(only = {"show"})
    public static void showPageSetUp() {
        String id = request.params.get("id");
        renderArgs.put("records", ElcukRecord.records(id));
    }

    /**
     * 跳转 到 创建物料出库单页面
     * TODO effect: 需要调整权限
     */
    @Check("procures.createdeliveryment")
    public static void materialPlan(List<Long> pids, String deliverName) {
        if(StringUtils.isBlank(deliverName))
            Validation.addError("", "出货单名称必须填写!");
        if(pids == null || pids.size() <= 0)
            Validation.addError("", "必须选择物料采购计划单!");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            MaterialUnits.index(new MaterialUnitPost(ProcureUnit.STAGE.PLAN));
        }
        MaterialPlan materialPlan = MaterialPlan
                .createFromProcures(pids, deliverName, User.findByUserName(Secure.Security.connected()));
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            MaterialUnits.index(new MaterialUnitPost(ProcureUnit.STAGE.PLAN));
        }
        flash.success("物料出货单 %s 创建成功.", pids.toString());
        MaterialPlans.show(materialPlan.id);
    }

    @Check("deliverplans.index")
    public static void index(MaterialPlanPost p) {
        List<MaterialPlan> materialPlans;
        if(p == null) p = new MaterialPlanPost();
        materialPlans = p.query();
        render(materialPlans, p);
    }

    public static void show(String id) {
        MaterialPlan dp = MaterialPlan.findById(id);
        List<MaterialPlanUnit> units = dp.units;
        render(dp, units);
    }

    /**
     * 修改物料出货单
     *
     * @param dp
     */
    public static void update(MaterialPlan dp) {
        validation.valid(dp);
        if(Validation.hasErrors())
            render("MaterialPlans/show.html", dp);
        dp.save();
        flash.success("更新成功.");
        show(dp.id);
    }

    /**
     * 更新出货单信息
     *
     * @param id
     * @param value
     * @param attr
     */
    public static void updateUnit(String id, String value, String attr) {
        MaterialPlanUnit unit = MaterialPlanUnit.findById(Long.valueOf(id));
        unit.updateAttr(attr, value);
        renderJSON(new Ret());
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

        flash.success("成功将 %s 采购计划从出货单 %s 中移除.", StringUtils.join(pids, ","), id);
        if(dp.units.isEmpty()) {
            dp.delete();
            index(null);
        } else {
            show(dp.id);
        }
    }

    /**
     * 根据出货单ID查询出货计划集合
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
        materialPlanUnit.receiptQty =  unit.receiptQty;
        materialPlanUnit.save();
        renderJSON(new Ret());
    }
}
