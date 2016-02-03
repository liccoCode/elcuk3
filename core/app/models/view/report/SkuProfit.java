package models.view.report;

import models.market.M;

import java.io.Serializable;
import java.util.*;

/**
 * Created by licco on 15/12/22.
 */
public class SkuProfit implements Serializable {

    private static final long serialVersionUID = -1628907214666206604L;

    /**
     * SKU
     */
    public String sku;

    /**
     * 市场
     */
    public M market;

    /**
     * SKU销量
     */
    public double quantity;

    /**
     * SKU销售额
     */
    public double totalfee;

    /**
     * SKU销售成本
     */
    public double skuSaleCost;

    /**
     * 采购成本
     */
    public double purchaseCost;

    /**
     * 物流成本
     */
    public double shipCost;

    /**
     * 税金成本
     */
    public double taxCost;

    /**
     * 销售费用
     */
    public double saleFee;

    /**
     * 亚马逊费用
     */
    public double amazonfee;
    /**
     * FBA费用
     */
    public double fbafee;

    /**
     * 总利润
     */
    public double totalprofit;

    /**
     * 分市场合计
     */
    public List<SkuProfit> marketProfitList;

    /**
     * 合计
     */
    public SkuProfit totalSkuProfit;


    public static SkuProfit handleSkuProfit(List<SkuProfit> skuProfits) {
        List<SkuProfit> marketProfit = new ArrayList<SkuProfit>();
        SkuProfit total_skuProfits = new SkuProfit();
        Map<M, SkuProfit> skuProfitMap = new HashMap<M, SkuProfit>();
        SkuProfit totalSkuProfit = new SkuProfit();
        for(SkuProfit skuProfit : skuProfits) {
            if(skuProfitMap.containsKey(skuProfit.market)) {
                SkuProfit p = skuProfitMap.get(skuProfit.market);
                SkuProfit.add_profit(p, skuProfit);
                skuProfitMap.put(skuProfit.market, p);
            } else {
                skuProfitMap.put(skuProfit.market, skuProfit);
            }
            /**总计**/
            SkuProfit.add_profit(totalSkuProfit, skuProfit);
        }

        Iterator iterator = skuProfitMap.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            SkuProfit obj = (SkuProfit) entry.getValue();
            marketProfit.add(obj);
        }
        total_skuProfits.marketProfitList = marketProfit;
        total_skuProfits.totalSkuProfit = totalSkuProfit;
        return total_skuProfits;
    }

    public static void add_profit(SkuProfit origin, SkuProfit profit) {
        origin.quantity += profit.quantity;
        origin.totalfee += profit.totalfee;
        origin.skuSaleCost += profit.skuSaleCost;
        origin.purchaseCost += profit.purchaseCost;
        origin.shipCost += profit.shipCost;
        origin.taxCost += profit.taxCost;
        origin.saleFee += profit.saleFee;
        origin.amazonfee += profit.amazonfee;
        origin.fbafee += profit.fbafee;
        origin.totalprofit += profit.totalprofit;
    }


}
