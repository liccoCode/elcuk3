package controllers;

import controllers.api.SystemOperation;
import helper.GTs;
import helper.Webs;
import models.ElcukRecord;
import models.OperatorConfig;
import models.material.Material;
import models.material.MaterialUnit;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.product.Category;
import models.view.Ret;
import models.view.post.MaterialPost;
import models.view.post.MaterialUnitPost;
import models.view.post.ProcurePost;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.Date;
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
        renderArgs.put("logs",
                ElcukRecord.records(Arrays.asList("materialUnit.save", "materialUnit.remove", "materialUnit.split"), 50));

        //为视图提供日期
        DateTime dateTime = new DateTime();
        renderArgs.put("tomorrow1", dateTime.plusDays(1).toString("yyyy-MM-dd"));
        renderArgs.put("tomorrow2", dateTime.plusDays(2).toString("yyyy-MM-dd"));
        renderArgs.put("tomorrow3", dateTime.plusDays(3).toString("yyyy-MM-dd"));
    }

    /**
     * 列表查询
     *
     * @param p 分页参数
     */
    public static void index(MaterialUnitPost p) {
        if(p == null) {
            p = new MaterialUnitPost();
            p.stages.add(ProcureUnit.STAGE.PLAN);
            p.stages.add(ProcureUnit.STAGE.OUTBOUND);
        }
        render(p);
    }

    /**
     * 仓库模块 物料计划查询
     * @param p
     */
    public static void indexWhouse(MaterialUnitPost p) {
        if(p == null) {
            p = new MaterialUnitPost();
            p.stages.add(ProcureUnit.STAGE.PLAN);
            p.stages.add(ProcureUnit.STAGE.OUTBOUND);
        }
        p.pagination = false;
        render(p);
    }

    /**
     * 根据ID返回单个信息
     */
    public static void findMaterialUnit(long id) {
        MaterialUnit materialUnit = MaterialUnit.findById(id);
        StringBuilder buff = new StringBuilder();
        buff.append("{").append("\"").append("id").append("\"").append(":").append("\"").append(materialUnit.id).append
                ("\"").append(",").append("\"").append("planQty").append("\"").append(":").append("\"")
                .append(materialUnit.planQty).append
                ("\"").append(",").append("\"").append("planPrice").append("\"").append(":").append("\"")
                .append(materialUnit.planPrice).append
                ("\"").append(",").append("\"").append("planDeliveryDate").append("\"").append(":").append("\"")
                .append(materialUnit.planDeliveryDate).append
                ("\"").append(",").append("\"").append("planCurrency").append("\"").append(":").append("\"")
                .append(materialUnit.planCurrency)
                .append("\"").append("}");
        renderJSON(buff.toString());
    }

    /**
     * 修改物料计划
     */
    public static void updateMaterialUnit( MaterialUnit unit) {
        MaterialUnit materialUnit = MaterialUnit.findById(unit.id);
        materialUnit.planQty =  unit.planQty;
        materialUnit.planPrice =  unit.planPrice;
        materialUnit.planCurrency =  unit.planCurrency;
        materialUnit.planDeliveryDate =  unit.planDeliveryDate;
        materialUnit.save();  
        flash.success("操作成功");
        MaterialPurchases.show(unit.materialPurchase.id);
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


    /**
     * 明天 后天 大后天 计划视图
     */
    public static void planView(Date date) {
        MaterialUnitPost p = new MaterialUnitPost(ProcureUnit.STAGE.DELIVERY);
        p.dateType = "attrs.planDeliveryDate";
        p.from = date;
        p.to = date;
        MaterialUnits.index(p);
    }

    /**
     * 物料计划创建物料出货单进行验证
     * @param pids
     */
    public static void createValidate(List<Long> pids) {
        String msg = MaterialUnit.validateIsInbound(pids);
        if(StringUtils.isNotBlank(msg)) {
            renderJSON(new Ret(false, msg));
        } else {
            renderJSON(new Ret(true));
        }
    }
}
