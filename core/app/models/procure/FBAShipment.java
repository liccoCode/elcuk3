package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import helper.FBA;
import helper.Webs;
import models.ElcukRecord;
import models.Notification;
import models.market.Account;
import notifiers.FBAMails;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.i18n.Messages;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.FBAShipmentQuery;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: wyattpan
 * Date: 9/12/12
 * Time: 4:27 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class FBAShipment extends Model {

    public enum S {
        /**
         * 还在 FBAShipment 的 PLAN 阶段, Amazon 还没有具体的 FBA Shipment
         */
        PLAN {
            @Override
            public String msg() {
                return "The shipment was planed by the seller, but has not create yet.";
            }
        },
        /**
         * 表示 FBA Shipment 的状态
         * The shipment was created by the seller, but has not yet shipped.
         */
        WORKING {
            @Override
            public String msg() {
                return "The shipment was created by the seller, but has not yet shipped.";
            }
        },
        /**
         * The shipment was picked up by the carrier.
         */
        SHIPPED {
            @Override
            public String msg() {
                return "The shipment was picked up by the carrier.";
            }
        },
        /**
         * The carrier has notified the Amazon fulfillment center that it is aware of the shipment.
         */
        IN_TRANSIT {
            @Override
            public String msg() {
                return "The carrier has notified the Amazon fulfillment center that it is aware of the shipment.";
            }
        },
        /**
         * The shipment was delivered by the carrier to the Amazon fulfillment center.
         */
        DELIVERED {
            @Override
            public String msg() {
                return "The shipment was delivered by the carrier to the Amazon fulfillment center.";
            }
        },
        /**
         * The shipment was checked-in at the receiving dock of the Amazon fulfillment center.
         */
        CHECKED_IN {
            @Override
            public String msg() {
                return "The shipment was checked-in at the receiving dock of the Amazon fulfillment center.";
            }
        },
        /**
         * The shipment has arrived at the Amazon fulfillment center, but not all items have been marked as received.
         */
        RECEIVING {
            @Override
            public String msg() {
                return "The shipment has arrived at the Amazon fulfillment center, but not all items have been marked as received.";
            }
        },
        /**
         * The shipment has arrived at the Amazon fulfillment center and all items have been marked as received.
         */
        CLOSED {
            @Override
            public String msg() {
                return "The shipment has arrived at the Amazon fulfillment center and all items have been marked as received.";
            }
        },
        /**
         * The shipment was cancelled by the seller after the shipment was sent to the Amazon fulfillment center.
         */
        CANCELLED {
            @Override
            public String msg() {
                return "The shipment was cancelled by the seller after the shipment was sent to the Amazon fulfillment center.";
            }
        },
        /**
         * The shipment was deleted by the seller.
         */
        DELETED {
            @Override
            public String msg() {
                return "The shipment was deleted by the seller.";
            }
        };

        /**
         * 状态的解释信息
         *
         * @return
         */
        public abstract String msg();

        /**
         * 是否处于可更改状态? 如果 FBA 运输出去了, 或者已经取消了, 则不可以进行变更了
         *
         * @return
         */
        public boolean isCanModify() {
            if(this == RECEIVING || this == CANCELLED || this == DELETED ||
                    this == SHIPPED || this == CLOSED) {
                return false;
            } else {
                return true;
            }
        }
    }

    @OneToOne
    public Account account;

    @ManyToOne(cascade = CascadeType.PERSIST)
    public Shipment shipment;

    /**
     * 与 ShipItem 的关联
     */
    @OneToMany(mappedBy = "fba", cascade = CascadeType.PERSIST)
    public List<ShipItem> shipItems = new ArrayList<ShipItem>();

    @Column(unique = true, nullable = false, length = 20)
    public String shipmentId;

    /**
     * 每一个 FBAShipment 拥有一个地址
     */
    @OneToOne
    public FBACenter fbaCenter;

    public String centerId;

    /**
     * 是否自己贴 Label
     * SELLER_LABEL
     * AMAZON_LABEL_ONLY
     * AMAZON_LABEL_PREFERRED
     * Note: Unless you are part of Amazon's label preparation program, SELLER_LABEL is the only valid
     */
    public String labelPrepType = "SELLER_LABEL";


    @Enumerated(EnumType.STRING)
    public S state = S.PLAN;

    /**
     * Amazon FBA 上的 title
     */
    public String title;

    /**
     * Amazon 上的 Items 的 html 记录
     */
    @Column(length = 3000/*没有使用 varchar(3000) 虽然可以支持 65535,直接使用了 longtext*/)
    public String itemsOnAmazonWithHTML;

    /**
     * 上次向 Amazon 查看 ShipmentItems 的时间
     */
    public Date lastWatchAmazonItemsAt;

    public Date createAt;

    /**
     * 签收时间, 需要通过 Shipment 的跟踪来设置
     */
    public Date receiptAt;

    /**
     * 开始接收的时间
     */
    public Date receivingAt;

    /**
     * 关闭/取消 时间
     */
    public Date closeAt;


    /**
     * 转移 FBA Shipment
     *
     * @param shipment
     */
    public void moveTo(Shipment shipment) {
        /**
         * FBA 转移的限制:
         * 1. ShipItem 的数量必须是 1 个
         * 2. 对应 Shipment 的运输地址必须是相同 FBA 仓库
         * 3. 运输项目的运输方式需要一样
         */
        if(!this.state.isCanModify()) {
            Validation.addError("", "FBA 已经无法变更状态. " + this.state);
        }
        if(this.shipItems.size() != 1) {
            Validation.addError("", "仅有当 FBA 中只有一个运输项目的时候才可以进行转移");
        }
        if(shipment.fbas.size() > 0) {
            FBAShipment fba = shipment.fbas.get(0);
            if(!fba.centerId.equals(this.centerId)) {
                Validation.addError("", "运输单中 FBA 的仓库地址不一样, 无法转移.");
            }
        }
        if(this.shipment.type != shipment.type) {
            ShipItem itm = this.shipItems.get(0);
            if(itm.unit.attrs.planArrivDate.getTime() < shipment.planArrivDate.getTime()) {
                Validation.addError("",
                        String.format("采购计划延期, 请与 PM 沟通调整计划. 采购计划预计在 %tF 抵达, 而目的运输单在 %tF 抵达.",
                                itm.unit.attrs.planArrivDate, shipment.planArrivDate));
            }
        }

        if(Validation.hasErrors()) return;
        String oldShipmentId = this.shipment.id;

        // 新 Shipment 的状态修改
        shipment.state = Shipment.S.CONFIRM;

        // 老 Shipment 的处理
        if(this.shipment.fbas.size() - 1 <= 0) {
            this.shipment.state = Shipment.S.PLAN;
            this.shipment.save();
        }

        // 仅当 FBA 只有一个运输项目的时候才可以转移
        ShipItem itm = this.shipItems.get(0);
        if(itm.unit.shipType != shipment.type) {
            Notification.notifies("采购计划运输提前",
                    String.format("采购计划 #%s 因运输单(%s)调整, 到库日期从 %tF 提前到 %tF", itm.unit.id,
                            shipment.id, itm.unit.attrs.planArrivDate, shipment.planArrivDate),
                    Notification.PM);
        }
        itm.shipment = shipment;
        itm.unit.attrs.planShipDate = shipment.planBeginDate;
        itm.unit.attrs.planArrivDate = shipment.planArrivDate;
        itm.unit.shipType = shipment.type;
        itm.unit.save();
        itm.save();

        if(shipment.fbas.size() <= 0) {
            shipment.target = this.address();
        }
        this.shipment = shipment;
        this.save();
        new ElcukRecord(Messages.get("shipment.moveFBA"),
                Messages.get("shipment.moveFBA.msg", this.shipmentId, oldShipmentId, shipment.id),
                oldShipmentId).save();
        Notification.notifies("FBA 运输单转移",
                Messages.get("shipment.moveFBA.msg", this.shipmentId, oldShipmentId, shipment.id),
                Notification.PM, Notification.SHIPPER);
    }

    /**
     * 设置 State, 并且根据 state 的变化判断是否需要邮件提醒, 并且根据状态设置 receivingAt 与 closeAt
     *
     * @param state
     */
    public void isNofityState(S state) {
        // 每一次的碰到 RECEIVING 状态都去检查一次的 ShipItem.unit 的阶段
        if(state == S.RECEIVING) {
            if(this.receivingAt == null)
            // 因为 Amazon 的返回值没有, 只能设置为最前检查到的时间
            {
                this.receivingAt = new Date();
            }

            // 当 FBA 检查到已经签收, ProcureUnit 进入 Inbound 阶段
            for(ShipItem itm : this.shipItems) {
                itm.unitStage(ProcureUnit.STAGE.INBOUND);
            }
            this.shipment.fbaReceviedBeforeShipmentDelivered();
        } else if(state == S.CLOSED || state == S.DELETED) {
            this.closeAt = new Date();
            for(ShipItem itm : this.shipItems) {
                itm.unitStage(ProcureUnit.STAGE.CLOSE);
            }
        }
        if(this.state != state) {
            FBAMails.shipmentStateChange(this, this.state, state);
        }
        this.state = state;
    }

    /**
     * 计算的总重量
     *
     * @return
     */
    public float totalWeight() {
        float weight = 0f;
        for(ShipItem itm : this.shipItems) {
            weight += itm.qty * itm.unit.product.weight;
        }
        return weight;
    }

    /**
     * 货物抵达后, FBA Shipment 的签收时间与开始入库时间的检查
     */
    public void receiptAndreceivingCheck() {
        // 已经开始接收的不再进行提醒
        if(this.state.ordinal() >= S.RECEIVING.ordinal()) return;
        if(this.receiptAt != null && (System.currentTimeMillis() - this.receiptAt.getTime() >=
                TimeUnit.DAYS.toMillis(2))) {
            FBAMails.receiptButNotReceiving(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        FBAShipment that = (FBAShipment) o;

        if(shipmentId != null ? !shipmentId.equals(that.shipmentId) : that.shipmentId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (shipmentId != null ? shipmentId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return this.shipmentId;
    }

    /**
     * 在签收状态之后
     *
     * @return
     */
    public boolean afterReceving() {
        if(this.state == S.RECEIVING || this.state == S.CLOSED ||
                this.state == S.CANCELLED/*像签收有误差的时候人工取消,会是 CANCEL 状态*/) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 将本地运输单的信息同步到 FBA Shipment<br/>
     * <p/>
     * 会通过 Record 去寻找从当前 FBAShipemnt 中删除的 ShipItem, 需要将其先从 FBA 中删除了再更新
     *
     * @throws FastRuntimeException 更新失败
     */
    public synchronized void updateFBAShipment(S state) {
        List<ShipItem> toBeUpdateItems = new ArrayList<ShipItem>();
        // 在手动更新 FBA 的时候, 同步 ShipItem, ProcureUnit, FBA
        for(ShipItem itm : this.shipItems) {
            itm.qty = itm.unit.qty();
        }

        toBeUpdateItems.addAll(this.shipItems);
        try {
            this.state = FBA.update(this, toBeUpdateItems, state != null ? state : this.state);
        } catch(Exception e) {
            throw new FastRuntimeException("向 Amazon 更新失败. " + Webs.E(e));
        }
        this.save();
    }

    /**
     * 删除这个 FBA Shipment
     */
    public synchronized void removeFBAShipment() {
        if(this.shipItems.size() > 0) {
            throw new FastRuntimeException("还拥有运输项目, 无法删除");
        }
        if(this.state != S.WORKING && this.state != S.PLAN) {
            throw new FastRuntimeException("已经运输出去, 无法删除.");
        }
        try {
            this.state = FBA.update(this, this.shipItems, S.DELETED);
            this.closeAt = new Date();
            this.save();
        } catch(FBAInboundServiceMWSException e) {
            throw new FastRuntimeException(e);
        }
    }

    /**
     * 入库进度
     *
     * @return
     */
    public F.T2<Integer, Integer> progress() {
        int recevied = 0;
        int total = 1;
        for(ShipItem itm : this.shipItems) {
            recevied += itm.recivedQty;
            total += itm.qty;
        }
        return new F.T2<Integer, Integer>(recevied, total > 1 ? total - 1 : total);
    }


    public String address() {
        return String.format("%s %s %s %s (%s)",
                this.fbaCenter.addressLine1, this.fbaCenter.city,
                this.fbaCenter.stateOrProvinceCode, this.fbaCenter.postalCode, this.centerId);
    }

    public String codeToCounrty() {
        return this.fbaCenter.codeToCountry();
    }

    public static List<String> uncloseFBAShipmentIds() {
        return FBAShipmentQuery.uncloseFBAShipmentIds();
    }

    public static FBAShipment findByShipmentId(String shipmentId) {
        return FBAShipment.find("shipmentId=?", shipmentId).first();
    }

    /**
     * 与当前运输单运输地址, FBA 仓库相同的其他运输单;(非周期型)
     *
     * @return
     */
    public List<Shipment> targetShipments() {
        if(this.shipment == null) return new ArrayList<Shipment>();
        return Shipment.find("SELECT DISTINCT(s) FROM Shipment s" +
                " LEFT JOIN s.fbas f" +
                " WHERE s.id!=? AND s.whouse=? AND cycle=false AND" +
                // 类似的 Shipment 有 FBA 则 centerId 需要一样, 或者没有 FBA
                " (f.centerId=? OR SIZE(s.fbas)=0) AND s.state IN (?,?) ORDER BY planBeginDate",
                this.shipment.id, this.shipment.whouse, this.centerId, Shipment.S.PLAN,
                Shipment.S.CONFIRM).fetch();
    }

}
