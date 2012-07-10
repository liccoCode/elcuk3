package models.procure;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.GenericModel;

import javax.persistence.*;

/**
 * 每一个运输单的运输项
 * User: wyattpan
 * Date: 6/25/12
 * Time: 12:24 PM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ShipItem extends GenericModel {
    public ShipItem() {
    }

    public ShipItem(Shipment shipment, ProcureUnit unit) {
        this.shipment = shipment;
        this.unit = unit;
    }

    public enum S {
        /**
         * 正常状态
         */
        NORMAL,
        /**
         * 此 ShipItem 已经被取消(对应的 Shipment 被取消导致)
         */
        CANCEL
    }

    @Id
    @GeneratedValue
    @Expose
    public Long id;

    @ManyToOne
    @Expose
    public Shipment shipment;

    @ManyToOne
    @Expose
    public ProcureUnit unit;

    @Enumerated(EnumType.STRING)
    public S state = S.NORMAL;
    /**
     * 此次运输的数量; 注意其他与产品有关的信息都从关联的 ProcureUnit 中获取
     */
    @Expose
    public Integer qty = 0;

    public ShipItem removeFromShipment() {
        /**
         *  TODO 需要检查什么?
         */
        return this.delete();
    }

    /**
     * ShipItem 被取消;
     * 1. 运输的数量设置为 0
     * 2. 状态设置为 CANCEL
     */
    public void cancel() {
        this.qty = 0;
        this.state = S.CANCEL;
        this.save();
    }
}
