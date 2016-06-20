package models.procure;

import com.google.gson.annotations.Expose;
import helper.DBUtils;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.PaymentUnit;
import models.finance.ProcureApply;
import models.product.Category;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Error;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.libs.F;

import javax.persistence.*;
import java.util.*;

/**
 * 采购单, 用来记录所采购的 ProcureUnit
 * User: wyattpan
 * Date: 6/18/12
 * Time: 4:50 PM
 */
@Entity
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
            public String label() {
                return "计划";
            }
        },
        /**
         * 确定采购单
         */
        CONFIRM {
            @Override
            public String label() {
                return "已确认";
            }
        },
        /**
         * 完成交货.
         */
        DONE {
            @Override
            public String label() {
                return "完成交货";
            }
        },
        CANCEL {
            @Override
            public String label() {
                return "取消";
            }
        };

        public abstract String label();
    }

    @OneToMany(mappedBy = "deliveryment", cascade = {CascadeType.PERSIST})
    public List<ProcureUnit> units = new ArrayList<>();

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

    @Expose
    public Date confirmDate;

    /**
     * 下单时间
     */
    public Date orderTime;

    /**
     * 交货时间
     */
    public Date deliveryTime;

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

    public enum T {
        NORMAL {
            @Override
            public String label() {
                return "普通单";
            }
        },
        MANUAL {
            @Override
            public String label() {
                return "手动单";
            }
        },
        Special {
            @Override
            public String label() {
                return "特采单";
            }
        },
        Expedited {
            @Override
            public String label() {
                return "加急单";
            }
        },
        New {
            @Override
            public String label() {
                return "新品单";
            }
        },
        B2B {
            @Override
            public String label() {
                return "B2B采购单";
            }
        };

        public abstract String label();
    }

    /**
     * 采购单类型
     */
    @Enumerated(EnumType.STRING)
    public T deliveryType;

    /**
     * 有无 Selling
     */
    @Expose
    public Boolean haveSelling = true;

    /**
     * 统计采购单中所有采购计划剩余的没有请款的金额
     *
     * @return CNY 币种下的总金额
     */
    public float leftAmount() {
        float leftAmount = 0;
        for(ProcureUnit unit : this.units) {
            leftAmount += unit.attrs.currency.toCNY(unit.leftAmount());
        }
        return leftAmount;
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
     * 计算此采购单的最早一个交货与最晚一个交货的时间, 如果只有一个, 那么最早==最晚
     *
     * @return
     */
    public F.T2<Date, Date> firstAndEndDeliveryDate() {
        Date first = null;
        Date end = null;
        List<Date> deliveryDates = new ArrayList<Date>();
        for(ProcureUnit unit : this.units) {
            if(unit.stage.ordinal() >= ProcureUnit.STAGE.DONE.ordinal() &&
                    unit.stage != ProcureUnit.STAGE.CLOSE)
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
     * 交货的状态.
     * 如果全部交货, 则进行交货状态更新
     *
     * @return
     */
    public F.T2<Integer, Integer> deliveryProcress() {
        int delivery = 0;
        int total = 0;
        for(ProcureUnit unit : this.units) {
            if(unit.stage != ProcureUnit.STAGE.PLAN && unit.stage != ProcureUnit.STAGE.DELIVERY)
                delivery += unit.qty();
            total += unit.qty();
        }
        if(Arrays.asList(S.PENDING, S.CONFIRM).contains(this.state) && delivery == total) {
            this.state = S.DONE;
            this.save();
        }
        return new F.T2<Integer, Integer>(delivery, total == 0 ? 1 : total);
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
            return ProcureUnit.find("cooperator=? AND stage=?", cooperator, ProcureUnit.STAGE.PLAN)
                    .fetch();
        }
    }

    /**
     * 批量确认
     *
     * @param deliverymentIds
     */
    public static List<String> batchConfirm(List<String> deliverymentIds) {
        List<String> errors = new ArrayList<>();
        List<String> confirmed = new ArrayList<>();
        for(String id : deliverymentIds) {
            Deliveryment dmt = Deliveryment.findById(id);
            dmt.confirm();

            if(Validation.hasErrors()) {
                for(Error error : Validation.errors()) {
                    String errMsg = String.format("ID: [%s] %s", id, error.message());
                    if(!errors.contains(errMsg)) errors.add(errMsg);
                }
                Validation.clear();
            } else {
                confirmed.add(id);
            }
        }
        if(!confirmed.isEmpty()) {
            new ERecordBuilder("deliveryment.confirm").msgArgs(StringUtils.join(confirmed, ",")).fid("1").save();
        }
        return errors;
    }

    /**
     * 确认下采购单
     */
    public void confirm() {
        if(this.state != S.PENDING)
            Validation.addError("", "采购单状态非 " + S.PENDING.label() + " 不可以确认");
        if(this.orderTime == null)
            Validation.addError("", "下单时间必须填写");
        if(Validation.hasErrors()) return;

        this.state = Deliveryment.S.CONFIRM;
        this.confirmDate = new Date();
        this.save();
    }

    /**
     * 获取 Units 的产品类型
     *
     * @return
     */
    public Set<Category> unitsCategorys() {
        Set<Category> categories = new HashSet<>();
        for(ProcureUnit unit : this.units) {
            categories.add(unit.product.category);
        }
        return categories;
    }

    /**
     * 取消采购单
     */
    public void cancel(String msg) {
        /**
         * 1. 只允许所有都是 units 都为 PLAN 的才能够取消.
         */
        for(ProcureUnit unit : this.units) {
            //   if(unit.stage != ProcureUnit.STAGE.DELIVERY)
            //    Validation.addError("deliveryment.units.cancel", "validation.required");
            //   else
            unit.toggleAssignTodeliveryment(null, false);
        }
        if(Validation.hasErrors()) return;
        this.state = S.CANCEL;
        this.memo = msg;
        this.save();

        new ElcukRecord(Messages.get("deliveryment.cancel"),
                Messages.get("deliveryment.cancel.msg", this.id, msg.trim()), this.id).save();
    }

    /**
     * 将 PLAN 状态的 ProcureUnit 添加到这个采购单中, 用户制作采购单
     *
     * @return
     */
    public List<ProcureUnit> assignUnitToDeliveryment(List<Long> pids) {
        if(!Arrays.asList(S.PENDING, S.CONFIRM).contains(this.state)) {
            Validation.addError("", "只允许 " + S.PENDING.label() + " 或者 " + S.CONFIRM.label() +
                    " 状态的[采购单]添加[采购单元]");
            return new ArrayList<ProcureUnit>();
        }
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(ProcureUnit unit : units) {
            if(isUnitToDeliverymentValid(unit)) {
                unit.toggleAssignTodeliveryment(this, true);
            }
            if(Validation.hasErrors()) return new ArrayList<ProcureUnit>();
            unit.save();
        }

        new ElcukRecord(Messages.get("deliveryment.addunit"),
                Messages.get("deliveryment.addunit.msg", pids, this.id), this.id).save();

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

        new ElcukRecord(Messages.get("deliveryment.delunit"),
                Messages.get("deliveryment.delunit.msg", pids, this.id), this.id).save();
        return units;
    }

    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = Deliveryment.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear()))
                        .toDate(),
                DateTime.parse(
                        String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear()))
                        .toDate()) + "";
        return String.format("DL|%s|%s", dt.toString("yyyyMM"),
                count.length() == 1 ? "0" + count : count);
    }

    /**
     * 是否可以和采购请款单分离?
     * (采购单向请款单中添加与剥离, 都需要保证这个采购单没有付款完成的付款单)
     *
     * @return
     */
    public boolean isProcureApplyDepartable() {
        // 这个采购单的采购计划所拥有的 PaymentUnit(支付信息)没有状态为 PAID 的.
        for(ProcureUnit unit : this.units) {
            for(PaymentUnit fee : unit.fees()) {
                if(fee.state == PaymentUnit.S.PAID)
                    return false;
            }
        }
        return true;
    }

    /**
     * 将采购单从其所关联的请款单中剥离开
     */
    public void departFromProcureApply() {
        /**
         * 1. 剥离没有过成功支付的采购单.
         * 2. 剥离后原有的 PaymentUnit 自动 remove 标记.
         */
        if(this.apply == null)
            Validation.addError("", "运输单没有添加进入请款单, 不需要剥离");
        if(!isProcureApplyDepartable()) {
            Validation.addError("", "当前采购单已经拥有成功支付信息, 无法剥离.");
            return;
        }
        for(ProcureUnit unit : this.units) {
            for(PaymentUnit fee : unit.fees()) {
                fee.procureFeeRemove(String.format(
                        "所属采购单 %s 从原有请款单 %s 中剥离.", this.id, this.apply.serialNumber));
            }
        }
        new ERecordBuilder("deliveryment.departApply")
                .msgArgs(this.id, this.apply.serialNumber)
                .fid(this.apply.id)
                .save();
        this.apply = null;
        this.save();
    }

    public static List<Deliveryment> openDeliveryments(S state) {
        return Deliveryment.find("state=? ORDER BY createDate DESC", state).fetch();
    }

    /**
     * 通过 ProcureUnit 来创建采购单
     * <p/>
     * ps: 创建 Delivery 不允许并发; 类锁就类锁吧... 反正常见 Delivery 不是经常性操作
     *
     * @param pids
     */
    public synchronized static Deliveryment createFromProcures(List<Long> pids, User user) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        Deliveryment deliveryment = new Deliveryment(Deliveryment.id());
        if(pids.size() != units.size()) {
            Validation.addError("deliveryment.units.create", "%s");
            return deliveryment;
        }
        for(ProcureUnit unit : units) isUnitToDeliverymentValid(unit);
        if(Validation.hasErrors()) return deliveryment;
        deliveryment.handler = user;
        deliveryment.state = S.PENDING;
        deliveryment.units.addAll(units);
        deliveryment.deliveryType = T.NORMAL;
        deliveryment.orderTime = new Date();
        for(ProcureUnit unit : deliveryment.units) {
            // 将 ProcureUnit 添加进入 Deliveryment , ProcureUnit 进入 DELIVERY 阶段
            unit.toggleAssignTodeliveryment(deliveryment, true);
        }
        deliveryment.save();
        new ERecordBuilder("deliveryment.createFromProcures")
                .msgArgs(StringUtils.join(pids, ","), deliveryment.id)
                .fid(deliveryment.id).save();
        return deliveryment;
    }

    private static boolean isUnitToDeliverymentValid(ProcureUnit unit) {
        if(unit.stage != ProcureUnit.STAGE.PLAN) {
            Validation.addError("deliveryment.units.unassign", "%s");
            return false;
        }
        if(unit.attrs.planDeliveryDate == null) Validation.addError("", String.format("[%s]的预计交货日期不能为空!", unit.id));
        return true;
    }

    /**
     * 查找，属于该采购单的产品要求
     */
    public List<CooperItem> getCopperItems() {
        List<CooperItem> cooperItems = new ArrayList<CooperItem>();

        for(ProcureUnit procureUnit : this.units) {
            if(procureUnit.cooperator != null) {
                CooperItem cooperItem = CooperItem.find("cooperator.id=? AND product.sku=?", procureUnit.cooperator.id,
                        procureUnit.product.sku).first();
                if(cooperItem == null)
                    Validation.addError("getCopperItems", "SKU: " + procureUnit.product.sku + " 不在供应商生产产品列表下!");
                else
                    cooperItems.add(cooperItem);
            }
        }
        return cooperItems;
    }

    public static T[] types() {
        return ArrayUtils.removeElement(T.values(), T.MANUAL);
    }

    public boolean canBeEdit() {
        return this.state == S.PENDING;
    }

    public boolean canBeCancle() {
        return Arrays.asList(S.PENDING, S.CONFIRM).contains(this.state);
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
                    "SELECT DISTINCT u.username AS username FROM Deliveryment d" +
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
     * 同步供应商到采购计划
     */
    public void syncCooperatorToUnits() {
        if(this.cooperator != null) {
            for(ProcureUnit unit : this.units) {
                unit.cooperator = this.cooperator;
                CooperItem cooperItem = cooperator.cooperItem(unit.sku);
                if(cooperItem != null) {
                    unit.attrs.price = cooperItem.price;
                    unit.attrs.currency = cooperItem.currency;
                }
                unit.save();
            }
        }
    }
}
