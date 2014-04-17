package controllers.api;

import play.Logger;
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
        String token = request.params.get("auth_token");
        if(token == null) {
            Logger.info("token is null");
            forbidden("token is null!");
        }

        String md5 = "baef851cab745d3441d4bc7ff6f27b28";
        if(!token.equals(md5)) {
            Logger.info("token is invalid!");
            forbidden("token is invalid!");
        }
    }
}
