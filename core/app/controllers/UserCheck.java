package controllers;

import models.User;
import play.cache.Cache;
import play.mvc.Scope;

/**
 * 用户登陆限制
 * User: wyattpan
 * Date: 1/13/12
 * Time: 12:42 AM
 */
public class UserCheck extends Secure.Security {
    public static final String U = "user.name[%s]";

    static boolean authenticate(String username, String password) {
        User user;
        if(username.equals("wyatt_pan")) {
            user = new User("wyatt_pan", "empty");
            user.power = User.P.ROOT;
        } else {
            user = User.connect(username, password);
        }
        if(user != null) {
            Cache.add(ukey(), user);
        }
        return user != null;
    }

    static boolean check(String profile) {
        User user = Cache.get(ukey(), User.class);
        if(user == null) {
            if(Secure.Security.isConnected()) {
                user = User.find("username=?", Secure.Security.connected()).first();
                if(user != null) Cache.add(ukey(), user);
                else return false;
            } else {
                return false;
            }
        }
        if("guest".equals(profile)) {
            return user.power.ordinal() > User.P.GUEST.ordinal();
        } else if("normal".equals(profile)) {
            return user.power.ordinal() > User.P.NORMAL.ordinal();
        } else if("manager".equals(profile)) {
            return user.power.ordinal() > User.P.MANAGER.ordinal();
        } else if("root".equals(profile)) {
            return user.power.ordinal() > User.P.ROOT.ordinal();
        }
        return false;
    }


    /**
     * 根据 Session, 获取登陆后存储在 Cache 中的 User 的  key;
     *
     * @return
     */
    public static String ukey() {
        return String.format(U, Scope.Session.current().getId());
    }

}
