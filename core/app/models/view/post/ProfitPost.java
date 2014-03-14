package models.view.post;

import models.market.M;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.*;

import models.view.report.Profit;
import services.MetricProfitService;
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
    public M market;
    public String sku;
    public String sellingId;
    public String category;

    public ProfitPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.end = now.toDate();
        DateTime from = DateTime.now().withDayOfYear(1);
        this.begin = from.toDate();
        this.perSize = 25;
        this.page = 1;
    }

    public ProfitPost(int perSize) {
        this.perSize = perSize;
    }

    @SuppressWarnings("unchecked")
    public List<Profit> query() {
        MetricProfitService service;
        List<Profit> profitlist = new ArrayList<Profit>();
        M befMarket = market;
        /**
         * 市场为空，则每个市场遍历
         */
        if(market == null) {
            M[] marray = models.market.M.values();
            for(M m : marray) {
                if(m != M.AMAZON_TOTAL) {
                    market = m;
                    profitlist = searchProfitList(profitlist);
                }
            }
        } else {
            /**
             * 市场为汇总类型,则所有市场数据累加
             */
            if(market == M.AMAZON_TOTAL) {
                market = null;
            }
            profitlist = searchProfitList(profitlist);
        }
        market = befMarket;
        return profitlist;
    }

    public List<Profit> searchProfitList(List<Profit> profitlist) {
        /**
         * 如果有类别，没有SKU，则查询类别下所有SKU的利润
         */
        if(!StringUtils.isBlank(category) && StringUtils.isBlank(sku)) {
            Category cat = Category.findById(category);
            for(Product pro : cat.products) {
                Profit profit = esProfit(begin, end, market, pro.sku, sellingId);
                profitlist.add(profit);
            }
        } else if(!StringUtils.isBlank(sku)) {
            Profit profit = esProfit(begin, end, market, sku, sellingId);
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
        Profit profit = new Profit();
        MetricProfitService service = new MetricProfitService(begin, end, market, prosku, sellingId);
        profit.sku = prosku;
        /**
         * 如果市场为空,则显示为汇总
         */
        if(market == null) {
            profit.market = M.AMAZON_TOTAL;
        } else {
            profit.market = market;
        }
        //总销售额
        profit.totalfee = service.sellingAmazonTotalFee();
        //亚马逊费用
        profit.amazonfee = service.sellingAmazonFee();
        //fba费用
        profit.fbafee = service.sellingAmazonFBAFee();
        //总销量
        profit.quantity = service.sellingAmazonQty();
        //采购价格
        profit.procureprice = service.payPrice();
        //运输价格
        profit.shipprice = service.shipPrice();
        //vat价格
        profit.vatprice = service.vatPrice();
        /**
         *  SKU总实际利润[A] = SKU总销售额[B] - SKU总亚马逊费用[C] - SKU总FBA费用[D]
         *  - SKU总销量[E] * (SKU平均采购单价[F] + SKU平均运费单价[G] + 关税和VAT单价[H])
         *  amazonfee,fbafee 数据为负数,所以用加
         */
        profit.totalprofit = profit.totalfee + profit.amazonfee + profit.fbafee
                - profit.quantity * (profit.procureprice + profit.shipprice + profit.vatprice);
        if(profit.totalfee != 0f) {
            //利润率
            profit.profitrate = profit.totalprofit / profit.totalfee * 100;
        } else {
            profit.profitrate = 0f;
        }
        return profit;
    }

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sbd = new StringBuilder("");
        List<Object> params = new ArrayList<Object>();
        return new F.T2<String, List<Object>>(sbd.toString(), params);
    }
}