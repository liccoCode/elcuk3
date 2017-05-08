package models.procure;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.List;

/**
 * Created by licco on 16/1/20.
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BtbCustom extends Model {

    private static final long serialVersionUID = -7229251820184285759L;

    public String customName;

    public String contacts;

    public String contactPhone;

    public String email;

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



    public boolean vaildRepeatCustomName() {
        boolean flag = false;
        List<BtbCustom> c;
        if(id == null) {
            c = BtbCustom.find("customName = ? ", customName).fetch();
        } else {
            c = BtbCustom.find("customName = ? and id <> ? ", customName, id).fetch();
        }
        if(c != null && c.size() > 0) {
            flag = true;
        }
        return flag;
    }

}
