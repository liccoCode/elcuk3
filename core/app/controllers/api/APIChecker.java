package controllers.api;

import play.mvc.Before;
import play.mvc.Controller;

/**
 * 对 API 调用的检查
 * User: wyattpan
 * Date: 8/10/12
 * Time: 4:10 PM
 */
public class APIChecker extends Controller {
    @Before
    public static void checkServer() {
        System.out.println(request.remoteAddress);
    }
}
