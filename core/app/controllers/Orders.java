package controllers;

import models.market.Orderr;
import play.mvc.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-6
 * Time: 下午4:02
 */
public class Orders extends Controller {

    public static void index() {
        List<Orderr> orders = Orderr.findAll();
        List<Orderr> noItems = new ArrayList<Orderr>();
        Map<String, Integer> rtMap = new HashMap<String, Integer>();
        for(Orderr o : orders) {
            if(o.items == null || o.items.size() <= 0) {
                noItems.add(o);
            } else {
                rtMap.put(o.orderId, o.items.size());
            }
        }
        if(noItems.size() != 0) {
            renderJSON(noItems);
        } else {
            rtMap.put("mapSize", rtMap.size());
            renderJSON(rtMap);
        }
    }
}
