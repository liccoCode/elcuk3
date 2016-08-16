package models.whouse;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.google.gson.annotations.Expose;
import helper.*;
import models.ElcukRecord;
import models.OperatorConfig;
import models.User;
import models.embedded.ERecordBuilder;
import models.market.Account;
import models.market.Selling;
import models.procure.FBAShipment;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import models.product.Product;
import mws.FBA;
import org.allcolor.yahp.converter.IHtmlToPdfTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.Model;
import play.modules.pdf.PDF;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 出库计划
 * <p>
 * (角色定义与出库记录的角色基本一致,
 * 主要为出货记录与运输单中间的信息传递,
 * 允许从运输单创建出库计划或者先创建出库计划然后再去关联运输单)
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/1/16
 * Time: 2:55 PM
 */
@Entity
public class ShipPlan extends Model implements ElcukRecord.Log {
    /**
     * Selling
     */
    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    /**
     * 送往的仓库
     */
    @OneToOne
    public Whouse whouse;

    /**
     * 货物运输的类型 (海运? 空运? 快递)
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Column(length = 20)
    public Shipment.T shipType;

    @ManyToOne
    public FBAShipment fba;

    /**
     * 所关联的运输出去的 ShipItem.
     */
    @OneToMany(mappedBy = "plan")
    public List<ShipItem> shipItems = new ArrayList<>();

    /**
     * 状态
     */
    @Required
    @Expose
    @Enumerated(EnumType.STRING)
    public S state;

    @Override
    public String to_log() {
        return String.format("[sid:%s] [仓库:%s] [计划数量:%s] [预计到库:%s] [运输方式:%s]",
                this.selling.sellingId, this.whouse.name(), this.planQty,
                Dates.date2Date(this.planArrivDate), this.shipType);
    }

    public enum S {
        Pending {
            @Override
            public String label() {
                return "待出库";
            }
        },
        Confirmd {
            @Override
            public String label() {
                return "已出库";
            }
        };

        public abstract String label();
    }

    /**
     * 计划出库数
     */
    public Integer planQty;

    /**
     * 出货数
     */
    @Required
    @Expose
    public Integer qty;

    /**
     * 预计运输时间
     */
    @Expose
    public Date planShipDate;

    /**
     * 预计到达时间
     */
    @Expose
    public Date planArrivDate;

    @Expose
    public String sku;// 冗余 sku 字段

    @Expose
    public String sid;// 冗余 sellingId 字段

    @Expose
    @Lob
    public String memo;

    /**
     * 记录一下是由哪个采购计划生成的
     */
    @Expose
    @OneToOne
    public ProcureUnit unit;

    @Expose
    public Date createDate = new Date();

    @Expose
    public Date updateDate = new Date();

    /**
     * 创建人
     */
    @Expose
    @OneToOne
    public User creator;

    public ShipPlan() {
        this.createDate = new Date();
        this.state = S.Pending;
    }

    public ShipPlan(Selling selling) {
        this();
        this.selling = selling;
        this.product = this.selling.listing.product;
        this.creator = User.current();
    }

    public ShipPlan(ProcureUnit unit) {
        this();
        this.planShipDate = unit.attrs.planShipDate;
        this.planArrivDate = unit.attrs.planArrivDate;
        this.planQty = unit.qty();
        this.shipType = unit.shipType;
        this.selling = unit.selling;
        this.sku = unit.sku;
        this.sid = unit.sid;
        this.product = unit.product;
        this.whouse = unit.whouse;
        this.unit = unit;
        this.fba = unit.fba;
        this.creator = User.current();
    }

    /**
     * 保存出库计划同时将其添加到运输单并生成出库记录
     * PS: 如果出现校验问题会抛出 FastRuntimeException, 需要调用者自行处理
     *
     * @return
     */
    public ShipPlan createAndOutbound(String shipmentId) {
        this.creator = User.current();
        if(this.valid()) {
            this.<ShipPlan>save().outbound();
            //添加到运输单
            if(StringUtils.isNotBlank(shipmentId)) {
                Shipment shipment = Shipment.findById(shipmentId);
                if(shipment != null) shipment.addToShip(this);
            } else {
                if(this.unit != null && StringUtils.isNotBlank(this.unit.shipmentId)) {
                    Shipment shipment = Shipment.findById(this.unit.shipmentId);
                    if(shipment != null) {
                        shipment.addToShip(this);
                    } else {
                        Validation.addError("",
                                String.format("采购计划[%s]指定的的运输单[%s]无效!", this.unit.id, this.unit.shipmentId));
                    }
                }
            }
        }
        if(Validation.hasErrors()) {
            throw new FastRuntimeException(Webs.V(Validation.errors()));
        }
        return this;
    }

    /**
     * 生成出库记录
     *
     * @return
     */
    public ShipPlan outbound() {
        OutboundRecord outboundRecord = new OutboundRecord(this);
        if(outboundRecord.exist()) {
            throw new FastRuntimeException(String.format("出库记录已经存在![采购计划ID: %s, 出库计划ID: %s]", this.unit.id, this.id));
        }
        outboundRecord.save();
        return this;
    }

    public boolean valid() {
        Validation.required("SKU", this.product);
        Validation.required("Sellinig ID", this.selling);
        Validation.required("仓库", this.whouse);
        Validation.required("预计运输时间", this.planShipDate);
        Validation.required("计划出库数量", this.planQty);
        Validation.required("运输方式", this.shipType);
        return !Validation.hasErrors();
    }

    public boolean exist() {
        return this.unit != null && ShipPlan.count("unit.id=?", unit.id) != 0;
    }

    public boolean isLock() {
        return this.state != S.Pending;
    }

    public static void postFbaShipments(List<Long> planIds) {
        List<ShipPlan> plans = ShipPlan.find(SqlSelect.whereIn("id", planIds)).fetch();
        if(plans.size() != planIds.size())
            Validation.addError("", "加载的数量");
        if(Validation.hasErrors()) return;

        for(ShipPlan plan : plans) {
            try {
                if(plan.fba != null) {
                    Validation.addError("", String.format("#%s 已经有 FBA 不需要再创建", plan.id));
                } else {
                    plan.postFbaShipment();
                }
            } catch(Exception e) {
                Validation.addError("", Webs.E(e));
            }
        }
    }

    /**
     * 创建 FBA
     */
    public synchronized FBAShipment postFbaShipment() {
        FBAShipment fba = null;
        try {
            fba = FBA.plan(this.selling.account, this);
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("", "向 Amazon 创建 Shipment PLAN 因 " + Webs.E(e) + " 原因失败.");
            return null;
        }
        try {
            fba.state = FBA.create(fba);
            this.fba = fba.save();
            if(this.unit != null) {//将创建的 FBA 同步到采购计划
                this.unit.fba = this.fba;
                this.unit.save();
            }
            this.save();
            new ERecordBuilder("shipment.createFBA")
                    .msgArgs(this.id, this.sku, this.fba.shipmentId)
                    .fid(this.id)
                    .save();
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("", "向 Amazon 创建 Shipment 错误 " + Webs.E(e));
        }
        return fba;
    }

    public int qty() {
        if(this.qty != null) return this.qty;
        return this.planQty;
    }

    /**
     * 采购单元相关联的运输单
     *
     * @return
     */
    public List<Shipment> relateShipment() {
        Set<Shipment> shipments = new HashSet<>();
        for(ShipItem shipItem : this.shipItems) {
            if(shipItem.shipment != null)
                shipments.add(shipItem.shipment);
        }
        return new ArrayList<>(shipments);
    }

    public void update(ShipPlan plan, String shipmentId) {
        if(this.fba != null && (plan.shipType != this.shipType || plan.planQty.intValue() != this.planQty.intValue())) {
            Validation.required("备注", plan.memo);
        }
        if(this.isLock()) {
            Validation.addError("", "已出库, 无法再修改");
        }
        List<String> logs = this.updateLogs(plan);
        this.changeShipItemShipment(StringUtils.isNotBlank(shipmentId) ? Shipment.<Shipment>findById(shipmentId) : null);
        if(Validation.hasErrors()) return;
        if(!logs.isEmpty()) {
            new ERecordBuilder("shipplan.update").msgArgs(this.id, StringUtils.join(logs, "<br>")).fid(this.id).save();
        }
        this.save();
    }

    /**
     * 调整运输单
     *
     * @param shipment
     */
    public void changeShipItemShipment(Shipment shipment) {
        if(shipment == null) return;
        if(shipment.state != Shipment.S.PLAN) {
            Validation.addError("", "涉及的运输单已经为" + shipment.state.label() + "状态, 只有"
                    + Shipment.S.PLAN.label() + "状态的运输单才可调整.");
            return;
        }
        if(this.shipItems.size() == 0) {
            // 出库计划没有运输项目, 调整运输单的时候, 需要创建运输项目
            shipment.addToShip(this);
        } else {
            for(ShipItem shipItem : this.shipItems) {
                if(this.shipType == Shipment.T.EXPRESS) {
                    if(shipItem.shipment.state == Shipment.S.PLAN) {
                        // 快递运输单调整, 运输项目全部删除, 重新设计.
                        shipItem.delete();
                    }
                } else {
                    Shipment originShipment = shipItem.shipment;
                    shipItem.adjustShipment(shipment);
                    if(Validation.hasErrors()) {
                        shipItem.shipment = originShipment;
                        shipItem.save();
                        return;
                    }
                }
            }
        }
    }

    public List<String> updateLogs(ShipPlan plan) {
        List<String> logs = new ArrayList<>();
        logs.addAll(Reflects.logFieldFade(this, "product.sku", plan.product.sku));
        logs.addAll(Reflects.logFieldFade(this, "selling.sellingId", plan.selling.sellingId));
        logs.addAll(Reflects.logFieldFade(this, "product.abbreviation", plan.product.abbreviation));
        logs.addAll(Reflects.logFieldFade(this, "whouse.id", plan.whouse.id));
        logs.addAll(Reflects.logFieldFade(this, "planShipDate", plan.planShipDate));
        logs.addAll(Reflects.logFieldFade(this, "planArrivDate", plan.planArrivDate));
        logs.addAll(Reflects.logFieldFade(this, "planQty", plan.planQty));
        logs.addAll(Reflects.logFieldFade(this, "shipType", plan.shipType));
        logs.addAll(Reflects.logFieldFade(this, "memo", plan.memo));
        return logs;
    }

    /**
     * 删除出货计划
     * TODO: 校验是否还有其他关联数据需要检查和删除
     */
    public void remove() {
        if(this.isLock()) {
            Validation.addError("", String.format("只允许 %s, %s 状态的出库计划进行取消", S.Pending.label()));
            return;
        }
        // 删除 FBA
        FBAShipment fba = this.fba;
        if(fba != null) {
            fba.plans.remove(this);
            fba.removeFBAShipment();
        }
        // 删除运输相关
        for(ShipItem item : this.shipItems) {
            item.delete();
        }

        //删除出库记录
        OutboundRecord outboundRecord = this.outboundRecord();
        if(outboundRecord != null) outboundRecord.delete();
        this.delete();
    }

    /**
     * 指定文件夹，为当前采购计划所关联的 FBA 生成 箱內麦 与 箱外麦
     *
     * @param folder
     * @param boxNumber
     */
    public void fbaAsPDF(File folder, Long boxNumber) throws Exception {
        if(this.fba != null) {
            // PDF 文件名称 :[国家] [运输方式] [数量] [产品简称] 外/内麦
            String namePDF = String.format("[%s][%s][%s][%s][%s]",
                    this.selling.market.countryName(),
                    this.shipType.label(),
                    this.planQty,
                    this.product.abbreviation,
                    this.id
            );
            Map map = this.fbaPDFParams();
            map.put("boxNumber", boxNumber);
            map.put("boxNumberStr", Webs.hundredNumber(boxNumber));

            PDF.Options options = new PDF.Options();
            options.pageSize = new IHtmlToPdfTransformer.PageSize(20.8d, 29.6d);
            PDFs.templateAsPDF(folder, namePDF + "外麦.pdf", "FBAs/boxLabel.html", options, map);
        } else {
            String message = "#" + this.id + "  " + this.sku + " 还没创建 FBA";
            FileUtils.writeStringToFile(new File(folder, message + ".txt"), message, "UTF-8");
        }
    }

    /**
     * 输出生成 PDF 时所需要的参数
     *
     * @return
     */
    public Map<String, Object> fbaPDFParams() {
        GTs.MapBuilder mapBuilder = GTs.MapBuilder.map("shipmentId",
                (Object) String.format("%s%s", fba.shipmentId.trim(), "U"))
                .put("shipFrom", Account.address(this.fba.account.type))
                .put("fba", this.fba)
                .put("deliveryDate", new SimpleDateFormat("yyyy年MM月dd日").format(this.planShipDate))
                .put("shipType", this.shipType)
                .put("isExpress", this.shipType == Shipment.T.EXPRESS)
                .put("product", this.product)
                .put("selling", this.selling)
                .put("addressName", OperatorConfig.getVal("addressname"))
                .put("brandName", OperatorConfig.getVal("brandname"))
                .put("shipmentDetailLabel", OperatorConfig.getVal("shipmentdetaillabel"))
                .put("companyName", OperatorConfig.getVal("companyname"));
        if(this.unit != null) mapBuilder.put("cooperator", this.unit.cooperator);
        return mapBuilder.build();
    }

    public String dateDesc() {
        List<Shipment> relateShipments = this.relateShipment();
        if(relateShipments.size() > 0) {
            Shipment shipment = relateShipments.get(0);
            if(this.planArrivDate != null && shipment.dates.planArrivDate != null &&
                    ((this.planArrivDate.getTime() - shipment.dates.planArrivDate.getTime()) != 0)) {
                return String.format("系统备注: 出库计划最新预计到库时间 %s, 比原预计到库日期 %s 差异 %s 天",
                        this.planArrivDate,
                        shipment.dates.planArrivDate,
                        (this.planArrivDate.getTime() - shipment.dates.planArrivDate.getTime()) / (24 * 60 * 60 * 1000)
                );
            }
        }
        return "";
    }

    public OutboundRecord outboundRecord() {
        return OutboundRecord.find("shipPlan=?", this).first();
    }
}
