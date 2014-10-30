package controllers;

import controllers.api.SystemOperation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-10-29
 * Time: PM3:28
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Channels extends Controller {
    public static void index() {
        render();
    }
}
