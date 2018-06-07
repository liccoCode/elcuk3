package models.whouse;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.HTTP;
import models.OperatorConfig;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.Shipment;
import models.view.Ret;
import models.view.dto.InventoryTurnoverDTO;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 出库单model
 * 直接跟ProcureUnit建立一对多的关系
 * Created by licco on 2016/11/29.
 */
@Entity
@DynamicUpdate
public class Outbound extends GenericModel {

    private static final long serialVersionUID = 163177419089864527L;
    private static final String MengTopUrl = "https://mengtop.elcuk-erp.com";

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    /**
     * 采购计划
     */
    @OneToMany(mappedBy = "outbound", cascade = {CascadeType.PERSIST})
    public List<ProcureUnit> units = new ArrayList<>();

    /**
     * 异动记录
     */
    @OneToMany(mappedBy = "outbound", cascade = {CascadeType.PERSIST})
    public List<StockRecord> records = new ArrayList<>();

    /**
     * 名称
     */
    @Required
    public String name;

    /**
     * 出库类型
     */
    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public StockRecord.C type;

    public String targetId;

    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public S status;

    public enum S {
        Create {
            @Override
            public String label() {
                return "已创建";
            }
        },
        Outbound {
            @Override
            public String label() {
                return "已出库";
            }
        },
        Cancel {
            @Override
            public String label() {
                return "已取消";
            }
        };

        public abstract String label();
    }

    @Enumerated(EnumType.STRING)
    @Required
    @Expose
    public Shipment.T shipType;

    /**
     * 目的国家
     */
    @OneToOne
    public Whouse whouse;

    /**
     * 项目名称
     */
    @Required
    public String projectName;

    /**
     * 发货人
     */
    @OneToOne
    public User consignor;

    /**
     * 出库时间
     */
    @Required
    public Date outboundDate;

    /**
     * 制单人
     */
    @OneToOne
    public User creator;

    /**
     * 创建时间
     */
    @Required
    public Date createDate;

    /**
     * 备注
     */
    public String memo;

    /**
     * 运输单ID
     */
    public String shipmentId;

    /**
     * 是否B2B
     */
    @Transient
    public boolean isb2b = false;

    public Outbound() {

    }

    public Outbound(ProcureUnit unit) {
        this.shipType = unit.shipType;
        this.whouse = unit.whouse;
        this.status = S.Create;
    }

    public void init() {
        this.id = id();
        this.status = S.Create;
        this.createDate = new Date();
        this.creator = Login.current();
    }


    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = Outbound.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate())
                + "";
        return String.format("PTC|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public void create(List<Long> pids) {
        this.init();
        this.projectName = this.isb2b ? "B2B" : OperatorConfig.getVal("brandname");
        this.save();
        pids.stream().filter(Objects::nonNull).forEach(id -> {
            ProcureUnit unit = ProcureUnit.findById(id);
            unit.outbound = this;
            unit.save();
        });
    }

    public void createOther(List<StockRecord> records) {
        for(StockRecord s : records) {
            ProcureUnit unit = ProcureUnit.findById(s.unitId);
            if(unit.availableQty < s.qty)
                Validation.addError("", "采购计划" + unit.id + "出库数量超过可用库存。");
        }
        if(Validation.hasErrors()) return;
        this.init();
        this.status = this.type == StockRecord.C.B2B ? S.Create : Outbound.S.Outbound;
        this.save();
        records.stream().filter(record -> record.unitId != null).forEach(record -> {
            ProcureUnit unit = ProcureUnit.findById(record.unitId);
            StockRecord stock = new StockRecord();
            stock.unit = unit;
            stock.recordId = record.unitId;
            stock.outbound = this;
            stock.whouse = unit.currWhouse;
            stock.creator = Login.current();
            stock.qty = record.qty;
            stock.currQty = unit.availableQty - record.qty;
            stock.type = StockRecord.T.OtherOutbound;
            stock.category = this.type;
            stock.save();
            if(this.type == StockRecord.C.B2B) {
                unit.outbound = this;
            } else {
                unit.availableQty = stock.currQty;
            }
            unit.save();
        });
    }

    public static void initCreateByShipItem(List<String> shipmentId) {
        List<Shipment> shipments = Shipment.find("id IN " + SqlSelect.inlineParam(shipmentId)).fetch();
        Map<String, List<Shipment>> map = new HashMap<>();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        shipments.forEach(shipment -> {
            ProcureUnit first = shipment.items.get(0).unit;
            String key;
            if(Objects.equals(shipment.projectName, User.COR.MengTop)) {
                key = String.format("%s_%s_%s_%s_%s", first.projectName, shipment.cooper.name, shipment.countryCode
                        , shipment.type.label(), formatter.format(shipment.dates.planBeginDate));
            } else {
                key = String.format("%s_%s_%s_%s_%s", first.projectName, shipment.cooper.name, shipment.whouse.name,
                        shipment.type.label(), formatter.format(shipment.dates.planBeginDate));
            }

            if(map.containsKey(key)) {
                map.get(key).add(shipment);
            } else {
                map.put(key, new ArrayList<>(Arrays.asList(shipment)));
            }
        });

        map.keySet().forEach(key -> {
            Outbound out = new Outbound();
            out.init();
            out.name = key;
            Shipment shipment = map.get(key).get(0);
            out.shipType = shipment.type;

            if(key.split("_")[0].equals(User.COR.MengTop.name())) {
                out.projectName = User.COR.MengTop.name();
                out.type = StockRecord.C.B2B;
                out.shipmentId = SqlSelect
                        .inlineParam(map.get(key).stream().map(ship -> ship.id).collect(Collectors.toList()));
                out.save();
            } else {
                ProcureUnit first = shipment.items.get(0).unit;
                out.projectName = first.projectName;
                out.type = StockRecord.C.Normal;
                out.whouse = shipment.whouse;
                out.targetId = shipment.cooper.id.toString();
                out.shipmentId = SqlSelect
                        .inlineParam(map.get(key).stream().map(ship -> ship.id).collect(Collectors.toList()));
                out.save();
            }
            map.get(key).stream().peek(s -> {
                s.out = out;
                s.save();
            }).flatMap(s -> s.items.stream())
                    .forEach(item -> {
                        ProcureUnit unit = item.unit;
                        if(Arrays.asList("PLAN", "DELIVERY", "DONE", "IN_STORAGE").contains(unit.stage.name())) {
                            unit.outbound = out;
                            unit.save();
                        }
                    });
        });
    }

    public static Ret confirmOutBound(List<String> ids) {
        for(String id : ids) {
            Outbound out = Outbound.findById(id);
            for(ProcureUnit p : out.units) {
                if(Arrays.asList("DELIVERY", "DONE").contains(p.stage.name())) {
                    return new Ret(false, "出库单【" + id + "】下的采购计划" + p.id + "不是已入仓状态，请查证");
                }
                String msg = ProcureUnit.validRefund(p);
                if(StringUtils.isNotEmpty(msg)) {
                    return new Ret(false, "出库单【" + id + "】下的" + msg);
                }
                if(!p.validBoxInfoIsComplete()) {
                    return new Ret(false, "采购计划【" + p.id + "】的包装信息没填，请先填写！");
                }
                if(p.availableQty < p.totalOutBoundQty()) {
                    return new Ret(false, "采购计划【" + p.id + "】的包装信息的总数量大于可用库存量，请先检查！");
                }
            }
            if(Objects.equals(out.type, StockRecord.C.B2B) && !OperatorConfig.getVal("brandname").equals("MengTop")) {
                StringBuilder sku = new StringBuilder();
                StringBuilder qty = new StringBuilder();
                StringBuilder currency = new StringBuilder();
                StringBuilder price = new StringBuilder();
                out.units.forEach(unit -> {
                    sku.append(unit.product.sku).append(",");
                    qty.append(unit.attrs.qty).append(",");
                    currency.append(unit.attrs.currency).append(",");
                    price.append(unit.attrs.price).append(",");
                });

                StringBuilder url = new StringBuilder(Outbound.MengTopUrl + "/api/StockApiDeal/checkIsOutBoundB2b");
                url.append("?auth_token=baef851cab745d3441d4bc7ff6f27b28&sku=").append(sku.toString());
                String result = HTTP.get(url.toString());
                Ret ret = JSON.parseObject(result, Ret.class);
                if(ret.flag) {
                    finalConfirm(out);
                    url = new StringBuilder(Outbound.MengTopUrl + "/api/StockApiDeal/createProcureUnit");
                    url.append("?auth_token=baef851cab745d3441d4bc7ff6f27b28&sku=").append(sku.toString());
                    url.append("&qty=").append(qty.toString()).append("&currency=").append(currency.toString());
                    url.append("&price=").append(price.toString());
                    url.append("&projectName=").append(OperatorConfig.getVal("brandname"));
                    result = HTTP.get(url.toString());
                    return JSON.parseObject(result, Ret.class);
                } else {
                    return ret;
                }
            } else {
                finalConfirm(out);
            }
        }
        return new Ret(false, "找不到出库单元");
    }

    private static void finalConfirm(Outbound out) {
        out.status = S.Outbound;
        out.outboundDate = new Date();
        out.save();
        out.units.forEach(p -> {
            if(Arrays.asList("IN_STORAGE").contains(p.stage.name())) {
                p.stage = ProcureUnit.STAGE.OUTBOUND;
            }
            int total_main = p.mainBox.num * p.mainBox.boxNum;
            int total_last = p.lastBox == null ? 0 : p.lastBox.num * p.lastBox.boxNum;
            p.outQty = total_main + total_last;
            p.availableQty = p.availableQty - p.outQty;
            p.save();
            createStockRecord(p, p.availableQty);
        });
    }

    public static void createStockRecord(ProcureUnit unit, int currQty) {
        StockRecord record = new StockRecord();
        record.creator = Login.current();
        record.whouse = unit.whouse;
        record.unit = unit;
        record.qty = unit.outQty;
        record.type = StockRecord.T.Outbound;
        record.recordId = unit.id;
        record.currQty = currQty;
        record.save();
    }


    public String showCompany() {
        switch(this.type) {
            case Normal:
            case B2B:
            case Refund:
                if(StringUtils.isNotEmpty(this.targetId)) {
                    Cooperator c = Cooperator.findById(Long.parseLong(this.targetId));
                    return c.name;
                }
            default:
                return this.targetId;
        }
    }

    public List<ProcureUnit> availableUnits() {
        if(Objects.isNull(this.whouse)) {
            return ProcureUnit.find("stage=? AND shipType=? AND projectName =? AND outbound.id is null",
                    ProcureUnit.STAGE.IN_STORAGE, this.shipType, this.projectName).fetch();
        } else {
            return ProcureUnit.find("stage=? AND whouse.id=? AND shipType=? AND projectName =? "
                            + "AND outbound.id = null", ProcureUnit.STAGE.IN_STORAGE, this.whouse.id, this.shipType,
                    this.projectName).fetch();
        }
    }

    public void addUnits(List<Long> pids) {
        List<ProcureUnit> procureUnits = ProcureUnit.find("id IN " + SqlSelect.inlineParam(pids)).fetch();
        procureUnits.forEach(unit -> {
            unit.outbound = this;
            unit.save();
        });
    }


    public static Map<String, InventoryTurnoverDTO> inventoryTurnover(List<Outbound> outbounds) {
        Map<String, InventoryTurnoverDTO> map = new HashMap<>();
        outbounds.forEach(out -> out.units.forEach(unit -> {
            int day = (int) ((out.createDate.getTime() - unit.attrs.deliveryDate.getTime()) / (1000 * 3600 * 24));
            if(day == 0) {
                unit.warehouseStage = ProcureUnit.WS.SAME_DAY;
            } else if(day > 0 && day <= 7) {
                unit.warehouseStage = ProcureUnit.WS.IN_SEVEN;
            } else if(day > 7 && day <= 15) {
                unit.warehouseStage = ProcureUnit.WS.TWO_WEEK;
            } else if(day > 15 && day <= 30) {
                unit.warehouseStage = ProcureUnit.WS.IN_MONTH;
            } else {
                unit.warehouseStage = ProcureUnit.WS.UP_MONTH;
            }
            unit.turnOverDay = day;
            Outbound.groupByCategoryMap(map, unit);
        }));
        return map;
    }

    private static void groupByCategoryMap(Map<String, InventoryTurnoverDTO> map, ProcureUnit unit) {
        String categoryId = unit.product.category.categoryId;
        InventoryTurnoverDTO dto;
        if(map.containsKey(categoryId)) {
            dto = map.get(categoryId);
        } else {
            dto = new InventoryTurnoverDTO();
            dto.categoryId = categoryId;
            map.put(categoryId, dto);
        }
        switch(unit.warehouseStage) {
            case SAME_DAY:
                dto.sameDayUnitNum++;
                dto.sameDayQty += unit.outQty;
                break;
            case IN_SEVEN:
                dto.inSevenUnitNum++;
                dto.inSevenQty += unit.outQty;
                break;
            case TWO_WEEK:
                dto.twoWeekUnitNum++;
                dto.twoWeekQty += unit.outQty;
                break;
            case IN_MONTH:
                dto.inMonthUnitNum++;
                dto.inMonthQty += unit.outQty;
                break;
            case UP_MONTH:
                dto.upMonthUnitNum++;
                dto.upMonthQty += unit.outQty;
                break;
            default:
                break;
        }
        dto.totalUnitNum++;
        dto.totalQty += unit.outQty;
    }

}
