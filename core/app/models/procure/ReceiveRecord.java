package models.procure;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import helper.J;
import helper.Reflects;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.embedded.UnitAttrs;
import models.qc.CheckTask;
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
import java.util.Arrays;
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
     */
    @Required
    @OneToOne(fetch = FetchType.LAZY)
    public ProcureUnit procureUnit;

    /**
     * 实际数量
     */
    @Min(0)
    @Expose
    public Integer qty;

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
    public CheckTaskDTO mainBox = new CheckTaskDTO();

    /**
     * 尾箱信息
     */
    @Lob
    public String lastBoxInfo;

    @Transient
    public CheckTaskDTO lastBox = new CheckTaskDTO();

    @PrePersist
    public void beforeSave() {
        this.marshalBoxs();
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
        this();
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
        this.deliveryUnit();
        this.triggerCheck();
        if(Validation.hasErrors()) return;
        this.save();
    }

    public void valid() {
        Validation.required("出货单", this.deliverPlan);
        Validation.required("物料计划", this.procureUnit);
        Validation.min("实际数量", this.qty, 0);
    }

    public boolean isLocked() {
        return this.isPersistent() && this.state == S.Received;
    }

    public void updateAttr(String attr, String value) {
        if(this.isLocked()) throw new FastRuntimeException("已收货的收货记录不允许修改!");
        if(StringUtils.isBlank(attr)) throw new FastRuntimeException("属性名称不能为空!");
        String[] chunks = StringUtils.splitByWholeSeparator(attr, ".");
        if(chunks.length != 2 || !Arrays.asList("mainBox", "lastBox").contains(chunks[0])) {
            throw new FastRuntimeException("不合法的属性名称!");
        }

        List<String> logs = new ArrayList<>();
        if(Arrays.asList("boxNum", "num").contains(chunks[1])) {
            logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
        } else if(Arrays.asList("singleBoxWeight", "length", "width", "height").contains(chunks[1])) {
            logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toDouble(value)));
        }
        logs.addAll(Reflects.logFieldFade(this, "qty", this.mainBox.qty() + this.lastBox.qty()));
        this.marshalBoxs();
        new ERecordBuilder("receiverecord.update")
                .msgArgs(this.id, StringUtils.join(logs, "<br/>")).fid(this.id)
                .save();
        this.save();
    }

    public boolean isExists() {
        return ReceiveRecord.find("procureUnit=?", this.procureUnit).fetch().size() != 0;
    }

    public void marshalBoxs() {
        this.mainBoxInfo = J.json(this.mainBox);
        this.lastBoxInfo = J.json(this.lastBox);
    }

    /**
     * 生成质检任务
     */
    public void triggerCheck() {
        if(this.isLocked()) {
            CheckTask checkTask = new CheckTask(this);
            if(!checkTask.isExists()) {
                checkTask.validateAndSave();
            }
        }
    }

    /**
     * 采购计划交货
     */
    public void deliveryUnit() {
        if(this.isLocked()) {
            UnitAttrs attrs = this.procureUnit.attrs;
            attrs.qty = this.qty;
            this.procureUnit.delivery(attrs);
        }
    }

    @Override
    public String to_log() {
        return null;
    }
}
