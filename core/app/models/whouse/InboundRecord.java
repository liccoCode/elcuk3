package models.whouse;

import com.google.gson.annotations.Expose;
import models.qc.CheckTask;
import org.apache.commons.lang.math.NumberUtils;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.Date;

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
    @OneToOne(fetch = FetchType.LAZY)
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
    }

    public void updateAttr(String attr, String value) {
        switch(attr) {
            case "qty":
                this.qty = NumberUtils.toInt(value);
                //预计数量 >= 实际数量才去计算 不合格数量(允许 实际数量 > 预计数量, 但不自动计算不合格数量)
                if(this.planQty >= this.qty) this.badQty = this.planQty - this.qty;
                break;
            case "badQty":
                this.badQty = NumberUtils.toInt(value);
                //预计数量 > 不合格数量才去计算 实际数量
                if(this.planQty > this.badQty) this.qty = this.planQty - this.badQty;
                break;
            case "memo":
                this.memo = value;
                break;
            case "targetWhouse":
                this.targetWhouse = Whouse.findById(NumberUtils.toLong(value));
                break;
            default:
                throw new FastRuntimeException("不支持的属性类型!");
        }
        this.save();
    }

    /**
     * 确认入库
     */
    public void confirm() {
        this.state = S.Inbound;
        this.completeDate = new Date();

        if(this.qty == 0) Validation.addError("", String.format("入库计划: [%s] 的实际良品数量为 0!", this.id));
        if(this.targetWhouse == null) Validation.addError("", String.format("入库计划: [%s] 的目标仓库为空!", this.id));
        if(Validation.hasErrors()) return;

        this.save();
        new StockRecord(this).save();
        //更新库存
        this.updateWhouseQty();
    }

    /**
     * 更新库存
     */
    public void updateWhouseQty() {
        //处理合格的库存
        WhouseItem whouseItem = WhouseItem.findItem(this.stockObj, this.targetWhouse);
        if(whouseItem != null) {
            whouseItem.qty += this.qty;
            whouseItem.save();
        }
        //处理不合格的库存
        if(this.badQty > 0) {
            WhouseItem defectiveWhouseItem = WhouseItem.findItem(this.stockObj, Whouse.defectiveWhouse());
            if(defectiveWhouseItem != null) {
                defectiveWhouseItem.qty += this.badQty;
                defectiveWhouseItem.save();
            }
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
}
