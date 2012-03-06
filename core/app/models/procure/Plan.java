package models.procure;

import models.User;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Date;
import java.util.List;

/**
 * 采购计划
 * User: wyattpan
 * Date: 3/2/12
 * Time: 12:42 PM
 */
@Entity
public class Plan extends GenericModel {

    @OneToOne
    public User creater;

    @Id
    public String id;

    public Date createDate;

    public Date planDate;


    /**
     * 此采购单中的采购单元
     */
    @OneToMany(mappedBy = "plan")
    public List<PItem> items;

    public static String cId() {
        DateTime now = DateTime.now();
        Plan pl = Plan.find("createDate>=? AND createDate<=? ORDER BY id DESC",
                DateTime.parse(String.format("%s-%s-%s", now.getYear(), now.getMonthOfYear(), "01")).toDate(),
                now.toDate()
        ).first();
        Integer nb = 0;
        if(pl != null) nb = Integer.valueOf(pl.id.split("_")[1]);
        return String.format("PL%s_%s", now.toString("yyMMdd"), nb + 1);
    }
}
