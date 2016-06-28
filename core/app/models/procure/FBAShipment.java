package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import helper.Webs;
import jobs.AmazonFBAInventoryReceivedJob;
import models.market.Account;
import mws.FBA;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.Logger;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.*;

/**
 * User: wyattpan
 * Date: 9/12/12
 * Time: 4:27 PM
 */
@Entity
@DynamicUpdate
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

    @Column(unique = true, nullable = false, length = 20)
    public String shipmentId;

    @OneToMany(mappedBy = "fba")
    public List<ProcureUnit> units = new ArrayList<ProcureUnit>();

    @Lob
    public String records = "";

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

    public Date createAt;

    /**
     * 关闭/取消 时间
     */
    public Date closeAt;

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
            Thread.sleep(500);
        } catch(Exception e) {
            if(e.getMessage().contains("Shipment is locked. No updates allowed") ||
                    e.getMessage().contains("Shipment is in locked status")) {
                this.state = FBAShipment.S.RECEIVING;
                this.save();
                Logger.warn("FBA update failed.(%s) because of: %s", this.shipmentId, e.getMessage());
            } else if(e.getMessage().contains("FBA31004")) {
                //fbaErrorCode=FBA31004, description=updates to status SHIPPED not allowed
                this.state = FBAShipment.S.IN_TRANSIT;
                this.save();
                Logger.warn("FBA update failed.(%s) because of: %s", this.shipmentId, e.getMessage());
            } else if(StringUtils.containsIgnoreCase(e.getMessage(), "NOT_IN_PRODUCT_CATALOG")) {
                //MSKU 错误
                throw new FastRuntimeException("向 Amazon 更新失败. 请检查 MSKU(SKU+UPC) 是否正确.");
            } else if(StringUtils.containsIgnoreCase(e.getMessage(), "MISSING_DIMENSIONS")) {
                //产品尺寸没有填写
                throw new FastRuntimeException("向 Amazon 更新失败. 请检查 SKU 的长宽高是否正确填写.");
            } else if(StringUtils.containsIgnoreCase(e.getMessage(), "Invalid Status change")) {
                //物流人员没有通过系统进行开始运输而手动在 Amazon 后台操作了 FBA.
                this.state = FBAShipment.S.SHIPPED;
                this.save();
            } else {
                //TODO:: ANDON_PULL_STRIKE_ONE
                if(e.getClass() == FBAInboundServiceMWSException.class) {
                    Webs.systemMail(
                            "UpdateFBAShipment 出现未知异常",
                            Webs.S(e),
                            Arrays.asList("duan@easya.cc", "licco@easya.cc")
                    );
                }
                throw new FastRuntimeException("向 Amazon 更新失败. " + Webs.E(e));
            }
        }
        this.save();
    }

    /**
     * 对更新 FBA 进行重复执行. (因 FBA 更新有 API 速度限制, 这里避免更新失败)
     *
     * @param times
     * @param state
     */
    public synchronized void updateFBAShipmentRetry(int times, S state) {
        try {
            updateFBAShipment(state);
        } catch(Exception e) {
            if(times > 0)
                updateFBAShipmentRetry(--times, state);
            else
                throw new FastRuntimeException(e.getMessage());
        }
    }

    /**
     * 将从 Amazon 解析的 Rows model 数据同步到当前系统中 FBA 的相关数据结构中.
     *
     * @param rows
     */
    public synchronized void syncFromAmazonInventoryRows(AmazonFBAInventoryReceivedJob.Rows rows) {
        if(rows == null) {
            Logger.warn("FBA %s Inventory Rows is empty!", this.shipmentId);
            return;
        }
        /**
         * 0. 记录 records
         * 1. 通过 ProcureUnit 向 Rows 中拿数据进行解析
         * 2. ProcureUnit 的 ShipItem 如果有多个, 优先满足
         */
        this.records = StringUtils.join(rows.records, "\n");
        for(ProcureUnit unit : this.units) {
            int fbaQty = rows.qty(unit.selling.merchantSKU);
            for(ShipItem shipItem : unit.shipItems) {
                if(fbaQty >= shipItem.qty) {
                    shipItem.recivedQty = shipItem.qty;
                    fbaQty -= shipItem.qty;
                } else {
                    shipItem.recivedQty = fbaQty;
                    fbaQty = 0;
                }
                shipItem.save();
            }
        }
        F.Option<Date> earliestDate = rows.getEarliestDate();
        if(earliestDate.isDefined())
            this.state = S.RECEIVING;
        this.save();
    }

    public F.Option<Date> getEarliestDate() {
        List<String> records = Arrays.asList(StringUtils.split(this.records, "\n"));
        return AmazonFBAInventoryReceivedJob.Rows.getEarliestDate(records);
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

    public String address() {
        String line2 = this.fbaCenter.addressLine2;
        if(StringUtils.isBlank(line2)) line2 = "";
        String provincecode = this.fbaCenter.stateOrProvinceCode;
        if(StringUtils.isBlank(provincecode)) provincecode = "";
        return String.format("%s %s %s %s %s %s %s %s",
                this.fbaCenter.name, this.fbaCenter.addressLine1, line2,
                this.fbaCenter.city, provincecode,
                this.fbaCenter.postalCode, this.fbaCenter.centerId, codeToCounrty());
    }

    public String codeToCounrty() {
        return this.fbaCenter.codeToCountry();
    }

    public static FBAShipment findByShipmentId(String shipmentId) {
        return FBAShipment.find("shipmentId=?", shipmentId).first();
    }

    public static List<FBAShipment> findBySHipmentIds(Collection<String> shipmentids) {
        return FBAShipment.find(SqlSelect.whereIn("shipmentId", shipmentids)).fetch();
    }

}
