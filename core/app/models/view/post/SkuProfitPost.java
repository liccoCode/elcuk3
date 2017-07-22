package models.view.post;

import com.alibaba.fastjson.JSON;
import helper.Caches;
import helper.Webs;
import models.market.M;
import models.product.Category;
import models.product.Product;
import models.view.dto.ProfitDto;
import models.view.report.SkuProfit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by licco on 15/12/22.
 */
public class SkuProfitPost {

    public Date begin;
    public Date end;
    public String pmarket;
    public String sku;
    public String sellingId;
    public String categories;
    public int page = 1;
    public int perSize = 10;

    public SkuProfitPost() {
        DateTime now = DateTime.now().withTimeAtStartOfDay();
        this.end = now.toDate();
        DateTime from = DateTime.now().withDayOfYear(1);
        this.begin = from.toDate();
        this.pmarket = "market";
    }

    public List<SkuProfit> query() {
        List<SkuProfit> skuProfits = new ArrayList<>();

        /**
         * 每个市场遍历
         */
        M[] marray = getMarket();
        for(M m : marray) {
            skuProfits = searchSkuProfitList(skuProfits, m, new SimpleDateFormat("yyyyMMdd").format(this.begin),
                    new SimpleDateFormat("yyyyMMdd").format(this.end));
        }
        return skuProfits;
    }

    public List<SkuProfit> searchSkuProfitList(List<SkuProfit> skuProfits, M skumarket, String from, String to) {

        if(!StringUtils.isBlank(categories) || !StringUtils.isBlank(sku)) {
            List<ProfitDto> dtos = null;
            String key = categories;
            if(!StringUtils.isBlank(sku)) key = sku;
            String cache_key = "skuprofitmap_" + key + "_" + skumarket.name() + "_" + from + "_" + to;
            Logger.info(cache_key);
            String cache_str = Caches.get(cache_key);
            if(!StringUtils.isBlank(cache_str)) {
                dtos = JSON.parseArray(cache_str, ProfitDto.class);
            }
            if(dtos == null) return skuProfits;

            Map<String, ProfitDto> profitmap = new HashMap<>();
            for(ProfitDto dto : dtos) {
                profitmap.put(dto.sku, dto);
            }
            if(!StringUtils.isBlank(sku)) {
                SkuProfit profit = redisProfit(profitmap, skumarket, sku, sellingId);
                if(profit.quantity != 0 || profit.totalfee != 0 || profit.purchaseCost != 0 || profit.shipCost != 0
                        || profit.taxCost != 0 || profit.skuSaleCost != 0 || profit.saleFee != 0) {
                    skuProfits.add(profit);
                }
            } else {
                String[] category_id = categories.split(",");
                for(String category : category_id) {
                    Category cat = Category.findById(category);
                    for(Product pro : cat.products) {
                        SkuProfit profit = redisProfit(profitmap, skumarket, pro.sku, sellingId);
                        if(profit.quantity != 0 || profit.totalfee != 0 || profit.saleFee != 0 || profit.taxCost != 0
                                || profit.shipCost != 0 || profit.skuSaleCost != 0 || profit.purchaseCost != 0) {
                            skuProfits.add(profit);
                        }
                    }
                }
            }
        }
        return skuProfits;
    }

    public SkuProfit redisProfit(Map<String, ProfitDto> profitMap, M market, String proSku, String sellingId) {
        try {
            ProfitDto dto = profitMap.get(proSku);
            if(dto == null) return initProfit(market, proSku, sellingId);
            SkuProfit skuProfit = new SkuProfit();
            skuProfit.sku = proSku;
            skuProfit.market = market;
            skuProfit.quantity = dto.qty;
            skuProfit.totalfee = Webs.scale2Double(dto.fee);

            skuProfit.purchaseCost = dto.qty * Webs.scale2Double(dto.purchase_price);
            skuProfit.shipCost = dto.qty * Webs.scale2Double(dto.ship_price);
            skuProfit.taxCost = dto.qty * Webs.scale2Double(dto.vat_price);
            skuProfit.skuSaleCost = skuProfit.purchaseCost + skuProfit.shipCost + skuProfit.taxCost;

            skuProfit.saleFee = Webs.scale2Double(dto.amazon_fee) + Webs.scale2Double(dto.fba_fee);
            skuProfit.amazonfee = Webs.scale2Double(dto.amazon_fee);
            skuProfit.fbafee = Webs.scale2Double(dto.fba_fee);
            skuProfit.totalprofit = Webs.scale2Double(dto.total_profit);

            return skuProfit;
        } catch(Exception e) {
            Logger.error(Webs.S(e));
        }
        return initProfit(market, proSku, sellingId);
    }


    public SkuProfit initProfit(M market, String prosku, String sellingId) {
        SkuProfit profit = new SkuProfit();
        profit.sku = prosku;
        profit.market = market;
        profit.quantity = 0;
        profit.totalfee = 0;
        profit.skuSaleCost = 0;
        profit.purchaseCost = 0;
        profit.shipCost = 0;
        profit.taxCost = 0;
        profit.saleFee = 0;
        profit.amazonfee = 0;
        profit.fbafee = 0;
        profit.totalprofit = 0;
        return profit;
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

}
