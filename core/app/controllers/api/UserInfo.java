package controllers.api;

import helper.J;
import models.User;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询erp系统的用户信息
 * <p/>
 * User: mac
 * Date: 14-11-6
 * Time: AM9:51
 */
@With(APIChecker.class)
public class UserInfo extends Controller {

    /**
     * 查询用户信息
     */
    public static void user() {
        List<User> users = User.find("closed!=true").fetch();
        List<String> usernames = new ArrayList<>();
        for(User user : users) usernames.add(user.username);
        renderJSON(J.json(usernames));
    }
}