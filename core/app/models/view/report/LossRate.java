package models.view.report;


import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-12-3
 * Time: 上午9:27
 */
public class LossRate implements Serializable {
    /**
     * sku
     */
    public String sku;
    /**
     * 丢失数量
     */
    public int lossqty;
    /**
     * 赔偿金额
     */
    public float compenusdamt;
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
    public Double totalamt;

}
