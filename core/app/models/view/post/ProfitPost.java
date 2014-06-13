package models.view.post;

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
    public String market;
    public String sku;
    public String sellingId;
    public String category;

    public ProfitPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.end = now.toDate();
        DateTime from = DateTime.now().withDayOfYear(1);
        this.begin = from.toDate();
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
        if(market.equals("market")) {
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

    @SuppressWarnings("unchecked")
    public List<Profit> query() {
        List<Profit> profitlist = new ArrayList<Profit>();
        /**
         * 每个市场遍历
         */
        if(market.equals("market")) {
            M[] marray = models.market.M.values();
            for(M m : marray) {
                profitlist = searchProfitList(profitlist, m);
            }
        } else {
            M skumarket = null;
            if(market != null && !market.equals("total")) {
                skumarket = M.valueOf(market);
            }
            profitlist = searchProfitList(profitlist, skumarket);
        }
        return profitlist;
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
        /**
         * 在途库存占用资金总金额(USD)
         */
        profit.wayfee = profit.wayqty * profit.procureprice + profit.wayqty * profit.shipprice;
        /**
         * (入库+在库)库存占用资金总金额(USD)
         */
        profit.inboundfee = profit.inboundqty * profit.procureprice + profit.inboundqty * profit.shipprice
                + profit.inboundqty * profit.vatprice;

        return profit;
    }

}