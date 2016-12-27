package models.whouse;

import com.google.gson.annotations.Expose;
import controllers.Login;
import models.OperatorConfig;
import models.User;
import models.procure.Cooperator;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 出库单model
 * 直接跟ProcureUnit建立一对多的关系
 * Created by licco on 2016/11/29.
 */
@Entity
@DynamicUpdate
public class Outbound extends GenericModel {

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
                DateTime.parse(String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear())).toDate()) +
                "";
        return String.format("PTC|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public void create(List<Long> pids) {
        this.init();
        this.projectName = this.isb2b ? "B2B" : OperatorConfig.getVal("brandname");
        this.save();
        for(Long id : pids) {
            ProcureUnit unit = ProcureUnit.findById(id);
            unit.outbound = this;
            unit.outQty = unit.availableQty;
            unit.save();
        }
    }

    public static void initCreateByShipItem(List<String> shipmentId) {
        List<Shipment> shipments = Shipment.find("id IN " + SqlSelect.inlineParam(shipmentId)).fetch();
        Outbound out = new Outbound();
        out.init();
        Shipment shipment = shipments.get(0);
        ProcureUnit first = shipment.items.get(0).unit;
        out.name = shipment.cooper.name + "-" + shipment.type.label() + "-" + shipment.whouse.name;
        out.projectName = first.projectName;
        out.shipType = shipment.type;
        out.type = T.Normal;
        out.whouse = shipment.whouse;
        out.targetId = shipment.cooper.id.toString();
        out.shipmentId = SqlSelect.inlineParam(shipmentId);
        out.save();
        for(Shipment s : shipments) {
            for(ShipItem item : s.items) {
                ProcureUnit unit = item.unit;
                unit.outbound = out;
                unit.outQty = unit.availableQty;
                unit.save();
            }
            s.out = out;
            s.save();
        }
    }

    public static void confirmOutBound(List<String> ids) {
        for(String id : ids) {
            Outbound out = Outbound.findById(id);
            for(ProcureUnit p : out.units) {
                if(p.stage != ProcureUnit.STAGE.IN_STORAGE) {
                    Validation.addError("", "出库单【" + id + "】下的采购计划" + p.id + "不是已入库状态，请查证");
                    return;
                }
                String msg = ProcureUnit.validRefund(p);
                if(StringUtils.isNotEmpty(msg)) {
                    Validation.addError("", "出库单【" + id + "】下的" + msg);
                    return;
                }
                if(p.mainBox.num == 0) {
                    Validation.addError("", "采购计划【" + p.id + "】的包装信息没填，请先填写！");
                    return;
                }
            }
            if(Validation.hasErrors()) {
                return;
            }
            out.status = S.Outbound;
            out.outboundDate = new Date();
            out.save();
            for(ProcureUnit p : out.units) {
                p.stage = ProcureUnit.STAGE.OUTBOUND;
                p.save();
                createStockRecord(p);
            }
        }
    }

    public static void createStockRecord(ProcureUnit unit) {
        StockRecord record = new StockRecord();
        record.whouse = unit.whouse;
        record.unit = unit;
        record.qty = unit.outQty;
        record.type = StockRecord.T.Outbound;
        record.recordId = unit.id;
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

}
