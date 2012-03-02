package models.procure;

import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/2/12
 * Time: 12:45 PM
 */
public class Procure extends GenericModel {
    @OneToOne
    public Plan plan; // 此采购单属于哪一份采购计划中划分出来的.

    @OneToOne
    public Shipment shipment; // 此采购单属于哪一个运输单

    /**
     * 此采购单中所具有的 PItem
     */
    @OneToMany
    public List<PItem> items;


    @Id
    public String id;

    @Column(unique = true, nullable = true)
    public String procureNo; // 与工厂签订的合同号

    @Lob
    public String memo;

}
