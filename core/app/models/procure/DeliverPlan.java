package models.procure;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        this.clearanceType = CT.Self;
    }

    public DeliverPlan(String id) {
        this();
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
    @Expose
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

    @OneToMany(mappedBy = "deliverPlan")
    public List<ReceiveRecord> receiveRecords = new ArrayList<>();

    public enum CT {
        Self {
            @Override
            public String label() {
                return "我司报关";
            }
        },
        Cooperator {
            @Override
            public String label() {
                return "工厂报关";
            }
        };

        public abstract String label();
    }

    @Expose
    @Enumerated(EnumType.STRING)
    public CT clearanceType;

    /**
     * 通过 ProcureUnit 来创建出货单
     * <p/>
     *
     * @param pids
     */
    public synchronized static DeliverPlan createFromProcures(List<Long> pids, User user) {
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
        deliverplan.handler = user;
        deliverplan.state = P.CREATE;
        deliverplan.clearanceType = CT.Self;
        deliverplan.cooperator = cop;
        for(ProcureUnit unit : units) {
            if(unit.deliverplan == null) {
                // 将 ProcureUnit 添加进入 出货单
                unit.toggleAssignTodeliverplan(deliverplan, true);
                unit.save();
            } else {
                Validation.addError("", String.format("采购计划单 %s 已经存在出货单 %s", unit.id, unit.deliverplan.id));
            }
        }
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
        Cooperator cop = units.get(0).cooperator;
        for(ProcureUnit unit : units) {
            if(isUnitToDeliverymentValid(unit, cop)) {
                unit.toggleAssignTodeliverplan(this, true);
            }
            if(Validation.hasErrors()) return new ArrayList<>();
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
            if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
                Validation.addError("deliveryment.units.unassign", "%s");
            } else {
                unit.toggleAssignTodeliverplan(null, false);
                unit.save();
            }
        }
        if(Validation.hasErrors()) return new ArrayList<>();
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
        Cooperator cooperator = this.units.get(0).cooperator;
        return ProcureUnit.find("cooperator=? AND stage=? AND deliverplan IS NULL", cooperator, ProcureUnit.STAGE
                .DELIVERY).fetch();
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

    /**
     * 确认出货单并生成收货记录
     */
    public void triggerReceiveRecords() {
        if(this.units == null || this.units.isEmpty()) return;
        this.state = P.DONE;
        this.save();
        for(ProcureUnit unit : this.units) {
            unit.stage = ProcureUnit.STAGE.INSHIPMENT;
            unit.save();

            ReceiveRecord receiveRecord = new ReceiveRecord(unit, this);
            if(!receiveRecord.isExists()) receiveRecord.validateAndSave();
        }
    }

    public boolean isLocked() {
        return this.state == P.DONE;
    }

    /**
     * 已经做过入库确认的人员名称
     *
     * @return
     */
    public static List<String> handlers() {
        List<String> names = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = DBUtils.rows(
                    "SELECT DISTINCT u.username AS username FROM DeliverPlan d" +
                            " LEFT JOIN User u ON u.id=d.handler_id"
            );
            if(rows != null && !rows.isEmpty()) {
                for(Map<String, Object> row : rows) {
                    if(row != null && row.containsKey("username")) {
                        if(row.get("username") != null) names.add(row.get("username").toString());
                    }
                }
            }
        } catch(NullPointerException e) {
            Logger.warn(Webs.E(e));
        }
        return names;
    }

    /**
     * 同步报告类型到采购计划中去
     */
    public void syncClearanceTypeToUnits() {
        for(ProcureUnit unit : this.units) {
            if(unit.clearanceType != this.clearanceType) {
                unit.clearanceType = this.clearanceType;
                unit.save();
            }
        }
    }

    /**
     * 尝试补全供应商
     * @return
     */
    public Cooperator cooperator() {
        if(this.cooperator == null && this.units != null && !this.units.isEmpty()) {
            this.cooperator = this.units.get(0).cooperator;
        }
        return this.cooperator;
    }
}
