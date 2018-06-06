package models.procure;

import models.product.Product;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import java.util.Date;

/**
 * 供销存model
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/6/4
 * Time: 上午10:03
 */
@Entity
public class ProcureUnitAnalyze extends Model {

    private static final long serialVersionUID = -3979047647306162456L;

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    public int year;

    public int month;

    public int planQty;

    /**
     * 收货数
     */
    public int qty;

    /**
     * 入库数
     */
    public int inboundQty;

    /**
     * 不良品数
     */
    public int unqualifiedQty;

    /**
     * 深圳出库数
     */
    public int outQty;

    /**
     * 其它出库数
     */
    public int stockQty;

    /**
     * 仓库退货数
     */
    public int refundQty;

    /**
     * 销量
     */
    public int units;
    /**
     * 退货量
     */
    public int returnQty;

    public String market;

    /**
     * 采购成本
     */
    public Float averageProcurePrice;
    /**
     * 运输费用、成本
     */
    public Float averageShipPrice;
    /**
     * VAT费用、成本
     */
    public Float averageVATPrice;

    /**
     * 创建日期
     */
    public Date createDate;

}
