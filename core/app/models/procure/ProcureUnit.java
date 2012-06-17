package models.procure;

import helper.Currency;
import models.User;
import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import java.util.Date;

/**
 * 每一个采购单元
 * User: wyattpan
 * Date: 6/11/12
 * Time: 5:23 PM
 */
@Entity
public class ProcureUnit extends Model {

    public enum S {
        /**
         * 计划
         */
        PLAN,
        /**
         * 已经下单
         */
        ORDERED,
        /**
         * 部分付款
         */
        PARTPAY,
        /**
         * 全部付款
         */
        FULPAY,
        /**
         * 已交货
         */
        DELIVERIED,
        /**
         * 运输中
         */
        SHIPPING,
        /**
         * 清关
         */
        CLEARGATE,
        /**
         * 入库中
         */
        RECIVING,
        /**
         * 采购完成
         */
        DONE
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;
    public String sid; // 一个 SellingId 字段

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;
    public String sku;// 冗余 sku 字段

    /**
     * 预计到库时间
     */
    public Date planArrivDate;

    /**
     * 计划采购数量(也就是采购数量)
     */
    public Integer planQty;

    public Float unitPrice;

    // ------------------------
    //TODO 供应商会重构成 modal
    public String supplier;

    public Currency currency;

    public Float price;

    /**
     * 预计交货日期
     */
    public Date planDeliveryDate;

    /**
     * 实际交货日期
     */
    public Date deliveryDate;

    /**
     * 实际交货数量
     */
    public Integer deliveryQty;

    /**
     * 操作人员
     */
    public User handler;


    public Integer inboundQTY;
    /**
     * 外联 ID, 例如 FBA 的运输货号
     */
    public String outerId;

    /**
     * 送往的仓库
     */
    @OneToOne
    public Whouse whouse;


    @Lob
    public String comment = " ";
}
