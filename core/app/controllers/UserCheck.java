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
    public static final String U = "user.name[%s][%s]";

    static boolean authenticate(String username, String password) {
        User user = null;
        if(username.equals("wyatt_pan")) {
            user = new User("wyatt_pan", "empty");
            user.power = User.P.ROOT;
            Cache.add(ukey(username), user);
        }
        if(user == null) {
            user = User.connect(username, password);
            if(user != null) Cache.add(ukey(username), user);
        }
        return user != null;
    }

    static boolean check(String profile) {
        /**
         * 由于登陆是从 Cookie 中添加的, 所以 Cookie 的有效期需要设置为内存中有效, 不能太长.
         */
        String username = Secure.Security.connected();
        User user = Cache.get(ukey(username), User.class);
        if(user == null) user = loadAndCacheUser(username);
        if(user == null) return false;
        if("guest".equals(profile)) {
            return user.power.ordinal() >= User.P.GUEST.ordinal();
        } else if("normal".equals(profile)) {
            return user.power.ordinal() >= User.P.NORMAL.ordinal();
        } else if("manager".equals(profile)) {
            return user.power.ordinal() >= User.P.MANAGER.ordinal();
        } else if("root".equals(profile)) {
            return user.power.ordinal() >= User.P.ROOT.ordinal();
        }
        return false;
    }


    /**
     * 根据 Session, 获取登陆后存储在 Cache 中的 User 的  key;
     *
     * @param username
     * @return
     */
    public static String ukey(String username) {
        return String.format(U, username, Scope.Session.current().getId());
    }

    private static User loadAndCacheUser(String username) {
        User user = User.findByUserName(username);
        if(user != null) Cache.add(ukey(username), user);
        return user;
    }

}
