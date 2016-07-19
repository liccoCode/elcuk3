package models.whouse;

import com.google.gson.annotations.Expose;
import models.User;
import models.market.Selling;
import models.procure.FBAShipment;
import models.procure.ShipItem;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
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
     * 出货数量
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
        }
    }

    public ShipPlan(ShipItem shipItem) {
        this.shipment = shipItem.shipment;
        this.planDate = shipment.dates.planBeginDate;
        this.stockObj = new StockObj(shipItem.unit.product.sku);
        this.qty = shipItem.qty;
        this.stockObj.setAttributes(shipItem);
        this.state = S.Confirmd;
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
}
