package models.material;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.view.Ret;
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
import java.util.*;

/**
 * 物料出貨單
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/6/12
 * Time: PM6:18
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
         * 已创建：创建成功后即为“已创建”
         */
        CREATE {
            @Override
            public String label() {
                return "已创建";
            }
        },

        /**
         * 已出货：确认出货数量后为“已出货”
         */
        DONE {
            @Override
            public String label() {
                return "已出货";
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
     * 创建物料出货单
     */
    public synchronized static MaterialPlan createMaterialPlan(List<Long> pids, String name, User user) {
        List<Material> materias = Material.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();

        MaterialPlan materialPlan = new MaterialPlan(MaterialPlan.id());
        if(pids.size() != materias.size()) {
            Validation.addError("materialPurchase.units.create", "%s");
            return materialPlan;
        }
        Cooperator cop = Cooperator
                .find("SELECT c FROM Cooperator c, IN(c.cooperItems) ci WHERE ci.material.id=? ORDER BY ci" +
                        ".id", materias.get(0).id).first();

        if(Validation.hasErrors()) return materialPlan;
        materialPlan.cooperator = cop;
        materialPlan.handler = user;
        materialPlan.name = name.trim();
        materialPlan.state = P.CREATE;
        materialPlan.address = cop.address;

        // 将 Material 添加进入 出货单
        for(Material material : materias) {
            MaterialPlanUnit planUnit = new MaterialPlanUnit();
            planUnit.materialPlan = materialPlan;
            planUnit.material = material;
            planUnit.handler = Login.current();
            planUnit.stage = ProcureUnit.STAGE.DELIVERY;
            materialPlan.units.add(planUnit);
        }
        materialPlan.save();
        new ERecordBuilder("materialPlan.createMaterialPlan")
                .msgArgs(StringUtils.join(pids, ","), materialPlan.id).fid(materialPlan.id).save();
        return materialPlan;
    }


    /**
     * 将指定 MaterialPlanUnit 从 出货单 中删除
     */
    public List<MaterialPlanUnit> unassignUnitToMaterialPlan(List<Long> pids) {
        List<MaterialPlanUnit> units = MaterialPlanUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(MaterialPlanUnit unit : units) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
                Validation.addError("materialPlan.units.unassign", "%s");
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

    /**
     * 确认物料出货单
     */
    public void confirm() {
        if(!Arrays.asList(P.CREATE).contains(this.state))
            Validation.addError("", "采购单状态非 " + P.CREATE.label() + " 不可以确认");
        if(Validation.hasErrors()) return;
        this.state = P.DONE;
        this.save();
    }

    public static MaterialPlan addunits(String id, String code) {
        MaterialPlan materialPlan = MaterialPlan.findById(id);
        //验证物料编码是否存在于出货单元里面
        long count = materialPlan.units.stream().filter(unit -> unit.material.code.equals(code)).count();
        if(count > 0) {
            Validation.addError("", "物料编码 %s 已经存在于物料出库单元！", code);
            return materialPlan;
        }
        Material material = Material.find("byCode", code).first();
        if(material == null) {
            Validation.addError("", "物料编码 %s 不存在！", code);
            return materialPlan;
        }
        // 将 Material 添加进入 出货单
        MaterialPlanUnit planUnit = new MaterialPlanUnit();
        planUnit.materialPlan = materialPlan;
        planUnit.material = material;
        planUnit.handler = Login.current();
        planUnit.stage = ProcureUnit.STAGE.DELIVERY;
        materialPlan.units.add(planUnit);
        materialPlan.save();
        new ERecordBuilder("materialPlan.addunits")
                .msgArgs(code, materialPlan.id).fid(materialPlan.id).save();
        return materialPlan;
    }

}
