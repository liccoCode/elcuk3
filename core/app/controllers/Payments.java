package controllers;

import helper.Webs;
import models.procure.Payment;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/26/12
 * Time: 11:32 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
@Check("root")
public class Payments extends Controller {
    public static void paymentClose(Payment pay, String msg) {
        if(pay == null || !pay.isPersistent()) throw new FastRuntimeException("你指定需要关闭的 Payment 不合法.");
        renderJSON(Webs.G(pay.close(msg)));
    }
}
