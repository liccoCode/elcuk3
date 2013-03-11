package controllers;

import exception.PaymentException;
import models.finance.PaymentUnit;
import models.view.Ret;
import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/28/13
 * Time: 10:43 AM
 */
public class PaymentUnits extends Controller {

    public static void remove(Long id) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        try {
            payUnit.remove();
            renderJSON(new Ret(true, String.format("PaymentUnit %s 删除成功.", id)));
        } catch(PaymentException e) {
            renderJSON(new Ret(false, e.getMessage()));
        }
    }

}
