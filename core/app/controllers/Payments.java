package controllers;

import helper.Currency;
import helper.J;
import helper.Webs;
import models.finance.Payment;
import models.product.Attach;
import models.view.Ret;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.cache.CacheFor;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Payments Controller
 * User: wyatt
 * Date: 1/24/13
 * Time: 4:43 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Payments extends Controller {

    public static void index() {
        List<Payment> payments = Payment.findAll();
        render(payments);
    }

    @CacheFor("5mn")
    public static void rates() {
        renderText(Currency.bocRatesHtml());
    }

    //TODO 查看需要权限
    public static void show(Long id) {
        Payment payment = Payment.findById(id);
        render(payment);
    }

    //TODO approval 需要权限
    public static void paymentUnitApproval(Long id, List<Long> paymentUnitIds) {
        checkAuthenticity();
        Payment payment = Payment.findById(id);
        payment.unitsApproval(paymentUnitIds);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("批复成功");
        show(id);
    }

    /**
     * TODO 需要权限
     * 为当前付款单付款
     */
    public static void payForIt(Long id, Long paymentTargetId,
                                Currency currency, Float actualPaid) {

        Validation.required("供应商支付账号", paymentTargetId);
        Validation.required("币种", currency);
        Validation.required("具体支付金额", actualPaid);

        Payment payment = Payment.findById(id);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        }

        payment.payIt(paymentTargetId, currency, actualPaid);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("支付成功.");

        show(id);
    }

    public static void shouldPaidUpdate(Long id, Float shouldPaid) {
        Payment payment = Payment.findById(id);
        payment.shouldPaid(shouldPaid);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(false, J.json(Validation.errors())));
        } else {
            renderJSON(new Ret(true, "更新成功"));
        }
    }

    // --------- File Resources -----------
    public static void uploads(Attach a) {
        a.setUpAttachName();
        Logger.info("%s File save to %s.[%s kb] at Payments", a.fid, a.location,
                a.fileSize / 1024);
        try {

            Payment.<Payment>findById(NumberUtils.toLong(a.fid)).upload(a);
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(J.G(a));
    }

}
