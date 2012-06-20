package models.embedded;

import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.Webs;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    @Column(nullable = false)
    public String supplier;

    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    public Currency currency;

    /**
     * 采购单价
     */
    @Expose
    public Float unitPrice;

    /**
     * 根据计划到库时间计算的还剩余天数
     *
     * @return
     */
    public float planLeftDays() {
        long millions = this.planArrivDate.getTime() - System.currentTimeMillis();
        return Webs.scalePointUp(0, millions / (24f * 60 * 60 * 1000));
    }
}
