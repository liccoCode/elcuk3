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

    @Column(unique = true, nullable = false, length = 20)
    public String shipmentId;

    @OneToMany(mappedBy = "fba")
    public List<ProcureUnit> units = new ArrayList<ProcureUnit>();

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
        // 每一次的碰到 RECEIVING 状态都去检查一次的 ShipItem.unit 的阶段
        /* TODO effect: FBA 状态变化所需要做的变化, 还需要讨论
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
        */
        this.state = state;
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
        try {
            this.state = FBA.update(this, state != null ? state : this.state);
        } catch(Exception e) {
            throw new FastRuntimeException("向 Amazon 更新失败. " + Webs.E(e));
        }
        this.save();
    }

    /**
     * 向 Amazon 提交报告, 对这个 FBA 进行删除标记
     * 收件减少 FBA 中的数量, 直到数量为 0 了则标记删除
     */
    public synchronized void removeFBAShipment() {
        if(this.state != S.WORKING && this.state != S.PLAN)
            Validation.addError("", "已经运输出去了, 无法删除.");
        if(Validation.hasErrors()) return;

        try {
            if(this.units.size() > 0) {
                this.updateFBAShipment(null);
            } else {
                this.state = FBA.update(this, S.DELETED);
                if(this.state == S.DELETED) {
                    /**
                     * 标记删除这个 FBA, 与其有关的采购计划全部清理
                     */
                    for(ProcureUnit unit : this.units) {
                        unit.fba = null;
                        unit.save();
                    }
                    this.closeAt = new Date();
                }
                this.save();
            }
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
        /* TODO FBA 的入库进度计算需要调整
        int recevied = 0;
        int total = 1;
        for(ShipItem itm : this.shipItems) {
            recevied += itm.recivedQty;
            total += itm.qty;
        }
        return new F.T2<Integer, Integer>(recevied, total > 1 ? total - 1 : total);
        */
        return new F.T2<Integer, Integer>(-1, -1);
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
        return new FBAShipmentQuery().uncloseFBAShipmentIds();
    }

    public static FBAShipment findByShipmentId(String shipmentId) {
        return FBAShipment.find("shipmentId=?", shipmentId).first();
    }

}
