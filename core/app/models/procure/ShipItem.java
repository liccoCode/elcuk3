package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.google.gson.annotations.Expose;
import helper.FBA;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 每一个运输单的运输项
 * User: wyattpan
 * Date: 6/25/12
 * Time: 12:24 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class ShipItem extends GenericModel {
    public ShipItem() {
    }

    /**
     * 给提交 FBA 的时候使用的
     *
     * @param msku
     * @param qty
     */
    public ShipItem(String msku, Integer qty) {
        this.unit = new ProcureUnit();
        this.unit.selling = new Selling();
        this.unit.selling.merchantSKU = msku;
        this.qty = qty;
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

    /**
     * 如果一个 ShipItem 拥有了 FBA, 那么这个 FBA 所属的 Shipment 应该与 ShipItem 所属的 Shipment 必须一样
     */
    @ManyToOne
    public FBAShipment fba;

    @ManyToOne(cascade = CascadeType.PERSIST)
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
     * Amazon 的 FBA ShipmentItem 具体接收的数量
     */
    @Expose
    public Integer recivedQty = 0;

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

    /**
     * 在通过 FBA 更新了 FNsku 以后, 自动尝试更新 Unit 关联的 Selling 的 Fnsku
     */
    public void updateSellingFNSku() {
        if(StringUtils.isNotBlank(this.fulfillmentNetworkSKU)) {
            if(!this.fulfillmentNetworkSKU.equals(this.unit.selling.fnSku)) {
                this.unit.selling.fnSku = this.fulfillmentNetworkSKU;
                this.unit.selling.save();
            }
        }
    }

    /**
     * ShipItem 被取消;
     * 删除这一条 ShipItem 记录;
     * 同时删除此 ShipItem 对应的 FBA 中的记录
     *
     * @return 删除后的临时对象
     */
    public F.T2<ShipItem, ProcureUnit> cancel() {
        this.shipment = null;
        ProcureUnit unit = this.unit;
        this.unit = null;
        F.T2<ShipItem, ProcureUnit> t2 = new F.T2<ShipItem, ProcureUnit>(this.<ShipItem>delete(), unit);
        if(t2._1.fba != null) {
            try {
                // 删除这个对象
                //TODO 等待测试
                FBA.update(t2._1.fba, Collections.singletonList(new ShipItem(t2._1.unit.selling.merchantSKU, 0)), t2._1.fba.state);
            } catch(Exception e) {
                throw new FastRuntimeException(e);
            }
        }
        return t2;
    }

    /**
     * 总重量 (kg)
     *
     * @return
     */
    public float totalWeight() {
        return this.qty * (this.unit.product.weight == null ? 0 : this.unit.product.weight);
    }

    /**
     * 总体积
     *
     * @return ._1: 单个体积(m3), ._2:总体积(m3), ._3:总体积(cm3), ._4:体积换算重量(cm3/5000)
     */
    public F.T4<Float, Float, Float, Float> totalVolume() {
        // 单位是 mm
        float l = this.unit.product.lengths;
        float w = this.unit.product.width;
        float h = this.unit.product.heigh;

        float singleVolume = (l * w * h) / 1000000000;
        // 换算成 m3
        return new F.T4<Float, Float, Float, Float>(singleVolume, this.qty * singleVolume,
                this.qty * singleVolume * 1000000, (this.qty * singleVolume * 1000000) / 5000);
    }

    /**
     * 总申报价格, 单位 USD
     *
     * @return
     */
    public float totalDeclaredValue() {
        return this.qty * this.unit.product.declaredValue;
    }

    public static List<ShipItem> sameFBAShipItems(String shipmentId) {
        return ShipItem.find("fba.shipmentId=?", shipmentId).fetch();
    }
}
