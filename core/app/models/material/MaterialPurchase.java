package models.material;

import com.google.gson.annotations.Expose;
import models.User;
import models.finance.ProcureApply;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.*;

/**
 * 物料采购单
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/5/31
 * Time: AM10:19
 */
@Entity
@DynamicUpdate
public class MaterialPurchase extends GenericModel {

    private static final long serialVersionUID = 6762554005097525886L;

    public MaterialPurchase() {

    }

    @Id
    @Column(length = 30)
    @Expose
    public String id;


    public String name;

    /**
     * 此采购单的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    @Required
    public Deliveryment.S state;

    /**
     * 采购计划
     */
    @OneToMany(mappedBy = "materialPurchase", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public List<MaterialUnit> units = new ArrayList<>();

    @ManyToOne
    public ProcureApply apply;

    @OneToOne
    public User handler;

    /**
     * 供应商
     * 一个采购单只能拥有一个供应商
     */
    @ManyToOne
    public Cooperator cooperator;

    @Expose
    @Required
    public Date createDate = new Date();
    
    /**
     * 采购单类型
     */
    @Enumerated(EnumType.STRING)
    public Deliveryment.T deliveryType;

    @Lob
    public String memo = " ";


    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = MaterialPurchase.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear()))
                        .toDate(),
                DateTime.parse(
                        String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear()))
                        .toDate()) + "";
        return String.format("WL|%s|%s", dt.toString("yyyyMM"),
                count.length() == 1 ? "0" + count : count);
    }

    /**
     * 获取此采购单的供应商, 如果没有采购货物, 则供应商为空, 否则为第一个采购计划的供应商(因为采购单只允许一个供应商)
     */
    public Cooperator supplier() {
        if(this.units.size() == 0) return null;
        return this.units.get(0).cooperator;
    }

    /**
      * 交货的状态.
      * 如果全部交货, 则进行交货状态更新
      *
      * @return

     public F.T2<Integer, Integer> deliveryProcress() {
         int delivery = 0;
         int total = 0;
         for(MaterialUnit unit : this.units) {
             if(!Objects.equals(unit.type, ProcureUnit.T.StockSplit)) {
                 if(unit.stage != ProcureUnit.STAGE.PLAN && unit.stage != ProcureUnit.STAGE.DELIVERY)
                     delivery += unit.qty();
                 total += unit.qty();
             }
         }
         if(Arrays.asList(Deliveryment.S.PENDING, Deliveryment.S.CONFIRM).contains(this.state) && delivery == total) {
             this.state = Deliveryment.S.DONE;
             this.save();
         }
         return new F.T2<>(delivery, total == 0 ? 1 : total);
     }  */

}
