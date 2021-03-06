package models.procure;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.whouse.Inbound;
import models.whouse.InboundUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.annotations.DynamicUpdate;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 16-1-21
 * Time: 上午10:12
 */
@Entity
@DynamicUpdate
public class DeliverPlan extends GenericModel {

    private static final long serialVersionUID = -5438540657539304801L;

    public DeliverPlan() {
    }

    public DeliverPlan(String id) {
        this.id = id;
    }


    @OneToMany(mappedBy = "deliverplan", cascade = {CascadeType.PERSIST})
    public List<ProcureUnit> units = new ArrayList<>();


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
     * 此出仓单的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    public P state;

    public enum P {
        /**
         * 已创建
         */
        CREATE {
            @Override
            public String label() {
                return "已创建";
            }
        },

        /**
         * 已交货
         */
        DONE {
            @Override
            public String label() {
                return "已交货";
            }
        };

        public abstract String label();
    }


    /**
     * 通过 ProcureUnit 来创建采购单
     * <p/>
     * ps: 创建 Delivery 不允许并发; 类锁就类锁吧... 反正常见 Delivery 不是经常性操作
     */
    public synchronized static DeliverPlan createFromProcures(List<Long> pids, String name, User user) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        DeliverPlan deliverplan = new DeliverPlan(DeliverPlan.id());
        if(pids.size() != units.size()) {
            Validation.addError("deliveryment.units.create", "%s");
            return deliverplan;
        }
        Cooperator cop = units.get(0).cooperator;
        for(ProcureUnit unit : units) {
            isUnitToDeliverymentValid(unit, cop);
        }
        if(Validation.hasErrors()) return deliverplan;
        deliverplan.cooperator = cop;
        deliverplan.handler = user;
        deliverplan.name = name.trim();
        deliverplan.units.addAll(units);
        // 将 ProcureUnit 添加进入 出货单 , ProcureUnit 进入 采购中 阶段
        for(ProcureUnit unit : deliverplan.units) {
            unit.toggleAssignTodeliverplan(deliverplan, true);
        }
        deliverplan.state = P.CREATE;
        deliverplan.save();
        new ERecordBuilder("deliverplan.createFromProcures")
                .msgArgs(StringUtils.join(pids, ","), deliverplan.id).fid(deliverplan.id).save();
        return deliverplan;
    }

    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        DeliverPlan deliverPlan = DeliverPlan.find("createDate>=? AND createDate<? ORDER BY createDate DESC",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate()
        ).first();
        String numStr = Optional.ofNullable(deliverPlan)
                .map(plan -> StringUtils.split(plan.id, "|"))
                .filter(charts -> ArrayUtils.isNotEmpty(charts) && charts.length == 3)
                .map(charts -> NumberUtils.toLong(charts[2]))//00 => 0, 01 => 1
                .map(num -> num + 1 + "")//0 => 1, 2 => 2, 10 => 11
                .map(num -> num.length() == 1 ? "0" + num : num)
                .orElse("00");
        return String.format("DP|%s|%s", dt.toString("yyyyMM"), numStr);
    }

    private static boolean isUnitToDeliverymentValid(ProcureUnit unit, Cooperator cop) {
        if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
            Validation.addError("", "采购计划单必须在采购中状态!");
            return false;
        }
        if(!cop.equals(unit.cooperator)) {
            Validation.addError("", "添加一个出货单只能一个供应商!");
            return false;
        }
        if(unit.deliverplan != null) {
            Validation.addError("", String.format("采购计划 %s 已经存在出货单 %s", unit.id, unit.deliverplan.id));
            return false;
        }
        return true;
    }


    /**
     * 将 PLAN 状态的 ProcureUnit 添加到这个出货单中, 用户制作出货单
     */
    public List<ProcureUnit> assignUnitToDeliverplan(List<Long> pids) {
        List<ProcureUnit> procureUnits = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        Cooperator singleCop = procureUnits.get(0).cooperator;
        for(ProcureUnit unit : procureUnits) {
            if(isUnitToDeliverymentValid(unit, singleCop)) {
                unit.toggleAssignTodeliverplan(this, true);
            }
            if(Validation.hasErrors()) return new ArrayList<>();
            unit.save();
        }
        new ElcukRecord(Messages.get("deliverplan.addunit"), Messages.get("deliverplan.addunit.msg", pids, this.id),
                this.id).save();
        return procureUnits;
    }

    /**
     * 将指定 ProcureUnit 从 出货单 中删除
     */
    public List<ProcureUnit> unassignUnitToDeliverplan(List<Long> pids) {
        List<ProcureUnit> procureUnits = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(ProcureUnit unit : procureUnits) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
                Validation.addError("deliveryment.units.unassign", "%s");
            } else {
                unit.toggleAssignTodeliverplan(null, false);
            }
        }
        if(Validation.hasErrors()) return new ArrayList<>();
        this.units.removeAll(procureUnits);
        this.save();

        new ElcukRecord(Messages.get("deliverplan.delunit"), Messages.get("deliverplan.delunit.msg", pids, this.id),
                this.id).save();
        return procureUnits;
    }

    /**
     * 返回此 DeliverPlan 可以用来添加的 ProcureUnits
     */
    public List<ProcureUnit> availableInPlanStageProcureUnits() {
        if(this.units.size() == 0) {
            return ProcureUnit.find("planstage=? AND attrs.planQty > 0", ProcureUnit.PLANSTAGE.DELIVERY).fetch(50);
        } else {
            Cooperator c = this.units.get(0).cooperator;
            return ProcureUnit.find("cooperator=? AND planstage!=? AND stage=? AND attrs.planQty>0",
                    c, ProcureUnit.PLANSTAGE.DELIVERY, ProcureUnit.STAGE.DELIVERY).fetch();
        }
    }

    /**
     * 获取此采购单的供应商, 如果没有采购货物, 则供应商为空, 否则为第一个采购计划的供应商(因为采购单只允许一个供应商)
     */
    public Cooperator supplier() {
        if(this.units.size() == 0) return null;
        return this.units.get(0).cooperator;
    }

    /**
     * 如果所有采购单都已经交货
     */
    public void delivery() {
        if(this.units == null || this.units.size() <= 0) return;
        boolean delivery = true;
        for(ProcureUnit unit : this.units) {
            if(unit.stage != ProcureUnit.STAGE.DONE) {
                delivery = false;
            }
        }
        if(delivery) {
            this.state = P.DONE;
            this.save();
        }
    }

    public String showInbounds() {
        List<Inbound> list = Inbound.find("plan.id=? AND status<> ? ", this.id, Inbound.S.Cancel).fetch();
        if(list == null || list.size() == 0) return "";
        StringBuilder ids = new StringBuilder();
        for(Inbound inbound : list) {
            ids.append(inbound.id).append("; ");
        }
        return ids.toString();
    }

    public long showNum(boolean showAll) {
        if(!showAll)
            return this.units.size();
        return this.units.stream()
                .filter(unit -> InboundUnit.validIsCreate(unit.id) && unit.stage == ProcureUnit.STAGE.DELIVERY).count();
    }

    public List<ProcureUnit> showUnits(boolean showAll) {
        if(!showAll)
            return this.units;
        return this.units.stream()
                .filter(unit -> InboundUnit.validIsCreate(unit.id) && unit.stage == ProcureUnit.STAGE.DELIVERY)
                .collect(Collectors.toList());
    }
}
