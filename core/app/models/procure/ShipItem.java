package models.procure;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.Date;

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

    /**
     * 实际发货时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date shipDate;
    /**
     * 实际到库时间
     */
    @Expose
    @Temporal(TemporalType.DATE)
    public Date arriveDate;

    /**
     * 这个创建 ShipItem 的时候默认填充 Selling 中的 FNSKU, 在创建好了 FBA 以后, 将 FBA 返回的值同步在这.
     */
    public String fulfillmentNetworkSKU;

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
     *
     * @return 删除后的临时对象
     */
    public F.T2<ShipItem, ProcureUnit> cancel() {
        this.shipment = null;
        ProcureUnit unit = this.unit;
        this.unit = null;
        return new F.T2<ShipItem, ProcureUnit>(this.<ShipItem>delete(), unit);
    }
}
