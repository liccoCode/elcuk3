package controllers;

import helper.Webs;
import models.finance.PaymentUnit;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/28/13
 * Time: 10:43 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class PaymentUnits extends Controller {

    @Check("paymentunits.destroy")
    public static void destroy(Long id, String reason) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        payUnit.remove(reason);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("删除成功");
        Applys.procure(payUnit.deliveryment.apply.id);
    }

    @Check("paymentunits.fixvalue")
    public static void fixValue(Long id, Float fixValue, String reason) {
        PaymentUnit paymentUnit = PaymentUnit.findById(id);
        Validation.required("fixValue", fixValue);
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Validation.errors().toString()));

        paymentUnit.fixValue(fixValue, reason);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("修正值更新成功.");
        }
        // NOTE: 如果 fixValue 的 routes 文件改变, 这里也需要改变
        redirect("/apply/" + paymentUnit.procureUnit.deliveryment.apply.id + "/procure#" + id);
    }

    @Check("paymentunits.deny")
    public static void deny(Long paymentId, Long id, String reason) {
        PaymentUnit paymentUnit = PaymentUnit.findById(id);
        paymentUnit.deny(reason);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("成功驳回");
        Payments.show(paymentId);
    }

}
