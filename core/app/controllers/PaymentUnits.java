package controllers;

import exception.PaymentException;
import helper.J;
import models.finance.PaymentUnit;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/28/13
 * Time: 10:43 AM
 */
public class PaymentUnits extends Controller {

    public static void destroy(Long id) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        try {
            payUnit.remove();
            renderJSON(new Ret(true, String.format("PaymentUnit %s 删除成功.", id)));
        } catch(PaymentException e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

    public static void fixValue(Long id, Float fixValue, String reason) {
        PaymentUnit unit = PaymentUnit.findById(id);
        Validation.required("fixValue", fixValue);
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Validation.errors().toString()));

        unit.fixValue(fixValue, reason);
        if(Validation.hasErrors())
            renderJSON(new Ret(false, J.G(Validation.errors())));
        else
            renderJSON(new Ret("更新成功."));
    }

}
