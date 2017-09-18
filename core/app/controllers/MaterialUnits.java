package controllers;

import controllers.api.SystemOperation;
import helper.Reflects;
import models.ElcukRecord;
import models.OperatorConfig;
import models.material.MaterialUnit;
import models.procure.Cooperator;
import models.product.Category;
import models.view.post.MaterialUnitPost;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import play.i18n.Messages;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/31
 * Time: 下午4:53
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialUnits extends Controller {

    @Before(only = {"index", "indexWhouse"})
    public static void beforeIndex() {
        List<Cooperator> cooperators = Cooperator.suppliers();
        String brandName = OperatorConfig.getVal("brandname");
        renderArgs.put("brandName", brandName);
        renderArgs.put("whouses", Whouse.find("type=?", Whouse.T.FBA).fetch());
        renderArgs.put("cooperators", cooperators);
        renderArgs.put("categoryIds", Category.categoryIds());
    }

    /**
     * 列表查询
     *
     * @param p 分页参数
     */
    public static void index(MaterialUnitPost p) {
        if(p == null) {
            p = new MaterialUnitPost();
        }
        render(p);
    }

    /**
     * 根据ID返回单个信息
     */
    public static void findMaterialUnit(long id) {
        MaterialUnit materialUnit = MaterialUnit.findById(id);
        StringBuilder buff = new StringBuilder();
        buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(materialUnit.id)
                .append("\"").append(",").append("\"").append("planQty").append("\"").append(":").append("\"")
                .append(materialUnit.planQty).append("\"").append(",").append("\"").append("planPrice").append("\"")
                .append(":").append("\"").append(materialUnit.planPrice).append("\"")
                .append(",").append("\"").append("planDeliveryDate").append("\"").append(":").append("\"")
                .append(materialUnit.planDeliveryDate).append("\"").append(",").append("\"")
                .append("planCurrency").append("\"").append(":").append("\"")
                .append(materialUnit.planCurrency)
                .append("\"").append("}");
        renderJSON(buff.toString());
    }

    /**
     * 修改物料计划
     */
    public static void updateMaterialUnit(MaterialUnit unit, String updateType, Long matId) {
        MaterialUnit materialUnit = MaterialUnit.findById(matId);

        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(materialUnit, "planQty", unit.planQty));
        logs.addAll(Reflects.logFieldFade(materialUnit, "planPrice", unit.planPrice));
        logs.addAll(Reflects.logFieldFade(materialUnit, "planCurrency", unit.planCurrency));
        materialUnit.planDeliveryDate = unit.planDeliveryDate;
        materialUnit.save();
        if(logs.size() > 0) {
            new ElcukRecord(Messages.get("materialunits.update"),
                    Messages.get("materialunits.update.msg", matId, StringUtils.join(logs, "<br>")),
                    materialUnit.materialPurchase.id).save();
        }

        flash.success("操作成功");
        if("MaterialUnitIndex".equals(updateType)) {
            index(new MaterialUnitPost());
        } else if("MaterialPurchaseShow".equals(updateType)) {
            MaterialPurchases.show(materialUnit.materialPurchase.id);
        }
    }


    /**
     * 删除物料计划
     */
    public static void destroy(long id) {
        MaterialUnit materialUnit = MaterialUnit.findById(id);
        String materialPurchaseId = materialUnit.materialPurchase.id;
        materialUnit.delete();
        flash.success("删除成功.");
        MaterialPurchases.show(materialPurchaseId);
    }

}
