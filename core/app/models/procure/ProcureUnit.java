package models.procure;

import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.JPAs;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.embedded.UnitAttrs;
import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import models.view.dto.TimelineEventSource;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.i18n.Messages;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 每一个采购单元
 * User: wyattpan
 * Date: 6/11/12
 * Time: 5:23 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class ProcureUnit extends Model {

    public ProcureUnit() {
    }

    /**
     * Copy 一个全新的 ProcureUnit, 用来将部分交货的 ProcureUnit 分单交货
     *
     * @param unit
     */
    public ProcureUnit(ProcureUnit unit) {
        this.cooperator = unit.cooperator;
        this.selling = unit.selling;
        this.sid = unit.sid;
        this.product = unit.product;
        this.sku = unit.sku;
        this.handler = unit.handler;
        this.whouse = unit.whouse;
        this.stage = STAGE.PLAN;
        UnitAttrs attr = new UnitAttrs();
        attr.planQty = unit.attrs.planQty - unit.attrs.qty;
        attr.price = unit.attrs.price;
        attr.currency = unit.attrs.currency;
        this.attrs = attr;
        this.comment(String.format("此采购计划由于 #%s 采购计划部分交货而自行分单创建.", unit.id));
    }

    /**
     * 阶段
     */
    public enum STAGE {
        /**
         * 计划阶段
         */
        PLAN {
            @Override
            public String toString() {
                return "计划中";
            }
        },
        /**
         * 采购阶段
         */
        DELIVERY {
            @Override
            public String toString() {
                return "采购中";
            }
        },
        /**
         * 完成了, 全部交货了
         */
        DONE {
            @Override
            public String toString() {
                return "已交货";
            }
        },
        /**
         * 正在运输中
         */
        SHIPPING {
            @Override
            public String toString() {
                return "全部运输";
            }
        },
        /**
         * 部分正在运输
         */
        PART_SHIPPING {
            @Override
            public String toString() {
                return "部分运输";
            }
        },
        /**
         * 运输完成
         */
        SHIP_OVER {
            @Override
            public String toString() {
                return "运输完成";
            }
        },
        /**
         * 关闭阶段, 不处理了
         */
        CLOSE {
            @Override
            public String toString() {
                return "关闭";
            }
        }
    }

    /**
     * 此采购计划的供应商信息.
     */
    @OneToOne
    public Cooperator cooperator;

    /**
     * 采购单
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public Deliveryment deliveryment;

    /**
     * 所关联的运输出去的 ShipItem.
     */
    @OneToMany(mappedBy = "unit")
    public List<ShipItem> relateItems;


    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;


    @Expose
    @CheckWith(MskuCheck.class)
    public String sid; // 一个 SellingId 字段

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    @Expose
    @CheckWith(SkuCheck.class)
    public String sku;// 冗余 sku 字段
    /**
     * 操作人员
     */
    @OneToOne
    public User handler;

    /**
     * 送往的仓库
     */
    @OneToOne
    public Whouse whouse;

    // ----------- 将不同阶段的数据封装到不同的对象当中去.

    /**
     * ProcureUnit 所涉及的参数
     */
    @Expose
    @Required
    public UnitAttrs attrs = new UnitAttrs();


    /**
     * 此 Unit 的状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public STAGE stage = STAGE.PLAN;

    public Date createDate = new Date();

    /**
     * 货物是否已经到达货代的地方
     */
    public boolean isPlaced = false;

    @Lob
    @Expose
    public String comment = " ";

    /**
     * ProcureUnit 的检查
     */
    public void validate() {
        Validation.current().valid(this);
        Validation.current().valid(this.attrs);
        Validation.required("procureunit.selling", this.selling);
        if(this.selling != null) this.sid = this.selling.sellingId;
        Validation.required("procureunit.whouse", this.whouse);
        Validation.required("procureunit.handler", this.handler);
        Validation.required("procureunit.product", this.product);
        if(this.product != null) this.sku = this.product.sku;
        Validation.required("procureunit.createDate", this.createDate);
        if(this.attrs != null) this.attrs.validate();
    }

    /**
     * ProcureUnit 交货
     *
     * @param attrs
     * @return T2: ._1:是否全部转移, ._2:新转移的采购单元
     */
    public F.T2<Boolean, ProcureUnit> delivery(UnitAttrs attrs) {
        /**
         * 1.
         */
        if(this.stage == STAGE.CLOSE || this.stage == STAGE.SHIP_OVER)
            Validation.addError("procureunit.delivery.stage", "%s");
        Validation.min("procureunit.attrs.qty", attrs.qty, 0);
        Validation.required("procureunit.attrs.qty", attrs.qty);
        Validation.required("procureunit.attrs.deliveryDate", attrs.deliveryDate);
        if(Validation.hasErrors()) return new F.T2<Boolean, ProcureUnit>(false, null);
        this.attrs = attrs;

        // 交货状态处理(1:大于;0:等于;-1:小于)
        int state = this.deliveryState();
        ProcureUnit unit = null;
        // 交货不足的情况, 再创建一个 ProcureUnit
        if(state == -1) unit = this.isLeekDelivery();
        else unit = this;
        new ElcukRecord(Messages.get("procureunit.delivery"),
                Messages.get("procureunit.delivery.msg", this.attrs.qty, this.attrs.planQty)
                , this.deliveryment.id).save();
        this.stage = STAGE.DONE;
        this.save();
        return new F.T2<Boolean, ProcureUnit>(state == -1, unit);
    }

    /**
     * 交货的数量少于预期情况
     */
    private ProcureUnit isLeekDelivery() {
        return new ProcureUnit(this).save();
    }

    /**
     * 交货的状态, 1: 交货多余计划;  0: 交货等于计划; -1:交货小于计划
     *
     * @return
     */
    private int deliveryState() {
        return attrs.qty - attrs.planQty;
    }

    public ShipItem ship(Shipment shipment, int size) {
        ShipItem shipItem = null;
        for(ShipItem itm : shipment.items) {
            if(itm.unit.equals(this)) {
                shipItem = itm;
                shipItem.qty += size;
            }
        }
        if(shipItem == null) {
            shipItem = new ShipItem();
            shipItem.shipment = shipment;
            shipItem.unit = this;
            shipItem.qty = size;
            this.relateItems.add(shipItem);
        }
        return shipItem;
    }

    public STAGE nextStage() {
        if(this.stage == STAGE.CLOSE || this.stage == STAGE.SHIP_OVER) {
            return this.stage;
        } else if(this.stage == STAGE.SHIPPING || this.stage == STAGE.PART_SHIPPING) {
            for(ShipItem item : this.relateItems) {
                if(item.shipment.state != Shipment.S.DONE) return this.stage;
            }
            return STAGE.SHIP_OVER;
        } else {
            F.T3<Integer, Integer, List<String>> leftQty = this.leftQty();
            if(leftQty._1 == 0) {
                return STAGE.SHIPPING;
            } else if(leftQty._1 > 0 && leftQty._1 < this.qty()) {
                return STAGE.PART_SHIPPING;
            }
        }
        return this.stage;
    }

    /**
     * 剩下的可运输的数量
     *
     * @return T3: ._1: 剩余的数量, ._2: 总运输的数量, ._3:影响的 ShipmentId
     */
    public F.T3<Integer, Integer, List<String>> leftQty() {
        int totalShiped = 0;
        List<String> shipments = new ArrayList<String>();
        for(ShipItem item : this.relateItems) {
            totalShiped += item.qty;
            shipments.add(item.shipment.id);
        }
        return new F.T3<Integer, Integer, List<String>>(this.qty() - totalShiped, totalShiped, shipments);
    }

    public String nickName() {
        return String.format("ProcureUnit[%s][%s][%s]", this.id, this.sid, this.sku);
    }

    /**
     * 设置 ProcureUnit 的 Deliveryment 的时候需要将 STAGE 也变化
     *
     * @param deliveryment
     */
    public void toggleAssignTodeliveryment(Deliveryment deliveryment, boolean assign) {
        if(assign) {
            this.deliveryment = deliveryment;
            this.stage = ProcureUnit.STAGE.DELIVERY;
        } else {
            this.deliveryment = null;
            this.stage = STAGE.PLAN;
        }
    }

    public void comment(String cmt) {
        this.comment = String.format("%s\r\n%s", cmt, this.comment).trim();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        ProcureUnit that = (ProcureUnit) o;

        if(id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    /**
     * 采购单元相关联的运输单
     *
     * @return
     */
    public List<Shipment> relateShipment() {
        return Shipment.find("SELECT DISTINCT s FROM Shipment s, IN(s.items) itm WHERE itm.unit.id=?", this.id).fetch();
    }

    public int qty() {
        if(this.attrs.qty != null) return this.attrs.qty;
        return this.attrs.planQty;
    }

    /**
     * 转换成记录日志的格式
     *
     * @return
     */
    public String to_log() {
        return String.format("[sid:%s] [仓库:%s] [供应商:%s] [计划数量:%s] [预计到库:%s]", this.sid, this.whouse.name(), this.cooperator.fullName, this.attrs.planQty, Dates.date2Date(this.attrs.planArrivDate));
    }

    /**
     * 根据 sku 或者 msku 加载 PLAN, DELIVERY Stage 的 ProcureUnit.
     *
     * @param selling
     * @param isSku
     * @return
     */
    public static List<ProcureUnit> skuOrMskuRelate(Selling selling, boolean isSku) {
        if(isSku)
            return find("sku=? AND stage IN (?,?,?)", Product.merchantSKUtoSKU(selling.merchantSKU), STAGE.PLAN, STAGE.DELIVERY, STAGE.DONE).fetch();
        else
            return find("selling=? AND stage IN (?,?,?)", selling, STAGE.PLAN, STAGE.DELIVERY, STAGE.DONE).fetch();
    }

    public static List<ProcureUnit> unitsFilterByStage(STAGE stage) {
        return ProcureUnit.find("stage=?", stage).fetch();
    }

    /**
     * 加载并且返回 Simile Timeline 的 Events
     * type 只允许为 sku, sid 两种类型; 如果 type 为空,默认为 sid
     */
    public static TimelineEventSource timelineEvents(String type, String val) {
        if(StringUtils.isBlank(type)) type = "sid";
        if("msku".equals(type)) type = "sid"; // 兼容
        if(!"sku".equals(type) && !"sid".equals(type))
            throw new FastRuntimeException("查看的数据类型(" + type + ")错误! 只允许 sku 与 sid.");

        DateTime dt = DateTime.now();
        List<ProcureUnit> units = ProcureUnit.find("stage not in (?,?) AND planArrivDate>=? AND planArrivDate<=? AND " + type/*sid/sku*/ + "=?",
                STAGE.SHIP_OVER, STAGE.CLOSE, dt.minusMonths(9).toDate(), dt.plusMonths(3).toDate(), val).fetch();


        // 将所有与此 SKU/SELLING 关联的 ProcureUnit 展示出来.(前 9 个月~后3个月)
        TimelineEventSource eventSource = new TimelineEventSource();
        Selling selling = Selling.findSellingOrSKUFromAnalyzesCachedSellingsOrSKUs(type, val);
        for(ProcureUnit unit : units) {
            TimelineEventSource.Event event = new TimelineEventSource.Event(selling, unit);
            event.startAndEndDate(type)
                    .titleAndDesc(unit.stage);
            if(unit.stage == STAGE.DONE) event.color("9C9C9C"); // 默认颜色
            else event.color();

            eventSource.events.add(event);
        }


        // 将当前 Selling 的销售情况展现出来
        eventSource.events.add(TimelineEventSource.currentQtyEvent(selling, type));

        return eventSource;
    }

    public static List<ProcureUnit> waitToShip(long whouseid) {
        return ProcureUnit.find("deliveryment.state IN (?,?) AND stage NOT IN (?,?) AND whouse.id=? ORDER BY attrs.planArrivDate",
                Deliveryment.S.DONE, Deliveryment.S.CONFIRM, STAGE.SHIPPING, STAGE.SHIP_OVER, whouseid).fetch();
    }


    static class MskuCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            ProcureUnit unit = (ProcureUnit) validatedObject;
            if(unit.selling == null) return false;
            String[] args = StringUtils.split(unit.selling.sellingId, Webs.S);
            if(args.length < 3) {
                setMessage("validation.msku", unit.selling.sellingId);
                return false;
            } else return true;
        }
    }

    static class SkuCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            ProcureUnit unit = (ProcureUnit) validatedObject;
            if(unit.product == null) return false;
            String[] args = StringUtils.split(unit.selling.sellingId, Webs.S);
            if(!StringUtils.contains(args[0], unit.product.sku)) {
                setMessage("validation.sku", unit.product.sku);
                return false;
            } else return true;
        }
    }
}
