package models.embedded;

import com.google.gson.annotations.Expose;
import helper.Currency;

import javax.persistence.Embeddable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 2:24 PM
 */
@Embeddable
public class UnitPlan {
    /**
     * 预计到库时间
     */
    @Expose
    public Date planArrivDate;

    /**
     * 计划采购数量(也就是采购数量)
     */
    @Expose
    public Integer planQty;
    // ------------------------
    //TODO 供应商会重构成 modal
    @Expose
    public String supplier;

    @Expose
    public Currency currency;

    /**
     * 采购单价
     */
    @Expose
    public Float unitPrice;
}
