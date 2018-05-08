package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment.FBAInboundServiceMWSClient;
import com.amazonservices.mws.FulfillmentInboundShipment.model.InboundShipmentItem;
import com.amazonservices.mws.FulfillmentInboundShipment.model.ListInboundShipmentItemsRequest;
import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.DBUtils;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.market.Account;
import models.market.Selling;
import models.product.Template;
import models.view.dto.AnalyzeDTO;
import models.whouse.Outbound;
import mws.MWSFulfilment;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.libs.F;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每一个运输单的运输项
 * User: wyattpan
 * Date: 6/25/12
 * Time: 12:24 PM
 */
@Entity
@DynamicUpdate
public class ShipItem extends GenericModel {
    private static final long serialVersionUID = -8123332841271888513L;

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
        this.qty = unit.realQty();
        this.fulfillmentNetworkSKU = unit.selling != null ? unit.selling.fnSku : "";
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
    public List<PaymentUnit> fees = new ArrayList<>();

    /**
     * 此次运输的数量; 注意其他与产品有关的信息都从关联的 ProcureUnit 中获取
     */
    @Expose
    public Integer qty = 0;

    /**
     * 调整修改运输的数量
     */
    @Expose
    public Integer adjustQty = 0;

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

    public int lossqty;
    public Currency currency;
    public Float compenamt;
    public Float compenusdamt;
    public String compentype;

    public String memo;

    /**
     * 占重比
     * 开始运输之后 赋值
     */
    public Float weightRatio;

    /**
     * 体积占比
     */
    public Float volumeRatio;

    /**
     * 最终占比
     * 体积重比
     */
    public Float finalRatio;

    /**
     * 采购成本 用于运输丢失率统计报表
     */
    @Transient
    public BigDecimal purchaseCost;

    /**
     * 运输成本 用于运输丢失率统计报表
     */
    @Transient
    public BigDecimal shipmentCost;

    /**
     * 损失成本 用于运输丢失率统计报表
     */
    @Transient
    public BigDecimal lossCost;

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
        ProcureUnit procureUnit = this.unit;
        this.unit = null;
        return new F.T2<>(this.delete(), procureUnit);
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
     * 如果有包装则取包装体积
     * 没有则取产品体积*数量
     *
     * @return
     */
    public double totalVolume() {
        if(this.unit.mainBox == null) {
            Float productVolume = (this.unit.product.lengths == null ? 0 : this.unit.product.lengths)
                    * (this.unit.product.width == null ? 0 : this.unit.product.width)
                    * (this.unit.product.heigh == null ? 0 : this.unit.product.heigh);
            return productVolume * this.qty;
        }
        double volume = this.unit.mainBox.length * this.unit.mainBox.width * this.unit.mainBox.height;
        return volume * this.unit.mainBox.boxNum;
    }


    /**
     * 根据运输项目关联的采购计划, 从缓存的 AnalyzeDTO 中获取 TurnOver
     *
     * @return
     */
    public F.T4<Float, Float, Float, Float> getTurnOverT4() {
        List<AnalyzeDTO> dtos = AnalyzeDTO.cachedAnalyzeDTOs("sid");
        if(dtos == null || dtos.size() == 0)
            return new F.T4<>(0f, 0f, 0f, 0f);
        for(AnalyzeDTO dto : dtos) {
            if(!dto.fid.equals(this.unit.sid)) continue;
            return dto.getTurnOverT4();
        }
        return new F.T4<>(0f, 0f, 0f, 0f);
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
        items.forEach(itm -> {
            itm.shipment = shipment;
            if(shipment.out != null && shipment.out.status == Outbound.S.Create) {
                itm.unit.outbound = shipment.out;
                itm.unit.save();
            } else if(shipment.out == null) {
                itm.unit.outbound = null;
                itm.unit.save();
            }
            itm.save();
        });
    }

    public void adjustShipment(Shipment shipment) {
        if(shipment.state != Shipment.S.PLAN)
            Validation.addError("", "只有在 %s " + Shipment.S.PLAN.label() + "状态的运输单可以调整");
        if(this.shipment != null && this.shipment.equals(shipment))
            Validation.addError("", "运输项目 %s 需要调整的运输单没有改变.");
        if(this.shipment != null && this.shipment.state != Shipment.S.PLAN
                && this.unit.revokeStatus != ProcureUnit.REVOKE.CONFIRM)
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
    public void receviedQty(int adjustQty, String msg, String compentype, Integer lossqty, Currency currency,
                            Float compenamt) {
        if(lossqty == null) lossqty = 0;
        if(compenamt == null) compenamt = 0f;
        if(StringUtils.isNotBlank(compentype)
                && !compentype.toLowerCase().equals(models.OperatorConfig.getVal("addressname").toLowerCase())) {
            if((lossqty != 0 && compenamt.intValue() == 0) || (lossqty == 0 && compenamt.intValue() != 0))
                Validation.addError("", "丢失数量和赔偿金额需同时填写,请检查.");
        }
        if(Validation.hasErrors()) return;
        int oldQty = this.adjustQty;
        this.adjustQty = adjustQty;
        this.lossqty = lossqty;
        this.currency = currency;
        this.compenamt = compenamt;
        this.compenusdamt = currency.toUSD(compenamt);
        this.compentype = compentype;
        this.memo = msg;
        this.save();
        if(Objects.equals(this.adjustQty, this.qty)) {
            this.unit.stage = ProcureUnit.STAGE.CLOSE;
            this.unit.save();
        }
        new ERecordBuilder("shipitem.receviedQty").msgArgs(msg, oldQty, adjustQty).fid(this.id).save();
    }

    public List<ElcukRecord> recivedLogs() {
        return ElcukRecord.records(this.id + "", Messages.get("shipitem.receviedQty"));
    }

    /**
     * 检查 recivedQty 值是否是由 0 修改为其他值的(标识 FBA 条码错误等原因造成的 FBA 接受数量为零的这种情况)
     *
     * @return
     */
    public boolean changeFromZero() {
        List<ElcukRecord> records = ElcukRecord.records(this.id + "", Messages.get("shipitem.receviedQty"));
        for(ElcukRecord r : records) {
            if(StringUtils.containsIgnoreCase(r.message, "从 0 修改为")) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;
        ShipItem shipItem = (ShipItem) o;
        return id.equals(shipItem.id);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    /**
     * 对运输项目进行请款
     *
     * @param fee
     */
    public void produceFee(PaymentUnit fee, FeeType feeType) {
        /*
         * 1. 检查是否拥有运输运费
         * 2. 选择运输运费类型
         * 3. 记录数量, 请款人...
         */
        if(feeType == null)
            Validation.addError("", "运输运费类型不存在, 请添加");
        if(fee.currency == null) Validation.addError("", "币种必须存在");
        if(fee.unitQty < 1) Validation.addError("", "数量必须大于等于 1");
        if(fee.cooperator == null) Validation.addError("", "请指定请款费用的费用关系人");
        if(Validation.hasErrors()) return;
        fee.shipItem = this;
        fee.shipment = this.shipment;
        fee.feeType = feeType;
        fee.payee = User.current();
        fee.amount = fee.unitPrice * fee.unitQty;
        this.shipment.fees.add(fee);
        this.shipment.save();
        new ERecordBuilder("paymentunit.applynew").msgArgs(fee.currency, fee.amount(), fee.feeType.nickName)
                .fid(fee.shipment.id).save();
    }

    /**
     * 计算运输单件数
     *
     * @return
     */
    public Integer caluTotalUnitByCheckTask() {
        return this.unit.totalBoxNum();
    }

    public Double caluTotalVolumeByCheckTask() {
        return this.unit.totalBoxVolume();
    }

    public Double caluTotalWeightByCheckTask() {
        return this.unit.totalBoxWeight();
    }

    public String showDeliverymentId() {
        ShipItem shipItem = ShipItem.findById(this.id);
        return shipItem.unit.deliveryment.id;
    }

    public String showDeclare() {
        List<Template> templates = this.unit.product.category.templates;
        List<String> ids = new ArrayList<>();
        if(templates == null || templates.size() == 0) {
            return "";
        } else {
            ids.addAll(templates.stream()
                    .map(template -> template.id.toString())
                    .collect(Collectors.toList()));
        }
        String message = "";
        StringBuilder sql = new StringBuilder("SELECT DISTINCT a.name AS declareName, p.value FROM ProductAttr p ");
        sql.append(" LEFT JOIN Attribute a ON a.id = p.attribute_id  ");
        sql.append(" LEFT JOIN Template_Attribute t ON p.attribute_id = t.attributes_id ");
        sql.append(" WHERE p.product_sku = '" + this.unit.product.sku + "'");
        sql.append(" AND t.templates_id IN " + JpqlSelect.inlineParam(ids));
        sql.append(" AND t.isDeclare = true ");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        if(rows != null && rows.size() > 1) {
            for(Map<String, Object> map : rows) {
                message += map.get("declareName").toString();
                message += ":" + map.get("value") + " ";
            }
        }
        return message;
    }

    public double totalRealWeight() {
        double totalWeight;
        double volume = this.unit.mainBox.length * this.unit.mainBox.width * this.unit.mainBox.height;
        if((volume / 5000) > this.unit.mainBox.singleBoxWeight) {
            totalWeight = volume * this.unit.mainBox.boxNum / 5000;
        } else {
            totalWeight = this.unit.mainBox.singleBoxWeight * this.unit.mainBox.boxNum;
        }
        return totalWeight;
    }

    public void endShipByHand() {
        Date date = new Date();
        if(date.getTime() < this.shipment.dates.inbondDate.getTime())
            Validation.addError("", "结束时间不可能早于入库事件");
        if(Validation.hasErrors()) return;
        this.unitStage(ProcureUnit.STAGE.CLOSE);
        new ElcukRecord("shipitem.endShipByHand", "手动完成", this.id.toString()).save();
    }

    public void syncReceiveQty(Long unitId) {
        ProcureUnit procureUnit = ProcureUnit.findById(unitId);
        Account account = procureUnit.fba.account;
        FBAInboundServiceMWSClient client = MWSFulfilment.client(account, procureUnit.fba.market());
        ListInboundShipmentItemsRequest request = new ListInboundShipmentItemsRequest(account.merchantId);
        request.setMWSAuthToken(account.token);
        request.setShipmentId(procureUnit.fba.shipmentId);
        List<InboundShipmentItem> items = client.listInboundShipmentItems(request).getListInboundShipmentItemsResult()
                .getItemData().getMember();
        this.recivedQty = items.stream().mapToInt(InboundShipmentItem::getQuantityReceived).sum();
        this.save();
    }

    public void crawlWeight(ShipmentMonthly monthly) {
        PaymentUnit paymentUnit = PaymentUnit.find("shipItem.id = ? AND remove = ? ", this.id, false).first();
        if(paymentUnit != null) {
            if(Objects.equals(paymentUnit.chargingWay, PaymentUnit.W.VOLUME)) {
                monthly.volumeWeight = Double.parseDouble(String.valueOf(paymentUnit.unitQty));
            } else {
                monthly.realWeight = Double.parseDouble(String.valueOf(paymentUnit.unitQty));
            }
            monthly.totalShippingFee = String.valueOf(paymentUnit.amount);
        }
    }

    public double packWeight() {
        FBAShipment fba = this.unit.fba;
        if(fba != null) {
            return fba.dto.singleBoxWeight / fba.dto.num * this.qty;
        } else {
            return 0d;
        }
    }

}
