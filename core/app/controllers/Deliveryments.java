package controllers;

import helper.J;
import models.User;
import models.procure.Deliveryment;
import models.procure.Payment;
import models.view.Ret;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

/**
 * 采购单控制器
 * User: wyattpan
 * Date: 6/19/12
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Deliveryments extends Controller {

}
