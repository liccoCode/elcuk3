package models.whouse;

import com.google.gson.annotations.Expose;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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
    @Expose
    @ManyToOne
    public Whouse whouse;

    /**
     * 数量
     */
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

    public StockRecord(InboundRecord in) {
        this.type = T.Inbound;
        this.qty = in.qty;
        this.stockObj = in.stockObj;
        this.recordId = in.id;
        this.whouse = in.targetWhouse;
        this.updateDate = new Date();
    }

    public StockRecord(OutboundRecord out) {
        this.type = T.Outbound;
        this.qty = out.qty;
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
        return String.format("%s%s", this.whouse.name, this.type.label());
    }
}
