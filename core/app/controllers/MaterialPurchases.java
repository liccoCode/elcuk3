package controllers;

import controllers.api.SystemOperation;
import helper.GTs;
import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.OperatorConfig;
import models.User;
import models.finance.ProcureApply;
import models.material.Material;
import models.material.MaterialPurchase;
import models.material.MaterialUnit;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.view.Ret;
import models.view.post.MaterialPurchasePost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/31
 * Time: 下午5:10
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialPurchases extends Controller {


    @Before(only = {"blank", "show", "update", "delunits", "cancel", "confirm"})
    public static void showPageSetUp() {
        String id = request.params.get("id");
        renderArgs.put("records", ElcukRecord.records(id));
        renderArgs.put("shippers", Cooperator.shippers());
        renderArgs.put("buyers", User.openUsers());
        renderArgs.put("brandName", OperatorConfig.getVal("brandname"));
    }

    @Before(only = {"index"})
    public static void beforeIndex(MaterialPurchasePost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        List<ProcureApply> availableApplies = ProcureApply.unPaidApplies(p == null ? null : p.cooperId);
        renderArgs.put("suppliers", suppliers);
        renderArgs.put("availableApplies", availableApplies);
    }

    public static void index(MaterialPurchasePost p) {
        if(p == null) p = new MaterialPurchasePost();
        List<MaterialPurchase> materialPurchases = p.query();
        render(materialPurchases, p);
    }

    public static void blank() {
        MaterialPurchase purchase = new MaterialPurchase();
        List<MaterialUnit> units = purchase.units;
        List<Material> materials = Material.suppliers();
        render(units, materials, purchase);
    }


    public static void show(String id) {
        MaterialPurchase dmt = MaterialPurchase.findById(id);
        notFoundIfNull(dmt);
        render(dmt);
    }

    /**
     * 模糊匹配物料
     *
     * @param name
     */
    public static void sameMaterial(String name) {
        List<Material> materials = Material.find("name like '" + name + "%'").fetch();
        List<String> names = materials.stream().map(p -> p.name).collect(Collectors.toList());
        renderJSON(J.json(names));
    }

    /**
     * 新增物料采购单
     *
     * @param purchase
     * @param units
     */
    public static void create(MaterialPurchase purchase, List<MaterialUnit> units) {
        Validation.required("物料采购单别名", purchase.name);
        purchase.id = MaterialPurchase.id();
        purchase.handler = Login.current();
        purchase.state = MaterialPurchase.S.PENDING;
        purchase.name = purchase.name.trim();
        purchase.deliveryType = Deliveryment.T.MANUAL;
        purchase.projectName = Login.current().projectName;

        units.stream().filter(unit -> unit.material != null).forEach(unit -> {
            unit.cooperator = purchase.cooperator;
            unit.handler = Login.current();
            unit.materialPurchase = purchase;
            unit.stage = MaterialUnit.STAGE.DELIVERY;
            unit.validateManual();
            if(Validation.hasErrors()) {
                render("MaterialPurchases/blank.html", purchase, units);
            }
            unit.save();
        });

        purchase.save();
        flash.success("MaterialPurchase %s 创建成功.", purchase.id);
        MaterialPurchases.show(purchase.id);
    }

    /**
     * 修改物料采购单
     *
     * @param dmt
     */
    public static void update(MaterialPurchase dmt) {
        validation.valid(dmt);
        if(Validation.hasErrors())
            render("MaterialPurchases/show.html", dmt);
        dmt.save();
        flash.success("更新成功.");
        show(dmt.id);
    }

    /**
     * 获取拥有这个 SKU 的所有供应商
     */
    public static void cooperators(Long id) {
        List<Cooperator> cooperatorList = Cooperator
                .find("SELECT c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.material.id=? ORDER BY ci.id", id)
                .fetch();
        StringBuilder buff = new StringBuilder();
        buff.append("[");
        for(Cooperator co : cooperatorList) {
            buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(co.id).append("\"")
                    .append(",").append("\"").append("name").append("\"").append(":").append("\"").append(co.name)
                    .append("\"").append("},");
        }
        buff.append("]");
        renderJSON(StringUtils.replace(buff.toString(), "},]", "}]"));
    }

    // 供应商的价格
    public static void price(long cooperId, Long materialId) {
        validation.required(cooperId);
        if(Validation.hasErrors())
            renderJSON(new Ret(Webs.V(Validation.errors())));
        Material m = Material.findById(materialId);
        CooperItem copItem = CooperItem.find(" cooperator.id=? AND material.id =?", cooperId, materialId).first();
        renderJSON(GTs.newMap("price", copItem.price).put("currency", copItem.currency).put("flag", true)
                .put("period", copItem.period).put("boxSize", copItem.boxSize).put("surplusPendingQty", m.surplusPendingQty()).build());
    }


    /**
     * 查询供应商下的所有的物料信息
     */
    public static void materials(Long cooperId) {
        List<Material> materialList = Material
                .find("SELECT m FROM CooperItem i Left JOIN i.material m WHERE i.cooperator.id=? AND i.type =? ",
                        cooperId, CooperItem.T.MATERIAL).fetch();
        StringBuilder buff = new StringBuilder();
        buff.append("[");
        for(Material co : materialList) {
            buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(co.id).append("\"")
                    .append(",").append("\"").append("code").append("\"").append(":").append("\"").append(co.code)
                    .append("\"").append("},");
        }
        buff.append("]");
        renderJSON(StringUtils.replace(buff.toString(), "},]", "}]"));
    }

    /**
     * 确认物料采购单, 这样才能进入运输单进行挑选
     */
    public static void confirm(String id) {
        MaterialPurchase dmt = MaterialPurchase.findById(id);
        dmt.confirm();
        if(Validation.hasErrors()) {
            render("MaterialPurchases/show.html", dmt);
        }
        new ElcukRecord(Messages.get("MaterialPurchases.confirm"), String.format("确认[物料采购单] %s", id), id).save();
        show(id);
    }

    /**
     * 取消物料采购单
     */
    public static void cancel(String id, String msg) {
        Validation.required("关闭原因", msg);
        MaterialPurchase dmt = MaterialPurchase.findById(id);
        dmt.cancel(msg);
        if(Validation.hasErrors()) {
            render("MaterialPurchases/show.html", dmt, msg);
        }
        show(dmt.id);
    }

    /**
     * 将 MaterialUnit 从 MaterialPurchase 中解除
     */
    public static void delunits(String id, List<Long> pids) {
        MaterialPurchase dmt = MaterialPurchase.findById(id);
        Validation.required("materialPurchases.delunits", pids);
        if(Validation.hasErrors())
            render("MaterialPurchases/show.html", dmt);
        dmt.unAssignUnitInMaterialPurchase(pids);

        if(Validation.hasErrors())
            render("MaterialPurchases/show.html", dmt);

        flash.success("成功将 %s 物料计划从当前物料采购单中移除.", StringUtils.join(pids, ","));
        show(dmt.id);
    }

    /**
     * 根据出货单ID查询出货计划集合
     *
     * @param id
     */
    public static void showMaterialUnitList(String id) {
        MaterialPurchase materialPurchase = MaterialPurchase.findById(id);
        List<MaterialUnit> units = materialPurchase.units;
        render("/MaterialUnits/_unit_list.html", units);
    }

    
    public static void validDmtIsNeedApply(String id) {
        MaterialPurchase dmt = MaterialPurchase.findById(id);
        if(Arrays.asList("CONFIRM", "PENDING").contains(dmt.state.name()))
            renderJSON(new Ret());
        if(dmt.state == MaterialPurchase.S.CANCEL)
            renderJSON(new Ret(false, "采购单已取消！"));
    }

}
