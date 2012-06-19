package controllers;

import models.procure.Deliveryment;
import play.mvc.Controller;
import play.mvc.With;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({FastRunTimeExceptionCatch.class, Secure.class, GzipFilter.class})
public class Deliveryments extends Controller {
    public static void detail(String id) {
        Deliveryment dlmt = Deliveryment.findById(id);
        render(dlmt);
    }

}
