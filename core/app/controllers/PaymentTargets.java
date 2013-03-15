package controllers;

import models.finance.PaymentTarget;
import models.procure.Cooperator;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 3/15/13
 * Time: 11:43 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class PaymentTargets extends Controller {

    @Before(only = {"index", "save", "update", "destroy"})
    public static void indexBefore() {
        List<PaymentTarget> targets = PaymentTarget.findAll();
        List<Cooperator> copers = Cooperator.findAll();
        renderArgs.put("targets", targets);
        renderArgs.put("copers", copers);
    }


    public static void index() {
        PaymentTarget t = new PaymentTarget();
        render(t);
    }

    public static void save(PaymentTarget t, Cooperator c) {
        Validation.required("账号", t.accountNumber);
        Validation.required("账户", t.accountUser);

        if(Validation.hasErrors())
            render("PaymentTargets/index.html", t);

        t.newPaymentTarget(c);

        if(Validation.hasErrors())
            render("PaymentTargets/index.html", t);

        flash.success("新的支付目标添加成功.");
        index();
    }

    public static void update(Long targetId, PaymentTarget t, Cooperator c) {
        Validation.required("账号", t.accountNumber);
        Validation.required("账户", t.accountUser);

        if(Validation.hasErrors())
            render("PaymentTargets/index.html", t);

        t.cooper = c;

        PaymentTarget target = PaymentTarget.findById(targetId);
        target.updateAttr(t);

        if(Validation.hasErrors())
            render("PaymentTargets/index.html", t);

        flash.success("更新成功.");
        index();
    }

    public static void destroy(Long targetId) {
        PaymentTarget target = PaymentTarget.findById(targetId);
        target.destroy();

        if(Validation.hasErrors()) {
            PaymentTarget t = new PaymentTarget();
            render("PaymentTargets/index.html", t);
        }
        flash.success("成功删除支付目标");
        renderJSON(new Ret(true));
    }
}
