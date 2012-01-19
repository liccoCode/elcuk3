package controllers;

import helper.Webs;
import models.market.Orderr;
import play.mvc.Controller;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-6
 * Time: 下午4:02
 */
public class Orders extends Controller {

    public static void o_index(Integer p, Integer s) {
        Webs.fixPage(p, s);
        List<Orderr> orders = Orderr.find("ORDER BY createDate DESC").fetch(p, s);
        Long count = Orderr.count();
        render(orders, count, p, s);
    }
}
