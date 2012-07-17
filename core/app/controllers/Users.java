package controllers;

import helper.J;
import models.User;
import models.view.Ret;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

/**
 * User 相关的操作
 * User: wyattpan
 * Date: 2/9/12
 * Time: 3:43 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Users extends Controller {

    public static void passwd(User u) {
        validation.valid(u.username);
        if(!validation.equals(u.password, u.confirm).ok)
            renderJSON(new Ret("Two time password is not the same."));
        if(!u.username.equalsIgnoreCase(Secure.Security.connected()))
            renderJSON(new Ret("You can only change " + Secure.Security.connected() + "`s password."));
        if(Validation.hasErrors())
            renderJSON(new Ret(J.json(Validation.errors())));
        User managedUser = User.findByUserName(Secure.Security.connected());
        if(managedUser == null)
            renderJSON(new Ret("User is not valid. Username or password is wrong!"));
        else {
            managedUser.changePasswd(u.password);
            renderJSON(J.json(managedUser));
            Cache.delete(UserCheck.ukey(Secure.Security.connected()));
            Cache.add(UserCheck.ukey(Secure.Security.connected()), managedUser);
        }
    }
}
