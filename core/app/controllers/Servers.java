package controllers;

import models.Server;
import play.mvc.Controller;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午1:58
 */
public class Servers extends Controller {

    public static void c(Server s) {
        Server saved = s.save();
        if(saved != null) {
            renderJSON(saved);
        } else {
            renderJSON("{flag: false}");
        }
    }

    public static void r(Long id) {
        renderJSON(Server.findById(id));
    }

    public static void p(Integer page) {
        renderJSON(Server.all().fetch(page, 10));
    }
}
