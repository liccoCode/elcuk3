package models.procure;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.util.List;

/**
 * Created by licco on 16/1/20.
 */
@Entity
public class BtbCustom extends Model {

    public String customName;

    public String contacts;

    public String contactPhone;

    public String email;

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
