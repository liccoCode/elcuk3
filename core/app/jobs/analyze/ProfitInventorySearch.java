package jobs.analyze;


import models.procure.CooperItem;
import models.view.post.ProfitPost;
import models.view.report.Profit;
import org.apache.commons.lang.StringUtils;
import play.cache.Cache;
import play.jobs.Job;
import play.jobs.On;

import java.util.*;

import models.ProfitInventory;

/**
 * 计算库存的成本
 * <p/>
 * User: cary
 * Date: 5/14/2015
 * Time: 3:06 PM
 */
@On("0 20 0,7,15 * * ?")
public class ProfitInventorySearch extends Job {

    private String RUNNING = "profitinventorysearch_running";
    private ProfitPost post;

    public ProfitInventorySearch(ProfitPost p) {
        this.post = p;
    }

    @Override
    public void doJob() {
        String skukey = "";
        String marketkey = "";
        String categorykey = "";
        if(post.sku != null) skukey = post.sku;
        if(post.pmarket != null) marketkey = post.pmarket;
        if(post.category != null) categorykey = post.category.toLowerCase();
        String postkey = helper.Caches.Q.cacheKey("profitpost", post.begin, post.end, categorykey, skukey, marketkey);
        if(isRnning(postkey)) return;
        Cache.add(postkey + RUNNING, postkey + RUNNING);
        List<Profit> profits = new ArrayList<Profit>();
        //从ES查找SKU的利润
        profits = post.Inventory();
        for(Profit p : profits) {
            ProfitInventory inv = new ProfitInventory();
            inv.sku = p.sku;
            inv.market = p.market.toString();
            inv.procureprice = p.procureprice;
            inv.shipprice = p.shipprice;
            inv.vatprice = p.vatprice;
            inv.workingqty = p.workingqty;
            inv.wayqty = p.wayqty;
            inv.inboundqty = p.inboundqty;
            inv.workingfee = p.workingfee;
            inv.wayfee = p.wayfee;
            inv.inboundfee = p.inboundfee;
            List<CooperItem> item = CooperItem.find("sky=?", inv.sku).fetch();
            if(item != null && item.size() > 0) {
                inv.cooperator = item.get(0).cooperator.name;
            }
            inv.save();
        }

        Cache.add(postkey, profits, "2h");
        Cache.delete(postkey + RUNNING);
    }

    /**
     * 确保同一时间只有一个 analyzes 正在计算
     *
     * @return
     */
    public boolean isRnning(String postkey) {
        return StringUtils.isNotBlank(Cache.get(postkey + RUNNING, String.class));
    }
}