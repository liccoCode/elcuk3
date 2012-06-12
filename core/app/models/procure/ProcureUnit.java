package models.procure;

import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import play.db.jpa.GenericModel;

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
public class ProcureUnit extends GenericModel {

    public enum S {
        /**
         * 计划
         */
        PLAN,
        /**
         * 购买
         */
        PAYED,
        /**
         * 发货
         */
        SHIP,
        /**
         * 清关
         */
        CLEAR_GATE,
        /**
         * 延迟
         */
        DELAY,
        /**
         * 收货
         */
        RECIVING,
        /**
         * 入库完成
         */
        END
    }

    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    /**
     * 计划的到货时间
     */
    public Date planArrivDate;

    public Integer planQty;

    public Float unitPrice;

    public String supplier;

    @Lob
    public String comment = " ";

    /**
     * 目的地
     */
    @OneToOne
    public Whouse dest;
}
