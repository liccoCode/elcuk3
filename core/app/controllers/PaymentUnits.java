package controllers;

import helper.J;
import helper.Webs;
import models.ElcukRecord;
import models.finance.PaymentUnit;
import models.view.Ret;
import play.data.validation.Validation;
import play.jobs.Job;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/28/13
 * Time: 10:43 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class PaymentUnits extends Controller {

    @Check("paymentunits.destroy")
    public static void destroy(Long id, String reason) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        payUnit.procureFeeRemove(reason);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("删除成功");
        Applys.procure(payUnit.deliveryment.apply.id);
    }

    @Check("paymentunits.destroy")
    public static void destroyByShipment(Long id, String reason, String shipmentId) {
        PaymentUnit payUnit = PaymentUnit.findById(id);
        payUnit.transportFeeRemove(reason);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("删除成功");
        Shipments.show(shipmentId);
    }

    @Check("paymentunits.fixvalue")
    public static void fixValue(Long id, Float fixValue, String reason) {
        PaymentUnit paymentUnit = PaymentUnit.findById(id);
        Validation.required("fixValue", fixValue);
        if(Validation.hasErrors())
            renderJSON(new Ret(false, Validation.errors().toString()));

        paymentUnit.fixValue(fixValue, reason);
        if(Validation.hasErrors()) {
            Webs.errorToFlash(flash);
        } else {
            flash.success("修正值更新成功.");
        }
        // NOTE: 如果 fixValue 的 routes 文件改变, 这里也需要改变
        redirect("/apply/" + paymentUnit.procureUnit.deliveryment.apply.id + "/procure#" + id);
    }

    @Check("paymentunits.deny")
    public static void deny(Long paymentId, Long id, String reason) {
        PaymentUnit paymentUnit = PaymentUnit.findById(id);
        paymentUnit.deny(reason);
        if(Validation.hasErrors())
            Webs.errorToFlash(flash);
        else
            flash.success("成功驳回");
        Payments.show(paymentId);
    }

    public static void show(Long id) {
        PaymentUnit fee = PaymentUnit.findById(id);
        render(fee);
    }

    public static void update(Long id, PaymentUnit fee) {
        PaymentUnit feeUnit = PaymentUnit.findById(id);
        feeUnit.update(fee);
        if(Validation.hasErrors()) {
            renderJSON(new Ret(Webs.VJson(Validation.errors())));
        }
        renderArgs.put("fee", feeUnit);
        render("PaymentUnits/show.json");
    }

    public static void records(final Long id) {
        List<ElcukRecord> records = await(new Job<List<ElcukRecord>>() {
            @Override
            public List<ElcukRecord> doJobWithResult() {
                PaymentUnit feeUnit = PaymentUnit.findById(id);
                return feeUnit.updateRecords();
            }
        }.now());
        renderJSON(J.json(records));
    }

}
