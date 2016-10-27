package jobs.analyze;


import models.ProfitInventory;
import models.procure.CooperItem;
import models.product.Category;
import models.view.post.ProfitPost;
import models.view.report.Profit;
import play.jobs.Job;

import java.util.List;

/**
 * 计算库存的成本
 * <p/>
 * User: cary
 * Date: 5/14/2015
 * Time: 3:06 PM
 */
//@On("0 20 0,7,15 * * ?")
public class ProfitInventorySearch extends Job {

    private ProfitPost post;

    public ProfitInventorySearch(ProfitPost p) {
        this.post = p;
    }

    @Override
    public void doJob() {
        List<Category> categorys = Category.all().fetch();
        for(Category cate : categorys) {
            report(cate.categoryId);
        }
    }


    public void report(String categoryid) {
        post.category = categoryid;
        String skukey = "";
        String marketkey = "";
        String categorykey = "";
        if(post.sku != null) skukey = post.sku;
        if(post.pmarket != null) marketkey = post.pmarket;
        if(post.category != null) categorykey = post.category.toLowerCase();
        //从ES查找SKU的利润
        List<Profit> profits = post.Inventory();
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
            List<CooperItem> item = CooperItem.find("sku=?", inv.sku).fetch();
            if(item != null && item.size() > 0) {
                inv.cooperator = item.get(0).cooperator.name;
            }
            inv.save();
        }
    }
}