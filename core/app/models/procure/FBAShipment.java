package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import helper.FBA;
import helper.Webs;
import models.market.Account;
import notifiers.FBAMails;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.FBAShipmentQuery;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    }

    @OneToOne
    public Account account;

    @ManyToOne
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
     * 最后的更新时间
     */
    public Date updateAt;

    @PreUpdate
    @PrePersist
    public void preUpdate() {
        this.updateAt = new Date();
    }

    /**
     * 设置 State, 并且根据 state 的变化判断是否需要邮件提醒, 并且根据状态设置 receivingAt 与 closeAt
     *
     * @param state
     */
    public void isNofityState(S state) {
        if(this.state != state)
            FBAMails.shipmentStateChange(this, this.state, state);
        this.state = state;
        if(this.state == S.RECEIVING) this.receivingAt = new Date();
        else if(this.state == S.CLOSED || this.state == S.DELETED) this.closeAt = new Date();
    }

    /**
     * 根据 Shipment 的装运输进度来判断 Amazon 是否已经签收
     *
     * @param shipment
     */
    public void checkReceipt(Shipment shipment) {
        if(this.receiptAt != null) return;
        this.receiptAt = shipment.arriveDate;
    }

    /**
     * 货物抵达后, FBA Shipment 的签收时间与开始入库时间的检查
     */
    public void receiptAndreceivingCheck() {
        // 只检查 PLAN 到 RECEIVING 之前的状态
        if(this.state.ordinal() >= S.RECEIVING.ordinal()) return;
        if(this.receiptAt != null && (System.currentTimeMillis() - this.receiptAt.getTime() >= TimeUnit.DAYS.toMillis(2))) {
            FBAMails.receiptButNotReceiving(this);
        }
    }


    /**
     * 将本地运输单的信息同步到 FBA Shipment<br/>
     * <p/>
     * 会通过 Record 去寻找从当前 FBAShipemnt 中删除的 ShipItem, 需要将其先从 FBA 中删除了再更新
     */
    public synchronized void updateFBAShipment(S state) {
        List<ShipItem> toBeUpdateItems = new ArrayList<ShipItem>();
        toBeUpdateItems.addAll(this.shipItems);
        try {
            this.state = FBA.update(this, toBeUpdateItems, state != null ? state : this.state);
        } catch(Exception e) {
            Validation.addError("", "向 Amazon 更新失败. " + Webs.E(e));
            return;
        }
        this.save();
    }

    /**
     * 删除这个 FBA Shipment
     */
    public synchronized void removeFBAShipment() {
        if(this.shipItems.size() > 0)
            throw new FastRuntimeException("还拥有运输项目, 无法删除");
        if(this.state != S.WORKING && this.state != S.PLAN)
            throw new FastRuntimeException("已经运输出去, 无法删除.");
        try {
            this.state = FBA.update(this, this.shipItems, S.DELETED);
            this.closeAt = new Date();
            this.save();
        } catch(FBAInboundServiceMWSException e) {
            throw new FastRuntimeException(e);
        }
    }

    /**
     * 入库过程中的检查
     *
     * @param fbaItems Amazon 更新下来的 msku 的入库数量
     */
    public void receivingCheck(Map<String, F.T2<Integer, Integer>> fbaItems, List<ShipItem> shippedItems) {
        if(this.state != S.RECEIVING) return;
        if(shippedItems == null || shippedItems.size() <= 0) return;
        if(fbaItems == null || fbaItems.size() <= 0) return;

        List<ShipItem> receivingTolong = new ArrayList<ShipItem>();
        List<ShipItem> receivingMissToMuch = new ArrayList<ShipItem>();

        for(ShipItem shipItem : shippedItems) {
            F.T2<Integer, Integer> receivedAndShipped = fbaItems.get(shipItem.unit.selling.merchantSKU);
            if(receivedAndShipped == null) continue;
            if(receivedAndShipped._1 == 0) continue;

            /**
             * 1. 检查入库时间过长
             * 入库时间长于 3 天, 并且接收了的数量与发送的数量不一样.
             */
            if((System.currentTimeMillis() - this.receivingAt.getTime() >= TimeUnit.DAYS.toMillis(3))
                    // TODO 难道真的要大于 10 个才提醒?
                    && !receivedAndShipped._1.equals(shipItem.qty)) {
                receivingTolong.add(shipItem);
            }

            /**
             * 2. 检查入库数量差据较大
             * 签收时间大于 3 天, 但是还没有进入 RECEVING 与其之后的状态则报告错误
             */
            if((System.currentTimeMillis() - this.receiptAt.getTime() >= TimeUnit.DAYS.toMillis(2))) {
                if(this.state == null) break;
                switch(this.state) {
                    case PLAN:
                    case WORKING:
                    case SHIPPED:
                    case IN_TRANSIT:
                    case DELIVERED:
                    case CHECKED_IN:
                        receivingMissToMuch.add(shipItem);
                }
            }
        }
        if(this.updateAt != null && (System.currentTimeMillis() - this.updateAt.getTime() >= TimeUnit.HOURS.toMillis(15)))
            FBAMails.itemsReceivingCheck(this, receivingTolong, receivingMissToMuch);
    }

    public String address() {
        return String.format("%s %s %s %s (%s)",
                this.fbaCenter.addressLine1, this.fbaCenter.city, this.fbaCenter.stateOrProvinceCode, this.fbaCenter.postalCode, this.centerId);
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


    @Override
    public String toString() {
        return this.shipmentId;
    }
}
