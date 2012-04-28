package controllers;

import models.Ret;
import models.product.AttrName;
import play.mvc.Controller;

import java.util.List;

/**
 * 对 Attribute 的控制
 * User: wyattpan
 * Date: 4/28/12
 * Time: 2:59 PM
 */
public class Attrs extends Controller {

    public static void index() {
        List<AttrName> attrs = AttrName.all().fetch();
        render(attrs);
    }

    public static void detail(String name) {
        AttrName att = AttrName.findById(name);
        render(att);
    }

    public static void create(AttrName a) {
        if(a.isPersistent()) renderJSON(new Ret("AttrName[" + a.name + "] 已经存在!"));
        a.save();
        renderJSON(new Ret());
    }

    public static void update(AttrName a) {
        if(!a.isPersistent()) renderJSON(new Ret("AttrName[" + a.name + "] 不存在!"));
        a.save();
        renderJSON(new Ret());
    }
}
