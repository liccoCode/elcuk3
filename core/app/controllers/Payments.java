package controllers;

import helper.HTTP;
import helper.J;
import helper.Webs;
import models.finance.Payment;
import models.product.Attach;
import models.view.Ret;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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

    @CacheFor("10mn")
    public static void rates() {
        Document doc = Jsoup.parse(HTTP.get("http://www.boc.cn/sourcedb/whpj/"));
        renderText(doc.select("table table table").get(0).outerHtml());
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
