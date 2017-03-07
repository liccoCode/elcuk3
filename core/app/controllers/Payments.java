package controllers;

import controllers.api.SystemOperation;
import helper.Currency;
import helper.J;
import helper.Webs;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.procure.Cooperator;
import models.product.Attach;
import models.view.Ret;
import models.view.post.PaymentUnitPost;
import models.view.post.PaymentsPost;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
import play.cache.CacheFor;
import play.data.binding.As;
import play.data.validation.Validation;
import play.jobs.Job;
import play.modules.pdf.PDF;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Payments Controller
 * User: wyatt
 * Date: 1/24/13
 * Time: 4:43 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Payments extends Controller {

    @Before(only = {"index"})
    public static void beforIndex() {
        List<Cooperator> cooperator = Cooperator.findAll();

        renderArgs.put("cooperator", cooperator);
    }


    @Check("payments.index")
    public static void index(PaymentsPost p) {
        if(p == null) p = new PaymentsPost();
        List<Payment> payments = p.query();
        render(payments, p);
    }

    @CacheFor("5mn")
    public static void bocRates() {
        String html = await(new Job<String>() {
            @Override
            public String doJobWithResult() throws Exception {
                return Currency.bocRatesHtml();
            }
        }.now());
        renderHtml(html);
    }

    @CacheFor("5mn")
    public static void xeRates(Currency currency) {
        String html = await(new Job<String>() {
            @Override
            public String doJobWithResult() throws Exception {
                return Currency.xeRatesHtml(currency);
            }
        }.now());
        renderHtml(html);
    }

    @Check("payments.show")
    public static void show(Long id, Integer p) {
        if(p == null) p = 1;
        Payment payment = Payment.findById(id);
        PaymentUnitPost post = new PaymentUnitPost(id, p);
        List<PaymentUnit> units = post.query();
        render(payment, units, p, post);
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
        show(id, null);
    }

    public static void unlock(Long id) {
        Payment payment = Payment.findById(id);
        payment.lockAndUnLock(false);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("成功解锁, 新请款可以再次进入这个付款单.");
        show(id, null);
    }

    /**
     * 为当前付款单付款
     */
    @Check("payments.payforit")
    public static void payForIt(Long id, Long paymentTargetId,
                                Currency currency, BigDecimal actualPaid,
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
            show(id, null);
        }

        payment.payIt(paymentTargetId, currency, ratio, ratio_publish_date, actualPaid);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("支付成功.");

        show(id, null);
    }

    @Check("payments.shouldpaidupdate")
    public static void shouldPaidUpdate(Long id, BigDecimal shouldPaid) {
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
        show(id, null);
    }


    /**
     * 为付款单生成 PDF 文档
     *
     * @param id
     */
    public static void pdf(Long id) {
        Payment payment = Payment.findById(id);
        PDF.Options options = new PDF.Options();
        options.filename = "Invoice_" + id + ".pdf";
        PDF.renderPDF(payment, options);
    }

}
