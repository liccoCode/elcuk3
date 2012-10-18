package models.procure;

import models.market.Account;
import notifiers.FBAMails;
import play.db.jpa.Model;
import play.libs.F;
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
        };

        /**
         * 状态的解释信息
         *
         * @return
         */
        public abstract String msg();
    }
    /*
    例子:
    ShipToAddress: {
        // -> 在 FBACenter
    	"addressLine1":"Boundary Way",
    	"city":"Hemel Hempstead",
    	"countryCode":"GB",
    	"name":"Amazon.co.uk",
    	"postalCode":"HP27LF",
    	"stateOrProvinceCode":"Hertfordshire"


    	"setAddressLine1":true,
    	"setAddressLine2":false,
    	"setCity":true,
    	"setCountryCode":true,
    	"setDistrictOrCounty":false,
    	"setName":true,
    	"setPostalCode":true,
    	"setStateOrProvinceCode":true,
    }
     */

    @OneToOne
    public Account account;

    @OneToMany(mappedBy = "fbaShipment")
    public List<Shipment> shipments = new ArrayList<Shipment>();

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
     * 设置 State, 并且根据 state 的变化判断是否需要邮件提醒, 并且根据状态设置 receivingAt 与 closeAt
     *
     * @param state
     */
    public void isNofityState(S state) {
        if(this.state != state)
            FBAMails.shipmentStateChange(this, this.state, state);
        this.state = state;
        if(this.state == S.RECEIVING) this.receivingAt = new Date();
        else if(this.state == S.CLOSED) this.closeAt = new Date();
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

            // 1. 检查入库时间过长
            if((System.currentTimeMillis() - this.receivingAt.getTime() >= TimeUnit.DAYS.toMillis(3)
                    && receivedAndShipped._1 > 0
                    && receivedAndShipped._1 < shipItem.qty)) {
                receivingTolong.add(shipItem);
            }

            // 2. 检查入库数量差据较大
            if((System.currentTimeMillis() - this.receiptAt.getTime() >= TimeUnit.DAYS.toMillis(2))
                    && Math.abs(receivedAndShipped._1 - shipItem.qty) >= 10) {
                receivingMissToMuch.add(shipItem);
            }
        }
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
}
