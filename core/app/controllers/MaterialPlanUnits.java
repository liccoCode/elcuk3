package controllers;

import controllers.api.SystemOperation;
import exception.PaymentException;
import helper.Webs;
import models.ElcukRecord;
import models.finance.FeeType;
import models.material.MaterialPlanUnit;
import models.view.Ret;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * 物料出货计划Controller
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/8
 * Time: PM4:57
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialPlanUnits extends Controller {

    public static void billingPrePay(Long id, Long applyId) {
        MaterialPlanUnit unit = MaterialPlanUnit.findById(id);
        try {
            unit.billingPrePay();
        } catch(PaymentException e) {
            Validation.addError("", e.getMessage());
        }
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 请款成功", FeeType.cashpledge().nickName);
        Applys.material(applyId);
    }


    /**
     * 付款申请
     *
     * @param id
     */
    public static void billingTailPay(Long id, Long applyId) {
        MaterialPlanUnit unit = MaterialPlanUnit.findById(id);
        try {
            unit.billingTailPay();
        } catch(PaymentException e) {
            Validation.addError("", e.getMessage());
        }
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 请款成功", FeeType.procurement().nickName);
        Applys.material(applyId);
    }


    /**
     * 修改出货计划的是否付款
     *
     * @param pid
     * @param applyId
     */
    public static void editPaySatus(Long pid, Long applyId, String reason) {
        MaterialPlanUnit unit = MaterialPlanUnit.findById(pid);
        try {
            unit.editPayStatus();
            new ElcukRecord(Messages.get("materialplanUnit.editPaySatus"),
                    "出货计划id:" + pid + " 更改收款状态:" + !unit.isNeedPay + " " + reason, String.valueOf(applyId)).save();
        } catch(Exception e) {
            Validation.addError("", e.getMessage());
        }
        Applys.material(applyId);
    }


    /**
     * 批量付款申请
     *
     * @param unitIds
     */
    public static void batchTailPay(Long[] unitIds) {
        if(unitIds == null || unitIds.length == 0) renderJSON(new Ret(false, "请选择请款明细!"));
        List<MaterialPlanUnit> units = MaterialPlanUnit.find("id IN " + SqlSelect.inlineParam(unitIds)).fetch();
        for(MaterialPlanUnit unit : units) {
            if(!unit.isNeedPay)
                renderJSON(new Ret(false, "出货计划ID:" + unit.id + "不可以请款!"));
            try {
                unit.billingTailPay();
            } catch(PaymentException e) {
                Validation.addError("", e.getMessage());
            }
            if(Validation.hasErrors())
                renderJSON(new Ret(Validation.errors().toString()));
        }
        renderJSON(new Ret(true, "尾款请款成功"));
    }

}
