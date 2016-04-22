package models.whouse;

import com.google.gson.annotations.Expose;
import helper.Reflects;
import models.embedded.ERecordBuilder;
import models.qc.CheckTask;
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
 * 入库记录
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 11:01 AM
 */
@Entity
public class InboundRecord extends Model {
    /**
     * 入库对象(SKU or 物料)
     */
    @Embedded
    @Expose
    public StockObj stockObj;

    /**
     * 入库来源
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public O origin;

    public enum O {
        CheckTask {
            @Override
            public String label() {
                return "质检入库";
            }
        },
        Other {
            @Override
            public String label() {
                return "其他入库";
            }
        };

        public abstract String label();
    }


    /**
     * 质检任务
     */
    @Expose
    @OneToOne
    public CheckTask checkTask;

    /**
     * 目标仓库
     */
    @Required
    @Expose
    @ManyToOne
    public Whouse targetWhouse;

    /**
     * 预计入库数量
     */
    @Min(0)
    @Required
    @Expose
    public Integer planQty;

    /**
     * 实际入库数量
     */
    @Min(1)
    @Required
    @Expose
    public Integer qty;

    /**
     * 不良品入库数量(该数量会直接入库到不良品仓)
     */
    @Min(0)
    @Required
    @Expose
    public Integer badQty;

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
                return "待入库";
            }
        },
        Inbound {
            @Override
            public String label() {
                return "已入库";
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

    @Lob
    @Expose
    public String memo = "";

    /**
     * 完成时间
     */
    @Expose
    public Date completeDate;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    public InboundRecord() {
        this.state = S.Pending;
        this.planQty = 0;
        this.qty = 0;
        this.badQty = 0;
    }

    public InboundRecord(O origin) {
        this();
        this.origin = origin;
    }

    public InboundRecord(CheckTask task) {
        this.planQty = task.qty;
        this.badQty = task.unqualifiedQty;
        this.qty = this.planQty - this.badQty;
        this.checkTask = task;
        this.origin = O.CheckTask;
        this.state = S.Pending;
        this.stockObj = new StockObj(task.sku);//TODO 添加物料的支持
        //把采购计划一些自身属性带入到入库记录,方便后期查询
        this.stockObj.setAttributes(task.units);
    }

    public void updateAttr(String attr, String value) {
        if(this.isLocked()) throw new FastRuntimeException("已经入库或取消状态下的入库记录不允许修改!");

        List<String> logs = new ArrayList<>();
        switch(attr) {
            case "qty":
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                //预计数量 >= 实际数量才去计算 不合格数量(允许 实际数量 > 预计数量, 但不自动计算不合格数量)
                if(this.planQty >= this.qty) {
                    logs.addAll(Reflects.logFieldFade(this, "badQty", this.planQty - this.qty));
                }
                break;
            case "badQty":
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                //预计数量 > 不合格数量才去计算 实际数量
                if(this.planQty > this.badQty) {
                    logs.addAll(Reflects.logFieldFade(this, "qty", this.planQty - this.badQty));
                }
                break;
            case "memo":
                logs.addAll(Reflects.logFieldFade(this, attr, value));
                break;
            case "targetWhouse":
                Whouse whouse = Whouse.findById(NumberUtils.toLong(value));
                logs.addAll(Reflects.logFieldFade(this, "targetWhouse", whouse != null ? whouse : null));
                break;
            default:
                throw new FastRuntimeException("不支持的属性类型!");
        }
        new ERecordBuilder("inboundrecord.update")
                .msgArgs(this.id, StringUtils.join(logs, "<br/>")).fid(this.id)
                .save();
        this.save();
    }


    /**
     * 批量确认入库
     *
     * @param rids
     */
    public static List<String> batchConfirm(List<Long> rids) {
        List<String> errors = new ArrayList<>();
        List<Long> confirmed = new ArrayList<>();

        for(Long rid : rids) {
            InboundRecord record = InboundRecord.findById(rid);
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
            new ERecordBuilder("inboundrecord.confirm").msgArgs(StringUtils.join(confirmed, ",")).fid("1").save();
        }
        return errors;
    }

    /**
     * 确认入库
     */
    public boolean confirm() {
        this.state = S.Inbound;
        this.completeDate = new Date();
        this.valid();
        if(Validation.hasErrors()) {
            return false;
        } else {
            List<StockRecord> records = StockRecord.recordsForInbound(this);
            for(StockRecord record : records) record.validateAndSave();
            if(Validation.hasErrors()) return false;
            this.save();
            return true;
        }
    }

    public void beforeCreate() {
        this.planQty = this.qty + this.badQty;
    }

    public void valid() {
        Validation.required("仓库", this.targetWhouse);
        Validation.required("入库来源", this.origin);

        Validation.required("预计入库数量", this.planQty);
        Validation.required("实际入库数量", this.qty);
        Validation.required("不良品入库数量", this.badQty);
        Validation.required("状态", this.state);

        Validation.min("预计入库数量", this.planQty, 0);
        Validation.min("实际入库数量", this.qty, 1);
        Validation.min("不良品入库数量", this.badQty, 0);

        this.stockObj.valid();
    }

    public boolean isLocked() {
        return this.state != S.Pending;
    }
}
