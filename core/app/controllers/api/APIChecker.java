package controllers.api;

import models.Server;
import play.Logger;
import play.Play;
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
        Server server = Server.find("ipAddress=?", request.remoteAddress).first();
        if(Play.mode.isProd()) {
            if(server == null) forbidden();
        } else {
            Logger.info("%s requrst API.", request.remoteAddress);
        }
    }
}
