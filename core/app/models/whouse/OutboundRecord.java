package models.whouse;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Optional;
import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.J;
import helper.Reflects;
import models.ElcukRecord;
import models.User;
import models.embedded.ERecordBuilder;
import models.market.M;
import models.procure.*;
import models.qc.CheckTaskDTO;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 出库记录
 * <p>
 * (角色等同于出货计划,但是手动创建的出货记录没有关联的出货计划)
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
     * 出库目标(货代 Or 供应商 Or 其他)
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
                return "Amazon 出库";
            }
        },
        B2B {
            @Override
            public String label() {
                return "B2B 出库";
            }
        },
        Refund {
            @Override
            public String label() {
                return "退回工厂";
            }
        },
        Process {
            @Override
            public String label() {
                return "品拓生产";
            }
        },
        Sample {
            @Override
            public String label() {
                return "取样";
            }
        },
        Other {
            @Override
            public String label() {
                return "其他出库";
            }
        };

        public abstract String label();
    }

    /**
     * 出货计划
     */
    @Expose
    @OneToOne
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

    @Expose
    @Enumerated(EnumType.STRING)
    public DeliverPlan.CT clearanceType;

    /**
     * 这些属性字段全部都是为了前台传递数据的
     */
    @Transient
    public Date planBeginDate;

    @Transient
    public String fba;

    @Transient
    public Shipment.T shipType;

    @Transient
    public String market;

    @Transient
    public String productCode;

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
        this.unmarshalBoxs();
    }

    public OutboundRecord() {
        this.planQty = 1;
        this.state = S.Pending;
        this.type = T.Normal;
        this.origin = O.Normal;
    }

    public OutboundRecord(T type, O origin) {
        this();
        this.origin = origin;
        this.type = type;
        this.handler = User.current();
    }

    public OutboundRecord(ShipPlan plan) {
        this(T.Normal, O.Normal);
        this.planQty = plan.planQty;
        this.qty = this.planQty;
        this.shipPlan = plan;
        this.whouse = this.findWhouse();
        this.stockObj = new StockObj(plan.product.sku);
        this.stockObj.setAttributes(plan);
        this.tryMatchAttrs();
    }

    /**
     * 该构造函数只服务于为质检不合格的入库记录自动生成出库记录
     *
     * @param inboundRecord
     */
    public OutboundRecord(InboundRecord inboundRecord) {
        this(T.Refund, O.Normal);
        if(!inboundRecord.isRefund()) {
            throw new FastRuntimeException("该构造函数只能用于为质检不合格的入库记录自动生成出库记录!");
        }
        this.qty = inboundRecord.badQty;
        this.planQty = this.qty;
        this.whouse = inboundRecord.targetWhouse;
        this.stockObj = inboundRecord.stockObj.dump();
        Optional cooperatorId = Optional.fromNullable(this.stockObj.attributes().get("cooperatorId"));
        if(cooperatorId.isPresent()) this.targetId = cooperatorId.get().toString();
    }

    /**
     * 为出库计划尝试匹配
     * 1. 出库对象
     * 2. 主箱信息 和 尾箱信息
     * 3. 报关类型
     * 4. 哪个仓库出货
     */
    public void tryMatchAttrs() {
        if(StringUtils.isBlank(this.targetId)) {
            //尝试匹配出库对象
            Cooperator cooperator = Cooperator.mainShipper();
            if(cooperator != null) this.targetId = cooperator.id.toString();
        }

        if(StringUtils.isBlank(this.mainBoxInfo) || StringUtils.isBlank(this.lastBoxInfo)) {
            if(this.stockObj != null && this.stockObj.procureunitId() != null) {
                //尝试匹配 主箱信息 和 尾箱信息
                ReceiveRecord receiveRecord = this.receiveRecord();
                if(receiveRecord != null) {
                    this.mainBoxInfo = receiveRecord.mainBoxInfo;
                    this.lastBoxInfo = receiveRecord.lastBoxInfo;
                    this.unmarshalBoxs();
                }
            }
            if(this.clearanceType == null) {
                //尝试匹配报关类型
                ProcureUnit procureUnit = this.procureUnit();
                if(procureUnit != null) {
                    this.clearanceType = procureUnit.clearanceType;
                }
            }
        }

        if(this.whouse == null) {
            this.whouse = this.findWhouse();
        }
    }

    /**
     * 出货目标(货代 Or 供应商)
     *
     * @return
     */
    public Cooperator getCooperator() {
        if(Arrays.asList(T.Normal, T.B2B, T.Refund).contains(this.type)) {
            return Cooperator.findById(NumberUtils.toLong(this.targetId));
        }
        return null;
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
        if(this.outboundDate == null) this.outboundDate = new Date();
        this.confirmValid();
        if(Validation.hasErrors()) {
            return false;
        } else {
            this.save();
            this.outboundProcureUnit();
            this.syncQtyToShipPlan();
            new StockRecord(this).doCreate();
            return true;
        }
    }

    public void updateAttr(String attr, String value) {
        if(this.isLocked()) throw new FastRuntimeException("已经入库或取消状态下的出库记录不允许修改!");
        List<String> logs = new ArrayList<>();
        switch(attr) {
            case "whouse":
                logs.addAll(Reflects.logFieldFade(this, "whouse", Whouse.findById(NumberUtils.toLong(value))));
                break;
            case "targetId":
            case "memo":
                logs.addAll(Reflects.logFieldFade(this, attr, value));
                break;
            case "outboundDate":
                logs.addAll(Reflects.logFieldFade(this, "outboundDate", Dates.cn(value).toDate()));
                break;
            case "qty":
            case "mainBox.boxNum":
            case "mainBox.num":
            case "lastBox.boxNum":
            case "lastBox.num":
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toInt(value)));
                break;
            case "mainBox.singleBoxWeight":
            case "mainBox.length":
            case "mainBox.width":
            case "mainBox.height":
            case "lastBox.singleBoxWeight":
            case "lastBox.length":
            case "lastBox.width":
            case "lastBox.height":
                logs.addAll(Reflects.logFieldFade(this, attr, NumberUtils.toDouble(value)));
                break;
            case "clearanceType":
                logs.addAll(Reflects.logFieldFade(this, attr, DeliverPlan.CT.valueOf(value)));
                break;
            default:
                throw new FastRuntimeException("不支持的属性类型!");
        }
        this.marshalBoxs();
        if(attr.contains(".boxNum") || attr.contains(".num")) {
            int fullQty = this.mainBox.qty() + this.lastBox.qty();
            if(fullQty != 0) logs.addAll(Reflects.logFieldFade(this, "qty", fullQty));
        }
        new ERecordBuilder("outboundrecord.update")
                .msgArgs(this.id, StringUtils.join(logs, "<br/>"))
                .fid(this.id)
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
        Validation.required("状态", this.state);
        Validation.min("预计出库数量", this.planQty, 1);
        this.typeValid();
        this.stockObj.valid();
    }

    /**
     * 根据 type 来校验 market 和 targetId 字段
     */
    public void typeValid() {
        if(StringUtils.isBlank(this.targetId)) Validation.addError("", "出库对象不能为空.");
        Optional whouseName = Optional.fromNullable(this.stockObj.attributes().get("whouseName"));
        switch(this.type) {
            case Normal:
                this.targetIdValidByCT(Cooperator.T.SHIPPER);
                if(whouseName.isPresent()) {
                    if(M.val(whouseName.get().toString()) == null) {
                        Validation.addError("", "去往国家必须为一个正常的 Market(例: FBA_DE).");
                    }
                } else {
                    Validation.addError("", "去往国家不能为空.");
                }
                break;
            case B2B:
                if(!whouseName.isPresent()) Validation.addError("", "去往国家不能为空.");
                this.targetIdValidByCT(Cooperator.T.SHIPPER);
                break;
            case Refund:
                this.targetIdValidByCT(Cooperator.T.SUPPLIER);
                break;
            case Process:
                if(!StringUtils.equalsIgnoreCase(this.targetId, "品拓生产部")) {
                    Validation.addError("", "出库对象只能为品拓生产部.");
                }
                break;
            case Sample:
                if(!Arrays.asList("质检部", "采购部", "运营部", "研发部", "生产部").contains(this.targetId)) {
                    Validation.addError("", "出库对象错误.");
                }
                break;
        }

    }

    /**
     * 根据供应商类别来校验 targetId 是否正确对应到 Cooperator
     *
     * @param cooperatorType
     */
    public void targetIdValidByCT(Cooperator.T cooperatorType) {
        if(StringUtils.isNotBlank(this.targetId) && cooperatorType != null &&
                Cooperator.find("id=? AND type=?", NumberUtils.toLong(this.targetId), cooperatorType) == null) {
            Validation.addError("", String.format("出库类别为 %s 的时候, 出库对象应该为 %s", this.type.label(), cooperatorType.to_s()));
        }
    }


    public void confirmValid() {
        this.valid();
        Validation.min("实际出库", this.qty, (double) 1);

        if(Validation.hasErrors()) return;
        if(!this.checkWhouseItemQty()) {
            Validation.addError("", String.format("仓库 [%s] 中 [%s] 可用库存不足", this.whouse.name, this.stockObj.stockObjId));
        }
    }

    public boolean isLocked() {
        return this.state != S.Pending;
    }

    /**
     * 设置采购计划是否出库状态为已出库
     */
    public void outboundProcureUnit() {
        Object procureunitId = this.stockObj.procureunitId();
        if(procureunitId != null) {
            ProcureUnit procureUnit = ProcureUnit.findById(NumberUtils.toLong(procureunitId.toString()));
            if(procureUnit != null) {
                procureUnit.isOut = ProcureUnit.OST.Outbound;
                procureUnit.save();
            }
        }
    }

    /**
     * 同步实际出库数到出库计划
     */
    public void syncQtyToShipPlan() {
        if(this.shipPlan != null) {
            ShipPlan plan = this.shipPlan;
            plan.state = ShipPlan.S.Confirmd;
            plan.qty = this.qty;
            plan.save();
        }
    }

    public boolean exist() {
        Object procureunitId = this.stockObj.procureunitId();
        Object shipPlanId = this.stockObj.shipPlanId();
        if(procureunitId != null) {
            return OutboundRecord.count("attributes LIKE ?",
                    "%\"procureunitId\":" + procureunitId.toString() + "%") != 0;
        } else if(shipPlanId != null) {
            return ShipPlan.count("attributes LIKE ?",
                    "%\"shipPlanId\":" + shipPlanId.toString() + "%") != 0;
        }
        return false;
    }

    public static boolean checkExistsWithUnitId(String procureunitId) {
        return StringUtils.isBlank(procureunitId) ||
                OutboundRecord.count("attributes LIKE ?", "%\"procureunitId\":" + procureunitId + "%") != 0;
    }

    /**
     * 检查仓库中的库存是否能够满足当前出库的数量
     *
     * @return
     */
    public boolean checkWhouseItemQty() {
        WhouseItem item = WhouseItem.findItem(this.stockObj, this.whouse);
        return item != null && item.qty >= Math.abs(this.qty);
    }

    public String targetName() {
        if(Arrays.asList(T.Normal, T.B2B, T.Refund).contains(this.type) && StringUtils.isNotBlank(this.targetId)) {
            Cooperator cooperator = Cooperator.findById(NumberUtils.toLong(this.targetId));
            return cooperator.name;
        } else {
            return targetId;
        }
    }

    /**
     * 根据 FBA 属性来尝试获取入库记录中选择的目标仓库
     *
     * @return
     */
    public Whouse findWhouse() {
        if(this.stockObj == null) return null;
        Optional fba = Optional.fromNullable(this.stockObj.attributes().get("fba"));
        if(fba.isPresent()) {
            Optional<InboundRecord> inboundRecord = Optional.fromNullable(
                    InboundRecord.findInboundRecordByFBA(fba.get().toString())
            );
            if(inboundRecord.isPresent()) return inboundRecord.get().targetWhouse;
        }
        return null;
    }

    public ReceiveRecord receiveRecord() {
        Long procureunitId = this.stockObj.procureunitId();
        if(procureunitId != null) {
            return ReceiveRecord.find("procureUnit.id=?", procureunitId).first();
        }
        return null;
    }

    public String cooperatorName() {
        ReceiveRecord receiveRecord = this.receiveRecord();
        if(receiveRecord != null && receiveRecord.procureUnit != null && receiveRecord.procureUnit.cooperator != null) {
            return receiveRecord.procureUnit.cooperator.name;
        }
        return null;
    }

    public String receiveRecordId() {
        ReceiveRecord receiveRecord = this.receiveRecord();
        if(receiveRecord != null) return receiveRecord.id;
        return null;
    }

    public ProcureUnit procureUnit() {
        Long procureunitId = this.stockObj.procureunitId();
        if(procureunitId != null) {
            return ProcureUnit.findById(procureunitId);
        }
        return null;
    }

    public void marshalBoxs() {
        this.mainBoxInfo = J.json(this.mainBox);
        this.lastBoxInfo = J.json(this.lastBox);
    }

    public void unmarshalBoxs() {
        this.mainBox = StringUtils.isEmpty(this.mainBoxInfo) ? new CheckTaskDTO() :
                JSON.parseObject(this.mainBoxInfo, CheckTaskDTO.class);
        this.lastBox = StringUtils.isEmpty(this.lastBoxInfo) ? new CheckTaskDTO() :
                JSON.parseObject(this.lastBoxInfo, CheckTaskDTO.class);
    }

    /**
     * 格式化出库对象
     *
     * @return
     */
    public String getFormatTarget() {
        switch(this.type) {
            case Normal:
            case B2B:
            case Refund:
                Cooperator cooperator = Cooperator.findById(NumberUtils.toLong(this.targetId));
                return cooperator.name;
            case Process:
            case Sample:
            case Other:
                return this.targetId;
            default:
                return null;
        }
    }
}
