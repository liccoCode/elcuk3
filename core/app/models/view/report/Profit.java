package models.view.report;

import models.market.M;

import java.io.Serializable;

/**
 * 利润的对象
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 3-10-14
 * Time: 上午11:51
 */
public class Profit implements Serializable, Comparable {
    private static final long serialVersionUID = -6924566933690728789L;

    /**
     * SKU
     */
    public String sku;
    /**
     * sellingId
     */
    public String sellingId;


    public String memo;

    /**
     * 市场
     */
    public M market;

    /**
     * SKU销售额
     */
    public double totalfee;
    /**
     * 亚马逊费用
     */
    public double amazonfee;
    /**
     * FBA费用
     */
    public double fbafee;
    /**
     * SKU销量
     */
    public double quantity;
    /**
     * 平均采购价
     */
    public double procureprice;
    /**
     * 平均运费单价
     */
    public double shipprice;
    /**
     * 关税和VAT单价
     */
    public double vatprice;

    /**
     * 总利润
     */
    public double totalprofit;
    /**
     * 利润率
     */
    public double profitrate;
    /**
     * 正在制作+已交货的数量
     */
    public int workingqty = 0;
    /**
     * 在途库存
     */
    public int wayqty = 0;
    /**
     * 入库+在库
     */
    public int inboundqty = 0;


    /**
     * (制作中+已交货)库存占用资金总金额(USD)
     */
    public double workingfee = 0f;

    /**
     * 在途库存占用资金总金额(USD)
     */
    public double wayfee = 0f;

    /**
     * (入库+在库)库存占用资金总金额(USD)
     */
    public double inboundfee = 0f;

    @Override
    public int compareTo(Object o) {
        if(o instanceof Profit) {
            Profit productAttr = (Profit) o;
            if(this.sku.compareTo(((Profit) o).sku) > 0) {
                return 1;
            }
            if(this.sku.compareTo(((Profit) o).sku) < 0) {
                return -1;
            } else {
                return 0;
            }
        }
        return 0;
    }

}
