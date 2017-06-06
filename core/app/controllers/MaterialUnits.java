package controllers;

import controllers.api.SystemOperation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/31
 * Time: 下午4:53
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MaterialUnits extends Controller {

    

}
