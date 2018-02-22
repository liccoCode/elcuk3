package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import models.Privilege;
import models.Role;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 3/4/14
 * Time: 11:05 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Roles extends Controller {

    @Before(only = {"show", "update", "delete"})
    public static void setUpShowPage() {
        List<Role> roles = Role.all().fetch();
        List<Privilege> modules = Privilege.find("pid=0").fetch();
        Map<Long, List<Privilege>> maps = Privilege.getMenuMap(modules);
        List<Privilege> privileges = Privilege.findAll();
        renderArgs.put("roles", roles);
        renderArgs.put("privileges", privileges);
        renderArgs.put("maps", maps);
        renderArgs.put("modules", modules);
    }

    @Check("teams.show")
    public static void show(Long id) {
        List<Role> roles = renderArgs.get("roles", List.class);
        Role role = new Role();
        if(roles != null && roles.size() > 0) {
            role = (Role) roles.get(0);
            if(id != null && id != 0L) role = Role.findById(id);
        }
        render(role);
    }

    public static void update(Role role) {
        if(!Role.exist(role.roleId)) {
            flash.error(String.format("Team %s 不存在!", role.roleId));
            redirect("/Roles/show");
        }
        validation.valid(role);
        if(Validation.hasErrors()) render("Teams/show.html", role);
        role.save();
        redirect("/Roles/show/" + role.roleId);
    }


    public static void delete(Long id) {
        checkAuthenticity();
        Role role = Role.findById(id);
        role.deleteRole();
        if(Validation.hasErrors()) render("Roles/show.html", role);
        flash.success("Team %s 删除成功", role.roleId);
        redirect("/Roles/show");
    }

    public static void blank() {
        Role role = new Role();
        render(role);
    }

    public static void create(Role role) {
        if(StringUtils.isBlank(role.roleName)) Validation.addError("", "Role Name 必须填写");
        validation.valid(role);
        if(Validation.hasErrors()) render("Roles/blank.html", role);
        role.save();
        flash.success("创建成功.");
        redirect("/Roles/show/" + role.roleId);
    }


    public static void addPrivileges(Long id, List<Long> privilegeId) {
        //if(privilegeId == null || privilegeId.size() == 0) renderJSON(new Ret(false, "必须选择权限"));
        Role role = Role.findById(id);
        try {
            role.addPrivileges(privilegeId);
        } catch(Exception e) {
            renderJSON(new Ret(false, Webs.e(e)));
        }
        int size = role.privileges.size();
        renderJSON(new Ret(true, String.format("添加成功, 共 %s 个权限", size)));
    }

    public static void showUser(Long roleId) {
        Role role = Role.findById(roleId);
        render(role);
    }
}
