package controllers.api;

import play.mvc.Controller;
import play.mvc.With;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/10/12
 * Time: 4:09 PM
 */
@With({APIChecker.class})
public class Tickets extends Controller {
    public static void h() {
        renderText("kdfj");
    }
}
