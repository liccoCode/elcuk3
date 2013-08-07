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
import play.data.binding.As;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * Payments Controller
 * User: wyatt
 * Date: 1/24/13
 * Time: 4:43 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Payments extends Controller {

    @Check("payments.index")
    public static void index() {
        List<Payment> payments = Payment.find("ORDER BY createdAt DESC").fetch();
        render(payments);
    }

    @CacheFor("5mn")
    public static void bocRates() {
        renderHtml(Currency.bocRatesHtml());
    }

    @CacheFor("5mn")
    public static void xeRates(Currency currency) {
        renderHtml(Currency.xeRatesHtml(currency));
    }

    @Check("payments.show")
    public static void show(Long id) {
        Payment payment = Payment.findById(id);
        render(payment);
    }

    /**
     * 锁定付款单
     */
    public static void lockIt(Long id) {
        Payment payment = Payment.findById(id);
        payment.lockAndUnLock(true);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("成功锁定, 新请款不会再进入这个付款单.");
        show(id);
    }

    public static void unlock(Long id) {
        Payment payment = Payment.findById(id);
        payment.lockAndUnLock(false);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("成功解锁, 新请款可以再次进入这个付款单.");
        show(id);
    }

    /**
     * 为当前付款单付款
     */
    @Check("payments.payforit")
    public static void payForIt(Long id, Long paymentTargetId,
                                Currency currency, Float actualPaid,
                                Float ratio, @As("yyyy-MM-dd HH:mm:ss") Date ratio_publish_date) {

        Validation.required("供应商支付账号", paymentTargetId);
        Validation.required("币种", currency);
        Validation.required("具体支付金额", actualPaid);
        Validation.required("汇率", ratio);
        Validation.required("汇率发布日期", ratio_publish_date);
        Validation.min("汇率", ratio, 0);

        Payment payment = Payment.findById(id);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
            show(id);
        }

        payment.payIt(paymentTargetId, currency, ratio, ratio_publish_date, actualPaid);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("支付成功.");

        show(id);
    }

    @Check("payments.shouldpaidupdate")
    public static void shouldPaidUpdate(Long id, Float shouldPaid) {
        Payment payment = Payment.findById(id);
        payment.shouldPaid(shouldPaid);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(false, Webs.VJson(Validation.errors())));
        } else {
            renderJSON(new Ret(true, "更新成功"));
        }
    }

    // --------- File Resources -----------
    @Check("payments.uploads")
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

    /**
     * 取消当前这个请款单
     */
    @Check("payments.cancel")
    public static void cancel(Long id, String reason) {
        Payment payment = Payment.findById(id);
        payment.cancel(reason);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("付款单取消成功.");
        show(id);
    }

}
