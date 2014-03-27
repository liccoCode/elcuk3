package controllers.api;

import models.Server;
import play.Logger;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Http.Header;

/**
 * 对 API 调用的检查
 * User: wyattpan
 * Date: 8/10/12
 * Time: 4:10 PM
 */
public class APIChecker extends Controller {
    @Before
    public static void checkServer() {
        Header head = request.headers.get("auth_token");
        if(head == null) {
            Logger.info("head is null");
            forbidden("head is null!");
        }
        String token = head.toString();
        if(token == null) {
            Logger.info("token is null");
            forbidden("token is null!");
        }

        String md5 = "[baef851cab745d3441d4bc7ff6f27b28]";
        if(!token.equals(md5)) {
            Logger.info("token is invalid!");
            forbidden("token is invalid!");
        }
    }
}
