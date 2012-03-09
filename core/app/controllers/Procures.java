package controllers;

import models.market.Account;
import models.market.Listing;
import models.procure.Plan;
import models.procure.Supplier;
import play.mvc.Controller;

import java.util.*;

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


    // -------------
    public static void warn() {
        List<Listing> listings = Listing.findAll();

        // 不同市场的可用的 Category
        Map<String, List<String>> avaCats = new HashMap<String, List<String>>();
        Map<String, Set<String>> avaCatsTmp = new HashMap<String, Set<String>>();
        for(Listing li : listings) {
            if(avaCatsTmp.containsKey(li.market.name())) {
                avaCatsTmp.get(li.market.name()).add(li.product.category.categoryId);
            } else {
                Set<String> cates = new HashSet<String>();
                cates.add(li.product.category.categoryId);
                avaCatsTmp.put(li.market.name(), cates);
            }
        }
        for(String market : avaCatsTmp.keySet()) {
            List<String> list = new ArrayList<String>(avaCatsTmp.get(market));
            Collections.sort(list);
            avaCats.put(market, list);
        }
        render(avaCats);
    }

    public static void warnItm(String market, String cat, int page) {
        List<Listing> lists = Listing.find("market=? AND product.category.categoryId=?", Account.M.val(market), cat).fetch(page, 20);
    }
}
