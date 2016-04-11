package models.whouse;

import com.google.gson.annotations.Expose;
import models.qc.CheckTask;
import org.apache.commons.lang.math.NumberUtils;
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
    @Expose
    public Integer planQty;

    /**
     * 实际入库数量
     */
    @Expose
    public Integer qty;

    /**
     * 不良品入库数量(该数量会直接入库到不良品仓)
     */
    @Expose
    public Integer badQty;

    /**
     * 状态
     */
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
                this.badQty = this.planQty - this.qty;
                break;
            case "badQty":
                this.badQty = NumberUtils.toInt(value);
                this.qty = this.planQty - this.badQty;
                break;
            case "memo":
                this.memo = value;
                break;
            case "targetWhouse":
                this.targetWhouse = Whouse.findById(Long.getLong(value));
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

    /**
     * 为手动添加的其他类型的入库单初始化一些字段
     */
    public void setUpAttrs() {
        this.state = S.Pending;
        this.origin = O.Other;
    }
}
