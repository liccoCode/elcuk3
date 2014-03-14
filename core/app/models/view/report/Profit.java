package models.view.report;

import models.market.M;

/**
 * 利润的对象
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 3-10-14
 * Time: 上午11:51
 */
public class Profit {
    /**
     * SKU
     */
    public String sku;
    /**
     * 市场
     */
    public M market;

    /**
     * SKU销售额
     */
    public Float totalfee;
    /**
     * 亚马逊费用
     */
    public Float amazonfee;
    /**
     * FBA费用
     */
    public Float fbafee;
    /**
     * SKU销量
     */
    public Float quantity;
    /**
     * 平均采购价
     */
    public Float procureprice;
    /**
     * 平均运费单价
     */
    public Float shipprice;
    /**
     * 关税和VAT单价
     */
    public Float vatprice;

    /**
     * 总利润
     */
    public Float totalprofit;
    /**
     * 利润率
     */
    public Float profitrate;

}
