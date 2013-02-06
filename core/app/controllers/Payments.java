package controllers;

import exception.PaymentException;
import models.finance.FeeType;
import models.finance.Payment;
import models.finance.PaymentUnit;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
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
