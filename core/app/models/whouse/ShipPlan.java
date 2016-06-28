package models.whouse;

import com.google.gson.annotations.Expose;
import models.procure.ShipItem;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * 出货计划
 * 角色定义为出货记录与运输单中间的信息传递,允许从运输单创建出货计划或者先创建出货计划然后再去关联运输单
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 2:55 PM
 */
@Entity
public class ShipPlan extends Model {
    /**
     * 运输单
     */
    @ManyToOne
    public Shipment shipment;

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
                return "待确认";
            }
        },
        Confirmd {
            @Override
            public String label() {
                return "已确认";
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
     * 预计出货时间
     */
    @Expose
    public Date planDate;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();


    @PrePersist
    @PreUpdate
    public void setupAttrs() {
        if(this.shipment != null) {
            this.state = S.Confirmd;
        }
    }

    public ShipPlan() {
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
        if(procureunitId != null) {
            return ShipPlan.count("attributes LIKE ?", "%\"procureunitId\":" + procureunitId.toString() + "%") != 0;
        }
        return false;
    }
}
