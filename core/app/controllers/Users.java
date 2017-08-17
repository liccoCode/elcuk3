package controllers;

import controllers.api.SystemOperation;
import helper.J;
import helper.Webs;
import models.*;
import models.product.Category;
import models.product.Team;
import models.view.Ret;
import models.view.post.UserPost;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.RandomStringUtils;
import play.Logger;
import play.data.validation.Validation;
import play.libs.Crypto;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.*;

/**
 * User 相关的操作
 * User: wyattpan
 * Date: 2/9/12
 * Time: 3:43 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Users extends Controller {

    @Check("users.index")
    public static void index(UserPost p) {
        if(p == null) p = new UserPost();
        List<User> users = p.query();
        List<Privilege> privileges = Privilege.findAll();
        List<Team> teams = Team.findAll();
        List<Role> roles = Role.findAll();
        List<Category> categories = Category.findAll();

        List<Privilege> modules = Privilege.find("pid=0").fetch();
        Map<Long, List<Privilege>> maps = Privilege.getMenuMap(modules);
        renderArgs.put("maps", maps);
        renderArgs.put("modules", modules);
        renderArgs.put("categories", categories);
        render(users, privileges, teams, roles, p);
    }

    @Before(only = {"home", "updates"})
    public static void setHomePage() {
        int page = NumberUtils.toInt(request.params.get("page"), 1);
        List<Notification> notifications = new ArrayList<>();
        User user = Login.current();
        if(user != null) {
            notifications = user.notificationFeeds(page);
        }
        renderArgs.put("notifications", notifications);
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
            renderJSON(new Ret(false, Webs.e(e)));
        }
        int size = user.privileges.size();
        renderJSON(new Ret(true, String.format("添加成功, 共 %s 个权限", size)));
    }

    public static void teams(Long id, List<Long> teamId) {
        //if(teamId == null || teamId.size() == 0) renderJSON(new Ret(false, "必须选择Team"));
        User user = User.findById(id);
        try {
            user.addTeams(teamId);
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.e(e)));
        }
        int size = user.teams.size();
        renderJSON(new Ret(true, String.format("添加成功, 共 %s 个Team", size)));
    }

    public static void categories(Long id, List<String> categoryId) {
        User user = User.findById(id);
        try {
            user.addCategories(categoryId);
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.e(e)));
        }
        int size = user.categories.size();
        renderJSON(new Ret(true, String.format("添加成功, 共 %s 个Category", size)));
    }

    public static void roles(Long id, List<Long> roleId) {
        //if(roleId == null || roleId.size() == 0) renderJSON(new Ret(false, "必须选择角色"));
        User user = User.findById(id);
        try {
            user.addRoles(roleId);
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.e(e)));
        }
        int size = user.roles.size();
        renderJSON(new Ret(true, String.format("添加成功, 共 %s 个Role", size)));
    }

    public static void updates(User wuser, Long userid, String newPassword, String newPasswordConfirm) {

        User user = User.findById(userid);
        if(!user.authenticate(wuser.password)) {
            Validation.addError("", "用户密码错误, 请确认当前用户的密码正确");
        }
        // 如果填写了新密码, 那么则需要修改密码
        if(StringUtils.isNotBlank(newPassword)) {
            Validation.equals("Password", newPassword, "Confirm Password", newPasswordConfirm);
        }
        if(Validation.hasErrors())
            render("Users/home.html", user);

        try {
            user.tel = wuser.tel;
            user.wangwang = wuser.wangwang;
            user.phone = wuser.phone;
            user.qq = wuser.qq;
            if(StringUtils.isNotBlank(newPassword)) {
                user.changePasswd(newPassword);
            } else {
                user.update();
            }
        } catch(Exception e) {
            Logger.error(Webs.s(e));
            Validation.addError("", Webs.e(e));
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
            Validation.addError("", Webs.e(e));
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
            renderJSON(J.g(user));
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.e(e)));
        }
    }

    public static void pass(String p) {
        renderText(Crypto.encryptAES(p));
    }

    public static void create() {
        String brandName = OperatorConfig.getVal("brandname");
        render(brandName);
    }

    /**
     * 添加新的用户
     *
     * @param user
     */
    @Check("users.index")
    public static void addUser(User user) {
        try {
            if(StringUtils.isBlank(user.username)) Webs.error("用户名不能为空");
            if(StringUtils.isBlank(user.email)) Webs.error("Email不能为空");
            if(StringUtils.isBlank(user.password)) Webs.error("密码不能为空");
            if(StringUtils.isBlank(user.confirm)) Webs.error("确认密码不能为空");
            if(!StringUtils.equals(user.password, user.confirm)) Webs.error("密码和确认密码填写不一致");
            if(User.count("username=?", user.username) > 0) Webs.error("用户名已经存在，请重新输入！");
            user.username = user.username.toLowerCase();
            user.save();
            flash.success("创建用户成功");
            redirect("/users/index");
        } catch(FastRuntimeException e) {
            flash.error(e.getMessage());
            render("Users/create.html", user);
        }
    }

    /**
     * 关闭用户
     * 1. 用户相关的历史权限的清理
     * 2. 修改用户的密码为随机
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
            /**
             * 清理TEAM的信息
             */
            Team.clearUserTeamsCache(user);
            user.privileges = new HashSet<>();
            user.password = RandomStringUtils.randomAlphanumeric(15);
            user.closed = true;
            user.save();
        } catch(Exception e) {
            renderJSON(new Ret("oOh~出现了一个错误: " + Webs.e(e)));
        }
        renderJSON(new Ret(true, "关闭用户成功"));
    }

    /**
     * 打开用户
     * 1. 打开用户相关的权限信息
     * 2. 修改用户的密码为随机生成的15位的字母和数字的组合
     * 3.将用户的状态改为未关闭
     *
     * @param id
     */
    @Check("users.index")
    public static void openUser(long id) {
        User user = User.findById(id);
        if(user == null)
            renderJSON(new Ret("用户不存在，无法打开"));
        try {
            Set<Privilege> privileges = Privilege.privileges(user.username, user.roles);
            Privilege.updatePrivileges(user.username, privileges);

            Set<Team> teams = Team.teams(user.username);
            Team.updateTeams(user.username, teams);

            user.password = RandomStringUtils.randomAlphanumeric(15);
            user.closed = false;
            user.save();
        } catch(Exception e) {
            renderJSON(new Ret("oOh~出现了一个错误: " + Webs.e(e)));
        }
        renderJSON(new Ret(true, String.format("打开用户成功,初始密码为 %s ,请联系管理员添加权限", user.password)));
    }
}
