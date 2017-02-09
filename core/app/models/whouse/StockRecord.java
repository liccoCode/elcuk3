package models.whouse;

import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;
import models.procure.ProcureUnit;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Arrays;
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

    @Required
    @Expose
    @ManyToOne
    public ProcureUnit unit;

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
                return "采购入库";
            }
        },
        Outbound {
            @Override
            public String label() {
                return "出库";
            }
        },
        Split {
            @Override
            public String label() {
                return "库存分拆";
            }
        },
        Refund {
            @Override
            public String label() {
                return "入库后退货";
            }
        },
        Stocktaking {
            @Override
            public String label() {
                return "库存调整";
            }
        },
        Split_Stock {
            @Override
            public String label() {
                return "库存分拆修改";
            }
        },
        Unqualified_Refund {
            @Override
            public String label() {
                return "不良品退货";
            }
        },
        Unqualified_Transfer {
            @Override
            public String label() {
                return "不良品转入";
            }
        };


        public abstract String label();
    }

    public enum C {
        Check {
            @Override
            public String label() {
                return "盘点";
            }
        },
        Normal {
            @Override
            public String label() {
                return "Amazon 出库";
            }
        },
        B2B {
            @Override
            public String label() {
                return "B2B 出库";
            }
        },
        Refund {
            @Override
            public String label() {
                return "退回工厂";
            }
        },
        Process {
            @Override
            public String label() {
                return "品拓生产";
            }
        },
        Sample {
            @Override
            public String label() {
                return "取样";
            }
        },
        Other {
            @Override
            public String label() {
                return "其他出库";
            }
        };

        public abstract String label();
    }

    /**
     * 类别
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public C category;

    /**
     * 记录 ID(入库 Or 出库)
     */
    public Long recordId;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate;

    public String memo;

    public StockRecord() {
        this.updateDate = new Date();
    }

    public StockRecord(StockObj stockObj) {
        this();
        this.stockObj = stockObj.dump();
        this.pickAttrs();
    }

    public StockRecord(InboundRecord in, boolean normal) {
        this(in.stockObj);
        if(normal) {
            this.qty = in.qty;
            this.whouse = in.targetWhouse;
        } else {
            this.qty = in.badQty;
            this.whouse = Whouse.defectiveWhouse();
            if(this.whouse == null) throw new FastRuntimeException("未找到不良品仓,请初始化不良品仓后再进行确认入库.");
        }
        this.type = T.Inbound;
        this.recordId = in.id;
    }

    public StockRecord(OutboundRecord out) {
        this(out.stockObj);
        this.type = T.Outbound;
        this.qty = -out.qty;
        this.recordId = out.id;
        this.whouse = out.whouse;
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

    public StockRecord valid() {
        Validation.required("仓库", this.whouse);
        Validation.required("数量", this.qty);
        this.stockObj.valid();
        return this;
    }

    public void doCerate() {
        this.save();
        this.updateWhouseQty();
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

    /**
     * 库存异动的 attributes 只存储 fba shipType 信息
     *
     * @return
     */
    public void pickAttrs() {
        if(this.stockObj != null && !this.stockObj.attributes().isEmpty()) {
            this.stockObj.attrs = Maps
                    .filterKeys(this.stockObj.attrs, Predicates.in(Arrays.asList("fba", "shipType", "whouseName"
                    )));
            this.stockObj.setAttributes();
        }
    }
}
