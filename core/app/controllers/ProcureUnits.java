package controllers;

import helper.Webs;
import models.finance.PaymentUnit;
import models.procure.ProcureUnit;
import play.data.validation.Validation;
import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/26/13
 * Time: 5:56 PM
 */
public class ProcureUnits extends Controller {
    /**
     * 预付款申请
     *
     * @param id
     */
    public static void billingPrePay(Long id, Long applyId) {
        ProcureUnit unit = ProcureUnit.findById(id);
        PaymentUnit fee = unit.billingPrePay();
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 请款成功", fee.feeType.nickName);
        Applys.procure(applyId);
    }

    /**
     * 尾款申请
     *
     * @param id
     */
    public static void billingTailPay(Long id, Long applyId) {
        ProcureUnit unit = ProcureUnit.findById(id);
        PaymentUnit fee = unit.billingTailPay();
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("%s 请款成功", fee.feeType.nickName);
        Applys.procure(applyId);
    }

}
