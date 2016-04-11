package models.whouse;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import models.User;
import models.procure.Cooperator;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Date;

/**
 * 出库记录
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 11:03 AM
 */
@Entity
public class OutboundRecord extends Model {
    /**
     * 出库对象(SKU or 物料)
     */
    @Embedded
    @Expose
    public StockObj stockObj;

    /**
     * 出库目标(货代 Or 仓库 的 ID)
     */
    public String targetId;

    @Required
    @Expose
    public T type;

    public enum T {
        Normal {
            @Override
            public String label() {
                return "正常出库";
            }
        },
        InternalTrans {
            @Override
            public String label() {
                return "内部调拨";
            }
        };

        public abstract String label();
    }

    /**
     * 出货计划
     */
    @Expose
    @ManyToOne
    public ShipPlan shipPlan;

    /**
     * 从哪个仓库出货
     */
    @Required
    @Expose
    @ManyToOne
    public Whouse whouse;

    @Required
    @Expose
    @Min(0)
    public Integer planQty;

    /**
     * 数量
     */
    @Required
    @Expose
    @Min(0)
    public Integer qty;

    /**
     * 状态
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public S state = S.Pending;

    public enum S {
        Pending {
            @Override
            public String label() {
                return "待出库";
            }
        },
        Outbound {
            @Override
            public String label() {
                return "已出库";
            }
        },
        Cancle {
            @Override
            public String label() {
                return "取消";
            }
        };

        public abstract String label();
    }

    /**
     * 操作人
     */
    @Expose
    @OneToOne
    public User handler;

    @Lob
    @Expose
    public String memo = " ";

    /**
     * 出库来源
     * 该字段仅用来方便前台过滤, 无其他实际意义
     */
    @Expose
    public O origin;

    public enum O {
        Normal {
            @Override
            public String label() {
                return "正常出库"; //系统自动生成的
            }
        },
        Other {
            @Override
            public String label() {
                return "其他出库"; //手动添加的
            }
        };

        public abstract String label();
    }

    /**
     * 出库时间
     */
    @Expose
    public Date outboundDate;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    public OutboundRecord() {
        this.qty = 0;
        this.planQty = 0;
        this.state = S.Pending;
    }

    public OutboundRecord(T type, O origin) {
        this();
        this.origin = origin;
        this.type = type;
    }

    public OutboundRecord(ShipPlan plan) {
        this.planQty = plan.qty;
        this.qty = this.planQty;
        this.origin = O.Normal;
        this.state = S.Pending;
        this.stockObj = plan.stockObj;
    }

    /**
     * 出货目标(货代)
     *
     * @return
     */
    public Cooperator getCooperator() {
        if(this.type == T.Normal) {
            return Cooperator.findById(NumberUtils.toLong(this.targetId));
        }
        throw new FastRuntimeException("类型(type)错误, 无法查询到合作伙伴!");
    }

    /**
     * 出货目标(内部仓库)
     *
     * @return
     */
    public Whouse getSelfWhouse() {
        if(this.type == T.InternalTrans) {
            return Whouse.findById(NumberUtils.toLong(this.targetId));
        }
        throw new FastRuntimeException("类型(type)错误, 无法查询到仓库!");
    }

    /**
     * 确认出库
     */
    public void confirm() {
        this.state = S.Outbound;
        this.outboundDate = new Date();

        if(this.qty == 0) Validation.addError("", String.format("出库计划: [%s] 的实际出库数量为 0!", this.id));
        if(this.whouse == null) Validation.addError("", String.format("出库计划: [%s] 的仓库为空!", this.id));
        if(Validation.hasErrors()) return;

        this.save();
        new StockRecord(this).save();
    }

    public void updateAttr(String attr, String value) {
        switch(attr) {
            case "qty":
                this.qty = NumberUtils.toInt(value);
                break;
            case "memo":
                this.memo = value;
                break;
            case "whouse":
                this.whouse = Whouse.findById(NumberUtils.toLong(value));
                break;
            case "targetId":
                this.targetId = value;
                break;
            default:
                throw new FastRuntimeException("不支持的属性类型!");
        }
        this.save();
    }

    public ElcukRecord buildRecord(String action, String message) {
        return new ElcukRecord(action, message, this.handler.username, this.id.toString()).save();
    }

    public void valid() {
        Validation.required("类型", this.type);
        Validation.required("仓库", this.whouse);
        Validation.required("预计出库数量", this.planQty);
        Validation.required("实际出库数量", this.qty);
        Validation.required("状态", this.state);
        Validation.min("预计出库数量", this.planQty, 1);
        this.stockObj.valid();
    }
}
