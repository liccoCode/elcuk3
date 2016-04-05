package models.whouse;

import com.google.gson.annotations.Expose;
import models.ElcukRecord;
import models.User;
import models.procure.Cooperator;
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
    @Expose
    @ManyToOne
    public Whouse whouse;

    /**
     * 数量
     */
    public Integer qty;

    /**
     * 状态
     */
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
     * 出库时间
     */
    @Expose
    public Date outboundDate;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    /**
     * 出货目标(货代)
     *
     * @return
     */
    public Cooperator getCooperator() {
        if(this.type == T.Normal) {
            return Cooperator.findById(Long.getLong(this.targetId));
        }
        throw new FastRuntimeException("类型(type)错误, 无法查询到合作伙伴!");
    }

    /**
     * 出货目标(内部仓库)
     *
     * @return
     */
    public Whouse getWhouse() {
        if(this.type == T.InternalTrans) {
            return Whouse.findById(Long.getLong(this.targetId));
        }
        throw new FastRuntimeException("类型(type)错误, 无法查询到仓库!");
    }

    /**
     * 确认出库
     */
    public void confirm() {
        this.state = S.Outbound;
        new StockRecord(this).save();
    }

    public ElcukRecord buildRecord(String action, String message) {
        return new ElcukRecord(action, message, this.handler.username, this.id.toString()).save();
    }
}
