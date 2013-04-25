package models.procure;

import com.google.gson.annotations.Expose;
import models.finance.PaymentUnit;
import models.market.Selling;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.util.ArrayList;
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

    /**
     * 分拆 ProcureUnit 时候拥有周期型运输单的使用使用
     *
     * @param unit
     * @param shipment
     */
    public ShipItem(ProcureUnit unit, Shipment shipment) {
        this.unit = unit;
        this.shipment = shipment;
        this.qty = unit.qty();
        this.fulfillmentNetworkSKU = unit.selling.fnSku;
        this.state = S.NORMAL;
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

    @Expose
    @ManyToOne
    public ProcureUnit unit;

    @OneToMany(mappedBy = "shipItem", orphanRemoval = true, fetch = FetchType.LAZY)
    public List<PaymentUnit> fees = new ArrayList<PaymentUnit>();

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
     * 通过 ShipItem 通知其关联的 ProcureUnit 的阶段进行改变
     *
     * @param stage
     */
    public void unitStage(ProcureUnit.STAGE stage) {
        this.unit.stage = stage;
        this.unit.save();
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
        return new F.T2<ShipItem, ProcureUnit>(this.<ShipItem>delete(), unit);
    }

    /**
     * 修改这个运输项目的运输单, 与之对应的 FBA 也会随之改变
     *
     * @param shipment
     */
    public void changeShipment(Shipment shipment) {
        if(this.fba != null) {
            this.fba.shipment = shipment;
            this.fba.save();
        }
        this.shipment = shipment;
        this.save();
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
    public F.T3<Float, Float, Float> totalVolume() {
        // 单位是 mm
        float l = this.unit.product.lengths;
        float w = this.unit.product.width;
        float h = this.unit.product.heigh;

        float singleVolume = (l * w * h) / 1000000000;
        // 换算成 m3
        return new F.T3<Float, Float, Float>(singleVolume, this.qty * singleVolume,
                (this.qty * singleVolume * 1000000) / 5000);
    }

    /**
     * 根据运输项目关联的采购计划, 从缓存的 AnalyzeDTO 中获取 TurnOver
     *
     * @return
     */
    public F.T4<Float, Float, Float, Float> getTurnOverT4() {
        List<AnalyzeDTO> dtos = AnalyzeDTO.cachedAnalyzeDTOs("sid");
        if(dtos == null || dtos.size() == 0)
            return new F.T4<Float, Float, Float, Float>(0f, 0f, 0f, 0f);
        for(AnalyzeDTO dto : dtos) {
            if(!dto.fid.equals(this.unit.sid)) continue;
            return dto.getTurnOverT4();
        }
        return new F.T4<Float, Float, Float, Float>(0f, 0f, 0f, 0f);
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
