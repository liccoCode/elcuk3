package models.material;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import models.User;
import models.finance.ProcureApply;
import models.procure.Cooperator;
import models.procure.Deliveryment;
import models.procure.ProcureUnit;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;

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

    /**
     * 下单时间(确认时间)
     */
    public Date orderTime;
    @Lob
    public String memo = " ";

    /**
     * 所属公司
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;


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
     * 确认物料下采购单
     */
    public void confirm() {
        if(!Arrays.asList(Deliveryment.S.APPROVE, Deliveryment.S.PENDING, Deliveryment.S.REJECT).contains(this.state))
            Validation.addError("", "采购单状态非 " + Deliveryment.S.PENDING.label() + " 不可以确认");
        if(Validation.hasErrors()) return;
        this.orderTime = new Date();
        this.state = Deliveryment.S.CONFIRM;
        this.save();
    }

    /**
     * 取消物料采购单
     */
    public void cancel(String msg) {
        // 只允许所有都是 units 都为 采购中 的才能够取消.
        if(this.units.stream().anyMatch(unit -> unit.stage != ProcureUnit.STAGE.DELIVERY)) {
            Validation.addError("", "采购计划必须全部都是采购中的才能取消采购单！");
            return;
        }
        if(Validation.hasErrors()) return;
        this.units.forEach(unit -> unit.toggleAssignTodeliveryment(null, false));
        this.state = Deliveryment.S.CANCEL;
        this.save();
        new ElcukRecord(Messages.get("materialPurchases.cancel"),
                Messages.get("materialPurchases.cancel.msg", this.id, msg.trim()), this.id).save();
    }


    /**
     * 将 MaterialUnit 从 MaterialPurchase 中解除
     *
     * @param pids
     */
    public List<MaterialUnit> unAssignUnitInMaterialPurchase(List<Long> pids) {
        List<MaterialUnit> units = MaterialUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(MaterialUnit unit : units) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
                Validation.addError("materialPurchase.units.unassign", "%s");
            } else if(this.deliveryType == Deliveryment.T.MANUAL) {
                //手动采购单中的默认的采购计划不允许从采购单中移除
                Validation.addError("", "手动单中默认的采购计划不允许被移除!");
            } else {
                unit.toggleAssignTodeliveryment(null, false);
            }
        }
        if(Validation.hasErrors()) return new ArrayList<>();
        this.units.removeAll(units);
        this.save();

        new ElcukRecord(Messages.get("materialPurchase.delunit"),
                Messages.get("materialPurchase.delunit.msg", pids, this.id), this.id).save();
        return units;
    }
}
