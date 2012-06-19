package models.embedded;

import com.google.gson.annotations.Expose;

import javax.persistence.Embeddable;
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
    public Date planDeliveryDate;

    /**
     * 实际交货日期
     */
    @Expose
    public Date deliveryDate;

    /**
     * 实际交货数量
     */
    @Expose
    public Integer deliveryQty;
}
