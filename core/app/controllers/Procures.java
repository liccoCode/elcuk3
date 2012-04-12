package controllers;

import helper.Caches;
import helper.PH;
import helper.Webs;
import models.PageInfo;
import models.Ret;
import models.market.Account;
import models.market.Listing;
import models.market.Selling;
import models.procure.PItem;
import models.procure.Plan;
import models.procure.Supplier;
import play.cache.Cache;
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

    @SuppressWarnings("unchecked")
    public static void warnItm(String market, String cat, int page) {
        List<Selling> sellings = Cache.get(String.format(Caches.WARN_ITEM_SELLING, market, cat), List.class);
        if(sellings == null) {
            if("all".equals(cat))
                sellings = Selling.find("market=? AND state!=?", Account.M.val(market), Selling.S.DOWN).fetch();
            else
                sellings = Selling.find("market=? AND listing.product.category.categoryId=? AND state!=?", Account.M.val(market), cat, Selling.S.DOWN).fetch();
            Selling.sortSellingWithQtyLeftTime(sellings);
            if(sellings != null && sellings.size() > 0)
                Cache.add(String.format(Caches.WARN_ITEM_SELLING, market, cat), sellings, "30mn");
        }
        List<PItem> pitms = new ArrayList<PItem>();
        int size = ((40 * page) > sellings.size() ? sellings.size() : (40 * page));
        for(int i = (page - 1) * 40; i < size; i++)
            pitms.add(sellings.get(i).calculatePItem());

        PageInfo<Selling> pif = new PageInfo<Selling>(pitms.size(), (long) sellings.size(), page, sellings);
        render(pitms, pif);
    }

    /**
     * 负责 Selling 的 Ps 修改与 Invisible 状态
     *
     * @param s
     * @param cat
     */
    public static void ps(Selling s, String cat) {
        if(!s.isPersistent()) renderJSON(new Ret("The Selling is not Persistent."));
        try {
            s.save();
            Cache.delete(String.format(Caches.WARN_ITEM_SELLING, s.market.name().toLowerCase(), cat));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret());
    }

    public static void pitem(PItem p, String id) {
        PItem pi = PH.unMarsh(id);
        if(pi == null) renderJSON(new Ret("Is not exist!"));
        if(p.onWay != null) pi.onWay = p.onWay;
        if(p.onWork != null) pi.onWork = p.onWork;
        if(p.seaBuy != null) pi.seaBuy = p.seaBuy;
        if(p.seaPatch != null) pi.seaPatch = p.seaPatch;
        if(p.airBuy != null) pi.airBuy = p.airBuy;
        if(p.airPatch != null) pi.airPatch = p.airPatch;
        PH.marsh(pi, id);
        renderJSON(new Ret());
    }


}
