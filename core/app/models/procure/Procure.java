package models.procure;

import org.joda.time.DateTime;
import play.db.jpa.GenericModel;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/2/12
 * Time: 12:45 PM
 */
//@Entity
public class Procure extends GenericModel {
    //    @OneToOne
    public Plan plan; // 此采购单属于哪一份采购计划中划分出来的.

    //    @OneToOne
    public Shipment shipment; // 此采购单属于哪一个运输单

    /**
     * 此采购单中所具有的 PItem
     */
//    @OneToMany(mappedBy = "procure")
    public List<PItem> items;


    //    @Id
    public String id;

    //    @Column(unique = true, nullable = true)
    public String procureNo; // 与工厂签订的合同号

    public Date createDate;

    //    @Lob
    public String memo;


    public static String cId() {
        DateTime now = DateTime.now();
        Procure pl = Procure.find("createDate>=? AND createDate<=? ORDER BY id LIMIT 1",
                DateTime.parse(String.format("%s-%s-%s", now.getYear(), now.getMonthOfYear(), "01")).toDate(),
                now.toDate()
        ).first();
        Integer nb = Integer.valueOf(pl.id.split("_")[1]);
        return String.format("PR%s_%s", now.toString("yyMMdd"), nb + 1);
    }
}
