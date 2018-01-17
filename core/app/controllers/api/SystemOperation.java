package controllers.api;

import controllers.Login;
import models.OperatorConfig;
import models.User;
import play.Logger;
import play.Play;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-10-17
 * Time: 下午3:07
 */
public class SystemOperation extends Controller {

    @Before(unless = {"login", "authenticate", "logout", "updates"})
    static void monitBefore() throws Throwable {
        User user = Login.current();
        String brandName = OperatorConfig.getVal("brandname");
        renderArgs.put("brandName", brandName);
        if(user.passwordDigest.equals(Crypto.encryptAES("123456"))) {
            flash.error("请先修改您的初始密码，不能为123456！");
            render("Users/home.html", user);
        }
        //这里应该是用于记录整个应用所有 Controller 的数据统计信息.
        boolean isB2B = Objects.equals(OperatorConfig.getVal("brandname"), User.COR.MengTop.name());
        renderArgs.put("isB2B", isB2B);
        Logger.info("playurl:%s playpath:%s username:%s ", request.url, Play.ctxPath, user.username);

    }
}

