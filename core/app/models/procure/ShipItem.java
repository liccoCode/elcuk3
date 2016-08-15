package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.DBUtils;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.market.Selling;
import models.product.Template;
import models.qc.CheckTask;
import models.view.dto.AnalyzeDTO;
import models.whouse.ShipPlan;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 每一个运输单的运输项
 * User: wyattpan
 * Date: 6/25/12
 * Time: 12:24 PM
 */
@Entity
@DynamicUpdate
public class ShipItem extends GenericModel {
    public ShipItem() {
    }

    /**
     * 给提交 FBA 的时候使用的
     *
     * @param msku
     * @param qty
     * @deprecated
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
        this.fulfillmentNetworkSKU = unit.selling.fnSku;
    }

    /**
     * 通过 ShipPlan 创建 ShipItem
     *
     * @param plan
     */
    public ShipItem(ShipPlan plan) {
        this.plan = plan;
        this.qty = plan.qty();
        this.fulfillmentNetworkSKU = plan.selling.fnSku;
    }

    @Id
    @GeneratedValue
    @Expose
    public Long id;

    @ManyToOne
    @Expose
    public Shipment shipment;

    /**
     * WARNING: 运输项关联的对象在(version:1.3.0)已经变更成了 ShipPlan,
     * 相关信息展示的时候应该从关联的 ShipPlan 身上获取.
     */
    @Expose
    @ManyToOne
    @Deprecated
    public ProcureUnit unit;

    @Expose
    @ManyToOne
    public ShipPlan plan;

    @OneToMany(mappedBy = "shipItem", orphanRemoval = true, fetch = FetchType.LAZY)
    public List<PaymentUnit> fees = new ArrayList<PaymentUnit>();

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
                this.unit().selling.fnSku = this.fulfillmentNetworkSKU;
                this.unit().selling.save();
            }
        }
    }

    /**
     * 通过 ShipItem 通知其关联的 ProcureUnit 的阶段进行改变
     *
     * @param stage
     */
    public void unitStage(ProcureUnit.STAGE stage) {
        ProcureUnit unit = this.unit();
        if(unit != null) {
            unit.stage = stage;
            unit.save();
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
        ProcureUnit unit = this.unit();
        this.unit = null;
        return new F.T2<ShipItem, ProcureUnit>(this.<ShipItem>delete(), unit);
    }

    /**
     * 总重量 (kg)
     *
     * @return
     */
    public float totalWeight() {
        if(this.unit != null) {
            return this.qty * (this.unit.product.weight == null ? 0 : this.unit.product.weight);
        } else {
            return this.qty * (this.plan.product.weight == null ? 0 : this.plan.product.weight);
        }
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
            if(!dto.fid.equals(this.unit().sid)) continue;
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
        return this.qty * this.unit().product.declaredValue;
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
            itm.unit().flushTask();
        }
    }

    public void adjustShipment(Shipment shipment) {
        if(shipment.state != Shipment.S.PLAN)
            Validation.addError("", "只有在 %s " + Shipment.S.PLAN.label() + "状态的运输单可以调整");
        if(this.shipment != null && this.shipment.equals(shipment))
            Validation.addError("", "运输项目 %s 需要调整的运输单没有改变.");
        if(this.shipment != null && this.shipment.state != Shipment.S.PLAN)
            Validation.addError("", "当前运输项目的运输单已经是不可更改");
        if(Validation.hasErrors()) return;
        this.shipment = shipment;
        this.unit().flushTask();
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
        if(StringUtils.isNotBlank(compentype) &&
                !compentype.toLowerCase().equals(models.OperatorConfig.getVal("addressname")
                        .toLowerCase())) {
            if((lossqty != 0 && compenamt.intValue() == 0) || (lossqty == 0 && compenamt.intValue() != 0))
                Validation.addError("", "丢失数量和赔偿金额需同时填写,请检查.");
        }
        if(StringUtils.isNotBlank(compentype) &&
                compenamt.equals(models.OperatorConfig.getVal("addressname").toLowerCase())) {

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
        new ERecordBuilder("shipitem.receviedQty")
                .msgArgs(msg, oldQty, adjustQty)
                .fid(this.id)
                .save();
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

        if(!id.equals(shipItem.id)) return false;

        return true;
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
        /**
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
        fee.save();

        new ERecordBuilder("paymentunit.applynew")
                .msgArgs(fee.currency, fee.amount(), fee.feeType.nickName)
                .fid(fee.shipment.id)
                .save();
    }

    public List<CheckTask> checkTasks() {
        return CheckTask.find("units=? ORDER BY creatat DESC", this.unit()).fetch();
    }

    public Integer caluTotalUnitByCheckTask() {
        List<CheckTask> tasks = this.checkTasks();
        if(tasks.size() > 0) {
            return tasks.get(0).totalBoxNum();
        } else {
            return 0;
        }
    }

    public Double caluTotalVolumeByCheckTask() {
        List<CheckTask> tasks = this.checkTasks();
        if(tasks.size() > 0) {
            return tasks.get(0).totalVolume();
        } else {
            return 0d;
        }
    }

    public Double caluTotalWeightByCheckTask() {
        List<CheckTask> tasks = this.checkTasks();
        if(tasks.size() > 0) {
            return tasks.get(0).totalWeight();
        } else {
            return 0d;
        }
    }

    public String showDeliverymentId() {
        ShipItem shipItem = ShipItem.findById(this.id);
        return shipItem.unit().deliveryment.id;
    }

    public String showDeclare() {
        List<Template> templates = this.unit().product.category.templates;
        List<String> ids = new ArrayList<>();
        if(templates == null || templates.size() == 0) {
            return "";
        } else {
            for(Template template : templates) {
                ids.add(template.id.toString());
            }
        }
        String message = "";
        StringBuilder sql = new StringBuilder("SELECT DISTINCT a.name AS declareName, p.value FROM ProductAttr p ");
        sql.append(" LEFT JOIN Attribute a ON a.id = p.attribute_id  ");
        sql.append(" LEFT JOIN Template_Attribute t ON p.attribute_id = t.attributes_id ");
        sql.append(" WHERE p.product_sku = '" + this.unit().product.sku + "'");
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

    /**
     * 为了老的运输单做一下过渡
     * (避免出现 VERSION: 1.3.0 上线后, 之前遗留的运输单无法打开)
     *
     * @return
     */
    public ProcureUnit unit() {
        if(this.plan != null) return plan.unit;
        return this.unit;
    }
}
