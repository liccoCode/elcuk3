package controllers.api;

import play.mvc.Before;
import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-10-17
 * Time: 下午3:07
 */
public class SystemOperation extends Controller {

    @Before(unless = {"login", "authenticate", "logout"})
    static void monitBefore() throws Throwable {
        //TODO: 这里应该是用于记录整个应用所有 Controller 的数据统计信息.
    }
}
