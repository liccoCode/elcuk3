package models.material;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.PaymentUnit;
import models.procure.CooperItem;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
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

    @Id
    @Column(length = 30)
    @Expose
    public String id;

    /**
     * 名称
     */
    public String name;

    /**
     * 此出货单的状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    public P state;

    public enum P {
        /**
         * 取消状态
         */
        CANCEL {
            @Override
            public String label() {
                return "取消";
            }
        }, /**
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


    /**
     * 财务审核状态
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Column(nullable = false)
    public S financeState;

    public enum S {
        /**
         * 待审核
         */
        PENDING_REVIEW {
            @Override
            public String label() {
                return "待审核";
            }
        },
        /**
         * 审核通过
         */
        APPROVE {
            @Override
            public String label() {
                return "已审";
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
     * 项目名称
     */
    @Required
    public String projectName;

    /**
     * 收货方供应商
     */
    @ManyToOne
    public Cooperator receiveCooperator;

    /**
     * 目的地(收货方供应商)
     */
    public String address;


    /**
     * 出货时间(即为确认交货数量时的时间)
     */
    @Expose
    public Date deliveryDate;

    @OneToOne
    public User handler;

    @Expose
    @Required
    public Date createDate = new Date();

    @Lob
    public String memo = " ";

    /**
     * 物料请款单
     */
    @ManyToOne
    public MaterialApply apply;
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
     * 将指定 MaterialPlanUnit 从 出货单 中删除
     */
    public List<MaterialPlanUnit> unassignUnitToMaterialPlan(List<Long> pids) {
        List<MaterialPlanUnit> planUnits = MaterialPlanUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(MaterialPlanUnit unit : planUnits) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
                Validation.addError("materialPlan.units.unassign", "%s");
            } else {
                unit.toggleAssignToMaterialPlan(null, false);
            }
        }
        if(Validation.hasErrors()) return new ArrayList<>();
        this.units.removeAll(planUnits);
        this.save();

        new ElcukRecord(Messages.get("materialplans.delunit"),
                Messages.get("materialplans.delunit.msg",  pids, this.id), this.id).save();

        return planUnits;
    }

    /**
     * 确认物料出货单
     */
    public void confirm() {
        if(!Arrays.asList(P.CREATE).contains(this.state))
            Validation.addError("", "出货单状态非 " + P.CREATE.label() + " 不可以确认");
        if(this.units.stream().anyMatch(unit -> unit.qty == 0))
            Validation.addError("", "出货单下存在交货数量为0的出货单元 不可以确认");
        if(Validation.hasErrors()) return;
        this.state = P.DONE;
        this.deliveryDate = new Date();
        this.save();
        new ElcukRecord(Messages.get("materialplans.confirm"),
                Messages.get("materialplans.confirm.msg", this.id), this.id).save();
    }

    /**
     * 出货单快速添加物料编码
     *
     * @param id
     * @param code
     * @return
     */
    public static MaterialPlan addunits(String id, String code) {
        MaterialPlan materialPlan = MaterialPlan.findById(id);
        //验证物料编码是否存在于出货单元里面
        long count = materialPlan.units.stream().filter(unit -> unit.material.code.equals(code)).count();
        if(count > 0) {
            Validation.addError("", "物料编码 %s 已经存在于物料出库单元！", code);
            return materialPlan;
        }
        //验证物料编码是否存在于物料信息
        Material material = Material.find("byCode", code).first();
        if(material == null) {
            Validation.addError("", "物料编码 %s 不存在！", code);
            return materialPlan;
        }
        //验证该物料与出货单是否同一个供应商
        List<CooperItem> cooperItems = CooperItem.find(" material.code=? AND cooperator.id = ?", code,
                materialPlan.cooperator.id).fetch();
        if(cooperItems == null || cooperItems.size() < 1) {
            Validation.addError("", "物料编码 %s 与当前出货单 供应商不一致！", code);
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
        new ElcukRecord(Messages.get("materialplans.addunit"),
                Messages.get("materialplans.addunit.msg", code ,id), id).save();
        return materialPlan;
    }

    /**
     * 财务审核
     *
     * @param pids
     */
    public static void approve(List<String> pids) {
        List<MaterialPlan> plans = MaterialPlan.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(MaterialPlan plan : plans) {
            if(plan.financeState != S.PENDING_REVIEW) {
                Validation.addError("", "采购单 %s 状态非 %s 不可以审核", plan.id, S.PENDING_REVIEW.label());
            }
        }
        if(Validation.hasErrors()) return;
        for(MaterialPlan plan : plans) {
            plan.financeState = S.APPROVE;
            plan.save();
        }
    }

    /**
     * 是否可以和采购请款单分离?
     * (采购单向请款单中添加与剥离, 都需要保证这个采购单没有付款完成的付款单)
     *
     * @return
     */
    public boolean isProcureApplyDepartable() {
        // 这个采购单的采购计划所拥有的 PaymentUnit(支付信息)没有状态为 PAID 的.
        for(MaterialPlanUnit unit : this.units) {
            for(PaymentUnit fee : unit.fees()) {
                if(fee.state == PaymentUnit.S.PAID){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * material页面获取出货单明细
     * @return
     */
    public List<MaterialPlanUnit> applyUnit() {
        return MaterialPlanUnit.find("materialPlan.id=? ",this.id).fetch();
    }



    /**
     * 将出货单从其所关联的请款单中剥离开
     */
    public void departFromProcureApply() {
        /**
         * 1. 剥离没有过成功支付的出货单.
         * 2. 剥离后原有的 PaymentUnit 自动 remove 标记.
         */
        if(this.apply == null){
            Validation.addError("", "出货单没有添加进入请款单, 不需要剥离");
        }
        if(!isProcureApplyDepartable()) {
            Validation.addError("", "当前出货单已经拥有成功支付信息, 无法剥离.");
            return;
        }
        for(MaterialPlanUnit unit : this.units) {
            for(PaymentUnit fee : unit.fees()) {
                fee.materialFeeRemove(String.format(
                        "所属出货单 %s 从原有请款单 %s 中剥离.", this.id, this.apply.serialNumber));
            }
        }
        new ERecordBuilder("materialplans.departapply")
                .msgArgs(this.id, this.apply.serialNumber)
                .fid(this.apply.id)
                .save();
        this.apply = null;
        this.save();
    }
}
