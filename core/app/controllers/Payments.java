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

    // ----------- Deliveryment Nested payments Resources ---------------

}
