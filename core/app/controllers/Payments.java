package controllers;

import models.procure.Deliveryment;
import play.mvc.Controller;

/**
 * Payments Controller
 * User: wyatt
 * Date: 1/24/13
 * Time: 4:43 PM
 */
//@With({GlobalExceptionHandler.class, Secure.class})
public class Payments extends Controller {

    public static void index(String deliveryId) {
        Deliveryment dmt = Deliveryment.findById(deliveryId);
        render(dmt);
    }

    public static void deliverymentApply(String deliveryId, Long procureUnitId) {

    }
}
