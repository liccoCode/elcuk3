package controllers;

import helper.J;
import helper.Webs;
import models.Privilege;
import models.User;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Validation;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User 相关的操作
 * User: wyattpan
 * Date: 2/9/12
 * Time: 3:43 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class Users extends Controller {

    @Check("users.index")
    public static void index() {
        List<User> users = User.findAll();
        List<Privilege> privileges = Privilege.findAll();
        render(users, privileges);
    }

    @Before(only = {"home", "updates"})
    public static void setHomePage() {
        int page = NumberUtils.toInt(request.params.get("page"), 1);
        renderArgs.put("notifications", Login.current().notificationFeeds(page));
    }

    public static void home() {
        User user = Login.current();
        render(user);
    }

    public static void privileges(Long id, List<Long> privilegeId) {
        if(privilegeId == null || privilegeId.size() == 0) renderJSON(new Ret(false, "必须选择权限"));
        User user = User.findById(id);
        try {
            user.addPrivileges(privilegeId);
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.E(e)));
        }
        int size = user.privileges.size();
        renderJSON(new Ret(true, String.format("添加成功, 共 %s 个权限", size)));
    }

    public static void updates(User user, String newPassword, String newPasswordConfirm) {
        user.confirm = user.password;
        validation.valid(user);

        // 如果填写了新密码, 那么则需要修改密码
        if(StringUtils.isNotBlank(newPassword)) {
            validation.equals(newPassword, newPasswordConfirm);
        }
        if(Validation.hasErrors())
            render("Users/home.html", user);

        try {
            user.update();
            if(StringUtils.isNotBlank(newPassword))
                user.changePasswd(newPassword);
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
            render("Users/home.html", user);
        }
        flash.success("修改成功.");
        redirect("/users/home");
    }

    public static void update(User user) {
        validation.valid(user);
        if(Validation.hasErrors())
            render("Users/index.html", user);
        try {
            user.update();
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
            render("Users/index.html", user);
        }
        flash.success("修改成功.");
        redirect("/users/index");
    }

    public static void passwd(String username, String password, String confirm) {
        if(!username.equals(Secure.Security.connected())) {
            session.clear();
            flash.error("不允许修改其他人的密码.");
            redirect("/");
        }
        validation.required(password);
        validation.required(confirm);
        validation.equals(password, confirm);
        if(Validation.hasErrors()) {
            flash.error("修改密码失败.");
            redirect("/");
        }

        User.findByUserName(Secure.Security.connected()).changePasswd(password);
        // 更新当前缓存的密码
        flash.success("密码修改成功");
        redirect("/");
    }

    public static void showJson(long id) {
        try {
            User user = User.findById(id);
            renderJSON(J.G(user));
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.E(e)));
        }
    }

    public static void pass(String p) {
        renderText(Crypto.encryptAES(p));
    }

    public static void create() {
        render();
    }

    /**
     * 添加新的用户
     *
     * @param user
     */
    @Check("users.index")
    public static void addUser(User user) {
        validation.required(user.password);
        validation.required(user.confirm);
        validation.required(user.username);
        validation.required(user.password);
        if(validation.hasErrors()) {
            render("Users/create.html", user);
        }
        try {
            user.save();
        } catch(Exception e) {
            Validation.addError("", Webs.E(e));
            render("Users/create.html", user);
        }
        flash.success("添加用户成功");
        redirect("/users/index");
    }

    /**
     * 关闭用户
     * 1. 用户相关的历史权限的清理
     * 2. 修改用户的密码
     * 3. 将用户的状态改变为已关闭
     *
     * @param id
     */
    @Check("users.index")
    public static void closeUser(long id) {
        User user = User.findById(id);
        if(user == null)
            renderJSON(new Ret("用户不存在，无法关闭"));
        try {
            Privilege.clearUserPrivilegesCache(user);
            user.privileges = new HashSet<Privilege>();
            user.password = "easyacc" + System.currentTimeMillis();
            user.closed = true;
            user.save();
        } catch(Exception e) {
            renderJSON(new Ret("oOh~出现了一个错误: " + e.getMessage()));
        }
        renderJSON(new Ret(true, "关闭用户成功"));
    }

    @Check("users.index")
    public static void openUser(long id) {
        User user = User.findById(id);
        if(user == null)
            renderJSON(new Ret("用户不存在，无法打开"));
        try {
            Set<Privilege> privileges = Privilege.privileges(user.username);
            Privilege.updatePrivileges(user.username, privileges);
            user.password = "123";
            user.closed = false;
            user.save();
        } catch(Exception e) {
            renderJSON(new Ret("oOh~出现了一个错误: " + e.getMessage()));
        }
        renderJSON(new Ret(true, "打开用户成功,初始密码为123,请联系管理员添加权限"));
    }
}
