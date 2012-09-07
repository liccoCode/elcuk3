package models.procure;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import models.User;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 采购单, 用来记录所采购的 ProcureUnit
 * User: wyattpan
 * Date: 6/18/12
 * Time: 4:50 PM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Deliveryment extends GenericModel {
    public Deliveryment() {
    }

    public Deliveryment(String id) {
        this.id = id;
    }

    public enum S {
        /**
         * 预定, 已经下单
         */
        PENDING,
        /**
         * 部分交货
         */
        DELIVERING,
        /**
         * 完成, 交货
         *
         * @deprecated
         */
        DELIVERY,
        /**
         * 完成交货.
         */
        DONE,
        /**
         * 全部付款
         */
        FULPAY,
        CANCEL
    }

    @OneToMany(mappedBy = "deliveryment")
    @OrderBy("state DESC")
    public List<Payment> payments = new ArrayList<Payment>();


    @OneToMany(mappedBy = "deliveryment", cascade = {CascadeType.PERSIST})
    public List<ProcureUnit> units = new ArrayList<ProcureUnit>();

    @OneToOne
    public User handler;

    @Expose
    @Required
    public Date createDate = new Date();

    /**
     * 此采购单的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    @Required
    public S state;

    /**
     * 可为每一个 Deliveryment 添加一个名称
     */
    public String name;


    @Id
    @Column(length = 30)
    @Expose
    public String id;

    @Lob
    public String memo = " ";

    /**
     * 返回此 Deliveryment 可以用来添加的 ProcureUnits
     *
     * @return
     */
    public List<ProcureUnit> availableInPlanStageProcureUnits() {
        if(this.units.size() == 0) {
            return ProcureUnit.find("stage=?", ProcureUnit.STAGE.PLAN).fetch();
        } else {
            Cooperator cooperator = this.units.get(0).cooperator;
            return ProcureUnit.find("cooperator=? AND stage=?", cooperator, ProcureUnit.STAGE.PLAN).fetch();
        }
    }

    /**
     * 取消采购单
     */
    public void cancel(String msg) {
        /**
         * 1. 只允许所有都是 units 都为 PLAN 的才能够取消.
         */
        for(ProcureUnit unit : this.units) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY)
                Validation.addError("deliveryment.units.cancel", "validation.required");
            else
                unit.toggleAssignTodeliveryment(null, false);
        }
        if(Validation.hasErrors()) return;
        this.state = S.CANCEL;
        this.save();

        new ElcukRecord(Messages.get("deliveryment.cancel"), Messages.get("deliveryment.cancel.msg", this.id, msg.trim()), this.id).save();
    }

    /**
     * 将 PLAN 状态的 ProcureUnit 添加到这个采购单中, 用户制作采购单
     *
     * @return
     */
    public List<ProcureUnit> assignUnitToDeliveryment(List<Long> pids) {
        if(this.state != S.PENDING) {
            Validation.addError("deliveryment.units.state", "%s");
            return new ArrayList<ProcureUnit>();
        }
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        Cooperator singleCop = units.get(0).cooperator;
        for(ProcureUnit unit : units) {
            if(isUnitToDeliverymentValid(unit, singleCop))
                unit.toggleAssignTodeliveryment(this, true);
        }
        if(Validation.hasErrors()) return new ArrayList<ProcureUnit>();
        this.units.addAll(units);
        // 实在无语, 级联保存无效, 只能如此.
        for(ProcureUnit unit : this.units) unit.save();

        new ElcukRecord(Messages.get("deliveryment.addunit"), Messages.get("deliveryment.addunit.msg", pids, this.id), this.id).save();

        return units;
    }

    /**
     * 将指定 ProcureUnit 从 Deliveryment 中删除
     *
     * @param pids
     */
    public List<ProcureUnit> unAssignUnitInDeliveryment(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(ProcureUnit unit : units) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY)
                Validation.addError("deliveryment.units.unassign", "%s");
            else
                unit.toggleAssignTodeliveryment(null, false);
        }
        if(Validation.hasErrors()) return new ArrayList<ProcureUnit>();
        this.units.removeAll(units);
        this.save();

        new ElcukRecord(Messages.get("deliveryment.delunit"), Messages.get("deliveryment.delunit.msg", pids, this.id), this.id).save();
        return units;
    }

    /**
     * 检查 Deliveryment 的下一个状态是什么
     *
     * @return
     */
    public S nextState() {
        /**
         * 1. 如果所属的 units 部分 DONE 则为交货中.
         * 2. 如果所属的 units 全部 DONE 则此采购单也 DONE 交货完成.
         */
        for(ProcureUnit unit : this.units) {
            if(unit.stage != ProcureUnit.STAGE.DONE) return S.DELIVERING;
        }
        return S.DONE;
    }

    public static String id() {
        DateTime dt = DateTime.now();
        String count = Deliveryment.count("createDate>=? AND createDate<=?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-30", dt.getYear(), dt.getMonthOfYear())).toDate()) + "";
        return String.format("DL|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public static List<Deliveryment> openDeliveryments() {
        return Deliveryment.find("state NOT IN (?,?)", S.DELIVERY, S.CANCEL).fetch();
    }

    /**
     * 通过 ProcureUnit 来创建采购单
     *
     * @param pids
     */
    public static Deliveryment createFromProcures(List<Long> pids, String name, User user) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        Deliveryment deliveryment = new Deliveryment(Deliveryment.id());
        if(pids.size() != units.size()) {
            Validation.addError("deliveryment.units.create", "%s");
            return deliveryment;
        }

        Cooperator cop = units.get(0).cooperator;
        for(ProcureUnit unit : units) {
            isUnitToDeliverymentValid(unit, cop);
        }
        if(Validation.hasErrors()) return deliveryment;
        deliveryment.handler = user;
        deliveryment.state = S.PENDING;
        deliveryment.name = name.trim();
        deliveryment.units.addAll(units);
        for(ProcureUnit unit : deliveryment.units) {
            unit.toggleAssignTodeliveryment(deliveryment, true);
        }
        deliveryment.save();

        new ElcukRecord(Messages.get("deliveryment.createFromProcures"),
                Messages.get("deliveryment.createFromProcures.msg", pids, deliveryment.id), deliveryment.id).save();
        return deliveryment;
    }

    private static boolean isUnitToDeliverymentValid(ProcureUnit unit, Cooperator cop) {
        if(unit.stage != ProcureUnit.STAGE.PLAN) {
            Validation.addError("deliveryment.units.unassign", "%s");
            return false;
        }
        if(!cop.equals(unit.cooperator)) {
            Validation.addError("deliveryment.units.singlecop", "%s");
            return false;
        }
        Validation.required("procureunit.planDeliveryDate", unit.attrs.planDeliveryDate);
        Validation.required("procureunit.planShipDate", unit.attrs.planShipDate);
        Validation.required("procureunit.planArrivDate", unit.attrs.planArrivDate);
        return true;
    }

}
