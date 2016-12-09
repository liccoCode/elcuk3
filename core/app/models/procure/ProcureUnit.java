package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.google.gson.annotations.Expose;
import helper.*;
import models.ElcukRecord;
import models.Role;
import models.User;
import models.activiti.ActivitiDefinition;
import models.activiti.ActivitiProcess;
import models.embedded.ERecordBuilder;
import models.embedded.UnitAttrs;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.market.Account;
import models.market.Selling;
import models.product.Product;
import models.qc.CheckTask;
import models.qc.CheckTaskDTO;
import models.view.dto.CooperItemDTO;
import models.whouse.*;
import mws.FBA;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.Logger;
import play.data.validation.Check;
import play.data.validation.CheckWith;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;
import play.modules.pdf.PDF;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 每一个采购单元
 * User: wyattpan
 * Date: 6/11/12
 * Time: 5:23 PM
 */
@Entity
@DynamicUpdate
public class ProcureUnit extends Model implements ElcukRecord.Log {

    public ProcureUnit() {
    }

    public ProcureUnit(Selling selling) {
        if(selling != null) {
            this.selling = selling;
            this.product = this.selling.listing.product;
        }
    }

    /**
     * Copy 一个全新的 ProcureUnit, 用来将部分交货的 ProcureUnit 分单交货
     *
     * @param unit
     */
    public ProcureUnit(ProcureUnit unit) {
        this.cooperator = unit.cooperator;
        this.selling = unit.selling;
        this.sid = unit.sid;
        this.product = unit.product;
        this.sku = unit.sku;
        this.handler = unit.handler;
        this.whouse = unit.whouse;
        this.deliveryment = unit.deliveryment;
        this.deliverplan = unit.deliverplan;
        // 刚刚创建 ProcureUnit 为 PLAN 阶段
        this.stage = STAGE.PLAN;
        this.planstage = PLANSTAGE.PLAN;
        this.shipType = unit.shipType;
        this.attrs.planDeliveryDate = unit.attrs.planDeliveryDate;
        this.attrs.planShipDate = unit.attrs.planShipDate;
        this.attrs.planArrivDate = unit.attrs.planArrivDate;
        this.attrs.price = unit.attrs.price;
        this.attrs.currency = unit.attrs.currency;
    }

    /**
     * 阶段
     */
    public enum STAGE {
        /**
         * 审批中
         */
        APPROVE {
            @Override
            public String label() {
                return "审批中";
            }
        },
        /**
         * 计划阶段; 创建一个新的采购计划
         */
        PLAN {
            @Override
            public String label() {
                return "计划中";
            }
        },
        /**
         * 采购阶段; 从采购计划列表添加进入采购单.
         */
        DELIVERY {
            @Override
            public String label() {
                return "采购中";
            }
        },
        /**
         * 完成了, 全部交货了; 在采购单中进行交货
         */
        DONE {
            @Override
            public String label() {
                return "已交货";
            }
        },
        /**
         * 深圳仓已入库
         */
        IN_STORAGE {
            @Override
            public String label() {
                return "已入库";
            }
        },
        PROCESSING {
            @Override
            public String label() {
                return "仓库加工";
            }
        },
        OUTBOUND {
            @Override
            public String label() {
                return "已出库";
            }
        },

        /**
         * 运输中; 运输单的点击开始运输
         */
        SHIPPING {
            @Override
            public String label() {
                return "运输中";
            }
        },
        /**
         * 抵达港口
         */
        SHIP_OVER {
            @Override
            public String label() {
                return "抵达港口";
            }
        },
        /**
         * 入库中; Amazon FBA 的状态变更为 RECEVING
         */
        INBOUND {
            @Override
            public String label() {
                return "入库中";
            }
        },
        /**
         * 关闭阶段, 不处理了; Amazon FBA 的状态变更为 CLOSE, CANCEL
         */
        CLOSE {
            @Override
            public String label() {
                return "结束";
            }
        };

        public abstract String label();
    }


    /**
     * 出货单阶段
     */
    public enum PLANSTAGE {

        /**
         * 计划阶段; 创建一个新的采购计划
         */
        PLAN {
            @Override
            public String label() {
                return "计划中";
            }
        },
        /**
         * 采购阶段; 从采购计划列表添加进入采购单.
         */
        DELIVERY {
            @Override
            public String label() {
                return "采购中";
            }
        },
        /**
         * 完成了, 全部交货了; 在采购单中进行交货
         */
        DONE {
            @Override
            public String label() {
                return "已交货";
            }
        };

        public abstract String label();
    }


    @OneToMany(mappedBy = "procureUnit", fetch = FetchType.LAZY)
    public List<PaymentUnit> fees = new ArrayList<>();

    /**
     * 此采购计划的供应商信息.
     */
    @OneToOne
    public Cooperator cooperator;

    /**
     * 采购单
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public Deliveryment deliveryment;


    /**
     * 出货单
     */
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST})
    public DeliverPlan deliverplan;

    @ManyToOne
    public FBAShipment fba;

    /**
     * 所关联的运输出去的 ShipItem.
     */
    @OneToMany(mappedBy = "unit")
    public List<ShipItem> shipItems = new ArrayList<>();


    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;


    @Expose
    @CheckWith(MskuCheck.class)
    public String sid; // 一个 SellingId 字段

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    @Expose
    @CheckWith(SkuCheck.class)
    public String sku;// 冗余 sku 字段
    /**
     * 操作人员
     */
    @OneToOne
    public User handler;

    /**
     * 送往的仓库
     */
    @OneToOne
    public Whouse whouse;

    /**
     * 当前仓库（深圳仓库）
     */
    @OneToOne
    public Whouse currWhouse;

    // ----------- 将不同阶段的数据封装到不同的对象当中去.

    /**
     * ProcureUnit 所涉及的参数
     */
    @Expose
    @Required
    public UnitAttrs attrs = new UnitAttrs();


    /**
     * 货物运输的类型 (海运? 空运? 快递)
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Column(length = 20)
    public Shipment.T shipType;


    /**
     * 此 Unit 的状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public STAGE stage = STAGE.PLAN;


    /**
     * 此 Unit 的状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public PLANSTAGE planstage = PLANSTAGE.PLAN;

    public Date createDate = new Date();

    /**
     * 货物是否已经到达货代的地方
     */
    public boolean isPlaced = false;


    /**
     * 是否需要付款
     */
    public boolean isNeedPay = true;


    @Lob
    @Expose
    public String comment = " ";

    /**
     * 采购取样
     */
    @Expose
    public Integer purchaseSample;

    /**
     * 是否生成了质检任务
     */
    @Expose
    public int isCheck = 0;

    /**
     * 装箱单是否核准
     */
    public boolean isConfirm = false;

    public enum S {
        NOSHIPED {
            @Override
            public String label() {
                return "采购员未确认";
            }
        },
        NOSHIPWAIT {
            @Override
            public String label() {
                return "采购员确认";
            }
        };

        public abstract String label();
    }

    /**
     * 采购计划的不发货处理状态
     */
    @Expose
    @Enumerated(EnumType.STRING)
    public S shipState;

    /**
     * 生产周期
     */
    @Transient
    public Integer period;

    public void setPeriod() {
        if(this.product.cooperators().size() > 0) {
            Long cid = this.cooperator.id;
            CooperItem cooperItem = CooperItem.find("cooperator.id=? AND sku=?", cid, this.sku).first();
            this.period = cooperItem.period;
        }

    }

    public enum OPCONFIRM {
        CONFIRM {
            @Override
            public String label() {
                return "待确认";
            }
        },

        CONFIRMED {
            @Override
            public String label() {
                return "已确认";
            }
        };

        public abstract String label();
    }

    /**
     * 运营确认
     */
    @Expose
    @Enumerated(EnumType.STRING)
    public OPCONFIRM opConfirm;

    public enum QCCONFIRM {
        CONFIRM {
            @Override
            public String label() {
                return "待确认";
            }
        },

        CONFIRMED {
            @Override
            public String label() {
                return "已确认";
            }
        };

        public abstract String label();
    }

    /**
     * 质检员确认
     */
    @Expose
    @Enumerated(EnumType.STRING)
    public QCCONFIRM qcConfirm;

    public enum OST {
        Pending {
            @Override
            public String label() {
                return "未出库";
            }
        },
        Outbound {
            @Override
            public String label() {
                return "已出库";
            }
        };

        public abstract String label();

    }

    /**
     * 是否出库
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public OST isOut = OST.Pending;

    @Transient
    public static String ACTIVITINAME = "procureunit.create";

    @Transient
    public List<CooperItemDTO> items = new ArrayList<>();

    /**
     * 相关联的质检任务
     *
     * @return
     */
    @Expose
    @OneToMany(mappedBy = "units", fetch = FetchType.LAZY)
    @OrderBy("creatat DESC")
    public List<CheckTask> taskList;

    /**
     * 质检结果
     */
    @Enumerated(EnumType.STRING)
    public InboundUnit.R result;

    /**
     * 入库数
     */
    public int inboundQty;

    /**
     * 不合格数量
     */
    public int unqualifiedQty;

    /**
     * 可用库存数
     */
    public int availableQty;

    /**
     * 实际出库数
     */
    public int outQty;

    /**
     * 项目名称
     */
    public String projectName;

    /**
     * 出库单
     */
    @ManyToOne
    public Outbound outbound;


    /**
     * 用来标识采购计划是否需要计入正常库存(当前只会用于 Rockend 内的 InventoryCostsReport 报表)
     * <p>
     * 1. 由于历史原因部分采购计划需要挪市场(DE=>UK),但是此时采购计划已经不允许修改了,采购就再创建一条新的采购计划
     * 2. B2B 采购计划
     */
    public String isInventory;

    /***
     * 原FBACenter信息移到采购计划身上储存
     */
    public String centerId;

    public String addressLine1;

    public String addressLine2;

    public String city;

    public String name;

    public String countryCode;

    public String stateOrProvinceCode;

    public String postalCode;

    /**
     * ProcureUnit 的检查
     */
    public void validate() {
        Validation.current().valid(this);
        Validation.current().valid(this.attrs);
        Validation.required("procureunit.selling", this.selling);
        if(this.selling != null) this.sid = this.selling.sellingId;
        Validation.required("procureunit.whouse", this.whouse);
        Validation.required("procureunit.handler", this.handler);
        Validation.required("procureunit.product", this.product);
        Validation.required("procureunit.cooperator", this.cooperator);
        if(this.product != null) this.sku = this.product.sku;
        Validation.required("procureunit.createDate", this.createDate);
        if(this.attrs != null) this.attrs.validate();
        if(this.selling != null && this.whouse != null &&
                this.whouse.account != null && this.whouse.type == Whouse.T.FBA) {
            if(!this.selling.account.uniqueName.equals(this.whouse.account.uniqueName)) {
                Validation.addError("", "procureunit.validate.whouse");
            }
        }
    }

    public String codeToCountry() {
        if(StringUtils.isBlank(this.countryCode)) return "";
        this.countryCode = this.countryCode.toUpperCase();
        if(this.countryCode.equals("GB")) return "United Kingdom";
        else if(this.countryCode.equals("US")) return "United States";
        else if(this.countryCode.equals("CA")) return "Canada";
        else if(this.countryCode.equals("CN")) return "China (Mainland)";
        else if(this.countryCode.equals("DE")) return "Germany";
        else if(this.countryCode.equals("FR")) return "France";
        else if(this.countryCode.equals("IT")) return "Italy";
        else if(this.countryCode.equals("JP")) return "Japan";
        return "";
    }


    /**
     * 手动单检查
     */
    public void manualValidate() {
        Validation.required("交货日期", this.attrs.planDeliveryDate);
        Validation.required("采购数量", this.attrs.planQty);
        Validation.required("procureunit.cooperator", this.cooperator);
        Validation.required("procureunit.handler", this.handler);
        Validation.required("procureunit.product", this.product);
        Validation.required("价格", this.attrs.price);
        if(this.product != null) this.sku = this.product.sku;
        Validation.required("procureunit.createDate", this.createDate);
        if(this.attrs != null) this.attrs.validate();
        if(this.selling == null && this.shipType != null) {
            Validation.addError("", "运输方式不为空时,selling也不能为空!");
        }
    }


    /**
     * 手动单采购计划数据验证
     */
    public void validateManual() {
        Validation.required("交货日期", this.attrs.planDeliveryDate);
        Validation.required("procureunit.handler", this.handler);
        Validation.required("procureunit.product", this.product);
        Validation.required("价格", this.attrs.price);
        if(this.product != null) this.sku = this.product.sku;
        Validation.required("procureunit.createDate", this.createDate);
        if(this.attrs != null) this.attrs.validate();
    }


    /**
     * 分拆采购计划: 工厂在原有采购计划基础上全部交货, 只能够部分交货的情况下进行的操作;
     * 1. 原有的采购计划仅仅数量变化.
     * 2. 新创建采购计划, 处理数量, FBA, Shipment 相关信息变化, 其他保持不变.
     *
     * @param unit
     */
    public ProcureUnit split(ProcureUnit unit) {
        int originQty = this.qty();
        if(unit.attrs.planQty != null) {
            if(unit.attrs.planQty > originQty)
                Validation.addError("", "因分批交货创建的采购计划的数量不可能大于原来采购计划的数量.");
            if(unit.attrs.planQty <= 0)
                Validation.addError("", "新创建分批交货的采购计划数量必须大于 0");
        }
        if(!this.isBeforeDONE())
            Validation.addError("", "已经交货或者成功运输, 不需要分拆采购计划.");
        ProcureUnit newUnit = null;
        if(this.selling == null) {
            //手动单拆分时将 拆分的采购计划 归属到 此采购计划 的采购单身上
            newUnit = unit;
            newUnit.deliveryment = this.deliveryment;
        } else {
            newUnit = new ProcureUnit(this);
            newUnit.attrs.planQty = unit.attrs.planQty;
        }
        newUnit.stage = STAGE.DELIVERY;
        if(unit.selling == null) {
            newUnit.manualValidate();
        } else {
            newUnit.validate();
        }


        List<Shipment> shipments = Shipment.similarShipments(newUnit.attrs.planShipDate,
                newUnit.whouse, newUnit.shipType);
        //无selling的手动单不做处理
        //快递不做判断
        if(unit.selling != null && newUnit.shipType != Shipment.T.EXPRESS
                && shipments.size() <= 0)
            Validation.addError("",
                    String.format("没有合适的运输单, 请联系运输部门, 创建 %s 之后去往 %s 的 %s 运输单.",
                            newUnit.attrs.planShipDate, newUnit.whouse.name, newUnit.shipType));

        if(Validation.hasErrors()) return newUnit;
        //无selling的手动单不做处理
        Shipment shipment = null;
        if(unit.selling != null && shipments.size() > 0) shipment = shipments.get(0);
        // FBA 变更
        if(this.fba != null)
            this.fba.updateFBAShipment(null);

        // 原采购计划数量变更
        this.attrs.planQty = originQty - newUnit.attrs.planQty;
        if(this.attrs.qty != null)
            this.attrs.qty = this.attrs.planQty;
        this.shipItemQty(this.qty());
        this.save();

        // 原采购计划的运输量变更
        int average = (int) Math.ceil((float) this.qty() / this.shipItems.size());
        for(int i = 0; i < this.shipItems.size(); i++) {
            // 平均化, 包含除不尽的情况
            if(i == this.shipItems.size() - 1) {
                this.shipItems.get(i).qty =
                        this.qty() - (average * this.shipItems.size() - 1);
            } else {
                this.shipItems.get(i).qty = average;
            }
        }

        // 分拆出的新采购计划变更
        newUnit.save();
        //生成质检任务
        newUnit.triggerCheck();
        if(unit.selling != null && shipments.size() > 0) shipment.addToShip(newUnit);

        new ERecordBuilder("procureunit.split")
                .msgArgs(this.id, originQty, newUnit.attrs.planQty, newUnit.id)
                .fid(this.id, ProcureUnit.class)
                .save();
        return newUnit;
    }

    /**
     * 是否完成全部交货
     *
     * @return
     */
    public boolean isBeforeDONE() {
        return Arrays.asList(STAGE.DELIVERY, STAGE.PLAN).contains(this.stage);
    }

    /**
     * 调整运输的数量
     *
     * @param qty
     */
    public void shipItemQty(int qty) {
        int leftQty = qty;
        for(ShipItem itm : this.shipItems) {
            if(!itm.isPersistent()) continue;
            if(leftQty - itm.qty >= 0) {
                itm.qty = leftQty;
                leftQty -= itm.qty;
            } else {
                itm.qty = leftQty;
                leftQty = 0;
            }
            itm.save();
        }
    }

    /**
     * ProcureUnit 交货
     *
     * @param attrs
     * @return T2: ._1:是否全部交货, ._2:新转移的采购单元
     */
    public Boolean delivery(UnitAttrs attrs) {
        /**
         * 1. 检查交货的数据是否符合
         * 2. 执行交货
         *  - 交货不足不允许交货.
         *  - 全部交货
         *  - 交货超额, Notify 提醒
         *
         */
        if(!Arrays.asList(STAGE.DONE, STAGE.DELIVERY).contains(this.stage))
            Validation.addError("", "采购计划" + this.stage.label() + "状态不可以交货.");
        if(this.deliveryment == null)
            Validation.addError("", "没有进入采购单, 无法交货.");
        if(attrs.qty == null) attrs.qty = 0;
        //Validation.required("procureunit.attrs.qty", attrs.qty);
        //Validation.min("procureunit.attrs.qty", attrs.qty, 0);
        Validation.required("procureunit.attrs.deliveryDate", attrs.deliveryDate);
        if(Validation.hasErrors())
            throw new FastRuntimeException("检查不合格");

        this.attrs = attrs;
        new ERecordBuilder("procureunit.delivery")
                .msgArgs(this.attrs.qty, this.attrs.planQty)
                .fid(this.id, ProcureUnit.class)
                .save();
        this.shipItemQty(this.qty());
        this.stage = STAGE.DONE;
        this.save();
        return this.attrs.planQty.equals(this.attrs.qty);
    }

    /**
     * 取消交货
     */
    public void revertDelivery(String msg) {
        if(this.stage != STAGE.DONE)
            Validation.addError("", "不是" + STAGE.DONE.label() + "状态, 无法返回" + STAGE.DELIVERY.label());
        if(StringUtils.isBlank(msg))
            Validation.addError("", "请填写取消交货的原因.");
        if(Validation.hasErrors()) return;

        this.stage = STAGE.DELIVERY;
        this.save();
        new ERecordBuilder("procureunit.revertdelivery")
                .msgArgs(msg)
                .fid(this.id, ProcureUnit.class)
                .save();
    }

    /**
     * 采购计划在不同阶段可以修改的信息不一样
     *
     * @param unit
     */
    public void update(ProcureUnit unit, String shipmentId, String reason) {
        Shipment.T oldShipType = this.shipType;
        /**
         * 1. 修改不同阶段可以修改的信息
         * 2. 根据运输类型修改运输单
         */
        // 1
        //采购计划的FBA已存在后再次编辑该采购计划的 运输方式 或采购数量 时对修改原因做不为空校验
        if(this.fba != null &&
                (unit.shipType != this.shipType || !Objects.equals(unit.attrs.planQty, this.attrs.planQty)))
            Validation.required("procureunit.update.reason", reason);
        if(this.stage == STAGE.CLOSE)
            Validation.addError("", "已经结束, 无法再修改");
        if(unit.cooperator == null) Validation.addError("", "供应商不能为空!");

        List<String> logs = new ArrayList<>();
        if(Arrays.asList(STAGE.APPROVE, STAGE.PLAN, STAGE.DELIVERY).contains(this.stage)) {
            logs.addAll(this.beforeDoneUpdate(unit));
        } else if(this.stage == STAGE.DONE) {
            logs.addAll(this.doneUpdate(unit));
        }
        this.comment = unit.comment;
        this.purchaseSample = unit.purchaseSample;
        // 2
        if(Arrays.asList(STAGE.APPROVE, STAGE.PLAN, STAGE.DELIVERY, STAGE.DONE).contains(this.stage)) {
            this.changeShipItemShipment(
                    StringUtils.isBlank(shipmentId) ? null : Shipment.findById(shipmentId),
                    oldShipType);
        }
        if(Validation.hasErrors()) return;

        if(logs.size() > 0) {
            if(StringUtils.isBlank(reason)) {
                new ERecordBuilder("procureunit.update").msgArgs(this.id, StringUtils.join(logs, "<br>"),
                        this.generateProcureUnitStatusInfo()).fid(this.id, ProcureUnit.class).save();
            } else {
                new ERecordBuilder("procureunit.deepUpdate").msgArgs(reason, this.id, StringUtils.join(logs, "<br>"),
                        this.generateProcureUnitStatusInfo()).fid(this.id, ProcureUnit.class).save();
            }
            noty(this.sku, StringUtils.join(logs, ","));
        }
        this.shipItemQty(this.qty());
        this.save();
    }

    /**
     * 修改手动单数据
     */
    public void updateManualData(ProcureUnit unit) {
        this.attrs.price = unit.attrs.price;
        this.attrs.planQty = unit.attrs.planQty;
        this.attrs.currency = unit.attrs.currency;
        this.attrs.planDeliveryDate = unit.attrs.planDeliveryDate;
        this.purchaseSample = unit.purchaseSample;
    }

    public void noty(String sku, String content) {
        content = User.username() + "修改," + content;
        Set<User> notyUsers = this.editToUsers();
        if(content.contains("日期") || content.contains("时间"))
            notyUsers.addAll(User.operations(sku));
        /**
         * 因为运输单上没有制单人，需要特定发给运输人员
         */
        if(content.contains("运输时间") || content.contains("shipType"))
            notyUsers.addAll(User.shipoperations());
    }

    /**
     * 通过反射获取采购计划更新的字段和值
     * 附: 在采购计划交货前的状态, 可以修改采购计划的:
     * 数量, 价格, 币种, 预计交货日期, 预计运输日期, 预计到库日期, 运输单
     *
     * @param unit
     * @return List<String>
     */
    public List<String> beforeDoneUpdate(ProcureUnit unit) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "attrs.planDeliveryDate", unit.attrs.planDeliveryDate));
        logs.addAll(Reflects.logFieldFade(this, "attrs.planShipDate", unit.attrs.planShipDate));
        logs.addAll(Reflects.logFieldFade(this, "attrs.planArrivDate", unit.attrs.planArrivDate));
        logs.addAll(Reflects.logFieldFade(this, "attrs.planQty", unit.attrs.planQty));
        logs.addAll(Reflects.logFieldFade(this, "attrs.price", unit.attrs.price));
        logs.addAll(Reflects.logFieldFade(this, "attrs.currency", unit.attrs.currency));
        logs.addAll(Reflects.logFieldFade(this, "attrs.qty", unit.attrs.qty));
        logs.addAll(Reflects.logFieldFade(this, "shipType", unit.shipType));
        return logs;
    }

    private List<String> doneUpdate(ProcureUnit unit) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "attrs.qty", unit.attrs.qty));
        logs.addAll(Reflects.logFieldFade(this, "attrs.planShipDate", unit.attrs.planShipDate));
        logs.addAll(Reflects.logFieldFade(this, "attrs.planArrivDate", unit.attrs.planArrivDate));
        logs.addAll(Reflects.logFieldFade(this, "shipType", unit.shipType));
        return logs;
    }

    /**
     * 生成当前采购计划的状态信息(是否抵达货代、FBA 信息、付款信息)
     *
     * @return String
     */
    public String generateProcureUnitStatusInfo() {
        String paymentInfo = "";
        PaymentUnit prePay = this.fetchPrePay();
        PaymentUnit tailPay = this.fetchTailPay();
        if(prePay != null) {
            if(prePay.state == PaymentUnit.S.APPLY) paymentInfo += "已申请预付款";
            if(prePay.state == PaymentUnit.S.PAID) paymentInfo += "已付预付款";
        }
        if(tailPay != null) {
            if(tailPay.state == PaymentUnit.S.APPLY) paymentInfo += " 已申请尾款";
            if(tailPay.state == PaymentUnit.S.PAID) paymentInfo += " 已付尾款";
        }
        return String.format("抵达货代: %s, FBA: %s, 付款信息: %s", this.isPlaced,
                this.fba != null ? this.fba.shipmentId : "无", StringUtils.isBlank(paymentInfo) ? "无" : paymentInfo);
    }

    /**
     * 调整采购计划所产生的运输项目的运输单
     *
     * @param oldShipType
     * @param shipment
     */
    public void changeShipItemShipment(Shipment shipment, Shipment.T oldShipType) {
        if(this.shipItems.stream()
                .anyMatch(item -> item.shipment != null && item.shipment.state != Shipment.S.PLAN)) {
            Validation.addError("", String.format("当前采购计划已经存在运输单, 且该运输单不是 %s 状态.", Shipment.S.PLAN.label()));
            return;
        }
        if(shipment != null && shipment.state != Shipment.S.PLAN) {
            Validation.addError("", String.format(
                    "需要关联的运输单 %s 为 %s 状态, 只有 %s 状态的运输单才可调整.", shipment.id, shipment.state.label(),
                    Shipment.S.PLAN.label()));
            return;
        }
        if(shipment == null) {
            // 1. 调整为快递运输单, 已经拥有的运输项目全部删除, 重新设计.
            // 2. 用户更改了运输方式但未选择运输单
            if(this.shipType == Shipment.T.EXPRESS || oldShipType != this.shipType) {
                this.shipItems.forEach(GenericModel::delete);
            }
        } else {
            if(this.shipItems.isEmpty()) {
                // 采购计划没有运输项目, 调整运输单的时候, 需要创建运输项目
                shipment.addToShip(this);
            } else {
                // 采购计划已经有运输项目, 调整运输项的运输单
                this.shipItems.stream()
                        .filter(item -> item.shipment != shipment)
                        .forEach(item -> {
                            Shipment originShipment = item.shipment;
                            item.adjustShipment(shipment);//调整运输项的运输单
                            if(Validation.hasErrors()) {
                                item.shipment = originShipment;
                                item.save();
                            }
                        });
            }
        }
    }

    /**
     * 通过 ProcureUnit 创建 FBA
     */
    public synchronized FBAShipment postFbaShipment(CheckTaskDTO dto) {
        FBAShipment fba = null;
        if(!dto.validedQtys(this.qty())) return fba;
        try {
            fba = FBA.plan(this.selling.account, this);
        } catch(FBAInboundServiceMWSException e) {
            String errMsg = e.getMessage();
            if(errMsg.contains("UNKNOWN_SKU") || errMsg.contains("NOT_IN_PRODUCT_CATALOG")) {
                Validation.addError("", String.format("向 Amazon 创建 Shipment PLAN 失败, 请检查[%s]在 Amazon 后台是否存在.",
                        this.selling.merchantSKU));
            } else if(errMsg.contains("UNFULFILLABLE_IN_DESTINATION_MP") || errMsg.contains("MISSING_DIMENSIONS")) {
                Validation.addError("", String.format(
                        "向 Amazon 创建 Shipment PLAN 失败, 请检查 [%s] 在 Amazon 后台的 Listing 的尺寸是否正确填写(数值和单位).",
                        this.selling.merchantSKU));
            } else if(errMsg.contains("ANDON_PULL_STRIKE_ONE")) {
                Validation.addError("", String.format(
                        "向 Amazon 创建 Shipment PLAN 失败, 请检查 [%s] 市场 [%s] 的其他的 FBA 是否报告了异常.",
                        this.selling.market.name(), this.selling.merchantSKU));
            } else {
                Validation.addError("", "向 Amazon 创建 Shipment PLAN 因 " + Webs.E(e) + " 原因失败.");
            }
            return null;
        }
        try {
            fba.dto = dto;
            fba.state = FBA.create(fba);
            this.fba = fba.doCreate();
            this.centerId = fba.fbaCenter.centerId;
            this.addressLine1 = fba.fbaCenter.addressLine1;
            this.addressLine2 = fba.fbaCenter.addressLine2;
            this.city = fba.fbaCenter.city;
            this.name = fba.fbaCenter.name;
            this.countryCode = fba.fbaCenter.countryCode;
            this.stateOrProvinceCode = fba.fbaCenter.stateOrProvinceCode;
            this.postalCode = fba.fbaCenter.postalCode;

            this.save();
            new ERecordBuilder("shipment.createFBA").msgArgs(this.id, this.sku, this.fba.shipmentId).fid(this.id).save();
        } catch(FBAInboundServiceMWSException e) {
            Logger.error(Webs.S(e));
            Validation.addError("", "向 Amazon 创建 Shipment 错误 " + Webs.E(e));
        }
        return fba;
    }

    public String nickName() {
        return String.format("ProcureUnit[%s][%s][%s]", this.id, this.sid, this.sku);
    }


    /**
     * 将 ProcureUnit 添加到/移出 采购单,状态改变
     *
     * @param deliveryment
     */
    public void toggleAssignTodeliveryment(Deliveryment deliveryment, boolean assign) {
        if(assign) {
            this.deliveryment = deliveryment;
            this.stage = ProcureUnit.STAGE.DELIVERY;
        } else {
            this.deliveryment = null;
            this.stage = STAGE.PLAN;
        }
    }


    /**
     * 将 ProcureUnit 添加到/移出 出库单,状态改变
     *
     * @param deliverplan
     */
    public void toggleAssignTodeliverplan(DeliverPlan deliverplan, boolean assign) {
        if(assign) {
            this.deliverplan = deliverplan;
            this.planstage = PLANSTAGE.DELIVERY;
        } else {
            this.deliverplan = null;
            this.planstage = PLANSTAGE.PLAN;
        }
    }


    public void comment(String cmt) {
        this.comment = String.format("%s%n%s", cmt, this.comment).trim();
    }


    public void remove() {
        /**
         * TODO: 这里需要理清楚
         * 1. 什么时候可以删除采购计划?
         * 2. 如果在拥有 FBA 后仍然可以删除采购计划, 需要如何处理?
         */
        for(PaymentUnit fee : this.fees()) {
            if(fee.state == PaymentUnit.S.PAID) {
                Validation.addError("", "采购计划" + this.id + "已经拥有成功的支付信息, 不可以删除.");
            } else if(fee.state == PaymentUnit.S.APPROVAL) {
                Validation.addError("", "采购计划" + this.id + "已经被批准, 准备付款, 请联系审核人.");
            }
            if(Validation.hasErrors()) return;
        }
        if(Arrays.asList(STAGE.PLAN, STAGE.DELIVERY).contains(this.stage)) {
            for(PaymentUnit fee : this.fees) {
                fee.permanentRemove();
            }
            // 删除 FBA
            FBAShipment fba = this.fba;
            if(fba != null) {
                fba.units.remove(this);
                fba.removeFBAShipment();
            }
            // 删除运输相关
            for(ShipItem item : this.shipItems) {
                item.delete();
            }

            //删除 质检任务相关
            List<CheckTask> tasks = this.taskList;
            for(CheckTask task : tasks) {
                task.delete();
            }
            //删除采购单关联
            Deliveryment deliveryment = this.deliveryment;
            if(deliveryment != null && deliveryment.units != null) {
                deliveryment.units.remove(this);
            }
            //删除出货单关联
            DeliverPlan deliverPlan = this.deliverplan;
            if(deliverPlan != null && deliverPlan.units != null) {
                deliverPlan.units.remove(this);
            }
            this.delete();
        } else {
            Validation.addError("",
                    String.format("只允许 %s, %s 状态的采购计划进行取消", STAGE.PLAN, STAGE.DELIVERY));
        }
    }

    /**
     * 采购单元相关联的运输单
     *
     * @return
     */
    public List<Shipment> relateShipment() {
        return this.shipItems.stream()
                .filter(shipItem -> shipItem.shipment != null)
                .map(shipItem -> shipItem.shipment)
                .collect(Collectors.toList());
    }

    public int qty() {
        if(this.attrs.qty != null) return this.attrs.qty;
        return this.attrs.planQty;
    }

    public int realQty() {
        int qty = this.qty() - this.fetchCheckTaskQcSample();
        if(purchaseSample != null) qty -= purchaseSample;
        return qty;
    }

    /**
     * 入库中的数量
     *
     * @return
     */
    public int inboundingQty() {
        int inboundingQty = 0;
        for(ShipItem shipItm : this.shipItems) {
            inboundingQty += shipItm.adjustQty;
        }
        return inboundingQty;
    }

    /**
     * 预付款申请
     */
    public PaymentUnit billingPrePay() {
        /**
         * 0. 基本检查
         * 1. 检查是否此采购计划是否已经存在一个预付款
         * 2. 申请预付款
         */
        this.billingValid();
        if(this.hasPrePay())
            Validation.addError("", "不允许重复申请预付款.");
        if(this.hasTailPay())
            Validation.addError("", "已经申请了尾款, 不需要再申请预付款.");
        if(Validation.hasErrors()) return null;

        PaymentUnit fee = new PaymentUnit(this);
        // 预付款的逻辑在这里实现, 总额的 30% 为预付款
        fee.feeType = FeeType.cashpledge();
        fee.amount = (float) (fee.amount * 0.3);
        fee.save();
        new ERecordBuilder("procureunit.prepay")
                .msgArgs(this.product.sku,
                        String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id, ProcureUnit.class)
                .save();
        return fee;
    }


    /**
     * 修改付款状态
     */
    public void editPayStatus() {
        this.isNeedPay = !this.isNeedPay;
        this.save();
    }

    /**
     * 尾款申请
     */
    public PaymentUnit billingTailPay() {
        /**
         * 0. 基本检查
         * 1. 检查是否已经存在一个尾款
         * 2. 申请尾款
         */
        this.billingValid();
        //2014-05-28要求没签收也可先付款
        //if(Arrays.asList(STAGE.PLAN, STAGE.DELIVERY).contains(this.stage))
        //    Validation.addError("", "请确定采购计划的交货数量(交货)");
        if(this.hasTailPay())
            Validation.addError("", "不允许重复申请尾款");
        if(Validation.hasErrors()) return null;

        PaymentUnit fee = new PaymentUnit(this);
        fee.feeType = FeeType.procurement();
        fee.amount = this.leftAmount();
        fee.save();
        new ERecordBuilder("procureunit.tailpay")
                .msgArgs(this.product.sku,
                        String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id, ProcureUnit.class)
                .save();
        return fee;
    }

    /**
     * 返工费用申请
     *
     * @return
     */
    public PaymentUnit billingReworkPay(float amount) {
        /**
         * 0. 基本检查
         * 1. 检查是否已经存在一个尾款
         * 2. 申请尾款
         */
        this.billingValid();
        if(Validation.hasErrors()) return null;
        PaymentUnit fee = new PaymentUnit(this);
        fee.feeType = FeeType.rework();
        //费用需要计算选中的质检任务内的返工费用
        fee.amount = amount;
        fee.save();
        new ERecordBuilder("procureunit.reworkpay")
                .msgArgs(this.product.sku,
                        String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id, ProcureUnit.class)
                .save();
        return fee;
    }

    /**
     * 1. 采购计划所在的采购单需要拥有一个请款单
     * 2. 采购计划需要已经交货
     */
    private void billingValid() {
        if(this.deliveryment.apply == null)
            Validation.addError("", String.format("采购计划所属的采购单[%s]还没有规划的请款单", this.deliveryment.id));
    }

    /**
     * 剩余的请款金额
     *
     * @return
     */
    public float leftAmount() {
        return totalAmount() - appliedAmount();
    }

    /**
     * 已经申请的金额
     *
     * @return
     */
    public float appliedAmount() {
        float appliedAmount = 0;
        for(PaymentUnit fee : this.fees()) {
            appliedAmount += fee.amount();
        }
        return appliedAmount;
    }

    /**
     * 当前采购计划所有请款的修正总额
     *
     * @return
     */
    public float fixValueAmount() {
        float fixValueAmount = 0;
        for(PaymentUnit fee : this.fees()) {
            fixValueAmount += fee.fixValue;
        }
        return fixValueAmount;
    }

    /**
     * 总共需要申请的金额
     *
     * @return
     */
    public float totalAmount() {
        return new BigDecimal(this.attrs.price.toString()).multiply(new BigDecimal(this.qty())).setScale(2, 4)
                .floatValue();
    }


    /**
     * 是否拥有了 预付款
     *
     * @return
     */
    public boolean hasPrePay() {
        for(PaymentUnit fee : this.fees()) {
            if(fee.feeType == FeeType.cashpledge())
                return true;
        }
        return false;
    }

    /**
     * 判断当前selling下面是否有unit
     *
     * @param sellingId
     * @return
     */
    public static boolean hasProcureUnitBySellings(String sellingId) {
        List<ProcureUnit> units = ProcureUnit.find("selling.sellingId = ? and stage <> ? ",
                sellingId, STAGE.APPROVE).fetch();
        if(units != null && units.size() > 0)
            return true;
        return false;
    }

    /**
     * 预付款
     *
     * @return
     */
    public PaymentUnit fetchPrePay() {
        for(PaymentUnit fee : this.fees()) {
            if(fee.feeType == FeeType.cashpledge()) return fee;
        }
        return null;
    }

    /**
     * 是否拥有了尾款
     *
     * @return
     */
    public boolean hasTailPay() {
        for(PaymentUnit fee : this.fees()) {
            if(fee.feeType == FeeType.procurement())
                return true;
        }
        return false;
    }

    /**
     * 尾款
     *
     * @return
     */
    public PaymentUnit fetchTailPay() {
        for(PaymentUnit fee : this.fees()) {
            if(fee.feeType == FeeType.procurement())
                return fee;
        }
        return null;
    }


    public List<PaymentUnit> fees() {
        List<PaymentUnit> fees = new ArrayList<>();
        for(PaymentUnit fee : this.fees) {
            if(fee.remove) continue;
            fees.add(fee);
        }
        return fees;
    }

    /**
     * 总重量 kg
     *
     * @return
     */
    public float totalWeight() {
        if(this.product.weight != null)
            return this.qty() * this.product.weight;
        else
            return 0f;
    }

    /**
     * 转换成记录日志的格式
     *
     * @return
     */
    @Override
    public String to_log() {
        return String.format("[sid:%s] [仓库:%s] [供应商:%s] [计划数量:%s] [预计到库:%s] [运输方式:%s]",
                this.sid, this.whouse.name(), this.cooperator.fullName, this.attrs.planQty,
                Dates.date2Date(this.attrs.planArrivDate), this.shipType);
    }

    public List<ElcukRecord> records() {
        return ElcukRecord.records(this.id + "",
                Arrays.asList("procureunit.save", "procureunit.update", "procureunit.remove", "procureunit.delivery",
                        "procureunit.revertdelivery", "procureunit.split", "procureunit.prepay", "procureunit.tailpay"),
                50);
    }

    /**
     * 页面上用来缓存 records 的 key
     *
     * @return
     */
    public String recordsPageCacheKey() {
        return Webs.Md5(ElcukRecord.pageCacheKey(ProcureUnit.class, this.id));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        ProcureUnit that = (ProcureUnit) o;

        if(id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    /**
     * 根据 sku 或者 msku 加载 PLAN, DELIVERY Stage 的 ProcureUnit.
     *
     * @param selling
     * @param isSku
     * @return
     */
    public static List<ProcureUnit> skuOrMskuRelate(Selling selling, boolean isSku) {
        if(isSku)
            return find("sku=? AND stage IN (?,?,?)", Product.merchantSKUtoSKU(selling.merchantSKU),
                    STAGE.PLAN, STAGE.DELIVERY, STAGE.DONE).fetch();
        else
            return find("selling=? AND stage IN (?,?,?)", selling, STAGE.PLAN, STAGE.DELIVERY,
                    STAGE.DONE).fetch();
    }

    public static List<ProcureUnit> unitsFilterByStage(STAGE stage) {
        return ProcureUnit.find("stage=?", stage).fetch();
    }

    /**
     * 批量创建 FBA, 返回的结果可以用来获取创建的 FBAShipment 列表
     *
     * @param unitIds
     * @param dtos
     * @return List<F.Promise<FBAShipment>>
     */
    public static void postFbaShipments(final List<Long> unitIds,
                                        final List<CheckTaskDTO> dtos) {
        final List<ProcureUnit> units = ProcureUnit.find(SqlSelect.whereIn("id", unitIds)).fetch();
        if(units.size() != unitIds.size() || units.size() != dtos.size()) {
            Validation.addError("", "加载的数量不一致");
        }
        if(Validation.hasErrors()) return;

        for(int i = 0; i < units.size(); i++) {
            ProcureUnit unit = units.get(i);
            try {
                if(unit.fba != null) {
                    Validation.addError("", String.format("#%s 已经有 FBA 不需要再创建", unit.id));
                } else {
                    unit.postFbaShipment(dtos.get(i));
                }
            } catch(Exception e) {
                Logger.error(Webs.S(e));
                Validation.addError("", "向 Amazon 创建 Shipment 因 " + Webs.E(e) + " 原因失败.");
            }
        }
    }

    /**
     * 批量更新 FBA 的箱内包装信息
     *
     * @param unitIds
     */
    public static void postFbaCartonContents(List<Long> unitIds, List<CheckTaskDTO> dtos) {
        List<ProcureUnit> units = ProcureUnit.find(SqlSelect.whereIn("id", unitIds)).fetch();
        if(units.size() != unitIds.size() || units.size() != dtos.size()) {
            Validation.addError("", "加载的数量");
        }
        if(Validation.hasErrors()) return;

        for(int i = 0; i < units.size(); i++) {
            ProcureUnit unit = units.get(i);
            CheckTaskDTO dto = dtos.get(i);
            if(!dto.validedQtys(unit.qty())) return;

            try {
                if(unit.fba == null) {
                    Validation.addError("", String.format("#%s 没有相关的 FBA, 请创建 FBA.", unit.id));
                } else {
                    FBAShipment fba = unit.fba;
                    fba.dto = dtos.get(i);
                    fba.save();
                    fba.postFbaInboundCartonContents();
                }
            } catch(Exception e) {
                Validation.addError("", Webs.E(e));
            }
        }
    }


    static class MskuCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            ProcureUnit unit = (ProcureUnit) validatedObject;
            if(unit.selling == null) return false;
            String[] args = StringUtils.split(unit.selling.sellingId, Webs.S);
            if(args.length < 3) {
                setMessage("validation.msku", unit.selling.sellingId);
                return false;
            } else return true;
        }
    }

    static class SkuCheck extends Check {

        @Override
        public boolean isSatisfied(Object validatedObject, Object value) {
            ProcureUnit unit = (ProcureUnit) validatedObject;
            if(unit.product == null) return false;
            if(unit.selling == null) return false;
            String[] args = StringUtils.split(unit.selling.sellingId, Webs.S);
            if(!StringUtils.contains(args[0], unit.product.sku)) {
                setMessage("validation.sku", unit.product.sku);
                return false;
            } else return true;
        }
    }

    /**
     * 采购计划，修改，删除时，通知 采购计划的所有者, 运输相关人员, 采购相关人员
     */
    public Set<User> editToUsers() {
        Set<User> users = new HashSet<>();
        users.add(this.handler);
        if(this.deliveryment != null)
            users.add(this.deliveryment.handler);
        users.addAll(this.relateShipment().stream().filter(shipment -> shipment.creater != null)
                .map(shipment -> shipment.creater).collect(Collectors.toList()));
        return users;
    }

    /**
     * 指定文件夹，为当前采购计划所关联的 FBA 生成 箱內麦 与 箱外麦
     *
     * @param folder 指定PDF文件，生成的文件目录
     */
    public void fbaAsPDF(File folder, Long boxNumber) throws Exception {
        if(fba != null) {
            // PDF 文件名称 :[国家] [运输方式] [数量] [产品简称] 外/内麦
            String namePDF = String.format("[%s][%s][%s][%s][%s]",
                    this.selling.market.countryName(),
                    this.shipType.label(),
                    this.attrs.planQty,
                    this.product.abbreviation,
                    this.id
            );

            Map<String, Object> map = new HashMap<>();
            String shipmentid = fba.shipmentId;
            shipmentid = shipmentid.trim() + "U";

            map.put("shipmentId", shipmentid);
            map.put("shipFrom", Account.address(this.fba.account.type));
            map.put("fba", this.fba);
            map.put("procureUnit", this);
            map.put("boxNumber", boxNumber);
            if(this.shipType == Shipment.T.EXPRESS) {
                map.put("isexpress", "1");
            } else {
                map.put("isexpress", "0");
            }

            PDF.Options options = new PDF.Options();
            //只设置 width height    margin 为零
            options.pageSize = new org.allcolor.yahp.converter.IHtmlToPdfTransformer.PageSize(20.8d, 29.6d);

            //生成箱外卖 PDF
            PDFs.templateAsPDF(folder, namePDF + "外麦.pdf", "FBAs/boxLabel.html", options, map);
        } else {
            String message = "#" + this.id + "  " + this.sku + " 还没创建 FBA";
            FileUtils.writeStringToFile(new File(folder, message + ".txt"), message, "UTF-8");
        }
    }


    /**
     * 查看当前采购计划(对应的质检任务)的是否发货状态
     *
     * @return
     */
    public String isship() {
        if(this.haveTask()) {
            CheckTask task = this.taskList.get(0);
            if(task != null && task.isship != null && task.checkstat != CheckTask.StatType.UNCHECK) {
                return task.isship.label();
            } else if(task != null && task.isship == null && this.taskList.size() > 1) {
                CheckTask secondTask = this.taskList.get(1);
                if(secondTask.isship != null)
                    return secondTask.isship.label();
            }
        }
        return null;
    }

    /**
     * 查看当前采购计划(对应的质检任务)的是否合格状态
     *
     * @return
     */
    public String result() {
        CheckTask task = this.lastCheckedTask();
        if(task != null) return task.result.label();
        return null;
    }

    /**
     * 获取对应的质检任务查看链接
     * 1. 存在1个已检的质检任务质检信息，则查看最新质检任务的质检信息
     * 2. 存在1个以上的已检的质检任务，查看质检信息查看列表页面
     *
     * @return
     */
    public String fetchCheckTaskLink() {
        if(this.haveTask()) {
            List<CheckTask> tasks = (List) CollectionUtils.select(this.taskList, o -> {
                CheckTask task = (CheckTask) o;
                return task.checkstat != CheckTask.StatType.UNCHECK;
            });
            if(tasks != null) {
                if(tasks.size() == 1) {
                    return String.format("/checktasks/%s/show", tasks.get(0).id);
                } else if(tasks.size() > 1) {
                    return String.format("/checktasks/%s/showList", this.id);
                }
            }
        }
        return null;
    }

    public Integer fetchCheckTaskQcSample() {
        if(this.haveTask()) {
            CheckTask task = this.taskList.get(0);
            if(task != null && task.qcSample != null) {
                return task.qcSample;
            }
        }
        return 0;
    }

    public boolean haveTask() {
        return this.taskList != null && this.taskList.size() != 0;
    }

    public int returnPurchaseSample() {
        return this.purchaseSample == null ? 0 : this.purchaseSample;
    }

    /**
     * 将数字转换成对应的三位数的字符串
     * <p/>
     * 示例：1 => 001; 10 => 010
     *
     * @param number
     * @return
     */
    public String numberToStr(Long number) {
        int targetSize = 3;
        int size = number.toString().length();
        return StringUtils.repeat("0", (targetSize - size)) + number.toString();
    }


    public String dateDesc() {
        if(this.stage == ProcureUnit.STAGE.CLOSE) {
            return "";
        }
        List<Shipment> relateShipments = this.relateShipment();

        String datedesc = "";
        if(relateShipments.size() > 0) {
            Shipment shipment = relateShipments.get(0);
            if(this.inboundingQty() <= 0) {
                if(shipment.dates.oldPlanArrivDate != null && shipment.dates.planArrivDate != null) {
                    datedesc = "系统备注:运输单最新预计到库时间" + shipment.dates.planArrivDate
                            + "，比原预计到库日期" + shipment.dates.oldPlanArrivDate
                            + "差异" +
                            (shipment.dates.planArrivDate.getTime() - shipment.dates.oldPlanArrivDate.getTime()) /
                                    (24 * 60 * 60 * 1000)
                            + "天";
                }
            } else {
                if(this.attrs.planArrivDate != null && shipment.dates.planArrivDate != null && ((this.attrs
                        .planArrivDate.getTime() - shipment.dates
                        .planArrivDate.getTime()) != 0)) {
                    datedesc = "系统备注:采购计划单最新预计到库时间" + this.attrs.planArrivDate
                            + "，比原预计到库日期" + shipment.dates.planArrivDate
                            + "差异" + (this.attrs.planArrivDate.getTime() - shipment.dates.planArrivDate.getTime()) /
                            (24 * 60 * 60 * 1000)
                            + "天";
                }
            }

        }
        return datedesc;
    }

    public void startActiviti(String username) {
        ActivitiDefinition definition = ActivitiDefinition.find("menuCode=?", ACTIVITINAME).first();
        RuntimeService runtimeService = ActivitiEngine.processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(definition.processid);
        models.activiti.ActivitiProcess p = new models.activiti.ActivitiProcess();
        p.definition = definition;
        p.objectId = this.id;
        p.billId = this.selling.sellingId;
        p.objectUrl = "/procureunits/showactiviti/" + this.id;
        p.processDefinitionId = processInstance.getProcessDefinitionId();
        p.processInstanceId = processInstance.getProcessInstanceId();
        p.createAt = new Date();
        p.creator = username;
        p.save();
        ActivitiProcess.claimProcess(processInstance.getProcessInstanceId(), username);
        //设置下一步审批人
        taskclaim(processInstance.getProcessInstanceId(), username);
    }


    public void taskclaim(String processInstanceId, String username) {
        //启动流程后设置各节点的审批人
        TaskService taskService = ActivitiEngine.processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        for(Task task : tasks) {
            if(task != null) {
                if(task.getName().contains("运营专员")) {
                    taskService.setAssignee(task.getId(), this.handler.username);
                } else {
                    Role role = Role.find("roleName=?", task.getName()).first();
                    for(User user : role.users) {
                        taskService.setAssignee(task.getId(), user.username);
                        //taskService.claim(task.getId(), user.username);
                    }
                }
            }
        }
    }

    public Map<String, Object> showInfo(Long id, String username) {
        ActivitiProcess ap = ActivitiProcess.find("definition.menuCode=? and objectId=?", ACTIVITINAME, id).first();
        List<Map<String, String>> infos = new ArrayList<>();
        int issubmit = 0;
        String taskname = "";
        if(ap == null) {
            ap = new ActivitiProcess();
        } else {
            //判断是否有权限提交流程
            taskname = ActivitiProcess.privilegeProcess(ap.processInstanceId, username);
            if(StringUtils.isNotBlank(taskname)) {
                issubmit = 1;
            }
            //查找流程历史信息
            infos = ActivitiProcess.processInfo(ap.processInstanceId);
        }

        Map<String, Object> map = new Hashtable<>();
        map.put("ap", ap);
        map.put("issubmit", issubmit);
        if(taskname != null) map.put("taskname", taskname);
        map.put("infos", infos);
        return map;
    }

    public void submitActiviti(ActivitiProcess ap, String flow, String username, String opition) {
        Map<String, Object> variableMap = new HashMap<>();
        if(StringUtils.isNotBlank(flow)) variableMap.put("flow", flow);

        //如果是最后异步判断是否是生效日期当天
        TaskService taskService = ActivitiEngine.processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processInstanceId(ap.processInstanceId).active().singleResult();

        ActivitiProcess.submitProcess(ap.processInstanceId, username, variableMap, opition);
        //价格生效
        if(ActivitiProcess.isEnded(ap.processInstanceId)) {
            this.stage = STAGE.PLAN;
            this.save();
        }

        //设置下一步审批人
        taskclaim(ap.processInstanceId, ap.creator);
    }

    public void resetUnitByTerminalProcess() {
        ShipItem item = ShipItem.find("unit.id = ?", this.id).first();
        if(item != null) {
            item.delete();
        }
        this.delete();
    }

    public int fetchCheckTaskQty() {
        if(this.haveTask()) {
            CheckTask task = this.taskList.get(0);
            if(task != null) return task.qty;
        }
        return 0;
    }

    public String fetchShipItem(String type) {
        if(shipItems != null && shipItems.size() > 0) {
            ShipItem item = shipItems.get(0);
            if(type.equals("lossqty")) {
                return String.valueOf(item.lossqty);
            } else if(type.equals("recivedQty")) {
                return item.adjustQty.toString();
            }
            if(item.compenamt == null || item.compenamt.intValue() == 0) {
                return String.valueOf(0);
            }
            return item.currency.symbol() + " " + item.compenamt;
        } else {
            return String.valueOf(0);
        }
    }

    /**
     * 生成质检任务
     */
    public void triggerCheck() {
        if(this.isPersistent() && this.shipType != null && this.isCheck == 0 && this.selling != null) {
            new CheckTask(this).save();
            this.isCheck = 1;
            this.save();
        }
    }

    /**
     * 根据 运输方式+运输单中的运输商 去匹配对应的货代仓库
     *
     * @return
     */
    public Whouse matchWhouse() {
        Shipment.T shiptype = null;
        Cooperator cooperator = null;
        if(this.shipItems != null && !this.shipItems.isEmpty()) {
            Shipment shipment = this.shipItems.get(0).shipment;
            shiptype = shipment.type;
            cooperator = shipment.cooper;
        }
        return Whouse.findByCooperatorAndShipType(
                cooperator != null ? cooperator : (Cooperator) Cooperator.find("name LIKE '%欧嘉国际%'").first(),
                shiptype != null ? shiptype : this.shipType
        );
    }

    public CheckTask
    lastCheckedTask() {
        if(this.haveTask()) {
            CheckTask task = this.taskList.get(0);
            if(task != null && task.isship != null && task.checkstat != CheckTask.StatType.UNCHECK) {
                return task;
            }
        }
        return null;
    }

    public List<CheckTask> uncheckTaskList() {
        return (List) CollectionUtils.select(this.taskList, o -> {
            CheckTask task = (CheckTask) o;
            return task.checkstat == CheckTask.StatType.UNCHECK;
        });
    }

    /**
     * 更新相关的质检任务的仓库
     */
    public void flushTask() {
        if(this.haveTask()) {
            List<CheckTask> tasks = this.uncheckTaskList();
            if(tasks != null && !tasks.isEmpty()) {
                Whouse wh = this.matchWhouse();
                if(wh != null) {
                    for(CheckTask task : tasks) task.shipwhouse = wh;
                }
            }
        }
    }

    /**
     * 尝试找出对应的出库记录
     *
     * @return
     */
    public OutboundRecord outboundRecord() {
        return OutboundRecord.find("attributes LIKE ?", "%\"procureunitId\":" + this.id.toString() + "%").first();
    }

    /**
     * 出库信息
     *
     * @return
     */
    public String outboundMsg() {
        if(this.isOut == OST.Outbound) {
            OutboundRecord outboundRecord = this.outboundRecord();
            if(outboundRecord != null && outboundRecord.state == OutboundRecord.S.Outbound) {
                StringBuilder msg = new StringBuilder();
                msg.append(String.format("出库数量: %s, ", outboundRecord.qty));

                List<CheckTask> tasks = CheckTask.find("units_id=? and checkstat!=? ORDER BY creatat DESC",
                        this.id, CheckTask.StatType.REPEATCHECK).fetch();
                if(tasks != null && !tasks.isEmpty()) {
                    msg.append(String.format("箱数: %s, ", tasks.get(0).totalBoxNum()));
                } else {
                    msg.append("箱数: 未知, ");
                }
                msg.append(String.format("出库时间: %s",
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(outboundRecord.outboundDate)));
                return msg.toString();
            }
        }
        return "暂无出库信息.";
    }

    /**
     * @param pids
     * @return
     */
    public static String validateIsInbound(List<Long> pids, String type) {
        Map<STAGE, Integer> status_map = new HashMap<>();
        List<ProcureUnit> units = ProcureUnit.find("id IN " + SqlSelect.inlineParam(pids)).fetch();
        for(ProcureUnit unit : units) {
            if(type.equals("createMachiningInboundBtn")) {
                if(unit.stage != STAGE.PROCESSING) {
                    return "请选择阶段为【仓库加工】的采购计划！";
                }
                validInbound(unit);
                validRefund(unit);
            } else if(type.equals("createInboundBtn")) {
                if(!(unit.stage == STAGE.DELIVERY || unit.stage == STAGE.IN_STORAGE)) {
                    return "请选择阶段为【采购中】和【已入库】的采购计划";
                }
                validInbound(unit);
                validRefund(unit);
            } else if(type.equals("createOutboundBtn")) {
                if(unit.stage != STAGE.IN_STORAGE) {
                    return "请选择阶段为【已入库】的采购计划！";
                }
                if(unit.outbound != null) {
                    return "采购计划【" + unit.id + "】已经在出库单 【" + unit.outbound.id + "】中！";
                }
                validRefund(unit);
            } else if(type.equals("createRefundBtn")) {
                if(!(unit.stage == STAGE.DONE || unit.stage == STAGE.IN_STORAGE || unit.stage == STAGE.PROCESSING)) {
                    return "请选择阶段为【已交货】和【已入库】或【仓库加工】的采购计划";
                }

            }
        }
        return "";
    }

    public static String validInbound(ProcureUnit unit) {
        if(StringUtils.isNotEmpty(InboundUnit.isAllInbound(unit.id))) {
            return "采购计划【" + unit.id + "】已经存在收货入库单 【" + InboundUnit.isAllInbound(unit.id) + "】";
        }
        return "";
    }

    public static String validRefund(ProcureUnit unit) {
        if(StringUtils.isNotEmpty(Refund.isAllReufund(unit.id))) {
            return "采购计划【" + unit.id + "】正在走退货流程，请查证！ 【" + Refund.isAllReufund(unit.id) + "】";
        }
        return "";
    }


}
