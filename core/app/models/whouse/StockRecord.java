package models.whouse;

import com.google.gson.annotations.Expose;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Date;

/**
 * 库存异动记录
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 11:12 AM
 */
@Entity
public class StockRecord extends Model {
    /**
     * 仓库
     */
    @Required
    @Expose
    @ManyToOne
    public Whouse whouse;

    /**
     * 数量
     */
    @Required
    @Expose
    public Integer qty;

    /**
     * 异动对象(SKU or 物料)
     */
    @Embedded
    @Expose
    public StockObj stockObj;

    /**
     * 类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public T type;

    public enum T {
        Inbound {
            @Override
            public String label() {
                return "入库";
            }
        },
        Outbound {
            @Override
            public String label() {
                return "出库";
            }
        },
        Stocktaking {
            @Override
            public String label() {
                return "盘库";
            }
        };

        public abstract String label();
    }

    /**
     * 记录 ID(入库 Or 出库)
     */
    public Long recordId;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate;

    public StockRecord() {
    }

    public StockRecord(InboundRecord in, boolean normal) {
        if(normal) {
            this.qty = in.qty;
            this.whouse = in.targetWhouse;
        } else {
            this.qty = -in.badQty;
            this.whouse = Whouse.defectiveWhouse();
        }
        this.type = T.Inbound;
        this.stockObj = in.stockObj;
        this.recordId = in.id;
        this.updateDate = new Date();
    }

    public StockRecord(OutboundRecord out) {
        this.type = T.Outbound;
        this.qty = -out.qty;
        this.stockObj = out.stockObj;
        this.recordId = out.id;
        this.whouse = out.whouse;
        this.updateDate = new Date();
    }

    public InboundRecord getInboundRecord() {
        if(this.type == T.Inbound) {
            return InboundRecord.findById(this.recordId);
        }
        throw new FastRuntimeException("类型(type) 错误, 无法找到对应的入库记录.");
    }

    public OutboundRecord getOutboundRecord() {
        if(this.type == T.Outbound) {
            return OutboundRecord.findById(this.recordId);
        }
        throw new FastRuntimeException("类型(type) 错误, 无法找到对应的出库记录.");

    }

    /**
     * 异动来源
     *
     * @return
     */
    public String recordOrigin() {
        return String.format("%s-%s", this.whouse.name, this.type.label());
    }

    public void valid() {
        Validation.required("仓库", this.whouse);
        Validation.required("数量", this.qty);
        this.stockObj.valid();
    }

    /**
     * 确认入库记录时记录两个库存异动(正常与不良品)
     *
     * @param inboundRecord
     */
    public static void recordsForInbound(InboundRecord inboundRecord) {
        new StockRecord(inboundRecord, true).doCerate();
        if(inboundRecord.badQty > 0) new StockRecord(inboundRecord, false).doCerate();//不良品
    }

    public void doCerate() {
        this.valid();
        if(!Validation.hasErrors()) {
            this.save();
            this.updateWhouseQty();
        }
    }

    /**
     * 更新相关仓库的库存数据
     */
    public void updateWhouseQty() {
        WhouseItem whouseItem = WhouseItem.findItem(this.stockObj, this.whouse);
        if(whouseItem != null) {
            whouseItem.qty += this.qty;
            whouseItem.save();
        }
    }
}
