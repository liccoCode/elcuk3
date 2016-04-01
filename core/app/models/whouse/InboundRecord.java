package models.whouse;

import com.google.gson.annotations.Expose;
import models.qc.CheckTask;
import play.db.jpa.Model;

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
    @Expose
    @ManyToMany
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

    /**
     * 完成时间
     */
    @Expose
    public Date completeDate;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();
}
