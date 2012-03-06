package controllers;

import models.procure.Plan;
import models.procure.Supplier;
import play.mvc.Controller;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/5/12
 * Time: 9:53 AM
 */
public class Procures extends Controller {
    public static void plan(int p, int s) {
        List<Plan> plans = Plan.find("ORDER BY id DESC").fetch(p, s);
        render(plans);
    }
    
    public static void planInfo(String pid) {
        Plan plan = Plan.findById(pid);
        List<Supplier> sups = Supplier.findAll();
        render(plan, sups);
    }
}
