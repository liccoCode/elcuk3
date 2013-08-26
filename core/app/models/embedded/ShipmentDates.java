package models.embedded;

import com.google.gson.annotations.Expose;
import play.data.validation.Required;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 5/10/13
 * Time: 9:44 AM
 */
@Embeddable
public class ShipmentDates implements Serializable{

    private static final long serialVersionUID = -8792808495254263522L;
    /**
     * 预计发货日期
     */
    @Expose
    @Required
    public Date planBeginDate;

    /**
     * 货运开始日期(实际发货日期)
     */
    @Expose
    public Date beginDate;


    /**
     * 到港时间
     */
    public Date atPortDate;

    /**
     * 提货时间
     */
    public Date pickGoodDate;

    /**
     * 预约时间
     */
    public Date bookDate;

    /**
     * 派送时间
     */
    public Date deliverDate;

    /**
     * 签收时间
     */
    public Date receiptDate;

    /**
     * 入库时间
     */
    public Date inbondDate;

    /**
     * 预计运输完成时间
     */
    @Expose
    @Required
    public Date planArrivDate;

    /**
     * 实际运输完成时间
     */
    @Expose
    public Date arriveDate;

}
