package models.whouse;

import com.google.gson.annotations.Expose;
import helper.Reflects;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Error;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
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
    @Enumerated(EnumType.STRING)
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
        this.type = T.Normal;
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

    public static List<String> batchConfirm(List<Long> rids) {
        List<String> errors = new ArrayList<>();
        List<Long> confirmed = new ArrayList<>();

        for(Long rid : rids) {
            OutboundRecord record = OutboundRecord.findById(rid);
            if(record.isLocked()) continue;

            if(record.confirm()) {
                confirmed.add(rid);
            } else {
                for(Error error : Validation.errors()) {
                    errors.add(String.format("ID: [%s] %s", rid.toString(), error.message()));
                }
                Validation.clear();
            }
        }
        if(!confirmed.isEmpty()) {
            new ERecordBuilder("outboundrecord.confirm").msgArgs(StringUtils.join(confirmed, ",")).fid("1").save();
        }
        return errors;
    }

    /**
     * 确认出库
     */
    public boolean confirm() {
        this.state = S.Outbound;
        this.outboundDate = new Date();
        this.confirmValid();
        if(Validation.hasErrors()) {
            return false;
        } else {
            this.save();
            this.outboundProcureUnit();
            new StockRecord(this).save();
            return true;
        }
    }

    public void updateAttr(String attr, String value) {
        if(this.isLocked()) throw new FastRuntimeException("已经入库或取消状态下的出库记录不允许修改!");

        List<String> logs = new ArrayList<>();
        switch(attr) {
            case "qty":
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                break;
            case "memo":
                logs.addAll(Reflects.logFieldFade(this, attr, value));
                break;
            case "whouse":
                Whouse whouse = Whouse.findById(NumberUtils.toLong(value));
                logs.addAll(Reflects.logFieldFade(this, "whouse", whouse != null ? whouse : null));
                break;
            case "targetId":
                logs.addAll(Reflects.logFieldFade(this, attr, value));
                break;
            default:
                throw new FastRuntimeException("不支持的属性类型!");
        }
        new ERecordBuilder("outboundrecord.update")
                .msgArgs(this.id, StringUtils.join(logs, "<br/>")).fid(this.id)
                .save();
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

    public void confirmValid() {
        Validation.required("接收对象", this.targetId);
        this.valid();
    }

    public boolean isLocked() {
        return this.state != S.Pending;
    }

    /**
     * 设置采购计划是否出库状态为已出库
     */
    public void outboundProcureUnit() {
        Object procureunitId = this.stockObj.attributes().get("procureunitId");
        if(procureunitId != null) {
            ProcureUnit procureUnit = ProcureUnit.findById(NumberUtils.toLong(procureunitId.toString()));
            if(procureUnit != null) {
                procureUnit.isOut = ProcureUnit.OST.Outbound;
                procureUnit.save();
            }
        }
    }

    public boolean exist() {
        Object procureunitId = this.stockObj.attributes().get("procureunitId");
        if(procureunitId != null) {
            return OutboundRecord.count("attributes LIKE ?", "%\"procureunitId\":" + procureunitId.toString() + "%")
                    != 0;
        }
        return false;
    }
}
