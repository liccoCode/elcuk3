package controllers;

import models.product.Whouse;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.mvc.Controller;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-8
 * Time: 上午7:32
 */
public class Whouses extends Controller {

    public static void c(@Valid Whouse w) {
        if(Validation.hasErrors()) {
            renderJSON(validation.errorsMap());
        }
        w.save();
        w.qtys = null; // 排除显示 qty
        renderJSON(w);
    }

    public static void r(Long id) {
        Whouse w = Whouse.findById(id);
        if(w != null) w.qtys = null;
        renderJSON(w);
    }

    public static void u(Whouse w) {
        validation.required(w.id);
        validation.required(w.name);
        w.save();
        w.qtys = null;
        renderJSON(w);
    }

    public static void d(Long id) {
        Whouse w = Whouse.findById(id);
        // Warehouse 的删除利用 @PreRemove 进行了检查
        renderJSON(w.delete());
    }

    public static void p(Integer page) {
        List<Whouse> whs = Whouse.all().fetch(page, 10);
        for(Whouse w : whs) {
            w.qtys = null;
        }
        renderJSON(whs);
    }

}
