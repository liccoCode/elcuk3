package models.procure;

import models.User;
import models.market.BtbOrder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 16/1/20
 * Time: 下午3:55
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BtbCustom extends Model {

    private static final long serialVersionUID = -7229251820184285759L;

    public String customName;

    public String contacts;

    public String contactPhone;

    public String email;

    @OneToMany(mappedBy = "btbCustom", fetch = FetchType.LAZY)
    public List<BtbCustomAddress> addresses = new ArrayList<>();

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

    public boolean validRepeatCustomName() {
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
