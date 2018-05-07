package models.procure;

import com.google.gson.annotations.Expose;
import helper.Dates;
import models.ElcukRecord;
import models.OperatorConfig;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.PaymentUnit;
import models.finance.ProcureApply;
import models.product.Category;
import models.view.Ret;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.libs.F;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购单, 用来记录所采购的 ProcureUnit
 * User: wyattpan
 * Date: 6/18/12
 * Time: 4:50 PM
 */
@Entity
@DynamicUpdate
public class Deliveryment extends GenericModel {

    private static final long serialVersionUID = -333313078948420021L;

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
                return "确认并已下单";
            }
        },
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
                return "审核通过";
            }
        },
        /**
         * 审核不通过
         */
        REJECT {
            @Override
            public String label() {
                return "审核不通过";
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

    @OneToMany(mappedBy = "deliveryment", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
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

    /**
     * 下单时间
     */
    public Date orderTime;

    /**
     * 交货时间
     */
    public Date deliveryTime;

    public Date confirmDate;

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
        /**
         * 普通单
         */
        NORMAL {
            @Override
            public String label() {
                return "普通单";
            }
        },

        /**
         * 手动单
         */
        MANUAL {
            @Override
            public String label() {
                return "手动单";
            }
        },
        /**
         * 挪货单
         */
        MOVE {
            @Override
            public String label() {
                return "挪货单";
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
     * 所属公司
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

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
        List<Date> deliveryDates = new ArrayList<>();
        for(ProcureUnit unit : this.units) {
            if(unit.stage.ordinal() >= ProcureUnit.STAGE.DONE.ordinal()
                    && unit.stage != ProcureUnit.STAGE.CLOSE)
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
        return new F.T2<>(first, end);
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
            if(!Objects.equals(unit.type, ProcureUnit.T.StockSplit)) {
                if(unit.stage != ProcureUnit.STAGE.PLAN && unit.stage != ProcureUnit.STAGE.DELIVERY)
                    delivery += unit.qty();
                total += unit.qty();
            }
        }
        if(Arrays.asList(S.PENDING, S.CONFIRM).contains(this.state) && delivery == total) {
            this.state = S.DONE;
            this.save();
        }
        return new F.T2<>(delivery, total == 0 ? 1 : total);
    }

    /**
     * 返回可以用来添加的 ProcureUnits
     *
     * @return
     */
    public List<ProcureUnit> availableInPlanStageProcureUnits() {
        if(this.units.size() == 0) {
            return ProcureUnit.find("stage=?", ProcureUnit.STAGE.PLAN).fetch(100);
        } else {
            Cooperator c = this.units.get(0).cooperator;
            return ProcureUnit.find("cooperator=? AND stage=?", c, ProcureUnit.STAGE.PLAN).fetch();
        }
    }

    /**
     * 确认下采购单
     */
    public void confirm() {
        if(!Arrays.asList(S.APPROVE, S.PENDING, S.REJECT).contains(this.state))
            Validation.addError("", "采购单状态非 " + S.PENDING.label() + " 不可以确认");
        Ret ret = this.validDmtIsNeedApply();
        if(ret.flag && this.state != S.APPROVE) {
            this.state = S.PENDING_REVIEW;
        } else {
            if(this.deliveryTime == null)
                Validation.addError("", "交货时间必须填写");
            if(this.orderTime == null)
                Validation.addError("", "下单时间必须填写");
            if(Validation.hasErrors()) return;
            this.confirmDate = new Date();
            this.state = S.CONFIRM;
        }
        this.save();
    }

    public Ret validDmtIsNeedApply() {
        double totalDmt = this.totalPerDeliveryment();
        double totalSeven = this.totalAmountForSevenDay();
        double checkLimit = Double.parseDouble(OperatorConfig.getVal("checklimit"));
        double checkLimitPerWeek = Double.parseDouble(OperatorConfig.getVal("checklimitperweek"));
        boolean flag = this.units.stream().anyMatch(unit -> ProcureUnit.find("cooperator.id =? and sku=?", this
                .cooperator.id, unit.sku).fetch().size() < 2);    //判断 该供应商包含第一次下单的sku
        boolean isNeedApply = (totalDmt > checkLimit);
        if(isNeedApply && flag)
            return new Ret(true, "单笔金额超过 ¥ " + checkLimit + "，并且是该供应商第一次下单的sku,需要审核，是否提交审核？");
        else if((totalSeven + totalDmt) > checkLimitPerWeek && flag)
            return new Ret(true, "该供应商本周金额超过 ¥ " + checkLimitPerWeek + "，并且是该供应商第一次下单的sku,需要审核，是否提交审核？");
        else
            return new Ret(false);
    }

    /**
     * 审核采购单
     */
    public void review(Boolean result, String msg) {
        if(this.state != S.PENDING_REVIEW)
            Validation.addError("", "采购单状态非 " + S.PENDING_REVIEW.label() + " 不可以确认");
        if(!result && StringUtils.isBlank(msg))
            Validation.addError("", "请填写审核不通过的原因！");
        if(Validation.hasErrors()) return;
        this.state = result ? S.APPROVE : S.REJECT;
        this.save();
        new ERecordBuilder("deliveryment.review").msgArgs(this.id, this.state.label(), msg).fid(this.id).save();
    }

    /**
     * 获取 Units 的产品类型
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
        // 只允许所有都是 units 都为 采购中 的才能够取消.
        if(this.units.stream().anyMatch(unit -> unit.stage != ProcureUnit.STAGE.DELIVERY)) {
            Validation.addError("", "采购计划必须全部都是采购中的才能取消采购单！");
            return;
        }
        if(Validation.hasErrors()) return;
        this.units.forEach(unit -> unit.toggleAssignTodeliveryment(null, false));
        this.state = S.CANCEL;
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
            Validation.addError("", "只允许 " + S.PENDING.label() + " 或者 " + S.CONFIRM.label()
                    + " 状态的[采购单]添加[采购单元]");
            return new ArrayList<>();
        }
        List<ProcureUnit> procureUnits = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        Cooperator singleCop = procureUnits.get(0).cooperator;
        for(ProcureUnit unit : procureUnits) {
            if(isUnitToDeliverymentValid(unit, singleCop)) {
                unit.toggleAssignTodeliveryment(this, true);
            }
            if(Validation.hasErrors()) return new ArrayList<>();
            unit.save();
        }

        new ElcukRecord(Messages.get("deliveryment.addunit"),
                Messages.get("deliveryment.addunit.msg", pids, this.id), this.id).save();

        return procureUnits;
    }

    /**
     * 将指定 ProcureUnit 从 Deliveryment 中删除
     *
     * @param pids
     */
    public List<ProcureUnit> unAssignUnitInDeliveryment(List<Long> pids) {
        List<ProcureUnit> procureUnits = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(pids)).fetch();
        for(ProcureUnit unit : procureUnits) {
            if(unit.stage != ProcureUnit.STAGE.DELIVERY) {
                Validation.addError("deliveryment.units.unassign", "%s");
            } else if(this.deliveryType == T.MANUAL && unit.selling == null) {
                //手动采购单中的默认的采购计划(没有 Selling)不允许从采购单中移除
                Validation.addError("", "手动单中默认的采购计划不允许被移除!");
            } else {
                unit.toggleAssignTodeliveryment(null, false);
            }
        }
        if(Validation.hasErrors()) return new ArrayList<>();
        this.units.removeAll(procureUnits);
        this.save();

        new ElcukRecord(Messages.get("deliveryment.delunit"),
                Messages.get("deliveryment.delunit.msg", pids, this.id), this.id).save();
        return procureUnits;
    }

    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = Deliveryment.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate())
                + "";
        return String.format("DL|%s|%s", dt.toString("yyyyMM"),
                count.length() == 1 ? "0" + count : count);
    }

    public static String b2bName() {
        String count = Deliveryment.count("deliveryType=? ", T.MOVE) + 1 + "";
        return String.format("YZ-%s", count.length() == 1 ? "00" + count : count.length() == 2 ? "0" + count : count);
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
        deliveryment.cooperator = cop;
        deliveryment.handler = user;
        deliveryment.state = S.PENDING;
        deliveryment.name = name.trim();
        deliveryment.units.addAll(units);
        deliveryment.deliveryType = T.NORMAL;
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

    /**
     * 查找，属于该采购单的产品要求
     */
    public List<CooperItem> getCopperItems() {
        List<CooperItem> cooperItems = new ArrayList<>();

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

    public List<ProcureUnit> applyUnit() {
        return ProcureUnit.find("deliveryment.id=? AND (type IS NULL OR type = ?) AND noPayment=?",
                this.id, ProcureUnit.T.ProcureSplit, false).fetch();
    }


    public double totalAmountForSevenDay() {
        List<Deliveryment> deliveryments = this.getRelateDelivery();
        return deliveryments.stream().mapToDouble(Deliveryment::totalPerDeliveryment).sum();
    }

    public double totalPerDeliveryment() {
        return this.units.stream().mapToDouble(ProcureUnit::totalAmountToCNY).sum();
    }

    public List<Deliveryment> getRelateDelivery() {
        String sql = "cooperator.id=? AND createDate >=? AND createDate<=? AND state <>?";
        return Deliveryment.find(sql, this.cooperator.id,
                Dates.morning(Dates.getMondayOfWeek()), Dates.night(new Date()), S.PENDING).fetch();
    }

    public static List<Cooperator> getDeliverymentCooperList(String id) {
        List<ProcureUnit> unitList = ProcureUnit.find("deliveryment.id=?", id).fetch();
        List<Long> cooperIds = unitList.stream().map(unit -> unit.cooperator.id).collect(Collectors.toList());
        return Cooperator.find(" id IN " + SqlSelect.inlineParam(cooperIds)).fetch();
    }

}
