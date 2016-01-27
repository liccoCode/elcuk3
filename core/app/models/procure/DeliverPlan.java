package models.procure;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import org.apache.commons.lang.StringUtils;
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
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 16-1-21
 * Time: 上午10:12
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class DeliverPlan extends GenericModel {

    public DeliverPlan() {
    }

    public DeliverPlan(String id) {
        this.id = id;
    }


    @OneToMany(mappedBy = "deliverplan", cascade = {CascadeType.PERSIST})
    public List<ProcureUnit> units = new ArrayList<ProcureUnit>();


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
     * 通过 ProcureUnit 来创建采购单
     * <p/>
     * ps: 创建 Delivery 不允许并发; 类锁就类锁吧... 反正常见 Delivery 不是经常性操作
     *
     * @param pids
     */
    public synchronized static DeliverPlan createFromProcures(List<Long> pids, String name,
                                                              User user) {
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
        for(ProcureUnit unit : deliverplan.units) {
            if (unit.deliverplan!=null)
                Validation.addError("", String.format("采购计划单 %s 已经存在出货单 %s",unit.id,unit.deliverplan.id));
            // 将 ProcureUnit 添加进入 出货单 , ProcureUnit 进入 采购中 阶段
            unit.toggleAssignTodeliverplan(deliverplan, true);
        }
        deliverplan.save();

        new ERecordBuilder("deliverplan.createFromProcures")
                .msgArgs(StringUtils.join(pids, ","), deliverplan.id)
                .fid(deliverplan.id).save();
        return deliverplan;
    }


    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = DeliverPlan.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear()))
                        .toDate(),
                DateTime.parse(
                        String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear()))
                        .toDate()) + "";
        return String.format("DP|%s|%s", dt.toString("yyyyMM"),
                count.length() == 1 ? "0" + count : count);
    }


    private static boolean isUnitToDeliverymentValid(ProcureUnit unit, Cooperator cop) {
        if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
            Validation.addError("", "采购计划单必须在采购中状态!");
            return false;
        }
        if(!cop.equals(unit.cooperator)) {
            Validation.addError("", "添加一个出库单只能一个供应商!");
            return false;
        }
        return true;
    }


    /**
     * 将 PLAN 状态的 ProcureUnit 添加到这个出货单中, 用户制作出货单
     *
     * @return
     */
    public List<ProcureUnit> assignUnitToDeliverplan(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        Cooperator singleCop = units.get(0).cooperator;
        for(ProcureUnit unit : units) {
            if(isUnitToDeliverymentValid(unit, singleCop)) {
                unit.toggleAssignTodeliverplan(this, true);
            }
            if(Validation.hasErrors()) return new ArrayList<ProcureUnit>();
            unit.save();
        }

        new ElcukRecord(Messages.get("deliverplan.addunit"),
                Messages.get("deliverplan.addunit.msg", pids, this.id), this.id).save();

        return units;
    }

    /**
     * 将指定 ProcureUnit 从 出货单 中删除
     *
     * @param pids
     */
    public List<ProcureUnit> unassignUnitToDeliverplan(List<Long> pids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(ProcureUnit unit : units) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY)
                Validation.addError("deliveryment.units.unassign", "%s");
            else
                unit.toggleAssignTodeliverplan(null, false);
        }
        if(Validation.hasErrors()) return new ArrayList<ProcureUnit>();
        this.units.removeAll(units);
        this.save();

        new ElcukRecord(Messages.get("deliverplan.delunit"),
                Messages.get("deliverplan.delunit.msg", pids, this.id), this.id).save();
        return units;
    }

    /**
     * 返回此 Deliveryment 可以用来添加的 ProcureUnits
     *
     * @return
     */
    public List<ProcureUnit> availableInPlanStageProcureUnits() {
        if(this.units.size() == 0) {
            return ProcureUnit.find("planstage=?", ProcureUnit.PLANSTAGE.DELIVERY).fetch();
        } else {
            Cooperator cooperator = this.units.get(0).cooperator;
            return ProcureUnit.find("cooperator=? AND deliverplan!=?", cooperator,
                    ProcureUnit.PLANSTAGE.DELIVERY)
                    .fetch();
        }
    }

    /**
     * 获取此采购单的供应商, 如果没有采购货物, 则供应商为空, 否则为第一个采购计划的供应商(因为采购单只允许一个供应商)
     *
     * @return
     */
    public Cooperator supplier() {
        if(this.units.size() == 0) return null;
        return this.units.get(0).cooperator;
    }



}
