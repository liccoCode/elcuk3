package controllers;

import controllers.api.SystemOperation;
import helper.Reflects;
import helper.Webs;
import models.ElcukRecord;
import models.OperatorConfig;
import models.User;
import models.material.*;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.Ret;
import models.view.post.MaterialPlanPost;
import models.view.post.MaterialPost;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
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


    @Before(only = {"show", "blank", "indexMaterial"})
    public static void showPageSetUp() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        renderArgs.put("cooperators", cooperators);
        List<MaterialBom> boms = MaterialBom.findAll();
        renderArgs.put("boms", boms);
        String id = request.params.get("id");
        renderArgs.put("records", ElcukRecord.records(id));
        renderArgs.put("brandName", OperatorConfig.getVal("brandname"));
    }

    @Before(only = {"index"})
    public static void beforeIndex(MaterialPost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        List<MaterialApply> avaliableApplies = MaterialApply
                .unPaidApplies(p == null ? null : p.cooperId);
        renderArgs.put("suppliers", suppliers);
        renderArgs.put("avaliableApplies", avaliableApplies);
        renderArgs.put("users", User.find("closed=?", false).fetch());
    }

    /**
     * 查询物料采购余量信息
     *
     * @param p
     */
    public static void indexMaterial(MaterialPost p) {
        if(p == null) p = new MaterialPost();
        List<Material> materials = p.planQuery();
        int size = materials.size();
        render(p, materials, size);
    }

    /**
     * 跳转到创建物料出库单页面
     */
    public static void blank(List<Long> pids, String planName) {
        if(pids == null || pids.size() <= 0)
            Validation.addError("", "必须选择物料信息!");
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            MaterialPlans.indexMaterial(new MaterialPost());
        }

        List<Material> units = Material.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        Cooperator cop = Cooperator.find("SELECT c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.material.id=? " +
                " ORDER BY ci.id", units.get(0).id).first();
        MaterialPlan dp = new MaterialPlan();
        dp.id = MaterialPlan.id();
        dp.state = MaterialPlan.P.CREATE;
        dp.name = planName;
        dp.cooperator = cop;
        dp.handler = Login.current();
        dp.projectName = Login.current().projectName.label();
        render(units, dp);
    }

    /**
     * 创建物料出货单
     */
    public static void create(MaterialPlan dp, List<Material> dtos) {
        //1 验证必填属性
        validation.valid(dp);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            MaterialPlans.indexMaterial(new MaterialPost());
        }
        //2 新增 出货单元
        dp.id = MaterialPlan.id();
        dp.handler = Login.current();
        dp.name = dp.name.trim();
        dp.state = MaterialPlan.P.CREATE;
        dp.financeState = MaterialPlan.S.PENDING_REVIEW;
        if(dp.receipt == MaterialPlan.R.WAREHOUSE) dp.receiveCooperator = null;
        dp.save();
        //3 新增 出货计划单元
        for(Material dto : dtos) {
            if(dto != null) {
                Material material = Material.findById(dto.id);
                MaterialPlanUnit planUnit = new MaterialPlanUnit();
                planUnit.materialPlan = dp;
                planUnit.material = material;
                planUnit.qty = dto.outQty;
                planUnit.handler = Login.current();
                planUnit.stage = ProcureUnit.STAGE.DELIVERY;
                planUnit.save();
            }
        }
        new ElcukRecord(Messages.get("materialplans.create"),
                Messages.get("materialplans.create.msg", dp.id), dp.id).save();
        flash.success("物料出货单 %s 创建成功.", dp.id);
        MaterialPlans.show(dp.id);
    }

    @Check("materialpurchases.index")
    public static void index(MaterialPlanPost p) {
        if(p == null) p = new MaterialPlanPost();
        List<MaterialPlan> materialPlans = p.query();
        render(materialPlans, p);
    }

    public static void show(String id) {
        MaterialPlan dp = MaterialPlan.findById(id);
        List<MaterialPlanUnit> units = dp.units;
        boolean qtyEdit = false;
        if(dp.state == MaterialPlan.P.CREATE) {
            qtyEdit = true;
        }
        boolean receipt = false;
        if(dp.receipt == MaterialPlan.R.WAREHOUSE) {
            receipt = true;
        }
        render(dp, units, qtyEdit, receipt);
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
        if(dp.receipt == MaterialPlan.R.WAREHOUSE) dp.receiveCooperator = null;
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
        Validation.required("请选择要解除的出货单元", pids);
        if(Validation.hasErrors()) show(dp.id);
        dp.unassignUnitToMaterialPlan(pids);
        if(Validation.hasErrors()) show(dp.id);

        flash.success("成功将 %s 出货单元从物料出货单 %s 中移除.", StringUtils.join(pids, ","), id);
        if(dp.units.isEmpty()) {
            dp.state = MaterialPlan.P.CANCEL;
            dp.save();
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
            MaterialPlan plan = MaterialPlan.findById(id);
            List<MaterialPlanUnit> units = plan.units;
            boolean qtyEdit = false;
            if(plan.state == MaterialPlan.P.CREATE) {
                qtyEdit = true;
            }
            boolean receipt = false;
            if(plan.receipt == MaterialPlan.R.WAREHOUSE) {
                receipt = true;
            }
            render("/MaterialPlans/show.html", dp, units, qtyEdit, receipt);
        } else {
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
        boolean qtyEdit = false;
        if(plan.state == MaterialPlan.P.CREATE) {
            qtyEdit = true;
        }
        render("/MaterialPlans/_unit_list.html", units, qtyEdit);
    }

    /**
     * 修改物料计划
     */
    public static void updateMaterialPlanUnit(MaterialPlanUnit unit, Long matId) {
        MaterialPlanUnit materialPlanUnit = MaterialPlanUnit.findById(matId);
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(materialPlanUnit, "receiptQty", unit.receiptQty));
        materialPlanUnit.save();
        if(logs.size() > 0) {
            new ElcukRecord(Messages.get("materialplanunits.update"),
                    Messages.get("materialplanunits.update.msg", matId, StringUtils.join(logs, "<br>")),
                    materialPlanUnit.materialPlan.id).save();
        }

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
     *
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
     *
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


    /**
     * 为出货单提交请款单申请
     */
    @Check("materialpurchases.index")
    public static void materialPlanToApply(List<String> pids, MaterialPlanPost p, Long applyId) {
        if(pids == null) pids = new ArrayList<>();
        if(pids.size() <= 0) {
            flash.error("请选择需纳入请款的出货单(相同供应商).");
            index(p);
        }
        MaterialApply apply = MaterialApply.findById(applyId);
        if(apply == null) apply = MaterialApply.buildMaterialApply(pids);
        else apply.appendMaterialApply(pids);

        if(apply == null || Validation.hasErrors()) {
            for(Error error : Validation.errors()) {
                flash.error(error.message());
            }
            index(p);
        } else {
            flash.success("物料请款单 %s 申请成功.", apply.serialNumber);
            Applys.material(apply.id);
        }
    }

    /**
     * 将出货单从其所关联的请款单中剥离开
     *
     * @param id
     */
    public static void departProcureApply(String id) {
        MaterialPlan dmt = MaterialPlan.findById(id);
        long applyId = dmt.apply.id;
        dmt.apply.updateAt(applyId);
        dmt.departFromProcureApply();

        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 剥离成功.", id);
        Applys.material(applyId);
    }
}
