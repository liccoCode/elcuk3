package models.procure;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/6/16
 * Time: 下午3:56
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BtbCustomAddress extends Model {

    private static final long serialVersionUID = 8527174010155272913L;
    
    @ManyToOne
    public BtbCustom btbCustom;

    /**
     * 收货人
     */
    public String receiver;

    /**
     * 收货人电话
     */
    public String receiverPhone;


    public String countryCode;

    public String city;

    public String postalCode;

    public String address;

}
