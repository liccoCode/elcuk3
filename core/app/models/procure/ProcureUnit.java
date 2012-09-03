package models.procure;

import com.google.gson.annotations.Expose;
import helper.JPAs;
import helper.Webs;
import models.User;
import models.embedded.UnitAttrs;
import models.market.Selling;
import models.product.Product;
import models.product.Whouse;
import models.view.TimelineEventSource;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.db.helper.JpqlSelect;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
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
     * 阶段
     */
    public enum STAGE {
        /**
         * 计划阶段
         */
        PLAN,
        /**
         * 采购阶段
         */
        DELIVERY,
        /**
         * 完成了, 全部交货了
         */
        DONE,
        /**
         * 运输完成
         */
        SHIP_OVER,
        /**
         * 关闭阶段, 不处理了
         */
        CLOSE
    }

    /**
     * 此采购计划的供应商信息.
     */
    @OneToOne
    public Cooperator cooperator;

    /**
     * 采购单
     */
    @ManyToOne(fetch = FetchType.LAZY)
    public Deliveryment deliveryment;

    /**
     * 所关联的运输出去的 ShipItem.
     */
    @OneToMany(mappedBy = "unit")
    public List<ShipItem> relateItems;


    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;


    @Expose
    public String sid; // 一个 SellingId 字段

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    @Expose
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

    @Lob
    @Expose
    public String comment = " ";

    /**
     * 检查参数并且创建新 ProcureUnit
     */
    public ProcureUnit checkAndCreate() {
        // 0. 检查是否已经存在
        if(this.id != null)
            if(ProcureUnit.findById(this.id) != null) throw new FastRuntimeException("ProcureUnit 已经存在了, 不允许重复创建.");
        this.check();
        return this.save();
    }

    public void checkAndUpdate() {
        this.check();
        this.save();
    }

    private void check() {
        // 检查预计日期不允许比当前日期小
        if(this.attrs.planArrivDate.getTime() - System.currentTimeMillis() < 0)
            throw new FastRuntimeException("预期日期已经过期!");
        // selling 不能为空
        if(this.selling == null) throw new FastRuntimeException(String.format("Selling %s 不存在", this.sid));
        else this.sid = this.selling.sellingId;
        //  目标仓库必须拥有
        if(this.whouse == null) throw new FastRuntimeException("目的仓库不能为空");
        // 采购人必须记录
        if(this.handler == null) throw new FastRuntimeException("必须拥有一个处理人.");
        if(this.product == null) throw new FastRuntimeException("没有关联 Product.");
        else this.sku = this.product.sku;
        // msku 与 sku 需要符合要求
        String[] args = StringUtils.split(this.sid, Webs.S);
        if(args.length < 3) throw new FastRuntimeException("SellingID 不符合 [msku]|[Market]|[AccountId] 的格式");
        if(!StringUtils.contains(args[0], this.sku)) throw new FastRuntimeException("SellingId 与 SKU 不一致, 请联系 IT.");
    }

    /**
     * @4 将创建好的 ProcureUnit 指派给存在的采购单.
     * ProcureUnit 从 Plan stage 升级成为 Delivery Stage
     * <p/>
     * Procure#: #1/4 采购单元分派给采购单
     */
    public ProcureUnit assignToDeliveryment(Deliveryment deliveryment) {
        /**
         * 1. 检查是否合法
         * 2. 进行转换
         */
        // 1
        if(this.attrs.planQty == null || this.attrs.planQty < 0)
            throw new FastRuntimeException("最终确认数量不能为空!");
        if(this.attrs.planDeliveryDate == null) throw new FastRuntimeException("预计交货时间不允许为空!");
        if(this.attrs.planDeliveryDate.getTime() > this.attrs.planArrivDate.getTime())
            throw new FastRuntimeException("预计交货时间不可能晚于预计到库时间!");
        if(deliveryment == null || !deliveryment.isPersistent())
            throw new FastRuntimeException("成为 Delivery Stage 采购单必须先存在.");
        for(ProcureUnit pu : deliveryment.units) {
            if(!pu.cooperator.equals(this.cooperator))
                //TODO supplier 改变, 这里也需要改变
                throw new FastRuntimeException("只允许相同工厂的 ProcureUnit 添加在同一个采购单中!");
        }

        // 2
        this.deliveryment = deliveryment;
        this.stage = STAGE.DELIVERY;
        return this.save();
    }


    public String nickName() {
        return String.format("ProcureUnit[%s][%s][%s]", this.id, this.sid, this.sku);
    }

    @SuppressWarnings("unchecked")
    public static List<String> suppliers() {
        Query query = JPAs.createQuery(new JpqlSelect().select("attrs.supplier").from("ProcureUnit").groupBy("attrs.supplier"));
        return query.getResultList();
    }

    public static List<ProcureUnit> findByStage(STAGE stage) {
        return ProcureUnit.find("stage=? ORDER BY planArrivDate", stage).fetch();
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

}
