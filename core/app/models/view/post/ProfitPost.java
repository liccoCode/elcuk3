package models.view.post;

import helper.Webs;
import models.market.M;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.*;

import models.view.report.Profit;
import services.MetricProfitService;
import services.MetricQtyService;
import models.product.Product;
import models.product.Category;

/**
 * 利润页面的搜索,不进入数据库
 * User: cary
 * Date: 3/11/14
 * Time: 6:59 PM
 */
public class ProfitPost extends Post<Profit> {
    public Date begin;
    public Date end;
    public String pmarket;
    public String sku;
    public String sellingId;
    public String category;

    public ProfitPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.end = now.toDate();
        DateTime from = DateTime.now().withDayOfYear(1);
        this.begin = from.toDate();
        this.pmarket = "market";
        this.perSize = 10;
        this.page = 1;
    }

    public ProfitPost(int perSize) {
        this.perSize = perSize;
    }


    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }

    /**
     * 计算总行数(带搜索条件的)
     *
     * @return
     */
    @Override
    public Long count(F.T2<String, List<Object>> params) {
        long count = 0;
        /**
         * 每个市场遍历
         */
        if(pmarket.equals("market")) {
            M[] marray = models.market.M.values();
            for(M m : marray) {
                count = calCount(count);
            }
        } else {
            count = calCount(count);
        }
        return count;
    }

    public long calCount(long count) {
        if(!StringUtils.isBlank(category) && StringUtils.isBlank(sku)) {
            Category cat = Category.findById(category);
            for(Product pro : cat.products) {
                count++;
            }

        } else if(!StringUtils.isBlank(sku)) {
            count++;
        }
        return count;
    }


    public M[] getMarket() {
        M[] marray = null;
        if(pmarket.equals("market")) {
            marray = models.market.M.values();

        } else {
            M skumarket = null;
            if(pmarket != null && !pmarket.equals("total")) {
                skumarket = M.valueOf(pmarket);
            }
            marray = new M[1];
            marray[0] = skumarket;
        }
        return marray;
    }

    @SuppressWarnings("unchecked")
    public List<Profit> query() {
        List<Profit> profitlist = new ArrayList<Profit>();
        /**
         * 每个市场遍历
         */
        M[] marray = getMarket();
        for(M m : marray) {
            profitlist = searchProfitList(profitlist, m);
        }
        return profitlist;
    }


    public List<Profit> calTotal(List<Profit> profits) {


        List<Profit> newprofits = new ArrayList<Profit>();
        /**
         * 计算每个SKU的合计
         */
        Profit skuprofit = initPorfit();
        if(profits.size() > 0) {
            Collections.sort(profits);

            skuprofit.sku = profits.get(0).sku;
            skuprofit.memo = skuprofit.sku + "合计";
            for(Profit p : profits) {
                p.totalfee = Webs.scale2PointUp(p.totalfee);
                p.amazonfee = Webs.scale2PointUp(p.amazonfee);
                p.fbafee = Webs.scale2PointUp(p.fbafee);
                p.procureprice = Webs.scale2PointUp(p.procureprice);
                p.shipprice = Webs.scale2PointUp(p.shipprice);
                p.vatprice = Webs.scale2PointUp(p.vatprice);
                p.totalprofit = Webs.scale2PointUp(p.totalprofit);
                p.profitrate = Webs.scale2PointUp(p.profitrate);
                p.workingfee = Webs.scale2PointUp(p.workingfee);
                p.wayfee = Webs.scale2PointUp(p.wayfee);
                p.inboundfee = Webs.scale2PointUp(p.inboundfee);

                if(skuprofit.sku.equals(p.sku)) {
                    p.sku = p.sku +String.valueOf(p.totalfee);
                    addProfit(skuprofit, p);
                } else {
                    skuprofit.sku = skuprofit.sku + "合计1";
                    newprofits.add(skuprofit);
                    skuprofit = initPorfit();
                    skuprofit.sku = p.sku;
                    skuprofit.memo = skuprofit.sku + "合计1";
                    addProfit(skuprofit, p);
                }

                newprofits.add(p);
            }
            skuprofit.sku = skuprofit.sku + "合计1";
            newprofits.add(skuprofit);
        }


        M[] marray = getMarket();
        Profit totalp = initPorfit();
        totalp.sku = "所有合计1";
        totalp.memo = "所有合计1";
        totalp.market = null;
        for(Profit p : profits) {
            addProfit(totalp, p);
        }
        for(M m : marray) {
            Profit mp = initPorfit();
            mp.sku = m.label() + "合计1";
            mp.market = m;
            mp.memo = m.label() + "合计1";
            for(Profit p : profits) {
                if(p.market == m) {
                    addProfit(mp, p);
                }
            }
            newprofits.add(mp);
        }
        newprofits.add(totalp);
        return newprofits;

    }


    private Profit initPorfit() {
        Profit totalp = new Profit();
        totalp.totalfee = 0f;
        totalp.amazonfee = 0f;
        totalp.fbafee = 0f;
        totalp.quantity = 0f;
        totalp.totalprofit = 0f;
        totalp.workingqty = 0;
        totalp.wayqty = 0;
        totalp.inboundqty = 0;
        totalp.workingfee = 0f;
        totalp.wayfee = 0f;
        totalp.inboundfee = 0f;
        return totalp;
    }

    private Profit addProfit(Profit total, Profit p) {
        total.totalfee += p.totalfee;
        total.amazonfee += p.amazonfee;
        total.fbafee += p.fbafee;
        total.quantity += p.quantity;
        total.totalprofit += p.totalprofit;
        total.workingqty += p.workingqty;
        total.wayqty += p.wayqty;
        total.inboundqty += p.inboundqty;
        total.workingfee += p.workingfee;
        total.wayfee += p.wayfee;
        total.inboundfee += p.inboundfee;
        return total;
    }


    public List<Profit> searchProfitList(List<Profit> profitlist, M skumarket) {
        /**
         * 如果有类别，没有SKU，则查询类别下所有SKU的利润
         */
        if(!StringUtils.isBlank(category) && StringUtils.isBlank(sku)) {
            Category cat = Category.findById(category);
            for(Product pro : cat.products) {
                Profit profit = esProfit(begin, end, skumarket, pro.sku, sellingId);
                profitlist.add(profit);
            }
        } else if(!StringUtils.isBlank(sku)) {
            Profit profit = esProfit(begin, end, skumarket, sku, sellingId);
            profitlist.add(profit);
        }
        return profitlist;
    }

    /**
     * 调用ES的API,获取利润Profit对象
     *
     * @param begin
     * @param end
     * @param market
     * @param prosku
     * @param sellingId
     * @return
     */
    public Profit esProfit(Date begin, Date end, M market,
                           String prosku, String sellingId) {
        MetricProfitService service = new MetricProfitService(begin, end, market, prosku, sellingId);
        Profit profit = service.calProfit();


        //增加库存数据
        MetricQtyService qtyservice = new MetricQtyService(market, prosku);
        profit = qtyservice.calProfit(profit);

        /**
         * (制作中+已交货)库存占用资金总金额(USD)
         */
        profit.workingfee = profit.workingqty * profit.procureprice;
        profit.workingfee = Webs.scale2PointUp(profit.workingfee);
        /**
         * 在途库存占用资金总金额(USD)
         */
        profit.wayfee = profit.wayqty * profit.procureprice + profit.wayqty * profit.shipprice;
        profit.wayfee = Webs.scale2PointUp(profit.wayfee);
        /**
         * (入库+在库)库存占用资金总金额(USD)
         */
        profit.inboundfee = profit.inboundqty * profit.procureprice + profit.inboundqty * profit.shipprice
                + profit.inboundqty * profit.vatprice;
        profit.inboundfee = Webs.scale2PointUp(profit.inboundfee);

        return profit;
    }

}