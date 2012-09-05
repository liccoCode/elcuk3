package models.embedded;

import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.J;
import helper.Webs;
import play.data.validation.InFuture;
import play.data.validation.Min;
import play.data.validation.Required;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 2:24 PM
 */
@Embeddable
public class UnitAttrs {
    /**
     * 预计发货时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    @Required
    public Date planShipDate;

    /**
     * 实际发货时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date shipDate;


    /**
     * 预计到库时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    @Required
    public Date planArrivDate;

    /**
     * 实际到库时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date arriveDate;

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

}
