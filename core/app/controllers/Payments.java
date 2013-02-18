package controllers;

import exception.PaymentException;
import helper.HTTP;
import helper.J;
import helper.Webs;
import models.finance.FeeType;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import models.product.Attach;
import models.view.Ret;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import play.Logger;
import play.cache.CacheFor;
import play.modules.router.Get;
import play.modules.router.Post;
import play.mvc.Controller;

import java.util.List;

/**
 * Payments Controller
 * User: wyatt
 * Date: 1/24/13
 * Time: 4:43 PM
 */
//@With({GlobalExceptionHandler.class, Secure.class})
public class Payments extends Controller {

    @Get("/payments")
    public static void index() {
        List<Payment> payments = Payment.findAll();
        render(payments);
    }

    @Get("/payments/{<[0-9]+>id}")
    public static void show(Long id) {
        Payment payment = Payment.findById(id);
        render(payment);
    }

    @Get("/payments/rates")
    @CacheFor("10mn")
    public static void rates() {
        Document doc = Jsoup.parse(HTTP.get("http://www.boc.cn/sourcedb/whpj/"));
        renderText(doc.select("table table table").get(0).outerHtml());
    }

    // --------- File Resources -----------
    @Post("/payments/files/upload")
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


    // ----------- Deliveryment Nested payments Resources ---------------

    /**
     * 采购单的请款
     */
    @Get("/deliveryment/{deliveryId}/payments")
    public static void deliveryPayments(String deliveryId) {
        Deliveryment dmt = Deliveryment.findById(deliveryId);
        List<FeeType> procureFeeTypes = FeeType.procure();
        render(dmt, procureFeeTypes);
    }

    /**
     * 采购单中的采购项目请款
     */
    @Post("/deliveryment/{deliveryId}/unitfees")
    public static void procureUnitApply(String deliveryId, Long procureUnitId, boolean prepay) {
        try {
            ProcureUnit unit = ProcureUnit.findById(procureUnitId);
            unit.billing(0, prepay);
            flash.success("请款添加成功.");
        } catch(PaymentException e) {
            flash.error(e.getMessage());
        }
        deliveryPayments(deliveryId);
    }

    /**
     * 采购单中的采购单级别请款
     */
    @Post("/deliveryment/{deliveryId}/fees")
    public static void deliverymentApply(String deliveryId, PaymentUnit pu) {
        Deliveryment dmt = Deliveryment.findById(deliveryId);
        try {
            dmt.billing(pu);
            flash.success("请款成功.");
        } catch(PaymentException e) {
            flash.error("因 %s 原因请款失败.", e.getMessage());
        }
        renderArgs.put("pu", pu);
        deliveryPayments(deliveryId);
    }


}
