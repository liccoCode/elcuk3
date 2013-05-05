package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.FBA;
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
        this.deliveryment = unit.deliveryment;
        // 刚刚创建 ProcureUnit 为 PLAN 阶段
        this.stage = STAGE.PLAN;
        this.shipType = unit.shipType;
        this.attrs.planDeliveryDate = unit.attrs.planDeliveryDate;
        this.attrs.planShipDate = unit.attrs.planShipDate;
        this.attrs.planArrivDate = unit.attrs.planArrivDate;
        this.attrs.price = unit.attrs.price;
        this.attrs.currency = unit.attrs.currency;
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
            public String label() {
                return "计划中";
            }
        },
        /**
         * 采购阶段; 从采购计划列表添加进入采购单.
         */
        DELIVERY {
            @Override
            public String label() {
                return "采购中";
            }
        },
        /**
         * 完成了, 全部交货了; 在采购单中进行交货
         */
        DONE {
            @Override
            public String label() {
                return "已交货";
            }
        },
        /**
         * 运输中; 运输单的点击开始运输
         */
        SHIPPING {
            @Override
            public String label() {
                return "运输中";
            }
        },
        /**
         * 运输完成; 运输单跟踪到派送;
         */
        SHIP_OVER {
            @Override
            public String label() {
                return "运输完成";
            }
        },
        /**
         * 入库中; Amazon FBA 的状态变更为 RECEVING
         */
        INBOUND {
            @Override
            public String label() {
                return "入库中";
            }
        },
        /**
         * 关闭阶段, 不处理了; Amazon FBA 的状态变更为 CLOSE, CANCEL
         */
        CLOSE {
            @Override
            public String label() {
                return "结束";
            }
        };

        public abstract String label();
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

    /**
     * 分拆采购计划: 工厂在原有采购计划基础上全部交货, 只能够部分交货的情况下进行的操作;
     * 1. 原有的采购计划仅仅数量变化.
     * 2. 新创建采购计划, 处理数量, FBA, Shipment 相关信息变化, 其他保持不变.
     *
     * @param unit
     */
    public ProcureUnit split(ProcureUnit unit) {
        int originQty = this.qty();
        if(unit.attrs.planQty >= originQty)
            Validation.addError("", "因分批交货创建的采购计划的数量不可能大于原来采购计划的数量.");
        if(unit.attrs.planQty <= 0)
            Validation.addError("", "新创建分批交货的采购计划数量必须大于 0");
        if(!this.isBeforeDONE())
            Validation.addError("", "已经交货或者成功运输, 不需要分拆采购计划.");

        ProcureUnit newUnit = new ProcureUnit(this);
        newUnit.attrs.planQty = unit.attrs.planQty;
        newUnit.stage = STAGE.DELIVERY;
        newUnit.validate();

        List<Shipment> shipments = Shipment.similarShipments(newUnit.attrs.planShipDate,
                newUnit.whouse, newUnit.shipType);
        if(shipments.size() <= 0)
            Validation.addError("",
                    String.format("没有合适的运输单, 请联系运输部门, 创建 %s 之后去往 %s 的 %s 运输单.",
                            newUnit.attrs.planShipDate, newUnit.whouse.name, newUnit.shipType));

        if(Validation.hasErrors()) return newUnit;
        Shipment shipment = shipments.get(0);
        // FBA 变更
        if(this.fba != null)
            this.fba.updateFBAShipment(null);

        // 原采购计划数量变更
        this.attrs.planQty = originQty - newUnit.attrs.planQty;
        if(this.attrs.qty != null)
            this.attrs.qty = this.attrs.planQty;
        this.save();

        // 原采购计划的运输量变更
        int average = (int) Math.ceil((float) this.qty() / this.shipItems.size());
        for(int i = 0; i < this.shipItems.size(); i++) {
            // 平均化, 包含除不尽的情况
            if(i == this.shipItems.size() - 1) {
                this.shipItems.get(i).qty =
                        this.qty() - (average * this.shipItems.size() - 1);
            } else {
                this.shipItems.get(i).qty = average;
            }
        }

        // 分拆出的新采购计划变更
        newUnit.save();
        shipment.addToShip(newUnit);

        new ERecordBuilder("procureunit.split")
                .msgArgs(this.id, newUnit.attrs.planQty, newUnit.id)
                .fid(this.id)
                .save();
        return newUnit;
    }

    /**
     * 是否完成全部交货
     *
     * @return
     */
    public boolean isBeforeDONE() {
        return Arrays.asList(STAGE.DELIVERY, STAGE.PLAN).contains(this.stage);
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
     * 采购计划的预计日期变更
     *
     * @param planDeliveryDate 预计交货日期
     * @param planShipDate     预计发货日期
     * @param planArrivDate    预计抵达日期
     */
    public void updatePlanDates(Date planDeliveryDate, Date planShipDate, Date planArrivDate) {
        this.attrs.planDeliveryDate = planDeliveryDate;
        this.attrs.planShipDate = planShipDate;
        this.attrs.planArrivDate = planArrivDate;
        this.attrs.validate();
        if(Validation.hasErrors()) return;
        this.save();
    }

    /**
     * 通过 ProcureUnit 创建 FBA
     */
    public synchronized FBAShipment postFbaShipment() {
        FBAShipment fba = null;
        try {
            fba = FBA.plan(this.selling.account, this);
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("", "向 Amazon 创建 Shipment PLAN 因 " + Webs.E(e) + " 原因失败.");
            return null;
        }
        try {
            fba.state = FBA.create(fba);
            this.fba = fba.save();
            this.save();
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("", "向 Amazon 创建 Shipment 错误 " + Webs.E(e));
        }
        return fba;
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
        /**
         * TODO: 这里需要理清楚
         * 1. 什么时候可以删除采购计划?
         * 2. 如果在拥有 FBA 后仍然可以删除采购计划, 需要如何处理?
         */
        if(this.stage == STAGE.PLAN || this.stage == STAGE.DELIVERY) {
            // 删除 FBA
            FBAShipment fba = this.fba;
            if(fba != null) {
                fba.units.remove(this);
                fba.removeFBAShipment();
            }

            // 删除运输相关
            for(ShipItem item : this.shipItems) {
                item.delete();
            }
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
