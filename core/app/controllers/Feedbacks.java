package controllers;

import play.mvc.Controller;
import play.mvc.With;

/**
 * 管理 Feedbacks 的功能
 * User: wyattpan
 * Date: 3/15/12
 * Time: 1:41 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Feedbacks extends Controller {
    public static void index() {
        render();
    }
}
