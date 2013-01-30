package controllers;

import exception.PaymentException;
import models.finance.FeeType;
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


    @Get("/deliveryment/{deliveryId}/payments")
    public static void index(String deliveryId) {
        Deliveryment dmt = Deliveryment.findById(deliveryId);
        List<FeeType> procureFeeTypes = FeeType.procure();
        render(dmt, procureFeeTypes);
    }

    @Post("/deliveryment/{deliveryId}/payments")
    public static void deliverymentApply(String deliveryId, Long procureUnitId, boolean prepay) {
        ProcureUnit unit = ProcureUnit.findById(procureUnitId);
        try {
            unit.billing(0, prepay);
            flash.success("请款添加成功.");
        } catch(PaymentException e) {
            flash.error(e.getMessage());
        }
        index(deliveryId);
    }
}
