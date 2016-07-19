package models.whouse;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.google.gson.annotations.Expose;
import helper.Webs;
import models.User;
import models.embedded.ERecordBuilder;
import models.market.Selling;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import mws.FBA;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
public class ShipPlan extends GenericModel {
    @Id
    @Column(length = 30)
    @Expose
    public String id;

    /**
     * Selling
     * <p>
     * PS:物料计划上线后不能再这样关联了
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;

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

    /**
     * 冗余运输单字段
     */
    @ManyToOne
    public Shipment shipment;

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
    public Date planDate;

    @Expose
    public String sku;// 冗余 sku 字段

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
        this.creator = User.current();
    }

    public ShipPlan(String sid) {
        this();
        if(StringUtils.isNotBlank(sid)) {
            this.selling = Selling.findById(sid);
        }
    }

    /**
     * @param shipItem
     * @deprecated
     */
    public ShipPlan(ShipItem shipItem) {
        this.shipment = shipItem.shipment;
        this.planDate = shipment.dates.planBeginDate;
        this.stockObj = new StockObj(shipItem.unit.product.sku);
        this.qty = shipItem.qty;
        this.stockObj.setAttributes(shipItem);
        this.state = S.Confirmd;
    }

    public ShipPlan(ProcureUnit unit) {
        this();
        this.planDate = unit.attrs.planShipDate;
        this.qty = unit.qty();
        this.shipType = unit.shipType;
        this.selling = unit.selling;
        this.sku = unit.sku;
        this.whouse = unit.whouse;
    }

    public ShipPlan triggerRecord(String targetId) {
        OutboundRecord outboundRecord = new OutboundRecord(this);
        if(StringUtils.isNotBlank(targetId)) outboundRecord.targetId = targetId;
        if(!outboundRecord.exist()) outboundRecord.save();
        return this;
    }

    public void valid() {
        Validation.required("状态", this.state);
        Validation.required("出货数量", this.qty);
        Validation.required("预计出货时间", this.planDate);
        this.stockObj.valid();
    }

    public boolean exist() {
        Object procureunitId = this.stockObj.attributes().get("procureunitId");
        return procureunitId != null &&
                ShipPlan.count("attributes LIKE ?", "%\"procureunitId\":" + procureunitId.toString() + "%") != 0;
    }

    public boolean isLock() {
        return this.state != S.Pending;
    }

    public ShipPlan doCreate() {
        //为了避免出现重复的 ID, 加上类锁
        synchronized(ShipPlan.class) {
            this.id = ShipPlan.id();
            return this.save();
        }
    }

    private static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = ShipPlan.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate()
        ) + "";
        return String.format("SP|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
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
}
