package controllers;

import controllers.api.SystemOperation;
import models.Server;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午1:58
 * @deprecated Servers 已经无用
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class Servers extends Controller {

    @Check("servers.index")
    public static void index() {
        List<Server> sers = Server.all().fetch();
        render(sers);
    }

    public static void blank(Server s) {
        if(s == null) s = new Server();
        render(s);
    }

    public static void create(Server s) {
        validation.valid(s);
        if(Validation.hasErrors()) {
            render("Servers/blank.html", s);
        }
        s.save();
        flash.success("%s 创建成功.", s.name);
        redirect("/servers/index");
    }

    public static void edit(long id) {
        Server s = Server.findById(id);
        render(s);
    }

    public static void update(Server s) {
        if(!s.isPersistent()) {
            flash.error("Server %s 不存在与系统中, 请重新添加.", s.name);
            render("Servers/blank.html", s);
        }
        validation.valid(s);
        if(Validation.hasErrors()) {
            render("Servers/edit.html", s);
        }
        s.save();
        flash.success("%s 更新成功.", s.name);
        redirect("/servers/index");
    }

}
