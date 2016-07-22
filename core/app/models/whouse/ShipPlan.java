package models.whouse;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.google.gson.annotations.Expose;
import helper.Reflects;
import helper.Webs;
import models.User;
import models.embedded.ERecordBuilder;
import models.market.Selling;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.product.Product;
import mws.FBA;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.*;

/**
 * 出货计划
 * <p>
 * (角色定义与出库记录的角色基本一致,
 * 主要为出货记录与运输单中间的信息传递,
 * 允许从运输单创建出货计划或者先创建出货计划然后再去关联运输单)
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 2:55 PM
 */
@Entity
public class ShipPlan extends Model {
    /**
     * Selling
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    /**
     * 送往的仓库
     */
    @OneToOne
    public Whouse whouse;

    /**
     * 货物运输的类型 (海运? 空运? 快递)
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Column(length = 20)
    public Shipment.T shipType;

    @ManyToOne
    public FBAShipment fba;

    /**
     * 所关联的运输出去的 ShipItem.
     */
    @OneToMany(mappedBy = "plan")
    public List<ShipItem> shipItems = new ArrayList<>();

    /**
     * 出货对象(SKU or 物料)
     */
    @Required
    @Embedded
    @Expose
    public StockObj stockObj;

    /**
     * 状态
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public S state;

    public enum S {
        Pending {
            @Override
            public String label() {
                return "待出库";
            }
        },
        Confirmd {
            @Override
            public String label() {
                return "已出库";
            }
        };

        public abstract String label();
    }

    /**
     * 计划出库数
     */
    public Integer planQty;

    /**
     * 出货数
     */
    @Required
    @Expose
    public Integer qty;

    /**
     * 预计运输时间
     */
    @Expose
    public Date planShipDate;

    /**
     * 预计到达时间
     */
    @Expose
    public Date planArrivDate;

    @Expose
    public String sku;// 冗余 sku 字段

    @Expose
    @Lob
    public String memo;

    /**
     * 记录一下是由哪个采购计划生成的
     */
    @Expose
    @OneToOne
    public ProcureUnit unit;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    /**
     * 创建人
     */
    @Expose
    @OneToOne
    public User creator;

    public ShipPlan() {
        this.createDate = new Date();
        this.state = S.Pending;
    }

    public ShipPlan(String sid) {
        this();
        if(StringUtils.isNotBlank(sid)) {
            this.selling = Selling.findById(sid);
            this.product = this.selling.listing.product;
        }
        this.creator = User.current();
    }

    public ShipPlan(ProcureUnit unit) {
        this();
        this.planShipDate = unit.attrs.planShipDate;
        this.planArrivDate = unit.attrs.planArrivDate;
        this.planQty = unit.qty();
        this.shipType = unit.shipType;
        this.selling = unit.selling;
        this.sku = unit.sku;
        this.product = unit.product;
        this.whouse = unit.whouse;
        this.unit = unit;
        this.creator = User.current();
    }

    public ShipPlan triggerRecord(String targetId) {
        OutboundRecord outboundRecord = new OutboundRecord(this);
        if(StringUtils.isNotBlank(targetId)) outboundRecord.targetId = targetId;
        if(!outboundRecord.exist()) outboundRecord.save();
        return this;
    }

    public void valid() {
        Validation.required("SKU", this.product);
        Validation.required("Sellinig ID", this.selling);
        Validation.required("仓库", this.whouse);
        Validation.required("预计运输时间", this.planShipDate);
        Validation.required("计划出库数量", this.planQty);
        Validation.required("运输方式", this.shipType);
    }

    public boolean exist() {
        return this.unit != null && ShipPlan.count("unit.id=?", unit.id) != 0;
    }

    public boolean isLock() {
        return this.state != S.Pending;
    }

    public static void postFbaShipments(List<Long> planIds) {
        List<ShipPlan> plans = ShipPlan.find(SqlSelect.whereIn("id", planIds)).fetch();
        if(plans.size() != planIds.size())
            Validation.addError("", "加载的数量");
        if(Validation.hasErrors()) return;

        for(ShipPlan plan : plans) {
            try {
                if(plan.fba != null) {
                    Validation.addError("", String.format("#%s 已经有 FBA 不需要再创建", plan.id));
                } else {
                    plan.postFbaShipment();
                }
            } catch(Exception e) {
                Validation.addError("", Webs.E(e));
            }
        }
    }

    /**
     * 通过 ProcureUnit 创建 FBA
     */
    public synchronized FBAShipment postFbaShipment() {
        FBAShipment fba = null;
        try {
            fba = FBA.plan(this.selling.account, this);
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("", "向 Amazon 创建 Shipment PLAN 因 " + Webs.E(e) + " 原因失败.");
            return null;
        }
        try {
            fba.state = FBA.create(fba);
            this.fba = fba.save();
            this.save();
            new ERecordBuilder("shipment.createFBA")
                    .msgArgs(this.id, this.sku, this.fba.shipmentId)
                    .fid(this.id)
                    .save();
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("", "向 Amazon 创建 Shipment 错误 " + Webs.E(e));
        }
        return fba;
    }

    public int qty() {
        if(this.qty != null) return this.qty;
        return this.planQty;
    }

    /**
     * 采购单元相关联的运输单
     *
     * @return
     */
    public List<Shipment> relateShipment() {
        Set<Shipment> shipments = new HashSet<>();
        for(ShipItem shipItem : this.shipItems) {
            if(shipItem.shipment != null)
                shipments.add(shipItem.shipment);
        }
        return new ArrayList<>(shipments);
    }

    public void update(ShipPlan plan, String shipmentId) {
        if(this.fba != null && (plan.shipType != this.shipType || plan.planQty.intValue() != this.planQty.intValue())) {
            Validation.required("备注", plan.memo);
        }
        if(this.isLock()) {
            Validation.addError("", "已出库, 无法再修改");
        }
        List<String> logs = this.updateLogs(plan);
        this.changeShipItemShipment(StringUtils.isNotBlank(shipmentId) ? Shipment.<Shipment>findById(shipmentId) : null);
        if(Validation.hasErrors()) return;
        if(!logs.isEmpty()) {
            new ERecordBuilder("shipplan.update").msgArgs(this.id, StringUtils.join(logs, "<br>")).fid(this.id).save();
        }
        this.save();
    }

    /**
     * 调整采购计划所产生的运输项目的运输单
     *
     * @param shipment
     */
    public void changeShipItemShipment(Shipment shipment) {
        if(shipment != null && shipment.state != Shipment.S.PLAN) {
            Validation.addError("", "涉及的运输单已经为" + shipment.state.label() + "状态, 只有"
                    + Shipment.S.PLAN.label() + "状态的运输单才可调整.");
            return;
        }
        if(this.shipItems.size() == 0) {
            // 出库计划没有运输项目, 调整运输单的时候, 需要创建运输项目
            if(shipment == null) return;
            shipment.addToShip(this);
        } else {
            for(ShipItem shipItem : this.shipItems) {
                if(this.shipType == Shipment.T.EXPRESS) {
                    if(shipItem.shipment.state == Shipment.S.PLAN) {
                        // 快递运输单调整, 运输项目全部删除, 重新设计.
                        shipItem.delete();
                    }
                } else {
                    if(shipment == null) return;
                    Shipment originShipment = shipItem.shipment;
                    shipItem.adjustShipment(shipment);
                    if(Validation.hasErrors()) {
                        shipItem.shipment = originShipment;
                        shipItem.save();
                        return;
                    }
                }
            }
        }
    }

    public List<String> updateLogs(ShipPlan plan) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "product.sku", plan.product.sku));
        logs.addAll(Reflects.logFieldFade(this, "selling.sellingId", plan.selling.sellingId));
        logs.addAll(Reflects.logFieldFade(this, "product.abbreviation", plan.product.abbreviation));
        logs.addAll(Reflects.logFieldFade(this, "whouse.id", plan.whouse.id));
        logs.addAll(Reflects.logFieldFade(this, "planShipDate", plan.planShipDate));
        logs.addAll(Reflects.logFieldFade(this, "planArrivDate", plan.planArrivDate));
        logs.addAll(Reflects.logFieldFade(this, "planQty", plan.planQty));
        logs.addAll(Reflects.logFieldFade(this, "shipType", plan.shipType));
        logs.addAll(Reflects.logFieldFade(this, "memo", plan.memo));
        return logs;
    }

}
