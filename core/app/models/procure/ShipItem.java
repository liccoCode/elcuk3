package models.procure;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import models.embedded.ERecordBuilder;
import models.finance.PaymentUnit;
import models.market.Selling;
import models.view.dto.AnalyzeDTO;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
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
        this(unit);
        this.shipment = shipment;
    }

    /**
     * 通过 ProcureUnit 创建 ShipItem
     *
     * @param unit
     */
    public ShipItem(ProcureUnit unit) {
        this.unit = unit;
        this.qty = unit.qty();
        this.fulfillmentNetworkSKU = unit.selling.fnSku;
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

    /**
     * 将运输项目调整到指定的运输单中
     *
     * @param ids
     * @param shipment
     * @return
     */
    public static void adjustShipment(List<Long> ids, Shipment shipment) {
        List<ShipItem> items = ShipItem.find(SqlSelect.whereIn("id", ids)).fetch();

        if(ids.size() != items.size())
            Validation.addError("", "提交的属于与系统中的数据不一致.");

        if(shipment.state != Shipment.S.PLAN)
            Validation.addError("", "只有在 %s " + Shipment.S.PLAN.label() + "状态的运输单可以调整");

        for(ShipItem itm : items) {
            if(itm.shipment.equals(shipment))
                Validation.addError("", "运输项目 %s 需要调整的运输单没有改变.");
            if(itm.shipment.state != Shipment.S.PLAN)
                Validation.addError("", "当前运输单物流已经确认, 如需调整请联系物流");
        }

        if(Validation.hasErrors()) return;

        //TODO 日志
        for(ShipItem itm : items) {
            itm.shipment = shipment;
            itm.save();
        }
    }

    public void adjustShipment(Shipment shipment) {
        if(shipment.state != Shipment.S.PLAN)
            Validation.addError("", "只有在 %s " + Shipment.S.PLAN.label() + "状态的运输单可以调整");
        if(this.shipment.equals(shipment))
            Validation.addError("", "运输项目 %s 需要调整的运输单没有改变.");
        if(this.shipment.state != Shipment.S.PLAN)
            Validation.addError("", "当前运输项目的运输单已经是不可更改");
        if(Validation.hasErrors()) return;
        this.shipment = shipment;
        this.save();
    }

    /**
     * 调整接收数量
     * 入库数量大于 10% 则不允许
     *
     * @param msg
     */
    public void receviedQty(int recivedQty, String msg) {
        float percent = ((float) Math.abs(recivedQty - this.qty) / this.qty);
        if(percent > 0.1)
            Validation.addError("", "入库库存与运输库存差据为 " + (percent * 100) + "百分比 大于 10 百分比 请检查数量.");
        if(Validation.hasErrors()) return;

        int oldQty = this.recivedQty;
        if(oldQty != recivedQty) {
            this.recivedQty = recivedQty;
            this.save();
            new ERecordBuilder("shipitem.receviedQty")
                    .msgArgs(msg, oldQty, recivedQty)
                    .fid(this.id)
                    .save();
        }
    }

    public List<ElcukRecord> recivedLogs() {
        return ElcukRecord.records(this.id + "", Messages.get("shipitem.receviedQty"));
    }
}
