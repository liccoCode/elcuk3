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
import play.libs.F;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
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
         * 预定.
         */
        PENDING {
            @Override
            public String toString() {
                return "计划";
            }
        },
        /**
         * 确定采购单
         */
        CONFIRM {
            @Override
            public String toString() {
                return "确认并已下单";
            }
        },
        /**
         * 完成交货.
         */
        DONE {
            @Override
            public String toString() {
                return "完成交货";
            }
        },
        CANCEL {
            @Override
            public String toString() {
                return "取消";
            }
        }
    }

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
     * 获取此采购单的供应商, 如果没有采购货物, 则供应商为空, 否则为第一个采购计划的供应商(因为采购单只允许一个供应商)
     *
     * @return
     */
    public Cooperator supplier() {
        if(this.units.size() == 0) return null;
        return this.units.get(0).cooperator;
    }

    /**
     * 计算此采购单的最早一个交货与最晚一个交货的时间, 如果只有一个, 那么最早==最晚
     *
     * @return
     */
    public F.T2<Date, Date> firstAndEndDeliveryDate() {
        Date first = null;
        Date end = null;
        List<Date> deliveryDates = new ArrayList<Date>();
        for(ProcureUnit unit : this.units) {
            if(unit.stage.ordinal() >= ProcureUnit.STAGE.DONE.ordinal() && unit.stage != ProcureUnit.STAGE.CLOSE)
                if(unit.attrs.deliveryDate != null) deliveryDates.add(unit.attrs.deliveryDate);
        }
        if(deliveryDates.size() > 2) {
            Collections.sort(deliveryDates);
            first = deliveryDates.get(0);
            end = deliveryDates.get(deliveryDates.size() - 1);
        } else if(deliveryDates.size() == 1) {
            first = deliveryDates.get(0);
            end = first;
        }
        return new F.T2<Date, Date>(first, end);
    }

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


    public static String id() {
        DateTime dt = DateTime.now();
        String count = Deliveryment.count("createDate>=? AND createDate<=?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-30", dt.getYear(), dt.getMonthOfYear())).toDate()) + "";
        return String.format("DL|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public static List<Deliveryment> openDeliveryments(S state) {
        return Deliveryment.find("state=? ORDER BY createDate DESC", state).fetch();
    }

    /**
     * 通过 ProcureUnit 来创建采购单
     *
     * ps: 创建 Delivery 不允许并发; 类锁就类锁吧... 反正常见 Delivery 不是经常性操作
     * @param pids
     */
    public synchronized static Deliveryment createFromProcures(List<Long> pids, String name, User user) {
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
