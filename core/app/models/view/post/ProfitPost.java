package models.view.post;

import com.alibaba.fastjson.JSON;
import helper.*;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.market.M;
import models.product.Category;
import models.product.Product;
import models.view.dto.ProfitDto;
import models.view.report.Profit;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Validation;
import play.libs.F;
import services.MetricProfitService;
import services.MetricQtyService;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 利润页面的搜索,不进入数据库
 * User: cary
 * Date: 3/11/14
 * Time: 6:59 PM
 */
public class ProfitPost {
    public Date begin;
    public Date end;
    public String pmarket;
    public String sku;
    public String sellingId;
    public String category;
    public int page = 1;
    public int perSize = 50;
    public long count = 1;

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


    public F.T2<String, List<Object>> params() {
        return null;
    }

    /**
     * 计算总行数(带搜索条件的)
     *
     * @return
     */
    public Long count(F.T2<String, List<Object>> params) {
        long flag = 0;
        /**
         * 每个市场遍历
         */
        if(pmarket.equals("market")) {
            M[] marray = models.market.M.values();
            for(M m : marray) {
                flag = calCount(flag);
            }
        } else {
            flag = calCount(flag);
        }
        return flag;
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
        List<Profit> profitlist = new ArrayList<>();
        /**
         * 每个市场遍历
         */
        M[] marray = getMarket();
        for(M m : marray) {
            profitlist = searchProfitList(profitlist, m, new SimpleDateFormat("yyyyMMdd").format(this.begin),
                    new SimpleDateFormat("yyyyMMdd").format(this.end));
        }
        return profitlist;
    }


    @SuppressWarnings("unchecked")
    public List<Profit> inventory() {
        List<Profit> profitlist = new ArrayList<>();
        /**
         * 每个市场遍历
         */
        M[] marray = models.market.M.values();
        for(M m : marray) {
            profitlist = searchInventoryList(profitlist, m);
        }
        return profitlist;
    }


    public List<Profit> calTotal(List<Profit> profits) {


        List<Profit> newprofits = new ArrayList<>();
        /**
         * 计算每个SKU的合计
         */
        Profit skuprofit = initPorfit();
        if(profits.size() > 0) {
            Collections.sort(profits);

            skuprofit.sku = profits.get(0).sku;
            skuprofit.memo = skuprofit.sku + "合计";
            for(Profit p : profits) {
                p.totalfee = Webs.scale2Double(p.totalfee);
                p.amazonfee = Webs.scale2Double(p.amazonfee);
                p.fbafee = Webs.scale2Double(p.fbafee);
                p.procureprice = Webs.scale2Double(p.procureprice);
                p.shipprice = Webs.scale2Double(p.shipprice);
                p.vatprice = Webs.scale2Double(p.vatprice);
                p.totalprofit = Webs.scale2Double(p.totalprofit);
                p.profitrate = Webs.scale2Double(p.profitrate);
                p.workingfee = Webs.scale2Double(p.workingfee);
                p.wayfee = Webs.scale2Double(p.wayfee);
                p.inboundfee = Webs.scale2Double(p.inboundfee);

                if(skuprofit.sku.equals(p.sku)) {
                    skuprofit = addProfit(skuprofit, p);
                } else {
                    skuprofit.sku = skuprofit.sku + "合计";
                    newprofits.add(skuprofit);
                    skuprofit = initPorfit();
                    skuprofit.sku = p.sku;
                    skuprofit.memo = skuprofit.sku + "合计";
                    skuprofit = addProfit(skuprofit, p);
                }

                newprofits.add(p);
            }
            skuprofit.sku = skuprofit.sku + "合计";
            newprofits.add(skuprofit);
        }


        M[] marray = getMarket();
        Profit totalp = initPorfit();
        totalp.sku = "所有合计";
        totalp.memo = "所有合计";
        totalp.market = null;
        for(Profit p : profits) {
            addProfit(totalp, p);
        }
        for(M m : marray) {
            Profit mp = initPorfit();
            if(m == null || m.label() == null) {
                mp.sku = "合计";
                mp.memo = "合计";
            } else {
                mp.sku = m.label() + "合计";
                mp.memo = m.label() + "合计";
            }

            mp.market = m;
            for(Profit p : profits) {
                if(p.market == m) {
                    mp = addProfit(mp, p);
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


    public List<Profit> searchProfitList(List<Profit> profitlist, M skumarket, String redisfrom, String redisto) {
        /**
         * 如果有类别，没有SKU，则查询类别下所有SKU的利润
         */
        if(!StringUtils.isBlank(category) || !StringUtils.isBlank(sku)) {
            List<ProfitDto> dtos = null;
            String key = category.toLowerCase();
            if(!StringUtils.isBlank(sku)) key = sku;
            String cacke_key = "profitmap_" + key + "_" + skumarket.name() + "_"
                    + redisfrom
                    + "_"
                    + redisto;
            String cache_str = Caches.get(cacke_key);
            if(!StringUtils.isBlank(cache_str)) {
                dtos = JSON.parseArray(cache_str, ProfitDto.class);
            }
            if(dtos == null) return profitlist;

            Map<String, ProfitDto> profitmap = new HashMap<>();
            for(ProfitDto dto : dtos) {
                profitmap.put(dto.sku, dto);
            }
            if(!StringUtils.isBlank(sku)) {
                Profit profit = redisProfit(profitmap, begin, end, skumarket, sku, sellingId);
                if(profit.totalfee != 0 || profit.amazonfee != 0
                        || profit.fbafee != 0 || profit.quantity != 0
                        || profit.workingqty != 0 || profit.wayqty != 0 || profit.inboundqty != 0) {
                    profitlist.add(profit);
                }
            } else {
                Category cat = Category.find("lower(categoryId)=?", category.toLowerCase()).first();
                for(Product pro : cat.products) {
                    Profit profit = redisProfit(profitmap, begin, end, skumarket, pro.sku, sellingId);
                    if(profit.totalfee != 0 || profit.amazonfee != 0
                            || profit.fbafee != 0 || profit.quantity != 0
                            || profit.workingqty != 0 || profit.wayqty != 0 || profit.inboundqty != 0) {
                        profitlist.add(profit);
                    }
                }
            }
        }
        return profitlist;
    }


    public List<Profit> searchInventoryList(List<Profit> profitlist, M skumarket) {
        /**
         * 如果有类别，没有SKU，则查询类别下所有SKU的利润
         */
        if(!StringUtils.isBlank(category) && StringUtils.isBlank(sku)) {
            Category cat = Category.findById(category);
            for(Product pro : cat.products) {
                Profit inventoryprofit = inventoryProfit(begin, end, skumarket, pro.sku, sellingId);
                if(inventoryprofit.workingqty != 0 || inventoryprofit.wayqty != 0 || inventoryprofit.inboundqty != 0) {
                    Profit profit = esProfit(begin, end, skumarket, pro.sku, sellingId);
                    profitlist.add(profit);
                }
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
        try {
            end = Dates.night(end);
            MetricProfitService service = new MetricProfitService(begin, end, market, prosku, sellingId);
            Profit profit = service.calProfit();

            //增加库存数据
            MetricQtyService qtyservice = new MetricQtyService(market, prosku);
            profit = qtyservice.calProfit(profit);

            /**
             * (制作中+已交货)库存占用资金总金额(USD)
             */
            profit.workingfee = profit.workingqty * profit.procureprice;
            profit.workingfee = Webs.scale2Double(profit.workingfee);
            /**
             * 在途库存占用资金总金额(USD)
             */
            profit.wayfee = profit.wayqty * profit.procureprice + profit.wayqty * profit.shipprice
                    + profit.wayqty * profit.vatprice;
            profit.wayfee = Webs.scale2Double(profit.wayfee);
            /**
             * (入库+在库)库存占用资金总金额(USD)
             */
            profit.inboundfee = profit.inboundqty * profit.procureprice + profit.inboundqty * profit.shipprice
                    + profit.inboundqty * profit.vatprice;
            profit.inboundfee = Webs.scale2Double(profit.inboundfee);

            return profit;
        } catch(Exception e) {
            Logger.info("profit.esProfit:::" + e.toString());
        }
        return initProfit(market,
                prosku, sellingId);
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
    public Profit redisProfit(Map<String, ProfitDto> profitmap, Date begin, Date end, M market,
                              String prosku, String sellingId) {
        try {
            ProfitDto dto = profitmap.get(prosku);
            if(dto == null) return initProfit(market, prosku, sellingId);
            Profit profit = new Profit();
            profit.sku = prosku;
            profit.market = market;
            //总销售额
            profit.totalfee = Webs.scale2Double(dto.fee);
            //亚马逊费用
            profit.amazonfee = Webs.scale2Double(dto.amazon_fee);
            //fba费用
            profit.fbafee = Webs.scale2Double(dto.fba_fee);
            //总销量
            profit.quantity = dto.qty;
            //采购价格
            profit.procureprice = Webs.scale2Double(dto.purchase_price);
            //运输价格
            profit.shipprice = Webs.scale2Double(dto.ship_price);
            //vat价格
            profit.vatprice = Webs.scale2Double(dto.vat_price);
            //利润
            profit.totalprofit = Webs.scale2Double(dto.total_profit);
            //利润率
            profit.profitrate = Webs.scale2Double(dto.profit_rate);

            //增加库存数据
            MetricQtyService qtyservice = new MetricQtyService(market, prosku);
            profit = qtyservice.calProfit(profit);

            /**
             * (制作中+已交货)库存占用资金总金额(USD)
             */
            profit.workingfee = profit.workingqty * profit.procureprice;
            profit.workingfee = Webs.scale2Double(profit.workingfee);
            /**
             * 在途库存占用资金总金额(USD)
             */
            profit.wayfee = profit.wayqty * profit.procureprice + profit.wayqty * profit.shipprice
                    + profit.wayqty * profit.vatprice;
            profit.wayfee = Webs.scale2Double(profit.wayfee);
            /**
             * (入库+在库)库存占用资金总金额(USD)
             */
            profit.inboundfee = profit.inboundqty * profit.procureprice + profit.inboundqty * profit.shipprice
                    + profit.inboundqty * profit.vatprice;
            profit.inboundfee = Webs.scale2Double(profit.inboundfee);
            return profit;
        } catch(Exception e) {
            Logger.error(Webs.s(e));
        }
        return initProfit(market,
                prosku, sellingId);
    }


    public Profit initProfit(M market,
                             String prosku, String sellingId) {
        Profit profit = new Profit();
        profit.sku = prosku;
        profit.sellingId = sellingId;
        profit.market = market;
        //总销售额
        profit.totalfee = 0;
        profit.totalfee = 0;
        //亚马逊费用
        profit.amazonfee = 0;
        profit.amazonfee = 0;
        //fba费用
        profit.fbafee = 0;
        profit.fbafee = 0;
        //总销量
        profit.quantity = 0;
        //采购价格
        profit.procureprice = 0;
        profit.procureprice = 0;
        //运输价格
        profit.shipprice = 0;
        profit.shipprice = 0;
        //vat价格
        profit.vatprice = 0;
        profit.vatprice = 0;
        //利润
        profit.totalprofit = 0;
        profit.totalprofit = 0;
        //利润率
        profit.profitrate = 0;
        profit.profitrate = 0;
        return profit;
    }

    public Profit inventoryProfit(Date begin, Date end, M market,
                                  String prosku, String sellingId) {
        end = Dates.night(end);
        MetricProfitService service = new MetricProfitService(begin, end, market, prosku, sellingId);
        Profit profit = new Profit();
        profit.sku = this.sku;
        profit.sellingId = sellingId;
        profit.market = market;

        //增加库存数据
        MetricQtyService qtyservice = new MetricQtyService(market, prosku);
        profit = qtyservice.calProfit(profit);

        /**
         * (制作中+已交货)库存占用资金总金额(USD)
         */
        profit.workingfee = profit.workingqty * profit.procureprice;
        profit.workingfee = Webs.scale2Double(profit.workingfee);
        /**
         * 在途库存占用资金总金额(USD)
         */
        profit.wayfee = profit.wayqty * profit.procureprice + profit.wayqty * profit.shipprice
                + profit.wayqty * profit.vatprice;
        profit.wayfee = Webs.scale2Double(profit.wayfee);
        /**
         * (入库+在库)库存占用资金总金额(USD)
         */
        profit.inboundfee = profit.inboundqty * profit.procureprice + profit.inboundqty * profit.shipprice
                + profit.inboundqty * profit.vatprice;
        profit.inboundfee = Webs.scale2Double(profit.inboundfee);

        return profit;
    }

    public void reCalculate() {
        SimpleDateFormat postParamFormater = new SimpleDateFormat("yyyy-MM-dd");
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("category", this.category()));
        params.add(new BasicNameValuePair("market", this.pmarket));
        params.add(new BasicNameValuePair("from", postParamFormater.format(this.begin)));
        params.add(new BasicNameValuePair("to", postParamFormater.format(this.end)));
        params.add(new BasicNameValuePair("is_sku", this.isSku() + ""));
        HTTP.post(String.format("%s/profit_batch_work", System.getenv(Constant.ROCKEND_HOST)), params);
    }

    public String category() {
        return StringUtils.isNotBlank(this.sku) ? this.sku : this.category.toLowerCase();
    }

    public int isSku() {
        return StringUtils.isNotBlank(this.sku) ? 1 : 0;
    }

    public List<Profit> fetch() {
        this.valid();
        if(Validation.hasErrors()) return Collections.emptyList();
        List<Profit> profits = Cache.get(this.cacheKey(), List.class);
        if(profits != null && profits.size() > 0) {
            return profits;
        } else {
            if(StringUtils.isBlank(Caches.get(SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE))) {
                HTTP.get(System.getenv(Constant.ROCKEND_HOST) + "/selling_sale_analyze");
            } else if(StringUtils.isBlank(Caches.get(this.runningKey()))) {
                this.reCalculate();
            }
            Validation.addError("", "正在计算中, 请稍后再来查看 :)");
            return Collections.emptyList();
        }
    }

    public void valid() {
        if(StringUtils.isBlank(this.category) && StringUtils.isBlank(this.sku)) {
            Validation.addError("", "请选择一个 Category OR SKU.");
        }
        if(StringUtils.isNotBlank(this.sku) && !Product.exist(this.sku)) {
            Validation.addError("", String.format("未找到 Product[%s].", this.sku));
        }
        if(StringUtils.isNotBlank(this.category) && !Category.exist(this.category)) {
            Validation.addError("", String.format("未找到 Category[%s].", this.category));
        }
    }

    public String cacheKey() {
        return helper.Caches.Q.cacheKey("profitpost",
                this.begin,
                this.end,
                this.category != null ? this.category.toLowerCase() : "",
                this.sku,
                this.pmarket);
    }

    public String runningKey() {
        SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
        String cate = "";
        if(StringUtils.isNotBlank(this.sku)) {
            cate = this.sku;
        } else if(StringUtils.isNotBlank(this.category)) {
            cate = this.category.toLowerCase();
        }
        return String.format("profitpost_%s_%s_%s_%s",
                cate,
                StringUtils.isNotBlank(this.pmarket) ? this.pmarket : "",
                dateFormater.format(this.begin),
                dateFormater.format(this.end));
    }
}
