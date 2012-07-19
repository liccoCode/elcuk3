package controllers;

import play.mvc.Controller;
import play.mvc.With;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 7/19/12
 * Time: 11:59 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class AmazonReviews extends Controller {
    public static void index() {
        render();
    }
}
