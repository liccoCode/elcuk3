package models.shipment;

import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/3/20
 * Time: 上午10:50
 */
@Entity
@DynamicUpdate
public class TransportRange extends Model {

    private static final long serialVersionUID = -5314661672581079633L;

    @ManyToOne
    public TransportChannelDetail detail;

    /**
     * 重量段(KG)
     */
    public String weightRange;

    /**
     * 价格范围(CNY)
     */
    public String priceRange;

    @Transient
    public String weightBegin;

    @Transient
    public String weightEnd;

    @Transient
    public String priceBegin;

    @Transient
    public String priceEnd;

    @Transient
    public Long rangeId;

}
