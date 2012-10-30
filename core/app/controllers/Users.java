package controllers;

import models.Privilege;
import models.User;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * User 相关的操作
 * User: wyattpan
 * Date: 2/9/12
 * Time: 3:43 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Users extends Controller {

    @Check("users.index")
    public static void index() {
        List<User> users = User.findAll();
        List<Privilege> privileges = Privilege.findAll();
        render(users, privileges);
    }

    public static void privileges(Long id, List<Long> privilegeId) {
        if(privilegeId == null || privilegeId.size() == 0) renderJSON(new Ret(false, "必须选择权限"));
        User user = User.findById(id);
        user.addPrivileges(privilegeId);
        int size = user.privileges.size();
        renderJSON(new Ret(true, String.format("添加成功, 共 %s 个权限", size)));
    }

    public static void update(User user) {
        validation.valid(user);
        if(Validation.hasErrors()) {
            render("Users/index.html", user);
        }
        user.save();
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
}
