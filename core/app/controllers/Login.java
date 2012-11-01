package controllers;

import models.Privilege;
import models.User;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户登陆限制
 * User: wyattpan
 * Date: 1/13/12
 * Time: 12:42 AM
 */
public class Login extends Secure.Security {

    /**
     * 登陆
     */
    static boolean authenticate(String username, String password) {
        /**
         * 1. 判断是否拥有此用户; 使用公司邮箱 @easyacceu.com
         * 2. 判断用户登陆是否正常
         */
        User user = User.findByUserName(username);
        if(user == null) return false;
        return user.authenticate(password);
    }

    static boolean check(String profile) {
        User user = current();
        if(user == null) return false;
        if("root_user".equals(user.username)) return true;
        Set<Privilege> privileges = Privilege.privileges(user.username);
        Privilege privilege = (Privilege) CollectionUtils.find(privileges, new PrivilegePrediect(profile.toLowerCase()));
        return privilege != null;
    }

    /**
     * 在用户登出以前做处理
     */
    static void onDisconnect() {
        Login.current().logout();
    }

    @SuppressWarnings("unchecked")
    public static User current() {
        /**
         * 初始化:
         * 1. 将用户信息缓存到 Cache.
         * 2. 初始化 Notification Queue
         */

        // 初始化 Cache 中的 用户缓存
        Map<String, User> users = Cache.get("users", Map.class);
        if(users == null) {
            synchronized(Login.class) {
                users = Cache.get("users", Map.class);
                if(users == null) { // 双重检测缓存
                    users = new ConcurrentHashMap<String, User>();
                    Cache.add("users", users);
                }
            }
        }

        // 初始化用户缓存中的用户;
        if(users.get(Secure.Security.connected()) == null) {
            User user = User.findByUserName(Secure.Security.connected());
            user.login();
            users.put(user.username, user);
        }
        return users.get(Secure.Security.connected());
    }

    @SuppressWarnings("unchecked")
    public static User updateUserCache(User user) {
        Map<String, User> users = Cache.get("users", Map.class);
        users.put(user.username, user);
        return users.get(user.username);
    }


    /**
     * 过滤权限
     */
    static class PrivilegePrediect implements Predicate {
        private String actionName;

        PrivilegePrediect(String actionName) {
            this.actionName = actionName;
        }

        @Override
        public boolean evaluate(Object o) {
            Privilege pri = (Privilege) o;
            return StringUtils.equals(this.actionName, pri.name);
        }
    }
}
