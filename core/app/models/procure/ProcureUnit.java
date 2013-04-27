package models.procure;

import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.Webs;
import models.ElcukRecord;
import models.Notification;
import models.User;
import models.embedded.ERecordBuilder;
import models.embedded.UnitAttrs;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.i18n.Messages;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.*;

/**
 * 每一个采购单元
 * User: wyattpan
 * Date: 6/11/12
 * Time: 5:23 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class ProcureUnit extends Model implements ElcukRecord.Log {

    public ProcureUnit() {
    }

    /**
     * Copy 一个全新的 ProcureUnit, 用来将部分交货的 ProcureUnit 分单交货
     *
     * @param unit
     */
    public ProcureUnit(ProcureUnit unit) {
        this.cooperator = unit.cooperator;
        this.selling = unit.selling;
        this.sid = unit.sid;
        this.product = unit.product;
        this.sku = unit.sku;
        this.handler = unit.handler;
        this.whouse = unit.whouse;
        // 刚刚创建 ProcureUnit 为 PLAN 阶段
        this.stage = STAGE.PLAN;
        this.attrs.planQty = unit.attrs.planQty - unit.qty();
        if(this.attrs.planQty < 0) this.attrs.planQty = 0;
        this.attrs.price = unit.attrs.price;
        this.attrs.currency = unit.attrs.currency;
        this.comment(String.format("此采购计划由于 #%s 采购计划分拆创建.", unit.id));
    }

    /**
     * 阶段
     */
    public enum STAGE {
        /**
         * 计划阶段; 创建一个新的采购计划
         */
        PLAN {
            @Override
            public String toString() {
                return "计划中";
            }
        },
        /**
         * 采购阶段; 从采购计划列表添加进入采购单.
         */
        DELIVERY {
            @Override
            public String toString() {
                return "采购中";
            }
        },
        /**
         * 完成了, 全部交货了; 在采购单中进行交货
         */
        DONE {
            @Override
            public String toString() {
                return "已交货";
            }
        },
        /**
         * 运输中; 运输单的点击开始运输
         */
        SHIPPING {
            @Override
            public String toString() {
                return "运输中";
            }
        },
        /**
         * 运输完成; 运输单跟踪到派送;
         */
        SHIP_OVER {
            @Override
            public String toString() {
                return "运输完成";
            }
        },
        /**
         * 入库中; Amazon FBA 的状态变更为 RECEVING
         */
        INBOUND {
            @Override
            public String toString() {
                return "入库中";
            }
        },
        /**
         * 关闭阶段, 不处理了; Amazon FBA 的状态变更为 CLOSE, CANCEL
         */
        CLOSE {
            @Override
            public String toString() {
                return "结束";
            }
        }
    }

    @OneToMany(mappedBy = "procureUnit", orphanRemoval = true, fetch = FetchType.LAZY)
    public List<PaymentUnit> fees = new ArrayList<PaymentUnit>();

    /**
     * 此采购计划的供应商信息.
     */
    @OneToOne
    public Cooperator cooperator;

    /**
     * 采购单
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public Deliveryment deliveryment;

    @ManyToOne
    public FBAShipment fba;

    /**
     * 所关联的运输出去的 ShipItem.
     */
    @OneToMany(mappedBy = "unit")
    public List<ShipItem> shipItems = new ArrayList<ShipItem>();


    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;


    @Expose
    @CheckWith(MskuCheck.class)
    public String sid; // 一个 SellingId 字段

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    @Expose
    @CheckWith(SkuCheck.class)
    public String sku;// 冗余 sku 字段
    /**
     * 操作人员
     */
    @OneToOne
    public User handler;

    /**
     * 送往的仓库
     */
    @OneToOne
    public Whouse whouse;

    // ----------- 将不同阶段的数据封装到不同的对象当中去.

    /**
     * ProcureUnit 所涉及的参数
     */
    @Expose
    @Required
    public UnitAttrs attrs = new UnitAttrs();


    /**
     * 货物运输的类型 (海运? 空运? 快递)
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Column(length = 20)
    public Shipment.T shipType;


    /**
     * 此 Unit 的状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public STAGE stage = STAGE.PLAN;

    public Date createDate = new Date();

    /**
     * 货物是否已经到达货代的地方
     */
    public boolean isPlaced = false;

    @Lob
    @Expose
    public String comment = " ";

    /**
     * ProcureUnit 的检查
     */
    public void validate() {
        Validation.current().valid(this);
        Validation.current().valid(this.attrs);
        Validation.required("procureunit.selling", this.selling);
        if(this.selling != null) this.sid = this.selling.sellingId;
        Validation.required("procureunit.whouse", this.whouse);
        Validation.required("procureunit.handler", this.handler);
        Validation.required("procureunit.product", this.product);
        if(this.product != null) this.sku = this.product.sku;
        Validation.required("procureunit.createDate", this.createDate);
        if(this.attrs != null) this.attrs.validate();
        if(this.selling != null && this.whouse != null && this.whouse.account != null) {
            if(!this.selling.market.equals(this.whouse.account.type) &&
                    this.whouse.type == Whouse.T.FBA) {
                Validation.addError("", "procureunit.validate.whouse");
            }
        }
    }


    public void updateWithShipment(Shipment shipment) {
        //TODO: effect 更新采购计划的运输运输单, 采购计划应该与运输单没有直接关系, 通过 ShipItem 来链接
        /*
        if(shipment == null) return;
        if(this.shipItem == null)
            shipment.addToShip(Arrays.asList(this.id));
        this.shipItem.changeShipment(shipment);
        if(this.shipType != shipment.type) {
            Shipment.T originType = this.shipType;
            this.shipType = shipment.type;
            this.comment(String.format("运输方式从 %s 变为 %s", originType, this.shipType));
        }
        this.save();
        */
    }

    /**
     * <pre>
     *
     * 分拆采购计划, 将原来一个采购计划根据 Whouse 与 qty 分拆出两个采购计划;
     * - 分开去往不同的地方
     * - 分拆进行先后交货
     * </pre>
     *
     * @param unit
     */
    public void split(ProcureUnit unit) {
        /**
         * 1. Selling 与 SKU 需要一样
         */
        int originQty = this.attrs.planQty;
        // 业务检查
        Validation.current().valid(this);
        unit.validate();
        if(this.stage != STAGE.PLAN && this.stage != STAGE.DELIVERY)
            Validation.addError(this.stage.toString(), "procureunit.split.state");
        Validation
                .equals("procureunit.selling", this.selling.sellingId, "", unit.selling.sellingId);
        Validation.equals("procureunit.sku", this.sku, "", unit.sku);
        Validation.max("procureunit.planQty", unit.attrs.planQty, this.attrs.planQty);
        int leftQty = this.attrs.planQty - unit.attrs.planQty;
        Validation.min("procureunit.planQty", leftQty, 0);
        //TODO: effect 分拆采购计划, 所关联的运输项目的数量需要平均分配
        // 如果 unit 对应的 shipment 已经运输, 不可再拆分
        /*
        if(this.shipItem != null && this.shipItem.shipment.state != Shipment.S.CONFIRM &&
                this.shipItem.shipment.state != Shipment.S.PLAN)
            Validation.addError("",
                    String.format("运输单 %s 已经为 %s 状态, 不运输再分拆已经发货了的采购计划.", this.shipItem.shipment.id,
                            this.shipItem.shipment.state));
        */
        if(Validation.hasErrors()) return;
        this.attrs.planQty = leftQty;
        //TODO effect: 如果拥有运输项目, 那么则将运输项目的数量也进行调整
//        this.casscadeShipItemQty(leftQty);
        // 如果被分开的 ProcureUnit 已经交货, 那么分开后也应该已经交货, 否则为 PLAN 阶段
        if(this.stage == STAGE.DELIVERY)
            unit.stage = this.stage;
        // 如果是在周期型运输单中, 则保留运输信息, 否则需要重新选择运输单
        // TODO effect: ??
        /*
        if(this.isHaveCycleShipment())
            unit.shipItem = new ShipItem(unit, this.shipItem.shipment);
        */
        unit.save();
        new ElcukRecord(Messages.get("procureunit.split"),
                Messages.get("procureunit.split.source.msg", originQty, leftQty), this.id + "")
                .save();
        new ElcukRecord(Messages.get("procureunit.split.target"),
                Messages.get("procureunit.split.target.msg", unit.to_log()), unit.id + "").save();
        /* TODO effect 需要调整通知
        if(this.shipItem != null && this.shipItem.shipment != null)
            Notification.notifies("采购计划分拆",
                    String.format("采购计划 #%s 被拆分, 请进入运输单 %s 确认并手动更新 FBA", this.id,
                            this.shipItem.shipment.id)
                    , Notification.PROCURE, Notification.SHIPPER);
        */
        this.save();
    }


    /**
     * 返回 ProcureUnit 的目标市场
     *
     * @return
     */
    public String shipMarket() {
        if(this.selling == null)
            throw new FastRuntimeException("采购计划没有关联 Selling, 不允许!");
        return this.selling.market.label();
    }

    public String shipType() {
        if(this.shipType == null)
            throw new FastRuntimeException("采购计划不可以没有运输方式!");
        return this.shipType.toString();
    }

    /**
     * ProcureUnit 交货
     *
     * @param attrs
     * @return T2: ._1:是否全部交货, ._2:新转移的采购单元
     */
    public Boolean delivery(UnitAttrs attrs) {
        /**
         * 1. 检查交货的数据是否符合
         * 2. 执行交货
         *  - 交货不足不允许交货.
         *  - 全部交货
         *  - 交货超额, Notify 提醒
         *
         */
        if(this.stage == STAGE.CLOSE || this.stage == STAGE.SHIP_OVER)
            Validation.addError("procureunit.delivery.stage", "%s");
        if(this.deliveryment == null)
            Validation.addError("", "没有进入采购单, 无法交货.");
        Validation.required("procureunit.attrs.qty", attrs.qty);
        Validation.min("procureunit.attrs.qty", attrs.qty, 0);
        Validation.required("procureunit.attrs.deliveryDate", attrs.deliveryDate);
        if(Validation.hasErrors())
            throw new FastRuntimeException("检查不合格");

        this.attrs = attrs;

        if(this.attrs.planQty > this.attrs.qty)
            throw new FastRuntimeException("交货量小于计划量, 请拆分采购计划.");
        if(this.attrs.planQty < this.attrs.qty)
            Notification.notifies(String.format("%s 超额交货", this.sku),
                    String.format("采购计划 %s 超额交货, 请从采购单 %s 找到产品的运输单进行调整, 避免运输数量不足.", this.id,
                            this.deliveryment.id), Notification.SHIPPER);

        new ElcukRecord(Messages.get("procureunit.delivery"),
                Messages.get("procureunit.delivery.msg", this.attrs.qty, this.attrs.planQty)
                , this.id + "").save();
        //TODO effect: 如果拥有运输项目, 那么则将运输项目的数量也进行调整
//        this.casscadeShipItemQty(this.attrs.planQty);
        // 当执行交货操作, ProcureUnit 进入交货完成阶段
        this.stage = STAGE.DONE;
        this.save();
        return this.attrs.planQty.equals(this.attrs.qty);
    }


    /**
     * 为采购计划添加运输项目(全部运输)
     * TODO: effect 为采购计划添加运输计划
     *
     * @param shipment
     * @return
     */
    public ShipItem ship(Shipment shipment) {
        ShipItem shipItem = null;
        for(ShipItem itm : shipment.items) {
            if(itm.unit.equals(this)) {
                shipItem = itm;
                shipItem.qty = this.qty();
            }
        }
        if(shipItem == null) {
            shipItem = new ShipItem();
            shipItem.shipment = shipment;
            shipItem.unit = this;
            shipItem.qty = this.qty();
            this.shipItems.add(shipItem);
        }
        return shipItem;
    }


    public String nickName() {
        return String.format("ProcureUnit[%s][%s][%s]", this.id, this.sid, this.sku);
    }

    /**
     * 将 ProcureUnit 添加到/移出 采购单,状态改变
     *
     * @param deliveryment
     */
    public void toggleAssignTodeliveryment(Deliveryment deliveryment, boolean assign) {
        if(assign) {
            this.deliveryment = deliveryment;
            this.stage = ProcureUnit.STAGE.DELIVERY;
        } else {
            this.deliveryment = null;
            this.stage = STAGE.PLAN;
        }
    }

    public void comment(String cmt) {
        this.comment = String.format("%s\r\n%s", cmt, this.comment).trim();
    }


    public void remove() {
        if(this.stage == STAGE.PLAN || this.stage == STAGE.DELIVERY) {
            new ElcukRecord(Messages.get("procureunit.remove"),
                    Messages.get("action.base", this.to_log()), "procures.remove").save();
            this.delete();
        } else {
            Validation.addError("",
                    String.format("只允许 %s, %s 状态的采购计划进行取消", STAGE.PLAN, STAGE.DELIVERY));
        }
    }

    /**
     * 采购单元相关联的运输单
     *
     * @return
     */
    public List<Shipment> relateShipment() {
        Set<Shipment> shipments = new HashSet<Shipment>();
        for(ShipItem shipItem : this.shipItems) {
            shipments.add(shipItem.shipment);
        }
        return new ArrayList<Shipment>(shipments);
    }

    public int qty() {
        if(this.attrs.qty != null) return this.attrs.qty;
        return this.attrs.planQty;
    }

    /**
     * 正在入库的数量;
     *
     * @return
     */
    public int inboundingQty() {
        //TODO effect: 正在入库数量需要重新计算: 总数量 - 运输项目已经接受的数量
//        return this.qty() - this.shipItem.recivedQty;
        return 0;
    }

    /**
     * 寻找 ProcureUnit 关联的 FBA
     *
     * @return
     */
    public FBAShipment fba() {
        //TODO effect: ProcureUnit 与 FBA 拥有直接联系, 需要调整.
        /*
        if(this.shipItem != null && this.shipItem.fba != null)
            return this.shipItem.fba;
        else
            return null;
            */
        return null;
    }

    /**
     * 通过运输计划获取到其运输单的预计开船时间
     * 1. 如果有运输单则返回运输单的预计开船时间
     * 2. 没有运输单则返回采购计划预计运输时间
     *
     * @return
     */
    public Date planShipBeginDate() {
        //TODO effect: 运输时间会根据运输项目会有多个.
        return null;
    }

    /**
     * 通过运输计划获取到运输单的预计到库时间
     *
     * @return
     */
    public Date planShipArriveDate() {
        //TODO effect: 同运输时间
        return null;
    }

    /**
     * 预付款申请
     */
    public PaymentUnit billingPrePay() {
        /**
         * 0. 基本检查
         * 1. 检查是否此采购计划是否已经存在一个预付款
         * 2. 申请预付款
         */
        this.billingValid();
        if(this.hasPrePay())
            Validation.addError("", "不允许重复申请预付款.");
        if(this.hasTailPay())
            Validation.addError("", "已经申请了尾款, 不需要再申请预付款.");
        if(Validation.hasErrors()) return null;

        PaymentUnit fee = new PaymentUnit(this);
        // 预付款的逻辑在这里实现, 总额的 30% 为预付款
        fee.feeType = FeeType.cashpledge();
        fee.amount = (float) (fee.amount * 0.3);
        fee.save();
        new ERecordBuilder("procureunit.prepay")
                .msgArgs(this.product.sku,
                        String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id)
                .save();
        return fee;
    }

    /**
     * 尾款申请
     */
    public PaymentUnit billingTailPay() {
        /**
         * 0. 基本检查
         * 1. 检查是否已经存在一个尾款
         * 2. 申请尾款
         */
        this.billingValid();
        if(Arrays.asList(STAGE.PLAN, STAGE.DELIVERY).contains(this.stage))
            Validation.addError("", "请确定采购计划的交货数量(交货)");
        if(this.hasTailPay())
            Validation.addError("", "不允许重复申请尾款");
        if(Validation.hasErrors()) return null;

        PaymentUnit fee = new PaymentUnit(this);
        fee.feeType = FeeType.procurement();
        fee.amount = this.leftAmount();
        fee.save();
        new ERecordBuilder("procureunit.tailpay")
                .msgArgs(this.product.sku,
                        String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id)
                .save();
        return fee;
    }

    /**
     * 1. 采购计划所在的采购单需要拥有一个请款单
     * 2. 采购计划需要已经交货
     */
    private void billingValid() {
        if(this.deliveryment.apply == null)
            Validation.addError("", String.format("采购计划所属的采购单[%s]还没有规划的请款单", this.deliveryment.id));
    }

    /**
     * 剩余的请款金额
     *
     * @return
     */
    public float leftAmount() {
        return totalAmount() - appliedAmount();
    }

    /**
     * 已经申请的金额
     *
     * @return
     */
    public float appliedAmount() {
        float appliedAmount = 0;
        for(PaymentUnit fee : this.fees()) {
            appliedAmount += fee.amount();
        }
        return appliedAmount;
    }

    /**
     * 当前采购计划所有请款的修正总额
     *
     * @return
     */
    public float fixValueAmount() {
        float fixValueAmount = 0;
        for(PaymentUnit fee : this.fees()) {
            fixValueAmount += fee.fixValue;
        }
        return fixValueAmount;
    }

    /**
     * 总共需要申请的金额
     *
     * @return
     */
    public float totalAmount() {
        return this.qty() * this.attrs.price;
    }

    /**
     * 是否拥有了 预付款
     *
     * @return
     */
    public boolean hasPrePay() {
        for(PaymentUnit fee : this.fees()) {
            if(fee.feeType == FeeType.cashpledge())
                return true;
        }
        return false;
    }

    /**
     * 是否拥有了尾款
     *
     * @return
     */
    public boolean hasTailPay() {
        for(PaymentUnit fee : this.fees()) {
            if(fee.feeType == FeeType.procurement())
                return true;
        }
        return false;
    }

    public List<PaymentUnit> fees() {
        List<PaymentUnit> fees = new ArrayList<PaymentUnit>();
        for(PaymentUnit fee : this.fees) {
            if(fee.remove) continue;
            fees.add(fee);
        }
        return fees;
    }

    /**
     * 转换成记录日志的格式
     *
     * @return
     */
    @Override
    public String to_log() {
        return String.format("[sid:%s] [仓库:%s] [供应商:%s] [计划数量:%s] [预计到库:%s] [运输方式:%s]",
                this.sid, this.whouse.name(), this.cooperator.fullName, this.attrs.planQty,
                Dates.date2Date(this.attrs.planArrivDate), this.shipType);
    }

    public List<ElcukRecord> records() {
        return ElcukRecord.fid(this.id + "").fetch();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        ProcureUnit that = (ProcureUnit) o;

        if(id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    /**
     * 根据 sku 或者 msku 加载 PLAN, DELIVERY Stage 的 ProcureUnit.
     *
     * @param selling
     * @param isSku
     * @return
     */
    public static List<ProcureUnit> skuOrMskuRelate(Selling selling, boolean isSku) {
        if(isSku)
            return find("sku=? AND stage IN (?,?,?)", Product.merchantSKUtoSKU(selling.merchantSKU),
                    STAGE.PLAN, STAGE.DELIVERY, STAGE.DONE).fetch();
        else
            return find("selling=? AND stage IN (?,?,?)", selling, STAGE.PLAN, STAGE.DELIVERY,
                    STAGE.DONE).fetch();
    }

    public static List<ProcureUnit> unitsFilterByStage(STAGE stage) {
        return ProcureUnit.find("stage=?", stage).fetch();
    }

    /**
     * 根据 采购单的状态, 采购计划的阶段, 运输方式, 去往仓库 进行运输项目的过滤
     *
     * @param whouseid
     * @param type
     * @return
     */
    public static List<ProcureUnit> waitToShip(long whouseid, Shipment.T type) {
        return ProcureUnit
                .find("deliveryment.state IN (?,?) AND stage NOT IN (?,?,?) AND shipType=?  AND whouse.id=? ORDER BY attrs.planArrivDate",
                        Deliveryment.S.DONE, Deliveryment.S.CONFIRM, STAGE.SHIPPING,
                        STAGE.SHIP_OVER, STAGE.CLOSE, type, whouseid).fetch();
    }


    static class MskuCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            ProcureUnit unit = (ProcureUnit) validatedObject;
            if(unit.selling == null) return false;
            String[] args = StringUtils.split(unit.selling.sellingId, Webs.S);
            if(args.length < 3) {
                setMessage("validation.msku", unit.selling.sellingId);
                return false;
            } else return true;
        }
    }

    static class SkuCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            ProcureUnit unit = (ProcureUnit) validatedObject;
            if(unit.product == null) return false;
            String[] args = StringUtils.split(unit.selling.sellingId, Webs.S);
            if(!StringUtils.contains(args[0], unit.product.sku)) {
                setMessage("validation.sku", unit.product.sku);
                return false;
            } else return true;
        }
    }
}
