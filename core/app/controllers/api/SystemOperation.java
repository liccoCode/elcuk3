package controllers.api;

import controllers.Login;
import models.Notification;
import models.OperatorConfig;
import models.User;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-10-17
 * Time: 下午3:07
 */
public class SystemOperation extends Controller {

    @Before(unless = {"login", "authenticate", "logout"})
    static void monitBefore() throws Throwable {
        //这里应该是用于记录整个应用所有 Controller 的数据统计信息.
        boolean isB2B = Objects.equals(OperatorConfig.getVal("brandname"), User.COR.MengTop.name());
        renderArgs.put("isB2B", isB2B);
    }
}

