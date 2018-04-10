package models.procure;

import com.alibaba.fastjson.JSON;
import com.amazonservices.mws.FulfillmentInboundShipment.FBAInboundServiceMWSException;
import com.google.gson.annotations.Expose;
import controllers.Login;
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
import models.qc.CheckTaskDTO;
import models.view.dto.CooperItemDTO;
import models.whouse.*;
import mws.FBA;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import play.Logger;
import play.data.validation.*;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.db.jpa.Model;
import play.modules.pdf.PDF;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    private static final long serialVersionUID = -4325210168069390776L;

    public ProcureUnit() {
    }

    public ProcureUnit(Selling selling) {
        if(selling != null) {
            this.selling = selling;
            this.product = this.selling.product;
        }
    }

    /**
     * Copy 一个全新的 ProcureUnit, 用来将部分交货的 ProcureUnit 分单交货
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
         * 深圳仓已入仓
         */
        IN_STORAGE {
            @Override
            public String label() {
                return "已入仓";
            }
        },
        OUTBOUND {
            @Override
            public String label() {
                return "已出仓";
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

    public static List<STAGE> procureStage() {
        return Arrays.asList(STAGE.PLAN, STAGE.DELIVERY, STAGE.DONE);
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

    public enum REVOKE {
        READY {
            @Override
            public String label() {
                return "准备撤销";
            }
        },
        CONFIRM {
            @Override
            public String label() {
                return "已撤销";
            }
        },
        CANCEL {
            @Override
            public String label() {
                return "取消撤销";
            }
        },
        NONE {
            @Override
            public String label() {
                return "";
            }
        };

        public abstract String label();
    }

    /**
     * 撤销出库状态
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public REVOKE revokeStatus;

    /**
     * 撤销出库原因
     */
    public String revokeMsg;

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
     * 采购单价是否含税，默认为否
     */
    public boolean containTax = false;

    /**
     * 是否采购取样
     */
    public boolean sample = false;

    /**
     * 税点
     * 单位 %
     */
    @Expose
    @Min(0)
    public Integer taxPoint = 0;

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

    /**
     * 统计报表用
     * 采购计划不进入 未请款金额
     */
    public boolean noPayment = false;

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

    /**
     * 一箱的数量
     */
    @Transient
    public Integer boxSize;

    public void setPeriod() {
        if(this.product.cooperators().size() > 0) {
            Long cid = this.cooperator.id;
            CooperItem cooperItem = CooperItem.find("cooperator.id=? AND sku=?", cid, this.sku).first();
            this.period = cooperItem.period;
            this.boxSize = cooperItem.boxSize;
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

    public enum T {
        ProcureSplit {
            @Override
            public String label() {
                return "采购分拆";
            }
        },
        StockSplit {
            @Override
            public String label() {
                return "库存分拆";
            }
        };

        public abstract String label();
    }

    /**
     * 分拆类型
     */
    @Enumerated(EnumType.STRING)
    public T type;

    /**
     * 质检结果
     */
    @Enumerated(EnumType.STRING)
    public InboundUnit.R result;

    /**
     * 初始采购数量
     */
    public int originQty;

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
     * 父节点ID
     */
    @ManyToOne
    public ProcureUnit parent;

    /**
     * 真实父节点ID
     */
    @ManyToOne
    public ProcureUnit realParent;

    /**
     * 出库单
     */
    @ManyToOne
    public Outbound outbound;

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
     * 分组编号
     * 打印出库单时用
     */
    @Transient
    public int groupNum;

    @Transient
    public CheckTaskDTO mainBox = new CheckTaskDTO();

    @Transient
    public CheckTaskDTO lastBox = new CheckTaskDTO();

    @Transient
    public boolean isb2b;

    @Transient
    public int currQty;

    /**
     * 差数是否退回
     */
    @Transient
    public boolean isReturn;

    public enum WS {
        SAME_DAY {
            @Override
            public String label() {
                return "当天发货";
            }
        },
        IN_SEVEN {
            @Override
            public String label() {
                return "7天内";
            }
        },
        TWO_WEEK {
            @Override
            public String label() {
                return "7-15天";
            }
        },
        IN_MONTH {
            @Override
            public String label() {
                return "一个月内";
            }
        },
        UP_MONTH {
            @Override
            public String label() {
                return "一个月以上";
            }
        };

        public abstract String label();
    }

    /**
     * 仓库周转天数
     */
    @Transient
    public int turnOverDay;

    @Transient
    public WS warehouseStage;

    /**
     * 用来标识采购计划是否需要计入正常库存(当前只会用于 Rockend 内的 InventoryCostsReport 报表)
     * <p>
     * 1. 由于历史原因部分采购计划需要挪市场(DE=>UK),但是此时采购计划已经不允许修改了,采购就再创建一条新的采购计划
     * 2. B2B 采购计划
     */
    public String isInventory;

    @PostLoad
    public void postPersist() {
        this.mainBox = JSON.parseObject(this.mainBoxInfo, CheckTaskDTO.class);
        this.lastBox = JSON.parseObject(this.lastBoxInfo, CheckTaskDTO.class);
    }

    public void marshalBoxs() {
        this.mainBoxInfo = J.json(this.mainBox);
        this.lastBoxInfo = J.json(this.lastBox);
    }

    public void marshalBoxs(ProcureUnit unit) {
        unit.mainBoxInfo = J.json(this.mainBox);
        unit.lastBoxInfo = J.json(this.lastBox);
    }

    /**
     * ProcureUnit 的检查
     */
    public void validate() {
        Validation.required("procureunit.selling", this.selling);
        if(this.selling != null) this.sid = this.selling.sellingId;
        Validation.required("procureunit.whouse", this.whouse);
        Validation.required("procureunit.handler", this.handler);
        Validation.required("procureunit.product", this.product);
        Validation.required("procureunit.cooperator", this.cooperator);
        if(this.product != null) this.sku = this.product.sku;
        Validation.required("procureunit.createDate", this.createDate);
        if(this.attrs != null) this.attrs.validate();
        if(this.selling != null && this.whouse != null && this.whouse.account != null
                && this.whouse.type == Whouse.T.FBA) {
            if(!this.selling.market.equals(this.whouse.market)) {
                Validation.addError("", "procureunit.validate.whouse");
            }
        }
        if(this.shipType != Shipment.T.EXPRESS) {
            Validation.required("procureunit.planShipDate", this.attrs.planShipDate);
            Validation.required("procureunit.planArrivDate", this.attrs.planArrivDate);
        }
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
    public ProcureUnit split(ProcureUnit unit, boolean type) {
        int planQty = this.attrs.planQty;
        if(unit.attrs.planQty != null) {
            if(unit.attrs.planQty > planQty)
                Validation.addError("", "因分批交货创建的采购计划的数量不可能大于原来采购计划的数量.");
            if(unit.attrs.planQty <= 0)
                Validation.addError("", "新创建分批交货的采购计划数量必须大于 0");
        }
        if(type && unit.selling == null)
            Validation.addError("", "分拆的子采购计划必须要有selling！");
        if(!this.isBeforeDONE())
            Validation.addError("", "已经交货或者成功运输, 不需要分拆采购计划.");
        if(this.unqualifiedQty > 0)
            Validation.addError("", "此采购计划存在不良品，请处理不良品后再进行分拆！");
        if(CooperItem.count("product.sku=? AND cooperator.id=?", unit.product.sku, this.cooperator.id) == 0)
            Validation.addError("", "该供应商下无此SKU产品，请确认！");
        ProcureUnit newUnit = new ProcureUnit();
        newUnit.cooperator = this.cooperator;
        newUnit.handler = Login.current();
        newUnit.deliveryment = this.deliveryment;
        newUnit.noPayment = this.noPayment;
        newUnit.whouse = unit.whouse;
        newUnit.sample = unit.sample;
        if(unit.sample) {
            newUnit.stage = STAGE.IN_STORAGE;
            newUnit.attrs.qty = unit.attrs.planQty;
            newUnit.inboundQty = unit.attrs.planQty;
        } else {
            newUnit.stage = STAGE.DELIVERY;
        }
        newUnit.planstage = PLANSTAGE.PLAN;
        newUnit.shipType = unit.shipType;
        newUnit.attrs.planQty = unit.attrs.planQty;
        newUnit.originQty = unit.attrs.planQty;
        newUnit.attrs.planDeliveryDate = unit.attrs.planDeliveryDate;
        newUnit.attrs.planShipDate = unit.attrs.planShipDate;
        newUnit.attrs.planArrivDate = unit.attrs.planArrivDate;
        newUnit.attrs.price = unit.attrs.price;
        newUnit.attrs.currency = unit.attrs.currency;
        newUnit.product = unit.product;
        newUnit.sku = unit.product.sku;
        newUnit.projectName = unit.projectName;
        newUnit.type = T.ProcureSplit;
        newUnit.containTax = unit.containTax;
        newUnit.taxPoint = unit.taxPoint;
        if(unit.selling != null) {
            newUnit.selling = unit.selling;
            newUnit.sid = unit.selling.sellingId;
        }
        newUnit.attrs.planQty = unit.attrs.planQty;
        newUnit.comment = unit.comment;
        if(type)
            newUnit.validate();
        List<Shipment> shipments = Shipment
                .similarShipments(newUnit.attrs.planShipDate, newUnit.whouse, newUnit.shipType);
        //无selling的手动单不做处理
        //快递不做判断
        if(unit.selling != null && !Arrays.asList(Shipment.T.EXPRESS, Shipment.T.DEDICATED).contains(newUnit.shipType)
                && shipments.size() <= 0)
            Validation.addError("", String.format("没有合适的运输单, 请联系运输部门, 创建 %s 之后去往 %s 的 %s 运输单.",
                    newUnit.attrs.planShipDate, newUnit.whouse.name, newUnit.shipType));
        if(Validation.hasErrors()) return newUnit;
        //无selling的手动单不做处理
        Shipment shipment = null;
        if(unit.selling != null && shipments.size() > 0) shipment = shipments.get(0);
        // 原采购计划数量变更
        this.attrs.planQty = planQty - newUnit.attrs.planQty;
        this.shipItemQty(this.attrs.planQty);
        // FBA 变更
        if(this.fba != null)
            this.fba.updateFBAShipment(null);
        this.save();

        newUnit.realParent = this;
        newUnit.parent = this.parent != null ? this.parent : this;
        // 分拆出的新采购计划变更
        newUnit.save();
        if(unit.selling != null && shipments.size() > 0) shipment.addToShip(newUnit);
        new ERecordBuilder("procureunit.split").msgArgs(this.id, planQty, newUnit.attrs.planQty, newUnit.id)
                .fid(this.id, ProcureUnit.class).save();
        return newUnit;
    }

    /**
     * 库存分拆
     *
     * @return
     */
    public ProcureUnit stockSplit(ProcureUnit unit, boolean type) {
        int available_qty = this.availableQty;
        if(unit.attrs.planQty != null) {
            if(unit.availableQty > available_qty)
                Validation.addError("", "因分批交货创建的采购计划的数量不可能大于原来采购计划的数量.");
            if(unit.availableQty <= 0)
                Validation.addError("", "新创建分批交货的采购计划数量必须大于 0");
        }
        if(type && unit.selling == null) {
            Validation.addError("", "分拆的子采购计划必须要有selling！");
        }
        if(CooperItem.count("product.sku=? AND cooperator.id=?", unit.product.sku, this.cooperator.id) == 0)
            Validation.addError("", "该供应商下无此SKU产品，请确认！");
        ProcureUnit newUnit = new ProcureUnit();
        newUnit.cooperator = this.cooperator;
        newUnit.handler = Login.current();
        newUnit.deliveryment = this.deliveryment;
        newUnit.deliverplan = this.deliverplan;
        newUnit.noPayment = this.noPayment;
        newUnit.whouse = unit.whouse;
        newUnit.stage = STAGE.IN_STORAGE;
        newUnit.planstage = PLANSTAGE.DONE;
        newUnit.shipType = unit.shipType;
        newUnit.originQty = unit.availableQty;
        newUnit.attrs.planQty = unit.availableQty;
        newUnit.attrs.qty = unit.availableQty;
        newUnit.inboundQty = unit.availableQty;
        newUnit.availableQty = unit.availableQty;
        newUnit.attrs.planDeliveryDate = unit.attrs.planDeliveryDate;
        newUnit.attrs.planShipDate = unit.attrs.planShipDate;
        newUnit.attrs.planArrivDate = unit.attrs.planArrivDate;
        newUnit.attrs.price = unit.attrs.price;
        newUnit.attrs.currency = unit.attrs.currency;
        newUnit.product = unit.product;
        newUnit.comment = unit.comment;
        newUnit.result = InboundUnit.R.Qualified;
        newUnit.attrs.deliveryDate = this.attrs.deliveryDate;
        newUnit.projectName = unit.projectName;
        newUnit.containTax = unit.containTax;
        newUnit.taxPoint = unit.taxPoint;
        if(unit.selling != null) {
            newUnit.selling = unit.selling;
            newUnit.sid = unit.selling.sellingId;
            newUnit.currWhouse = Whouse
                    .autoMatching(unit.shipType, Objects.equals(unit.projectName, User.COR.MengTop.name()) ? "B2B"
                            : unit.selling.market.shortHand(), unit.fba);
        } else {
            newUnit.currWhouse = Whouse.autoMatching(unit.shipType,
                    Objects.equals(unit.projectName, User.COR.MengTop.name()) ? "B2B" : "", unit.fba);
        }
        newUnit.type = T.StockSplit;
        newUnit.sku = unit.product.sku;
        /*库存分拆需要分析包装信息*/
        if(this.lastBox != null && this.mainBox != null && this.mainBox.num != 0
                && newUnit.availableQty % this.mainBox.num == 0) {
            newUnit.mainBox.boxNum = newUnit.availableQty / this.mainBox.num;
            newUnit.mainBox.num = this.mainBox.num;
            newUnit.mainBox.singleBoxWeight = this.mainBox.singleBoxWeight;
            newUnit.mainBox.length = this.mainBox.length;
            newUnit.mainBox.width = this.mainBox.width;
            newUnit.mainBox.height = this.mainBox.height;
            this.mainBox.boxNum -= newUnit.mainBox.boxNum;
        } else if(this.lastBox != null && this.mainBox != null && this.lastBox.boxNum > 0
                && newUnit.availableQty % this.mainBox.num <= this.lastBox.num) {
            newUnit.mainBox.boxNum = (int) Math.floor(newUnit.availableQty / this.mainBox.num);
            newUnit.mainBox.num = newUnit.mainBox.boxNum == 0 ? 0 : this.mainBox.num;
            newUnit.mainBox.singleBoxWeight = this.mainBox.singleBoxWeight;
            newUnit.mainBox.length = this.mainBox.length;
            newUnit.mainBox.width = this.mainBox.width;
            newUnit.mainBox.height = this.mainBox.height;
            newUnit.lastBox.boxNum = this.lastBox.boxNum;
            newUnit.lastBox.num = newUnit.availableQty % this.mainBox.num;
            newUnit.lastBox.length = this.lastBox.length;
            newUnit.lastBox.width = this.lastBox.width;
            newUnit.lastBox.height = this.lastBox.height;
            this.mainBox.boxNum -= newUnit.mainBox.boxNum;
            this.lastBox.num -= newUnit.lastBox.num;
            this.lastBox.boxNum = this.lastBox.num == 0 ? 0 : 1;
        }
        newUnit.marshalBoxs();
        this.marshalBoxs();
        List<Shipment> shipments = Shipment.similarShipments(newUnit.attrs.planShipDate, newUnit.whouse,
                newUnit.shipType);
        //无selling的手动单不做处理
        //快递不做判断
        if(unit.selling != null && !Arrays.asList(Shipment.T.EXPRESS, Shipment.T.DEDICATED).contains(newUnit.shipType)
                && shipments.size() <= 0)
            Validation.addError("", String.format("没有合适的运输单, 请联系运输部门, 创建 %s 之后去往 %s 的 %s 运输单.",
                    newUnit.attrs.planShipDate, newUnit.whouse.name, newUnit.shipType));
        if(Validation.hasErrors()) return newUnit;
        //无selling的手动单不做处理
        Shipment shipment = null;
        if(unit.selling != null && shipments.size() > 0) shipment = shipments.get(0);
        this.availableQty = available_qty - newUnit.availableQty;
        this.shipItemQty(this.availableQty);
        // FBA 变更
        if(this.fba != null)
            this.fba.updateFBAShipment(null);
        // 原采购计划数量变更
        this.save();
        newUnit.parent = this.parent != null ? this.parent : this;
        newUnit.realParent = this;
        // 分拆出的新采购计划变更
        newUnit.save();
        if(unit.selling != null && shipments.size() > 0) shipment.addToShip(newUnit);
        new ERecordBuilder("procureunit.split").msgArgs(this.id, available_qty, newUnit.attrs.planQty, newUnit.id)
                .fid(this.id, ProcureUnit.class).save();
        this.createStockRecord(newUnit, newUnit.availableQty, StockRecord.T.Split, newUnit.availableQty,
                this.availableQty);
        return newUnit;
    }

    /**
     * 创建异动记录
     *
     * @param unit
     * @param qty
     * @param type
     */
    public void createStockRecord(ProcureUnit unit, int qty, StockRecord.T type, int childCurrQty, int parentCurrQty) {
        StockRecord record = new StockRecord();
        record.creator = Login.current();
        record.whouse = unit.currWhouse;
        record.unit = unit;
        record.qty = qty;
        record.currQty = childCurrQty;
        record.type = type;
        record.recordId = unit.id;
        record.save();
        if(unit.parent != null) {
            StockRecord parent_stock = new StockRecord();
            parent_stock.whouse = unit.parent.currWhouse;
            parent_stock.unit = unit.parent;
            parent_stock.qty = 0 - qty;
            parent_stock.type = type;
            parent_stock.recordId = unit.parent.id;
            parent_stock.creator = Login.current();
            parent_stock.currQty = parentCurrQty;
            parent_stock.save();
        }
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
        this.attrs.qty = 0;
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
        /*
         * 1. 修改不同阶段可以修改的信息
         * 2. 根据运输类型修改运输单
         */
        //采购计划的FBA已存在后再次编辑该采购计划的 运输方式 或采购数量 时对修改原因做不为空校验
        if(this.fba != null
                && (unit.shipType != this.shipType || !Objects.equals(unit.attrs.planQty, this.attrs.planQty)))
            Validation.required("procureunit.update.reason", reason);

        if(unit.cooperator == null) Validation.addError("", "供应商不能为空!");
        if(this.realParent != null && unit.isReturn
                && Arrays.asList(STAGE.APPROVE, STAGE.PLAN, STAGE.DELIVERY).contains(this.stage)) {
            if(unit.attrs.planQty - this.attrs.planQty > this.realParent.attrs.planQty) {
                Validation.addError("", "修改值" + unit.attrs.planQty + " 过大，请重新填写数量！");
            }
        }
        if(this.parent != null && unit.isReturn && this.stage == STAGE.IN_STORAGE) {
            if(unit.attrs.planQty - this.attrs.planQty > this.parent.attrs.planQty) {
                Validation.addError("", "修改值" + unit.attrs.planQty + " 过大，请重新填写数量！");
            }
        }
        if(StringUtils.isNotEmpty(unit.selling.sellingId) && StringUtils.isEmpty(shipmentId) && unit.stage != STAGE.DONE
                && !Arrays.asList(Shipment.T.EXPRESS, Shipment.T.DEDICATED).contains(unit.shipType)) {
            Validation.addError("", "请选择运输单！");
        }
        if(StringUtils.isNotEmpty(shipmentId)) {
            Shipment shipment = Shipment.findById(shipmentId);
            if(this.stage != STAGE.DONE && !shipment.type.name().equals(unit.shipType.name())) {
                Validation.addError("", "运输单的运输方式与采购计划的运输方式不符，请重新选择！");
            }
        }
        if(Validation.hasErrors()) return;
        if(this.stage == STAGE.CLOSE) {
            this.comment = unit.comment;
            this.save();
            return;
        }
        List<String> logs = new ArrayList<>();
        if(Arrays.asList(STAGE.APPROVE, STAGE.PLAN, STAGE.DELIVERY).contains(this.stage)) {
            if(this.realParent != null && unit.isReturn) {
                ProcureUnit it = this.realParent != null ? ProcureUnit.findById(this.realParent.id)
                        : ProcureUnit.findById(this.parent.id);
                if(Arrays.asList(STAGE.APPROVE, STAGE.PLAN, STAGE.DELIVERY).contains(it.stage)) {
                    it.attrs.planQty += (this.attrs.planQty - unit.attrs.planQty);
                    it.save();
                }
            }
            logs.addAll(this.beforeDoneUpdate(unit));
            if(unit.selling != null) {
                this.sid = unit.selling.sellingId;
            }
        } else if(this.stage == STAGE.DONE) {
            logs.addAll(this.doneUpdate(unit));
        }
        if(Validation.hasErrors()) return;
        if(Arrays.asList(STAGE.APPROVE, STAGE.PLAN, STAGE.DELIVERY, STAGE.DONE).contains(this.stage)) {
            this.changeShipItemShipment(StringUtils.isBlank(shipmentId) ? null : Shipment.findById(shipmentId),
                    oldShipType);
        }

        this.projectName = unit.projectName;
        this.comment = unit.comment;
        this.purchaseSample = unit.purchaseSample;
        this.taxPoint = unit.taxPoint;
        this.containTax = unit.containTax;
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
     * 库存修改  和采购修改分开
     *
     * @param unit
     * @param shipmentId
     * @param reason
     */
    public void stockUpdate(ProcureUnit unit, String shipmentId, String reason) {
        Shipment.T oldShipType = this.shipType;
        /*
         * 1. 修改不同阶段可以修改的信息
         * 2. 根据运输类型修改运输单
         */
        if(this.fba != null && (unit.shipType != this.shipType || unit.availableQty != this.availableQty))
            Validation.required("procureunit.update.reason", reason);
        if(unit.cooperator == null) Validation.addError("", "供应商不能为空!");
        if(this.realParent != null && unit.isReturn && STAGE.IN_STORAGE == this.stage) {
            if(unit.availableQty - this.availableQty > this.realParent.availableQty) {
                Validation.addError("", "修改值过大，请重新填写数量！");
            }
        }
        if(StringUtils.isNotEmpty(unit.selling.sellingId) && StringUtils.isEmpty(shipmentId)
                && !Arrays.asList(Shipment.T.EXPRESS, Shipment.T.DEDICATED).contains(unit.shipType)) {
            Validation.addError("", "请选择运输单！");
        }
        if(StringUtils.isNotEmpty(shipmentId)) {
            Shipment shipment = Shipment.findById(shipmentId);
            if(!shipment.type.name().equals(unit.shipType.name())) {
                Validation.addError("", "运输单的运输方式与采购计划的运输方式不符，请重新选择！");
            }
        }
        if(Validation.hasErrors()) return;
        List<String> logs = new ArrayList<>();
        this.comment = unit.comment;
        this.autoUpdateComment(unit);
        this.purchaseSample = unit.purchaseSample;
        this.projectName = unit.projectName;
        if(Validation.hasErrors()) return;
        //仓库加工修改
        int parentCurrQty = 0;
        if(this.parent != null && unit.isReturn) {
            ProcureUnit it = this.realParent != null ? ProcureUnit.findById(this.realParent.id)
                    : ProcureUnit.findById(this.parent.id);
            if(it.stage == STAGE.IN_STORAGE) {
                it.availableQty += (this.availableQty - unit.availableQty);
                it.save();
            }
            parentCurrQty = it.availableQty;
        }
        logs.addAll(this.changeStageUpdate(unit));
        if(STAGE.IN_STORAGE == this.stage) {
            this.changeShipItemShipment(StringUtils.isBlank(shipmentId) ? null : Shipment.findById(shipmentId),
                    oldShipType);
        }
        if(logs.size() > 0)
            this.stage = STAGE.IN_STORAGE;
        int diffQty = this.availableQty - unit.availableQty;
        logs.addAll(this.afterDoneUpdate(unit));
        if(this.parent != null && this.type == T.StockSplit) {
            this.originQty = this.availableQty;
            this.attrs.planQty = this.availableQty;
            this.attrs.qty = this.availableQty;
            this.inboundQty = this.availableQty;
        }
        String shortHand = "";
        if(unit.selling != null) {
            this.sid = unit.selling.sellingId;
            shortHand = unit.selling.market.shortHand();
        }
        this.currWhouse = Whouse
                .autoMatching(unit.shipType, this.projectName.equals("B2B") ? "B2B" : shortHand, this.fba);
        if(logs.size() > 0) {
            new ERecordBuilder("procureunit.deepUpdate").msgArgs(reason, this.id, StringUtils.join(logs, "<br>"),
                    this.generateProcureUnitStatusInfo()).fid(this.id, ProcureUnit.class).save();
            noty(this.sku, StringUtils.join(logs, ","));
        }
        this.shipItemQty(this.qty());
        this.save();
        if(diffQty != 0) {
            this.createStockRecord(this, -diffQty, StockRecord.T.Split_Stock, this.availableQty, parentCurrQty);
        }
    }

    /**
     * 去往仓库和运输方式变更日志添加到comment
     *
     * @param unit
     */
    public void autoUpdateComment(ProcureUnit unit) {
        StringBuilder log = new StringBuilder();
        if(this.whouse != unit.whouse) {
            log.append(" 修改去往国家：").append(this.whouse == null ? "空" : this.whouse.country)
                    .append(" => ").append(unit.whouse == null ? "空" : unit.whouse.country).append("; ");
        }
        if(this.shipType != unit.shipType) {
            log.append(" 修改运输方式：").append(this.shipType == null ? "空" : this.shipType.label())
                    .append(" => ").append(unit.shipType == null ? "空" : unit.shipType.label()).append("; ");
        }
        if(log.length() > 0) {
            log.insert(0, LocalDate.now());
        }
        this.comment += log.toString();
    }

    /**
     * 修改手动单数据
     */
    public void updateManualData(ProcureUnit unit, int diff) {
        if((this.parent != null || this.realParent != null) && Arrays.asList(STAGE.APPROVE, STAGE.PLAN, STAGE.DELIVERY)
                .contains(this.stage)) {
            if(unit.attrs.planQty - this.attrs.planQty > this.realParent.attrs.planQty) {
                Validation.addError("", "修改值" + unit.attrs.planQty + " 过大，请重新填写数量！");
            }
        }
        if(this.parent != null && this.stage == STAGE.IN_STORAGE) {
            if(unit.availableQty - this.availableQty > this.parent.availableQty) {
                Validation.addError("", "修改值过大，请重新填写数量！");
            }
        }
        if(Validation.hasErrors()) return;
        this.attrs.price = unit.attrs.price;
        this.attrs.currency = unit.attrs.currency;
        this.attrs.planDeliveryDate = unit.attrs.planDeliveryDate;
        this.purchaseSample = unit.purchaseSample;
        this.projectName = unit.projectName;
        this.taxPoint = unit.taxPoint;
        this.containTax = unit.containTax;
        if(this.stage.name().equals("IN_STORAGE")) {
            if(diff != 0) {
                this.availableQty = unit.availableQty;
                this.originQty = this.availableQty;
                this.attrs.planQty = this.availableQty;
                this.attrs.qty = this.availableQty;
                this.inboundQty = this.availableQty;
                if(this.parent != null) {
                    this.parent.availableQty += diff;
                    this.parent.save();
                }
                this.createStockRecord(this, -diff, StockRecord.T.Split_Stock, this.availableQty,
                        this.parent.availableQty);
            }
            this.currWhouse = Whouse.autoMatching(this.shipType, this.projectName.equals("B2B") ? "B2B" : "", this.fba);
        } else if(diff != 0) {
            this.attrs.planQty = unit.attrs.planQty;
            if(this.realParent != null) {
                this.realParent.attrs.planQty += diff;
                this.realParent.originQty += diff;
                this.realParent.save();
            }
        }
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
        logs.addAll(Reflects.logFieldFade(this, "availableQty", unit.availableQty));
        logs.addAll(Reflects.logFieldFade(this, "shipType", unit.shipType));
        logs.addAll(Reflects.logFieldFade(this, "whouse", unit.whouse));
        logs.addAll(Reflects.logFieldFade(this, "selling", unit.selling));
        logs.addAll(Reflects.logFieldFade(this, "cooperator", unit.cooperator));
        return logs;
    }

    private List<String> doneUpdate(ProcureUnit unit) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "attrs.planShipDate", unit.attrs.planShipDate));
        logs.addAll(Reflects.logFieldFade(this, "attrs.planArrivDate", unit.attrs.planArrivDate));
        return logs;
    }

    private List<String> afterDoneUpdate(ProcureUnit unit) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "attrs.planShipDate", unit.attrs.planShipDate));
        logs.addAll(Reflects.logFieldFade(this, "attrs.planArrivDate", unit.attrs.planArrivDate));
        logs.addAll(Reflects.logFieldFade(this, "availableQty", unit.availableQty));
        logs.addAll(Reflects.logFieldFade(this, "selling", unit.selling));
        return logs;
    }

    private List<String> changeStageUpdate(ProcureUnit unit) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "whouse", unit.whouse));
        logs.addAll(Reflects.logFieldFade(this, "shipType", unit.shipType));
        logs.addAll(Reflects.logFieldFade(this, "currWhouse", unit.currWhouse));
        return logs;
    }

    /**
     * 生成当前采购计划的状态信息(是否抵达货代、FBA 信息、付款信息)
     *
     * @return String
     */
    public String generateProcureUnitStatusInfo() {
        String msg = "";
        PaymentUnit pre_pay = this.fetchPrePay();
        PaymentUnit tail_pay = this.fetchTailPay();
        if(pre_pay != null) {
            if(pre_pay.state == PaymentUnit.S.APPLY) msg += "已申请预付款";
            if(pre_pay.state == PaymentUnit.S.PAID) msg += "已付预付款";
        }
        if(tail_pay != null) {
            if(tail_pay.state == PaymentUnit.S.APPLY) msg += " 已申请尾款";
            if(tail_pay.state == PaymentUnit.S.PAID) msg += " 已付尾款";
        }
        return String.format("抵达货代: %s, FBA: %s, 付款信息: %s", this.isPlaced,
                this.fba != null ? this.fba.shipmentId : "无", StringUtils.isBlank(msg) ? "无" : msg);
    }

    /**
     * 调整采购计划所产生的运输项目的运输单
     *
     * @param oldShipType
     * @param shipment
     */
    private void changeShipItemShipment(Shipment shipment, Shipment.T oldShipType) {
        if(this.revokeStatus != null && !Arrays.asList(REVOKE.CONFIRM, REVOKE.NONE).contains(this.revokeStatus)) {
            Validation.addError("", "当前采购计划正在等待物流确认撤销出库...");
            return;
        }
        if(this.shipItems.stream().anyMatch(item -> item.shipment != null && item.shipment.state != Shipment.S.PLAN
                && this.revokeStatus != REVOKE.CONFIRM)) {
            Validation.addError("", String.format("当前采购计划已经存在运输单, 且该运输单不是 %s 状态.", Shipment.S.PLAN.label()));
            return;
        }
        if(shipment != null && shipment.state != Shipment.S.PLAN) {
            Validation.addError("", String.format("需要关联的运输单 %s 为 %s 状态, 只有 %s 状态的运输单才可调整.",
                    shipment.id, shipment.state.label(), Shipment.S.PLAN.label()));
            return;
        }

        if(shipment == null) {
            // 1. 调整为快递运输单, 已经拥有的运输项目全部删除, 重新设计.
            // 2. 用户更改了运输方式但未选择运输单
            if(Arrays.asList(Shipment.T.EXPRESS, Shipment.T.DEDICATED).contains(this.shipType)
                    || oldShipType != this.shipType) {
                this.shipItems.forEach(GenericModel::delete);
            }
            this.changeOutbound(null);
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
            this.changeOutbound(shipment.out);
        }
    }

    /**
     * 修改采购计划后更换出库单
     *
     * @param out
     */
    private void changeOutbound(Outbound out) {
        if(!(this.outbound != null && this.outbound.status == Outbound.S.Outbound)) {
            Optional<Outbound> optional = Optional.ofNullable(out);
            optional.ifPresent(value -> this.outbound = (value.status != Outbound.S.Outbound ? out : this.outbound));
        }
    }

    /**
     * 通过 ProcureUnit 创建 FBA
     */
    public synchronized FBAShipment postFbaShipment(CheckTaskDTO dto) {
        this.postFBAValidate(dto);
        if(Validation.hasErrors()) return null;
        FBAShipment fbaShipment = this.planFBA();
        this.confirmFBA(fbaShipment);
        this.submitFBACartonContent(dto);
        return fbaShipment;
    }

    /**
     * 1. 创建 FBAInboundShipmentPlan
     *
     * @return
     */
    public FBAShipment planFBA() {
        try {
            FBAShipment fbaShipment = FBA.plan(this.selling.account, this);
            return fbaShipment.save();
        } catch(FBAInboundServiceMWSException e) {
            FBA.FBA_ERROR_TYPE errorType = FBA.fbaErrorFormat(e);
            Validation.addError("", String.format("向 Amazon 创建 Shipment PLAN 失败, %s", errorType.message()));
            return null;
        }
    }

    /**
     * 2. 确认 FBAInboundShipmentPlan
     *
     * @param fba
     * @return
     */
    public FBAShipment confirmFBA(FBAShipment fba) {
        if(fba == null) return fba;
        try {
            fba.state = FBA.create(fba, Collections.singletonList(this));
            this.fba = fba.save();
            this.save();
            new ERecordBuilder("shipment.createFBA").msgArgs(this.id, this.sku, this.fba.shipmentId).fid(this.id).save();
        } catch(FBAInboundServiceMWSException e) {
            Logger.error(Webs.s(e));
            Validation.addError("", "向 Amazon 创建 Shipment 错误 " + Webs.e(e));
        }
        return fba;
    }

    /**
     * 3. 提交 FBAInboundCartonContent 包装信息
     */
    public void submitFBACartonContent(CheckTaskDTO dto) {
        if(this.fba != null && dto != null) {
            this.fba.dto = dto;
            this.fba.save();
            this.fba.submitFbaInboundCartonContentsFeed();
        }
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
            this.originQty = this.attrs.planQty;
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
        /*
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
            this.fees.forEach(PaymentUnit::permanentRemove);
            // 删除 FBA
            FBAShipment fbaShipment = this.fba;
            if(fbaShipment != null) {
                fbaShipment.units.remove(this);
                fbaShipment.removeFBAShipment();
            }
            // 删除运输相关
            this.shipItems.forEach(ShipItem::delete);

            //删除采购单关联
            Deliveryment d = this.deliveryment;
            if(d != null && d.units != null) {
                d.units.remove(this);
            }
            //删除出货单关联
            DeliverPlan deliverPlan = this.deliverplan;
            if(deliverPlan != null && deliverPlan.units != null) {
                deliverPlan.units.remove(this);
            }
            this.delete();
        } else {
            Validation.addError("", String.format("只允许 %s, %s 状态的采购计划进行取消", STAGE.PLAN, STAGE.DELIVERY));
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

    /**
     * 优先返回可用库存作为第一参考值
     *
     * @return
     */
    public int qty() {
        if(this.attrs.qty != null && this.attrs.qty != 0) return this.attrs.qty;
        return this.attrs.planQty;
    }

    public int qtyForFba() {
        if(this.availableQty != 0) return this.availableQty;
        if(this.attrs.qty != null && this.attrs.qty != 0) return this.attrs.qty;
        return this.attrs.planQty;
    }

    public int realQty() {
        int qty = this.qty();
        if(purchaseSample != null) qty -= purchaseSample;
        return qty;
    }

    public int shipmentQty() {
        if(this.stage == STAGE.IN_STORAGE) {
            return availableQty;
        } else if(Arrays.asList("OUTBOUND", "SHIPPING", "SHIP_OVER", "INBOUND", "CLOSE").contains(this.stage.name())) {
            return outQty;
        } else {
            return qty();
        }
    }

    public int paidQty() {
        if(Arrays.asList("IN_STORAGE", "OUTBOUND", "SHIPPING", "SHIP_OVER", "INBOUND", "CLOSE")
                .contains(this.stage.name()))
            return inboundQty;
        else
            return this.qty();
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
     * 丢失量
     *
     * @return
     */
    public int lostQty() {
        return this.shipItems.stream().mapToInt(item -> item.lossqty).sum();
    }

    public String showCompensation() {
        String temp = "";
        if(this.shipItems.size() > 0) {
            temp = this.shipItems.get(0).currency.symbol() + this.shipItems.get(0).compenamt.toString();
        }
        return temp;
    }

    /**
     * 预付款申请
     */
    public PaymentUnit billingPrePay() {
        /*
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
        float pre = this.cooperator.first == 0 ? (float) 0.3 : (float) this.cooperator.first / 100;
        fee.amount = new BigDecimal(fee.amount).multiply(new BigDecimal(pre)).setScale(2, 4).floatValue();
        fee.save();
        //2018-02-05 要求请款操作修改请款单的 updateAt
        this.deliveryment.apply.updateAt = new Date();
        this.deliveryment.apply.save();
        new ERecordBuilder("procureunit.prepay")
                .msgArgs(this.id, String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id, ProcureUnit.class).save();
        return fee;
    }

    /**
     * 申请中期付款
     *
     * @return
     */
    public PaymentUnit billingMediumPay() {
        if(this.hasSecondPay())
            Validation.addError("", "不允许重复申请中期付款.");
        if(this.hasTailPay())
            Validation.addError("", "已经申请了尾款, 不需要再申请预付款.");
        if(Validation.hasErrors()) return null;
        PaymentUnit fee = new PaymentUnit(this);
        fee.feeType = FeeType.mediumPayment();
        float second = (float) this.cooperator.second / 100;
        fee.amount = new BigDecimal(fee.amount).multiply(new BigDecimal(second)).setScale(2, 4).floatValue();
        if(fee.amount + this.appliedAmount() >= this.totalAmount()) {
            Validation.addError("", "中期请款已经超过采购计划总额，请验证或者直接申请尾款！");
            return null;
        }
        fee.save();
        //2018-02-05 要求请款操作修改请款单的 updateAt
        this.deliveryment.apply.updateAt = new Date();
        this.deliveryment.apply.save();
        new ERecordBuilder("procureunit.mediumpay")
                .msgArgs(this.id, String.format("%s %s", fee.currency.symbol(), fee.amount))
                .fid(this.id, ProcureUnit.class).save();
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
        //2018-02-05 要求请款操作修改请款单的 updateAt
        this.deliveryment.apply.updateAt = new Date();
        this.deliveryment.apply.save();
        new ERecordBuilder("procureunit.tailpay")
                .msgArgs(this.product.sku, String.format("%s %s", fee.currency.symbol(), fee.amount))
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
        if(this.type == T.StockSplit)
            return 0;
        return totalAmount() - appliedAmount();
    }

    /**
     * 已经申请的金额
     *
     * @return
     */
    public float appliedAmount() {
        if(Objects.equals(T.StockSplit, this.type)) return 0f;
        float appliedAmount = 0;
        float pre = this.cooperator.first == 0 ? (float) 0.3 : (float) this.cooperator.first / 100;
        float second = this.cooperator.second / 100;
        if(this.parent == null) {
            if(this.ifParent()) {
                if(this.hasPrePay())
                    appliedAmount += this.attrs.price * this.paidQty() * pre;
                if(this.hasSecondPay())
                    appliedAmount += this.attrs.price * this.paidQty() * second;
                if(this.hasTailPay())
                    appliedAmount = this.attrs.price * this.paidQty();
            } else {
                for(PaymentUnit fee : this.fees()) {
                    appliedAmount += fee.amount();
                }
            }
        } else {
            ProcureUnit parent_unit = this.parent;
            /*如果父采购计划没有请款,则按照正常请款**/
            if(parent_unit.hasPrePay() && !parent_unit.hasEqualWithPrePay() && !this.hasPrePay()) {
                appliedAmount += this.attrs.price * this.paidQty() * pre;
            }
            for(PaymentUnit fee : this.fees()) {
                appliedAmount += fee.amount();
            }
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
        return new BigDecimal(this.attrs.price.toString())
                .multiply(new BigDecimal(this.paidQty()))
                .setScale(2, 4)
                .floatValue();
    }

    public float totalAmountToCNY() {
        return this.attrs.currency.toCNY(new BigDecimal(this.attrs.price.toString())
                .multiply(new BigDecimal(this.paidQty())).setScale(2, 4).floatValue());
    }

    /**
     * 是否拥有了 预付款
     *
     * @return
     */
    public boolean hasPrePay() {
        return this.fees().stream().anyMatch(fee -> fee.feeType == FeeType.cashpledge());
    }

    public boolean hasEqualWithPrePay() {
        float pre = this.cooperator.first == 0 ? (float) 0.3 : (float) this.cooperator.first / 100;
        double total = this.fees().stream().filter(fee -> fee.feeType == FeeType.cashpledge())
                .mapToDouble(PaymentUnit::amount).sum();
        return new BigDecimal(this.attrs.price * this.paidQty() * pre).compareTo(new BigDecimal(total)) == 0;
    }

    /**
     * 是否拥有 中期付款
     *
     * @return
     */
    public boolean hasSecondPay() {
        return this.fees().stream().anyMatch(fee -> fee.feeType == FeeType.mediumPayment());
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
        return units != null && units.size() > 0;
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
        List<PaymentUnit> units = new ArrayList<>();
        for(PaymentUnit fee : this.fees) {
            if(fee.remove) continue;
            units.add(fee);
        }
        return units;
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
     * 包装信息总重量 由仓库部门填写
     *
     * @return
     */
    public double totalBoxWeight() {
        double mainWeight = 0, lastWeight = 0;
        if(this.mainBox != null)
            mainWeight = this.mainBox.singleBoxWeight * this.mainBox.boxNum;
        if(this.lastBox != null)
            lastWeight = this.lastBox.singleBoxWeight * this.lastBox.boxNum;
        return mainWeight + lastWeight;
    }

    /**
     * 获取当前采购计划重量
     * 如果包装信息没填，则取sku重量
     *
     * @return
     */
    public double getRecentlyWeight() {
        double mainWeight, lastWeight = 0;
        if(this.mainBox != null) {
            mainWeight = this.mainBox.singleBoxWeight * this.mainBox.boxNum;
        } else {
            mainWeight = this.product.getRecentlyWeight();
        }
        if(this.lastBox != null)
            lastWeight = this.lastBox.singleBoxWeight * this.lastBox.boxNum;
        return mainWeight + lastWeight;
    }

    public double totalBoxVolume() {
        double mainVolume = 0, lastVolume = 0;
        if(this.mainBox != null)
            mainVolume = this.mainBox.totalVolume();
        if(this.lastBox != null)
            lastVolume = this.lastBox.totalVolume();
        return mainVolume + lastVolume;
    }

    public int totalBoxNum() {
        int total = 0;
        if(this.mainBox != null)
            total += this.mainBox.boxNum;
        if(this.lastBox != null)
            total += this.lastBox.boxNum;
        return total;
    }

    /**
     * 转换成记录日志的格式
     *
     * @return
     */
    @Override
    public String toLog() {
        return String.format("[sid:%s] [仓库:%s] [供应商:%s] [计划数量:%s] [预计到库:%s] [运输方式:%s]",
                this.sid, this.whouse.name(), this.cooperator.fullName, this.attrs.planQty,
                Dates.date2Date(this.attrs.planArrivDate), this.shipType);
    }

    public List<ElcukRecord> records() {
        return ElcukRecord.records(this.id + "",
                Arrays.asList("procureunit.save", "procureunit.update", "procureunit.remove", "procureunit.delivery",
                        "procureunit.revertdelivery", "procureunit.split", "procureunit.prepay", "procureunit.tailpay",
                        "procureunit.adjuststock", "refund.confirm", "refund.transfer"), 50);
    }

    /**
     * 页面上用来缓存 records 的 key
     *
     * @return
     */
    public String recordsPageCacheKey() {
        return Webs.md5(ElcukRecord.pageCacheKey(ProcureUnit.class, this.id));
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
        int res = super.hashCode();
        res = 31 * res + (id != null ? id.hashCode() : 0);
        return res;
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
     */
    public static void postFbaShipments(final List<Long> unitIds, final List<CheckTaskDTO> dtos) {
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
                } else if(unit.selling == null) {
                    Validation.addError("", String.format("#%s 没有 Selling 无法创建 FBA", unit.id));
                } else {
                    unit.postFbaShipment(dtos.get(i));
                    if(unit.stage == STAGE.IN_STORAGE) {
                        unit.currWhouse = Whouse.autoMatching(unit.shipType, unit.selling.market.shortHand(), unit.fba);
                        unit.save();
                    }
                }
            } catch(Exception e) {
                Logger.error(Webs.s(e));
                Validation.addError("", "向 Amazon 创建 Shipment 因 " + Webs.e(e) + " 原因失败.");
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
            if(!dto.validedQtys(unit.qtyForFba())) return;

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
                Validation.addError("", Webs.e(e));
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
            options.pageSize = new IHtmlToPdfTransformer.PageSize(20.8d, 29.6d);

            //生成箱外卖 PDF
            String path = Objects.equals(this.projectName, User.COR.MengTop.name())
                    ? "FBAs/b2bBoxLabel.html" : "FBAs/boxLabel.html";
            PDFs.templateAsPDF(folder, namePDF + "外麦.pdf", path, options, map);
        } else if(Objects.equals(this.projectName, User.COR.MengTop.name())) {
            String namePDF = String
                    .format("MengTop_[%s][%s][%s]", this.attrs.planQty, this.product.abbreviation, this.id);
            Map<String, Object> map = new HashMap<>();
            map.put("procureUnit", this);
            map.put("boxNumber", boxNumber);
            PDF.Options options = new PDF.Options();
            //只设置 width height    margin 为零
            options.pageSize = new IHtmlToPdfTransformer.PageSize(20.8d, 29.6d);
            //生成箱外卖 PDF
            String path = "FBAs/b2bBoxLabel.html";
            PDFs.templateAsPDF(folder, namePDF + "外麦.pdf", path, options, map);
        } else {
            String message = "#" + this.id + "  " + this.sku + " 还没创建 FBA";
            FileUtils.writeStringToFile(new File(folder, message + ".txt"), message, "UTF-8");
        }
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
                            + "差异"
                            + (shipment.dates.planArrivDate.getTime() - shipment.dates.oldPlanArrivDate.getTime())
                            / (24 * 60 * 60 * 1000)
                            + "天";
                }
            } else {
                if(this.attrs.planArrivDate != null && shipment.dates.planArrivDate != null
                        && ((this.attrs.planArrivDate.getTime() - shipment.dates.planArrivDate.getTime()) != 0)) {
                    datedesc = "系统备注:采购计划单最新预计到库时间" + this.attrs.planArrivDate
                            + "，比原预计到库日期" + shipment.dates.planArrivDate
                            + "差异" + (this.attrs.planArrivDate.getTime() - shipment.dates.planArrivDate.getTime())
                            / (24 * 60 * 60 * 1000) + "天";
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
     * 根据 运输方式+运输单中的运输商 去匹配对应的货代仓库
     *
     * @return
     */
    public Whouse matchWhouse() {
        Shipment.T shiptype = null;
        Cooperator c = null;
        if(this.shipItems != null && !this.shipItems.isEmpty()) {
            Shipment shipment = this.shipItems.get(0).shipment;
            shiptype = shipment.type;
            c = shipment.cooper;
        }
        return Whouse.findByCooperatorAndShipType(
                c != null ? c : (Cooperator) Cooperator.find("name LIKE '%欧嘉国际%'").first(),
                shiptype != null ? shiptype : this.shipType
        );
    }

    /**
     * @param pids
     * @return
     */
    public static String validateIsInbound(List<Long> pids, String type) {
        Map<STAGE, Integer> status_map = new HashMap<>();
        List<ProcureUnit> units = ProcureUnit.find("id IN " + SqlSelect.inlineParam(pids)).fetch();
        String msg = "";
        for(ProcureUnit unit : units) {
            if(type.equals("createInboundBtn")) {
                if(unit.stage != STAGE.DELIVERY) {
                    return "请统一选择阶段为【采购中】的采购计划！";
                }
                msg = StringUtils.isNotEmpty(validInbound(unit)) ? validInbound(unit) : validRefund(unit);
            } else if(type.equals("createOutboundBtn")) {
                if(unit.stage != STAGE.IN_STORAGE) {
                    return "请选择阶段为【已入仓】的采购计划！";
                }
                if(unit.outbound != null) {
                    return "采购计划【" + unit.id + "】已经在出库单 【" + unit.outbound.id + "】中！";
                }
                msg = validRefund(unit);
            } else if(type.equals("createRefundBtn")) {
                if(unit.stage != STAGE.IN_STORAGE) {
                    return "请统一选择阶段为【已入仓】的采购计划！";
                }
                if(unit.parent != null && T.StockSplit == unit.type) {
                    return "库存分拆的子采购计划【" + unit.id + "】无法进行退货！";
                }
                if(unit.outbound != null) {
                    return "采购计划【" + unit.id + "】已经在出库单 【" + unit.outbound.id + "】中,请先解除！";
                }
                msg = validRefund(unit);
            }
            if(StringUtils.isNotEmpty(msg))
                return msg;
        }
        return msg;
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

    public static Map<Integer, List<ProcureUnit>> pageNumForTen(List<Outbound> outbounds) {
        Map<Integer, List<ProcureUnit>> ten = new HashMap<>();
        int k = 0;
        for(Outbound outbound : outbounds) {
            int groupNum = 0;
            List<ProcureUnit> iu = ProcureUnit.findUnitOrderByShipment(outbound.id);
            Map<String, Integer> map = new HashMap<>();
            for(ProcureUnit u : iu) {
                if(u.shipItems.size() == 0) {
                    u.groupNum = 0;
                } else {
                    String shipment_id = u.shipItems.get(0).shipment.id;
                    if(map.containsKey(shipment_id)) {
                        u.groupNum = map.get(shipment_id);
                    } else {
                        groupNum++;
                        map.put(shipment_id, groupNum);
                        u.groupNum = groupNum;
                    }
                }
            }
            int max = iu.size();
            for(int i = 0; i < iu.size(); i += 10) {
                int num = max - i;
                ten.put(k, iu.subList(i, num > 10 ? i + 10 : i + num));
                k++;
            }
        }
        return ten;
    }

    public static List<ProcureUnit> findUnitOrderByShipment(String outId) {
        String sql = "Select DISTINCT u FROM ProcureUnit u LEFT JOIN u.shipItems i WHERE u.outbound.id = ? "
                + "ORDER BY i.shipment.id ";
        return ProcureUnit.find(sql, outId).fetch();
    }

    public String showInboundIds() {
        List<InboundUnit> list = InboundUnit.find("unit.id=?", this.id).fetch();
        StringBuilder ids = new StringBuilder();
        for(InboundUnit unit : list) {
            ids.append(unit.inbound.id).append(",");
        }
        return ids.toString();
    }


    public String showInboundUnitIds() {
        List<InboundUnit> list = InboundUnit.find("unit.id=?", this.id).fetch();
        StringBuilder ids = new StringBuilder();
        for(InboundUnit unit : list) {
            ids.append(unit.id).append(",");
        }
        return ids.toString();
    }

    /* 加载出 CheckTaskDTO 对象
     *
     * @return
     */
    public CheckTaskDTO loadCheckTaskDTO() {
        if(this.fba != null && StringUtils.isNotBlank(this.fba.fbaCartonContents)) {
            return J.from(this.fba.fbaCartonContents, CheckTaskDTO.class);
        }
        return null;
    }

    public boolean isEditInput() {
        int size = ProcureUnit.find("realParent.id =?", this.id).fetch().size();
        if(size > 0) {
            return true;
        }
        if(this.realParent != null) {
            /*如果父采购计划是采购中，入库中才能修改**/
            if(Arrays.asList(STAGE.IN_STORAGE, STAGE.DELIVERY).contains(this.parent.stage)
                    && T.StockSplit == this.type) {
                return false;
            } else if(T.ProcureSplit == this.type && this.stage == STAGE.IN_STORAGE) {
                return true;
            } else if(Arrays.asList(STAGE.DONE, STAGE.OUTBOUND).contains(this.stage)) {
                return true;
            }
            return this.stage != this.realParent.stage;
        }
        return !Arrays.asList(STAGE.PLAN, STAGE.DELIVERY).contains(this.stage);
    }

    public boolean isManualEdit() {
        if(this.stage != STAGE.DELIVERY) {
            return true;
        }
        int size = ProcureUnit.find("parent.id =?", this.id).fetch().size();
        return size > 0;
    }

    public boolean postFBAValidate(CheckTaskDTO dto) {
        if(this.qty() == 0) {
            Validation.addError("", "数量不允许为 0!");
        }
        if(this.selling == null || StringUtils.isBlank(this.selling.merchantSKU)) {
            Validation.addError("", "Selling(MerchantSKU) 不允许为空!");
        }
        if(dto != null) dto.validedQtys(this.qty());
        return !Validation.hasErrors();
    }

    public boolean validBoxInfoIsComplete() {
        return Objects.equals(this.projectName, User.COR.MengTop.name()) && this.mainBox != null
                && this.mainBox.num != 0
                || !(this.mainBox == null || this.mainBox.num == 0 || this.mainBox.length == 0 || this.mainBox.width == 0
                || this.mainBox.height == 0);
    }

    public boolean validBoxInfoIsCorrect() {
        if(this.stage == STAGE.IN_STORAGE) {
            return this.availableQty == this.totalOutBoundQty();
        } else {
            return this.outQty == this.totalOutBoundQty();
        }
    }

    public int totalOutBoundQty() {
        int total_main = 0, total_last = 0;
        if(this.mainBox != null)
            total_main = this.mainBox.num * this.mainBox.boxNum;
        if(this.lastBox != null)
            total_last = this.lastBox.num * this.lastBox.boxNum;
        return total_main + total_last;
    }

    /**
     * 对应运输单是否 计划中 状态
     *
     * @return
     */
    public boolean isShipmentPlan() {
        return this.shipItems.size() > 0 && this.shipItems.get(0).shipment.state != Shipment.S.PLAN;
    }

    public Date qcDate() {
        InboundUnit unit = InboundUnit.find("unit.id = ? ORDER BY id DESC", this.id).first();
        return unit != null ? unit.qcDate : null;
    }

    public static void cancelAMZOutbound(String msg, Long[] ids) {
        List<ProcureUnit> units = ProcureUnit.find("id IN " + SqlSelect.inlineParam(ids)).fetch();
        units.forEach(unit -> {
            unit.revokeStatus = REVOKE.READY;
            unit.revokeMsg = msg;
            unit.save();
        });
    }

    public void confirmCancelAMZOutbound() {
        this.availableQty += this.outQty;
        this.outbound = null;
        this.stage = STAGE.IN_STORAGE;
        this.revokeStatus = REVOKE.CONFIRM;

        StockRecord stockRecord = new StockRecord();
        stockRecord.creator = Login.current();
        stockRecord.whouse = this.whouse;
        stockRecord.unit = this;
        stockRecord.qty = this.outQty;
        stockRecord.currQty = this.availableQty;
        stockRecord.type = StockRecord.T.CancelOutbound;
        stockRecord.category = StockRecord.C.Normal;
        stockRecord.memo = this.revokeMsg;
        stockRecord.recordId = this.id;
        stockRecord.save();
        new ERecordBuilder("outbound.cancel").msgArgs(this.id, this.outQty, this.revokeMsg).fid(this.id).save();

        this.outQty = 0;
        this.save();
    }

    public int boxNum() {
        if(this.fba != null && this.fba.dto != null)
            return this.fba.dto.boxNum;
        if(this.cooperator.cooperItem(this.sku) != null)
            return this.cooperator.cooperItem(this.sku).boxNum(this.qty());
        return 1;
    }

    public float otherPrice() {
        CooperItem cooperItem = CooperItem.find("cooperator.id=? AND sku=?", this.cooperator.id, this.sku).first();
        return cooperItem == null ? 0 : cooperItem.otherPrice;
    }

    public boolean ifParent() {
        return ProcureUnit.count("parent.id=?", this.id) > 0;
    }

    public boolean includePayment(Long paymentId) {
        return paymentId != null && (this.fees.stream().filter(unit -> unit.payment.id - paymentId == 0).count() > 0);
    }

    public Date yesterdayPlanShipDate() {
        if(this.attrs == null || this.attrs.planShipDate == null) return null;
        return new Date((this.attrs.planShipDate.getTime() - 24 * 60 * 60 * 1000));
    }

    public double reallyWeight() {
        if(this.mainBox != null) {
            return this.totalBoxWeight();
        } else {
            return this.product.weight * this.qtyForFba();
        }
    }

    public double currentWeight() {
        return new BigDecimal(this.product.getRecentlyWeight() * this.shipmentQty())
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
