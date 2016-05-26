package models.procure;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.J;
import helper.Reflects;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.qc.CheckTaskDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.data.validation.Error;
import play.data.validation.Min;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 收货记录
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 5/23/16
 * Time: 2:47 PM
 */
@Entity
public class ReceiveRecord extends GenericModel implements ElcukRecord.Log {

    @Id
    @Expose
    @Required
    @Column(length = 30, nullable = false)
    public String id;

    /**
     * 出货单
     */
    @Required
    @ManyToOne
    public DeliverPlan deliverPlan;

    /**
     * 物料计划
     * TODO: 等待物料相关的功能上线后需要变更为物料而不是采购计划
     */
    @Required
    @OneToOne(fetch = FetchType.LAZY)
    public ProcureUnit procureUnit;

    /**
     * 实际数量
     */
    @Min(0)
    @Expose
    public int qty = 0;


    /**
     * 状态
     */
    @Expose
    @Required
    @Enumerated(EnumType.STRING)
    @Column(length = 12, nullable = false)
    public S state = S.Pending;

    public enum S {
        Pending {
            @Override
            public String label() {
                return "待收货";
            }
        },
        Received {
            @Override
            public String label() {
                return "已收货";
            }
        };

        public abstract String label();
    }

    /**
     * 确认人
     */
    @OneToOne
    public User confirmer;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    /**
     * 确认日期
     */
    @Expose
    public Date confirmDate;

    /**
     * 主箱信息
     */
    @Lob
    public String mainBoxInfo;

    @Transient
    public CheckTaskDTO mainBox;

    /**
     * 尾箱信息
     */
    @Lob
    public String lastBoxInfo;

    @Transient
    public CheckTaskDTO lastBox;

    @PreUpdate
    public void beforeSave() {
        this.mainBoxInfo = J.json(this.mainBox);
        this.lastBoxInfo = J.json(this.lastBox);
    }

    @PostLoad
    public void postPersist() {
        this.mainBox = JSON.parseObject(this.mainBoxInfo, CheckTaskDTO.class);
        this.lastBox = JSON.parseObject(this.lastBoxInfo, CheckTaskDTO.class);
    }

    public ReceiveRecord() {
        this.createDate = new Date();
        this.updateDate = new Date();
        this.state = S.Pending;
    }

    public ReceiveRecord(ProcureUnit procureUnit, DeliverPlan deliverPlan) {
        super();
        this.id = ReceiveRecord.id();
        this.deliverPlan = deliverPlan;
        this.procureUnit = procureUnit;
    }

    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        long count = ReceiveRecord.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate()
        );
        return String.format("SH|%s|%s", dt.toString("yyyyMM"), count < 10 ? "0" + count : count);
    }

    public static List<String> batchConfirm(List<String> rids) {
        List<String> errors = new ArrayList<>();
        List<String> confirmed = new ArrayList<>();

        for(String rid : rids) {
            ReceiveRecord record = ReceiveRecord.findById(rid);
            if(record.isLocked()) continue;
            record.confirm();

            if(Validation.hasErrors()) {
                for(Error error : Validation.errors()) {
                    String errMsg = String.format("ID: [%s] %s", rid, error.message());
                    if(!errors.contains(errMsg)) errors.add(errMsg);
                }
                Validation.clear();
            } else {
                confirmed.add(rid);
            }
        }
        if(!confirmed.isEmpty()) {
            new ERecordBuilder("receiverecord.confirm").msgArgs(StringUtils.join(confirmed, ",")).fid("1").save();
        }
        return errors;
    }


    public void confirm() {
        this.state = S.Received;
        this.confirmDate = new Date();
        this.confirmer = User.current();
        this.valid();
        //TODO 生成质检任务
        //List<StockRecord> stockRecords = this.buildStockRecords();

        if(Validation.hasErrors()) return;
        this.save();
    }

    public void valid() {
        Validation.required("出货单", this.deliverPlan);
        Validation.required("物料计划", this.procureUnit);
        Validation.required("实际数量", this.qty);
        //TODO 校验主箱与尾箱
    }

    public boolean isLocked() {
        return this.state != S.Pending;
    }

    public void updateAttr(String attr, String value) {
        if(this.isLocked()) throw new FastRuntimeException("已收货收货记录不允许修改!");

        List<String> logs = new ArrayList<>();
        switch(attr) {
            case "qty":
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                break;
            //TODO: 主箱与尾箱
            default:
                throw new FastRuntimeException("不支持的属性类型!");
        }
        new ERecordBuilder("inboundrecord.update")
                .msgArgs(this.id, StringUtils.join(logs, "<br/>")).fid(this.id)
                .save();
        this.save();
    }

    public boolean isExists() {
        return ReceiveRecord.find("procureUnit=?", this.procureUnit).fetch().size() != 0;
    }

    @Override
    public String to_log() {
        return null;
    }
}
