package controllers;

import helper.J;
import models.User;
import models.procure.Deliveryment;
import models.procure.Payment;
import models.view.Ret;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Deliveryments extends Controller {
    public static void detail(String id) {
        Deliveryment dlmt = Deliveryment.findById(id);
        render(dlmt);
    }

    public static void payment(Payment pay, Deliveryment payObj) {
        pay.payer = User.findByUserName(Secure.Security.connected());
        renderJSON(J.G(payObj.payForDeliveryment(pay)));
    }

    public static void paymentComplate(Deliveryment dlmt) {
        if(!dlmt.isPersistent()) throw new FastRuntimeException("你指定需要清款的采购单不合法.");
        dlmt.complatePayment();
        renderJSON(J.G(dlmt));
    }

    public static void comment(String id, String msg) {
        Deliveryment deliveryment = Deliveryment.findById(id);
        if(deliveryment == null || !deliveryment.isPersistent()) throw new FastRuntimeException("Deliveryment 不存在.");
        deliveryment.memo = msg;
        renderJSON(J.G(deliveryment.save()));
    }

    @Check("root")
    public static void cancel(String id) {
        Deliveryment dlmt = Deliveryment.findById(id);
        if(dlmt == null || !dlmt.isPersistent()) throw new FastRuntimeException("Deliveryment 不存在.");
        dlmt.cancel(Secure.Security.connected());
        renderJSON(new Ret());
    }
}
