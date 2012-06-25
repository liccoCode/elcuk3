package models.embedded;

import com.google.gson.annotations.Expose;

import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 6/18/12
 * Time: 2:28 PM
 */
@Embeddable
public class UnitDelivery {
    /**
     * 预计交货日期
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date planDeliveryDate;

    /**
     * 实际交货日期
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date deliveryDate;

    /**
     * 实际确认采购数量, 设置了以后, 舍弃 Plan 数量
     */
    public Integer ensureQty;

    /**
     * 实际交货数量
     */
    @Expose
    public Integer deliveryQty;
}
