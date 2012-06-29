package models.procure;

import com.google.gson.annotations.Expose;
import helper.JPAs;
import helper.Webs;
import models.User;
import models.embedded.UnitDelivery;
import models.embedded.UnitPlan;
import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.libs.F;
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
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProcureUnit extends Model {

    /**
     * 阶段
     */
    public enum STAGE {
        /**
         * 计划阶段
         */
        PLAN,
        /**
         * 采购阶段
         */
        DELIVERY,
        /**
         * 完成了, 全部交货了
         */
        DONE,
        /**
         * 运输完成
         */
        SHIP_OVER,
        /**
         * 关闭阶段, 不处理了
         */
        CLOSE
    }

    /**
     * 采购单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Deliveryment deliveryment;

    /**
     * 所关联的运输出去的 ShipItem.
     */
    @OneToMany(mappedBy = "unit")
    public List<ShipItem> relateItems;

    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;
    @Expose
    public String sid; // 一个 SellingId 字段

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;
    @Expose
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
     * 计算阶段
     */
    @Expose
    public UnitPlan plan;

    /**
     * 采购|交付阶段
     */
    @Expose
    public UnitDelivery delivery;

    /**
     * 此 Unit 的状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public STAGE stage;


    @Lob
    @Expose
    public String comment = " ";


    /**
     * 检查参数并且创建新 ProcureUnit
     */
    public ProcureUnit checkAndCreate() {
        // 0. 检查是否已经存在
        if(this.id != null)
            if(ProcureUnit.findById(this.id) != null) throw new FastRuntimeException("ProcureUnit 已经存在了, 不允许重复创建.");
        this.check();
        return this.save();
    }

    public void checkAndUpdate() {
        this.check();
        this.save();
    }

    private void check() {
        // 1. 预期日期不允许为空
        if(this.plan.planArrivDate == null) throw new FastRuntimeException("预期日期不允许为空");
        // 2. 检查预计日期不允许比当前日期小
        if(this.plan.planArrivDate.getTime() - System.currentTimeMillis() < 0)
            throw new FastRuntimeException("预期日期已经过期!");
        // 3. 采购单价不允许 < 0
        if(this.plan.unitPrice <= 0) throw new FastRuntimeException("采购单价不允许小于等于 0");
        // 4. 采购量不允许 < 0
        if(this.plan.planQty <= 0) throw new FastRuntimeException("采购量不允许小于等于 0");
        // 5. 采购价格单位不允许为空
        if(this.plan.currency == null) throw new FastRuntimeException("采购的货币单位不允许为空.");
        // 6. 采购商不允许为空
        if(StringUtils.isBlank(this.plan.supplier)) throw new FastRuntimeException("供应商不能为空.");
        // 7. selling 必须存在
        if(StringUtils.isBlank(this.sid)) throw new FastRuntimeException("采购对应的 Selling 不能为空.");
        this.selling = Selling.findById(this.sid);
        if(this.selling == null) throw new FastRuntimeException(String.format("Selling %s 不存在", this.sid));
        // 8. 目标仓库必须拥有
        if(this.whouse == null) throw new FastRuntimeException("目的仓库不能为空");
        // 9. 采购人必须记录
        if(this.handler == null) throw new FastRuntimeException("必须拥有一个处理人.");
        this.product = Product.findById(this.sku);
        if(this.product == null) throw new FastRuntimeException("没有关联 Product.");
    }

    /**
     * @return
     * @4 将创建好的 ProcureUnit 指派给存在的采购单.
     * ProcureUnit 从 Plan stage 升级成为 Delivery Stage
     */
    public ProcureUnit assignToDeliveryment(Deliveryment deliveryment) {
        /**
         * 1. 检查是否合法
         * 2. 进行转换
         */
        // 1
        if(this.delivery.ensureQty == null || this.delivery.ensureQty < 0)
            throw new FastRuntimeException("最终确认数量不能为空!");
        if(this.delivery.planDeliveryDate == null) throw new FastRuntimeException("预计交货时间不允许为空!");
        if(this.delivery.planDeliveryDate.getTime() > this.plan.planArrivDate.getTime())
            throw new FastRuntimeException("预计交货时间不可能晚于预计到库时间!");
        if(deliveryment == null || !deliveryment.isPersistent())
            throw new FastRuntimeException("成为 Delivery Stage 采购单必须先存在.");
        for(ProcureUnit pu : deliveryment.units) {
            if(!pu.plan.supplier.equals(this.plan.supplier))
                //TODO supplier 改变, 这里也需要改变
                throw new FastRuntimeException("只允许相同工厂的 ProcureUnit 添加在同一个采购单中!");
        }

        // 2
        this.deliveryment = deliveryment;
        this.stage = STAGE.DELIVERY;
        return this.save();
    }

    /**
     * 此 ProcureUnit 工厂制作完成, 交货.
     *
     * @return
     */
    public ProcureUnit deliveryComplete() {
        if(this.stage != STAGE.DELIVERY) throw new FastRuntimeException("此采购计划的状态错误! 请找 IT 核实.[" + this.id + "]");
        if(this.delivery.deliveryDate == null) throw new FastRuntimeException("不允许更新实际交货日期为空");
        if(this.delivery.deliveryQty == null) throw new FastRuntimeException("不允许实际交货数量为空");
        if(this.delivery.deliveryQty < 0) throw new FastRuntimeException("不允许实际交货数量小于 0");
        this.stage = STAGE.DONE;
        this.deliveryment.beDelivery();

        return this.save();
    }

    public void close(String reason) {
        /**
         * 关闭这个 ProcureUnit, 需要找到所有起影响的元素, 然后将她们接触绑定. 并且记录 memo
         */
        if(this.stage == STAGE.DONE) throw new FastRuntimeException("已经完成的 ProcureUnit 不需要删除!");
        if(this.stage == STAGE.CLOSE) throw new FastRuntimeException("已经关闭了.");
        if(this.stage == STAGE.PLAN) {
            this.comment = reason;
        } else if(this.stage == STAGE.DELIVERY) {
            this.delivery.deliveryQty = 0;
            this.deliveryment.memo = "CLOSED ProcureUnit[" + this.id + "]\r\b" + this.deliveryment.memo;
            this.deliveryment.save();
        }

        this.stage = STAGE.CLOSE;
        this.save();
    }

    /**
     * 将当前 ProcureUnit 的数量转移到某一个 Shipment 的 ShipItem 中
     *
     * @param shipment
     * @param qty
     * @return
     */
    public ShipItem transformToShipment(Shipment shipment, Integer qty) {
        /**
         * 检查
         * 1. 首先找出 Shipment 中与此 ProcureUnit 一样的 ShipItem, 如果没有则创建
         * 2. 计算此 ProcureUnit 的剩余库存量
         * 3. ShipItem 转移的数量不能大于 ProcureUnit 的剩余库存量
         * 3. 保存
         */
        if(qty < 0) throw new FastRuntimeException("不允许填写负数!");
        if(shipment.state != Shipment.S.PLAN) throw new FastRuntimeException("此运输单已经发出, 不允许再修改!");
        if(qty > this.delivery.deliveryQty)
            throw new FastRuntimeException("转移的数量大于此 ProcureUnit 实际具有的数量!(" + qty + ">" + this.delivery.deliveryQty + ")");
        List<ShipItem> shipItems = ShipItem.find("shipment=? AND unit=?", shipment, this).fetch();
        if(shipItems.size() > 1) {
            Webs.systemMail("More then one ShipItem with the same SHIPMENT AND PROCUREUNIT",
                    String.format("More then one ShipItem with the same SHIPMENT AND PROCUREUNIT\r\n%s\r\n%s",
                            Webs.G(shipItems),
                            Webs.G(this)));
            throw new FastRuntimeException(String.format("同 Shipment(%s), ProcureUnit(%s) 的 ShipItem 拥有多与一个!", shipment.id, this.id));
        }
        F.T2<Integer, Set<String>> leftQtyTuple = leftTransferQty();
        if(qty > leftQtyTuple._1) throw new FastRuntimeException("库存不足够转移. 剩余: " + leftQtyTuple._1);

        ShipItem shipItem = null;
        if(shipItems.size() == 0) shipItem = new ShipItem(shipment, this);
        else shipItem = shipItems.get(0);


        shipItem.qty += qty;
        return shipItem.save();
    }

    /**
     * 判断这个 ProcureUnit 所关联的 ShipItem 是否全部运输完成.
     *
     * @return
     */
    public boolean canBeShipOver() {
        for(ShipItem item : this.relateItems) {
            if(item.shipment.state != Shipment.S.DONE) return false;
        }
        return true;
    }

    /**
     * 将当前的 ProcureUnit 变成 SHIP_OVER 状态, 会首先进行检查是否可以进行 SHIP_OVER 状态.
     */
    public void beShipOver() {
        if(!canBeShipOver()) return;
        this.stage = ProcureUnit.STAGE.SHIP_OVER;
        this.save();
    }

    public F.T2<Integer, Set<String>> leftTransferQty() {
        int shippedQty = 0;
        Set<String> shipmentIds = new HashSet<String>();
        for(ShipItem item : this.relateItems) {
            shippedQty += item.qty;
            shipmentIds.add(item.shipment.id);
        }
        return new F.T2<Integer, Set<String>>(this.delivery.deliveryQty - shippedQty, shipmentIds);
    }

    @SuppressWarnings("unchecked")
    public static List<String> suppliers() {
        Query query = JPAs.createQuery(new JpqlSelect().select("plan.supplier").from("ProcureUnit").groupBy("plan.supplier"));
        return query.getResultList();
    }

    public static List<ProcureUnit> findByStage(STAGE stage) {
        return ProcureUnit.find("stage=?", stage).fetch();
    }

    public static List<ProcureUnit> findWaitingForShip() {
        List<ProcureUnit> procureUnits = ProcureUnit.find("stage=? AND deliveryment.state!=? AND deliveryment.state!=?",
                STAGE.DONE, Deliveryment.S.PENDING, Deliveryment.S.CANCEL).fetch();
        Collections.sort(procureUnits, new Comparator<ProcureUnit>() {
            @Override
            public int compare(ProcureUnit p1, ProcureUnit p2) {
                return p2.leftTransferQty()._1 - p1.leftTransferQty()._1;
            }
        });
        return procureUnits;
    }

}
