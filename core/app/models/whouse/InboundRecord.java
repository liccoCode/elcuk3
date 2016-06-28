package models.whouse;

import com.google.common.base.Optional;
import com.google.gson.annotations.Expose;
import helper.*;
import models.User;
import models.embedded.ERecordBuilder;
import models.market.M;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.qc.CheckTask;
import models.qc.CheckTaskDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;
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
import java.util.Map;

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
     * 去往仓库
     */
    @Expose
    @ManyToOne
    public Whouse toWhouse;

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
    @Min(0)
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
     * 确认人
     */
    @OneToOne
    public User confirmer;

    /**
     * 完成时间
     */
    @Expose
    public Date completeDate;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    /**
     * 主箱信息
     */
    @Lob
    public String mainBoxInfo;

    /**
     * 尾箱信息
     */
    @Lob
    public String lastBoxInfo;


    /**
     * 这些属性字段全部都是为了前台传递数据的
     */
    @Transient
    public String fba;

    @Transient
    public Shipment.T shipType;

    @Transient
    public String productCode;

    @Transient
    public M market;

    @Transient
    public String procureunitId;

    @Transient
    public CheckTaskDTO mainBox = new CheckTaskDTO();

    @Transient
    public CheckTaskDTO lastBox = new CheckTaskDTO();

    /**************************************/

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
        this(O.CheckTask);
        this.checkTask = task;
        if(task.receiveRecord != null) {
            this.planQty = task.receiveRecord.qty;
            if(this.isRefund()) {
                //不合格时所有的数量都入库到不良品仓
                this.badQty = task.receiveRecord.qty;
                this.targetWhouse = Whouse.defectiveWhouse();
            } else {
                this.qty = task.receiveRecord.qty - task.unqualifiedQty;
                this.badQty = task.unqualifiedQty;
            }
        }
        this.stockObj = new StockObj(task.units.product.sku);//TODO 添加物料的支持
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
            case "completeDate":
                logs.addAll(Reflects.logFieldFade(this, "completeDate", Dates.cn(value).toDate()));
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
            record.confirm();

            if(Validation.hasErrors()) {
                for(Error error : Validation.errors()) {
                    String errMsg = String.format("ID: [%s] %s", rid.toString(), error.message());
                    if(!errors.contains(errMsg)) errors.add(errMsg);
                }
                Validation.clear();
            } else {
                confirmed.add(rid);
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
    public void confirm() {
        this.state = S.Inbound;
        this.completeDate = new Date();
        this.confirmer = User.current();
        this.valid();
        if(Validation.hasErrors()) return;

        this.save();
        this.inboundProcureunit();//设置采购计划入库
        for(StockRecord record : this.buildStockRecords()) record.doCreate();
        //为质检不合格的入库生成出库记录(退给工厂)
        if(this.isRefund()) new OutboundRecord(this).save();
    }

    public void beforeCreate() {
        if(this.planQty == 0) {
            this.planQty = this.qty + this.badQty;
        }
        this.marshalBoxs();
    }

    public void valid() {
        Validation.required("仓库", this.targetWhouse);
        Validation.required("入库来源", this.origin);

        Validation.required("预计入库数量", this.planQty);
        Validation.required("实际入库数量", this.qty);
        Validation.required("不良品入库数量", this.badQty);
        Validation.required("状态", this.state);

        Validation.min("预计入库数量", this.planQty, 0);
        Validation.min("实际入库数量", this.qty, 0);
        Validation.min("不良品入库数量", this.badQty, 0);

        this.stockObj.valid();
    }

    public boolean isLocked() {
        return this.state != S.Pending;
    }

    /**
     * 判断当前的入库记录是否需要退货给工厂
     *
     * @return
     */
    public boolean isRefund() {
        return this.checkTask != null && this.checkTask.result == CheckTask.ResultType.NOTAGREE;
    }

    /**
     * 确认入库记录时记录两个库存异动(正常与不良品)
     *
     * @return
     */
    public List<StockRecord> buildStockRecords() {
        List<StockRecord> records = new ArrayList();
        try {
            //只要是入库到不良品仓就只处理 badQty
            if(StringUtils.containsIgnoreCase(this.targetWhouse.name, "不良品仓")) {
                if(this.badQty > 0) records.add(new StockRecord(this, false).valid());
            } else {
                if(this.qty > 0) records.add(new StockRecord(this, true).valid());
                if(this.badQty > 0) records.add(new StockRecord(this, false).valid());
            }
            return records;
        } catch(FastRuntimeException e) {
            Validation.addError("", e.getMessage());
        }
        return records;
    }

    /**
     * 尝试匹配供应商
     *
     * @return
     */
    public Cooperator cooperator() {
        this.stockObj.attributes();
        if(!this.stockObj.attrs.containsKey("cooperatorId")) {
            this.stockObj.resetAttrs();
            this.save();
        }
        Optional cooperatorId = Optional.fromNullable(this.stockObj.attrs.get("cooperatorId"));
        if(cooperatorId.isPresent()) {
            return Cooperator.findById(NumberUtils.toLong(cooperatorId.get().toString()));
        } else {
            return null;
        }
    }

    /**
     * 已经做过入库确认的人员名称
     *
     * @return
     */
    public static List<String> confirmers() {
        List<String> names = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = DBUtils.rows(
                    "SELECT DISTINCT u.username AS username FROM InboundRecord i" +
                            " LEFT JOIN User u ON u.id=i.confirmer_id"
            );
            if(rows != null && !rows.isEmpty()) {
                for(Map<String, Object> row : rows) {
                    if(row != null && row.containsKey("username")) {
                        if(row.get("username") != null) names.add(row.get("username").toString());
                    }
                }
            }
        } catch(NullPointerException e) {
            Logger.warn(Webs.E(e));
        }
        return names;
    }

    /**
     * 根据 FBA 属性来尝试匹配入库记录
     *
     * @return InboundRecord
     */
    public static InboundRecord findInboundRecordByFBA(String fba) {
        return InboundRecord.find("attributes LIKE ?", "%\"fba\":\"" + fba + "\"%").first();
    }

    /**
     * 根据 采购计划 ID 属性来尝试匹配入库记录
     *
     * @return InboundRecord
     */
    public static InboundRecord findInboundRecordByProcureunitId(Long procureunitId) {
        return InboundRecord.find("attributes LIKE ?", "%\"procureunitId\":" + procureunitId + "%").first();
    }


    /**
     * 采购计划入库
     */
    public void inboundProcureunit() {
        Long procureunitId = this.stockObj.procureunitId();
        if(procureunitId != null) {
            ProcureUnit unit = ProcureUnit.findById(procureunitId);
            if(unit != null) {
                unit.stage = ProcureUnit.STAGE.INWAREHOUSE;
                unit.save();
            }
        }
    }

    public void marshalBoxs() {
        this.mainBoxInfo = J.json(this.mainBox);
        this.lastBoxInfo = J.json(this.lastBox);
    }
}
