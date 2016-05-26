package models.embedded;

import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.Webs;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 2:24 PM
 */
@Embeddable
public class UnitAttrs implements Serializable {

    private static final long serialVersionUID = 1146012808015481704L;
    /**
     * 预计发货时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    @Required
    public Date planShipDate;

    /**
     * 预计到库时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    @Required
    public Date planArrivDate;


    /**
     * 预计交货日期
     */
    @Expose
    @Temporal(TemporalType.DATE)
    @Required
    public Date planDeliveryDate;

    /**
     * 实际交货日期
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date deliveryDate;


    /**
     * 计划采购数量(在确认了 delivery 的 ensureQty 后,这个数据仅仅作为保存参考)
     */
    @Expose
    @Required
    @Min(0)
    public Integer planQty;

    /**
     * 实际交货数量
     */
    public Integer qty;


    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Required
    public Currency currency;

    /**
     * 采购单价
     */
    @Expose
    @Required
    @Min(0)
    public Float price;

    /**
     * 根据计划到库时间计算的还剩余天数
     *
     * @return
     */
    public float planLeftDays() {
        long millions = this.planArrivDate.getTime() - System.currentTimeMillis();
        return Webs.scalePointUp(0, millions / (24f * 60 * 60 * 1000));
    }

    public void validate() {
        // 两个计划的时间
        if(this.planDeliveryDate != null && this.planShipDate != null)
            Validation.past("procureunit.planDeliveryDate", this.planDeliveryDate,
                    new Date(this.planShipDate.getTime() + 1));
        if(this.planShipDate != null && this.planArrivDate != null)
            Validation.past("procureunit.planShipDate", this.planShipDate, new Date(this.planArrivDate.getTime() + 1));
    }

    public UnitAttrs() {
    }

    public UnitAttrs(Integer qty, Date deliveryDate) {
        this.qty = qty;
        this.deliveryDate = deliveryDate;
    }
}
