package models.view.report;

import helper.Currency;
import models.market.M;
import models.procure.ProcureUnit;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-12-3
 * Time: 上午9:27
 */
public class LossRate implements Serializable {
    private static final long serialVersionUID = -7841401203972410589L;
    /**
     * sku
     */
    public String sku;

    /**
     * 运输量
     */
    public int qty;

    /**
     * 丢失数量
     */
    public int lossqty;
    /**
     * 赔偿金额
     */
    public float compenusdamt;

    /**
     * 赔偿类型
     */
    public String compentype;

    /**
     * FBA
     */
    public String fba;

    /**
     * 总丢失量
     */
    public BigDecimal totalqty;
    /**
     * 总运输量
     */
    public BigDecimal shipqty;
    /**
     * 丢失率
     */
    public BigDecimal lossrate;
    /**
     * 总赔偿金额
     */
    public Float totalamt;

    /**
     * 货币
     */
    public Currency currency;

    /**
     * sku单价
     */
    public float price;

    /**
     * sku丢失总价
     */
    public float totallossprice;

    /**
     * sku丢失物流成本总价
     */
    public float totalShipmentprice;

    /**
     * 市场
     */
    public M market;

    /**
     * 赔偿比例
     */
    public BigDecimal payrate;

    public ProcureUnit unit;

    public LossRate(){}

    public LossRate(BigDecimal totalqty) {
        this.totalqty = new BigDecimal(0);
        this.shipqty = new BigDecimal(0);
        this.lossrate = new BigDecimal(0.0);
        this.totalamt = 0f;
        this.payrate = new BigDecimal(0.0);
    }

}
