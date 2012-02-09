package controllers;

import models.User;
import play.cache.Cache;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User 相关的操作
 * User: wyattpan
 * Date: 2/9/12
 * Time: 3:43 PM
 */
@With({Secure.class})
public class Users extends Controller {

    public static void passwd(User u) {
        validation.required(u.username);
        validation.required(u.password);
        if(!u.username.equalsIgnoreCase(Secure.Security.connected()))
            renderJSON(new Error("username", "you can not change others password.", new String[]{}));
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        User oldUser = Cache.get(UserCheck.ukey(Secure.Security.connected()), User.class);
        if(oldUser == null) {
            if(Secure.Security.isConnected())
                oldUser = User.find("username=?", u.username).first();
            else
                renderJSON(new Error("user", "User is not valid. Username or password is wrong! Or try to relogin.", new String[]{}));
        }

        oldUser.changePasswd(u.password);
        renderJSON(oldUser);
    }
}
