package controllers;

import helper.Webs;
import models.User;
import models.procure.Deliveryment;
import models.procure.Payment;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({FastRunTimeExceptionCatch.class, Secure.class, GzipFilter.class})
public class Deliveryments extends Controller {
    public static void detail(String id) {
        Deliveryment dlmt = Deliveryment.findById(id);
        render(dlmt);
    }

    public static void payment(Payment pay, Deliveryment dlmt) {
        pay.payer = User.findByUserName(Secure.Security.connected());
        renderJSON(Webs.exposeGson(dlmt.payForDeliveryment(pay)));
    }

    public static void paymentClose(Payment pay, String msg) {
        if(pay == null || !pay.isPersistent()) throw new FastRuntimeException("你指定需要关闭的 Payment 不合法.");
        renderJSON(Webs.exposeGson(pay.close(msg)));
    }

    public static void paymentComplate(Deliveryment dlmt) {
        if(!dlmt.isPersistent()) throw new FastRuntimeException("你指定需要清款的采购单不合法.");
        dlmt.complatePayment();
        renderJSON(Webs.exposeGson(dlmt));
    }
}
