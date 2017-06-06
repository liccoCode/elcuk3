package controllers;

import com.alibaba.fastjson.JSON;
import controllers.api.SystemOperation;
import helper.GTs;
import helper.J;
import helper.Webs;
import models.finance.ProcureApply;
import models.material.Material;
import models.material.MaterialPurchase;
import models.material.MaterialUnit;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.product.Product;
import models.view.Ret;
import models.view.post.DeliveryPost;
import models.view.post.MaterialPurchasePost;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

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

    @Before(only = {"index"})
    public static void beforeIndex(DeliveryPost p) {
        List<Cooperator> suppliers = Cooperator.suppliers();
        List<ProcureApply> availableApplies = ProcureApply.unPaidApplies(p == null ? null : p.cooperId);
        renderArgs.put("suppliers", suppliers);
        renderArgs.put("availableApplies", availableApplies);
    }

    public static void index(MaterialPurchasePost p) {
        if(p == null) p = new MaterialPurchasePost();
        List<MaterialPurchase> materialPurchases = null;
        materialPurchases = p.query();
        render(materialPurchases , p);
    }

    public static void blank() {
        MaterialPurchase purchase = new MaterialPurchase();
        List<MaterialUnit> units = purchase.units;
        List<Material> materials = Material.suppliers();
        render(units, materials, purchase);
    }


    public static void show(Long id) {
        MaterialPurchase purchase = new MaterialPurchase();
        List<MaterialUnit> units = purchase.units;
        List<Material> materials = Material.suppliers();
        render(units, materials, purchase);
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


    public static void create(MaterialPurchase purchase , List<MaterialUnit> units) {
        Validation.required("采购单别名", purchase.name);
        purchase.id = MaterialPurchase.id();
        purchase.handler = Login.current();
        purchase.state = Deliveryment.S.PENDING;
        purchase.name = purchase.name.trim();
        purchase.deliveryType = Deliveryment.T.MANUAL;

        units.stream().filter(unit -> unit.material != null).forEach(unit -> {
                    unit.cooperator = purchase.cooperator;
                    unit.creator = Login.current();
                    unit.materialPurchase = purchase;
                    unit.stage = ProcureUnit.STAGE.DELIVERY;
                    unit.validateManual();
                    if(Validation.hasErrors()) {
                        render("MaterialPurchases/blank.html", purchase, units);
                    }
                    unit.save();
                });

        purchase.save();
        flash.success("Deliveryment %s 创建成功.", purchase.id);
        //MaterialPurchases.show(purchase.id);

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
            buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(co.id).append
                    ("\"").append(",").append("\"").append("name").append("\"").append(":").append("\"").append(co.name)
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

        CooperItem copItem = CooperItem.find(" cooperator.id=? AND material.id =?", cooperId, materialId).first();
        renderJSON(GTs.newMap("price", copItem.price).put("currency", copItem.currency).put("flag", true)
                .put("period", copItem.period).put("boxSize", copItem.boxSize).build());
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
            buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(co.id).append
                    ("\"").append(",").append("\"").append("name").append("\"").append(":").append("\"").append(co.name)
                    .append("\"").append("},");
        }
        buff.append("]");
        renderJSON(StringUtils.replace(buff.toString(), "},]", "}]"));
    }


}
