package models.material;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.DeliverPlan;
import models.procure.ProcureUnit;
import models.whouse.Inbound;
import models.whouse.InboundUnit;
import models.whouse.StockRecord;
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
public class MaterialPlan extends GenericModel {

    private static final long serialVersionUID = -5438540657539304801L;

    public MaterialPlan(String id) {
        this.id = id;
    }

    @Id
    @Column(length = 30)
    @Expose
    public String id;

    /**
     * 名称
     */
    public String name;

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
     * 收货类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = true)
    public R receipt;

    public enum R {
        /**
         * 工厂代收
         */
        FACTORY {
            @Override
            public String label() {
                return "工厂代收";
            }
        },

        /**
         * 仓库自收
         */
        WAREHOUSE {
            @Override
            public String label() {
                return "仓库自收";
            }
        };

        public abstract String label();
    }

    @OneToMany(mappedBy = "materialPlan", cascade = {CascadeType.PERSIST})
    public List<MaterialPlanUnit> units = new ArrayList<>();

    /**
     * 供应商
     * 一个采购单只能拥有一个供应商
     */
    @ManyToOne
    public Cooperator cooperator;

    /**
     * 出库类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public StockRecord.C type;

    /**
     * 目的地
     */
    @Required
    public String address;

    /**
     * 收货方
     */
    @Required
    public String receive;

    /**
     * 项目名称
     */
    @Required
    public String projectName;

    @OneToOne
    public User handler;

    @Expose
    @Required
    public Date createDate = new Date();

    @Lob
    public String memo = " ";


    /**
     * 生成ID
     *
     * @return
     */
    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        MaterialPlan deliverPlan = MaterialPlan.find("createDate>=? AND createDate<? ORDER BY createDate DESC",
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
        return String.format("WDP|%s|%s", dt.toString("yyyyMM"), numStr);
    }


    /**
     * 通过 MaterialUnit 来创建采购单
     * <p/>
     * ps: 创建 Delivery 不允许并发; 类锁就类锁吧... 反正常见 Delivery 不是经常性操作
     */
    public synchronized static MaterialPlan createFromProcures(List<Long> pids, String name, User user) {
        List<MaterialUnit> units = MaterialUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        MaterialPlan materialPlan = new MaterialPlan(MaterialPlan.id());
        if(pids.size() != units.size()) {
            Validation.addError("materialPurchase.units.create", "%s");
            return materialPlan;
        }
        Cooperator cop = units.get(0).cooperator;
        for(MaterialUnit unit : units) {
            isUnitToaterialPurchaseValid(unit, cop);
        }
        if(Validation.hasErrors()) return materialPlan;
        materialPlan.cooperator = cop;
        materialPlan.handler = user;
        materialPlan.name = name.trim();
        materialPlan.state = P.CREATE;

        // 将 MaterialUnit 添加进入 出货单 , MaterialUnit 进入 采购中 阶段
        for(MaterialUnit unit : units) {
            unit.toggleAssignTodeliverplan(materialPlan, true);
            MaterialPlanUnit planUnit = new MaterialPlanUnit();
            planUnit.materialPlan =  materialPlan;
            planUnit.materialUnit =  unit;
            planUnit.handler = Login.current();
            planUnit.stage = ProcureUnit.STAGE.DELIVERY;
            planUnit.planDeliveryDate =  unit.planDeliveryDate;
            materialPlan.units.add(planUnit);
        }
        
        materialPlan.save();
        new ERecordBuilder("materialPlan.createFromProcures")
                .msgArgs(StringUtils.join(pids, ","), materialPlan.id).fid(materialPlan.id).save();
        return materialPlan;
    }

    private static boolean isUnitToaterialPurchaseValid(MaterialUnit unit, Cooperator cop) {
        if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
            Validation.addError("", "物料采购计划单必须在采购中状态!");
            return false;
        }
        if(!cop.equals(unit.cooperator)) {
            Validation.addError("", "添加一个出货单只能一个供应商!");
            return false;
        }
        /** 验证物料计划下面的 出货计划数量总和是否等于 物料计划的数量 **/
         

        return true;
    }


    /**
     * 将指定 MaterialPlanUnit 从 出货单 中删除
     */
    public List<MaterialPlanUnit> unassignUnitToMaterialPlan(List<Long> pids) {
        List<MaterialPlanUnit> units = MaterialPlanUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(MaterialPlanUnit unit : units) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
                Validation.addError("deliveryment.units.unassign", "%s");
            } else {
                unit.toggleAssignToMaterialPlan(null, false);
            }
        }
        if(Validation.hasErrors()) return new ArrayList<>();
        this.units.removeAll(units);
        this.save();

        new ElcukRecord(Messages.get("deliverplan.delunit"),
                Messages.get("deliverplan.delunit.msg", pids, this.id), this.id).save();
        return units;
    }

}
