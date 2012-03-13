package models.procure;

import org.joda.time.DateTime;
import play.db.jpa.GenericModel;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/2/12
 * Time: 12:53 PM
 */
//@Entity
public class Shipment extends GenericModel {

    //    @OneToMany(mappedBy = "shipment")
    public List<PItem> items;

    //    @Id
    public String id;

    public Date createDate;

    public static String cId() {
        DateTime now = DateTime.now();
        Shipment pl = Shipment.find("createDate>=? AND createDate<=? ORDER BY id LIMIT 1",
                DateTime.parse(String.format("%s-%s-%s", now.getYear(), now.getMonthOfYear(), "01")).toDate(),
                now.toDate()
        ).first();
        Integer nb = Integer.valueOf(pl.id.split("_")[1]);
        return String.format("SP%s_%s", now.toString("yyMMdd"), nb + 1);
    }
}
