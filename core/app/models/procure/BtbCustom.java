package models.procure;

import models.User;
import models.market.BtbOrder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.ArrayList;
import java.util.Date;
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

    /**
     * 创建人
     */
    @OneToOne
    public User creator;

    public Date createDate;

    public Date updateDate;

    public boolean isDel = false;

    @OneToMany(mappedBy = "btbCustom", cascade = {CascadeType.PERSIST})
    public List<BtbOrder> orders = new ArrayList<>();

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
