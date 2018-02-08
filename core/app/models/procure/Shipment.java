package models.procure;

import com.alibaba.fastjson.JSON;
import com.google.gson.annotations.Expose;
import controllers.Login;
import helper.*;
import helper.Currency;
import models.ElcukConfig;
import models.ElcukRecord;
import models.OperatorConfig;
import models.User;
import models.embedded.ERecordBuilder;
import models.embedded.ShipmentDates;
import models.finance.FeeType;
import models.finance.PaymentUnit;
import models.finance.TransportApply;
import models.whouse.Outbound;
import models.whouse.Whouse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.ShipmentQuery;

import javax.persistence.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 一张运输单
 * User: wyattpan
 * Date: 6/17/12
 * Time: 5:32 PM
 */
@Entity
@DynamicUpdate
public class Shipment extends GenericModel implements ElcukRecord.Log {

    private static final long serialVersionUID = -608639102102679863L;

    public Shipment() {
        this.createDate = new Date();
        // 暂时这么写
        this.source = "深圳";
    }

    /**
     * 复制一个拥有新 ID 的 Shipment
     *
     * @param shipment
     */
    public Shipment(Shipment shipment) {
        this();
        this.id = Shipment.id();
        this.state = shipment.state;
        this.creater = shipment.creater;
        this.dates.beginDate = shipment.dates.beginDate;
        this.dates.planBeginDate = shipment.dates.planBeginDate;
        this.dates.planArrivDate = shipment.dates.planArrivDate;
        this.dates.planArrivDateForCountRate = shipment.dates.planArrivDate;
        // FBA 不做处理
        this.type = shipment.type;
        this.whouse = shipment.whouse;
        this.source = shipment.source;
        this.target = shipment.target;
    }

    public Shipment(String id) {
        this();
        this.type = T.EXPRESS;
        this.id = id;
    }

    public Shipment(Date planBeginDate, T type, Whouse whouse) {
        this();
        this.id = Shipment.id();
        this.dates.planBeginDate = planBeginDate;
        this.type = type;
        this.whouse = whouse;
        this.calcuPlanArriveDate();
        this.title = String.format("%s 去往 %s 在 %s", this.id, this.whouse.name(),
                Dates.date2Date(this.dates.planBeginDate));
    }

    public enum T {
        /**
         * 海运
         */
        SEA {
            @Override
            public String label() {
                return "海运";
            }

            @Override
            public String pic() {
                return "fa fa-fw fa-ship";
            }
        },
        /**
         * 空运
         */
        AIR {
            @Override
            public String label() {
                return "空运";
            }

            @Override
            public String pic() {
                return "fa fa-fw fa-plane";
            }
        },
        /**
         * 快递
         */
        EXPRESS {
            @Override
            public String label() {
                return "快递";
            }

            @Override
            public String pic() {
                return "fa fa-fw fa-archive";
            }
        },
        /**
         * 专线
         */
        DEDICATED {
            @Override
            public String label() {
                return "专线";
            }

            @Override
            public String pic() {
                return "fa fa-fw fa-truck";
            }
        },
        /**
         * 铁路
         */
        RAILWAY {
            @Override
            public String label() {
                return "铁路";
            }

            @Override
            public String pic() {
                return "fa fa-fw fa-train";
            }
        };

        public abstract String label();

        public abstract String pic();
    }

    public enum S {
        /**
         * 取消状态
         */
        CANCEL {
            @Override
            public String label() {
                return "取消";
            }
        },
        /**
         * 计划中
         */
        PLAN {
            @Override
            public String label() {
                return "计划中";
            }
        },
        /**
         * 确认运输单, 并创建了 FBA Shipment
         */
        CONFIRM {
            @Override
            public String label() {
                return "锁定准备运输";
            }
        },
        SHIPPING {
            @Override
            public String label() {
                return "运输中";
            }
        },
        /**
         * 清关
         */
        CLEARANCE {
            @Override
            public String label() {
                return "清关中";
            }
        },
        BOOKED {
            @Override
            public String label() {
                return "已预约";
            }
        },
        DELIVERYING {
            @Override
            public String label() {
                return "派送中";
            }
        },
        RECEIPTD {
            @Override
            public String label() {
                return "已签收";
            }
        },
        RECEIVING {
            @Override
            public String label() {
                return "入库中";
            }
        },
        DONE {
            @Override
            public String label() {
                return "完成";
            }
        };

        public abstract String label();
    }

    /**
     * 此 Shipment 的运输项
     */
    @OneToMany(mappedBy = "shipment", cascade = {CascadeType.PERSIST})
    public List<ShipItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "shipment", orphanRemoval = true, cascade = {CascadeType.PERSIST})
    public List<PaymentUnit> fees = new ArrayList<>();

    @ManyToOne
    public TransportApply apply;

    /**
     * 运输合作商
     */
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public Cooperator cooper;

    @OneToOne(cascade = CascadeType.PERSIST)
    public Whouse whouse;

    @OneToOne(fetch = FetchType.LAZY)
    public User creater;

    /**
     * 这个 Shipment 自己拥有的 title, 会使用在 FBAShipment 上
     */
    public String title;

    @Id
    @Column(length = 30)
    @Expose
    @Required
    public String id;

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    @Expose
    @Required
    public S state = S.PLAN;

    /**
     * 此货运单人工创建的时间
     */
    @Expose
    @Required
    public Date createDate = new Date();

    /**
     * 因 Shipment 需要记录的时间太多, 所以将其分开记录
     */
    @Embedded
    @Expose
    public ShipmentDates dates = new ShipmentDates();


    /**
     * 货运类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Required
    public T type;

    /**
     * 类似顺风发货单号的类似跟踪单号;
     * <p/>
     * 不要使用 trackNo 唯一了, 因为业务不允许..
     * 提供:
     * 1. 合并运输单
     * 2. 相同 trackNo 的 Shipment 提示功能
     */
    @Expose
    @Column
    public String trackNo = null;

    /**
     * 工作号
     */
    @Expose
    @Column
    public String jobNumber;

    /**
     * 总重量(kg)货代
     */

    @Expose
    @Column
    public Float totalWeightShipment;

    /**
     * 总体积(m³)货代
     */
    @Expose
    @Column
    public Float totalVolumeShipment;

    /**
     * 货代计费方式
     */
    @Expose
    @Column
    public String shipmentTpye;

    /**
     * 总托盘数(货代)
     */
    @Expose
    @Column
    public Integer totalStockShipment;

    /**
     * 国际快递商人
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    public iExpress internationExpress;

    /**
     * 起始地址
     */
    @Expose
    public String source;

    /**
     * 目的地址
     */
    @Expose
    public String target;

    /**
     * 国际运输的运输信息的记录
     */
    @Lob
    public String iExpressHTML = " ";

    @Lob
    public String memo = " ";

    /**
     * 发票编号
     */
    @Unique
    @Column(unique = true)
    @Expose
    public String invoiceNo;

    /**
     * 对应出库单
     * 可能为空
     */
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public Outbound out;

    /**
     * 多个traceno
     */
    @Transient
    public List<String> tracknolist = new ArrayList<>();

    /**
     * 完成天数
     * 完成时间 - 开始运输时间
     */
    @Transient
    public Integer realDay;

    public enum W {
        PrePay {
            @Override
            public String label() {
                return "预付";
            }
        },
        ArrivePay {
            @Override
            public String label() {
                return "到付";
            }
        };

        public abstract String label();
    }

    /**
     * 贸易方式
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public W tradeMode;

    /**
     * 所属公司
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public User.COR projectName;

    public enum FLAG {
        ARRAY_TO_STR,
        STR_TO_ARRAY
    }

    /**
     * 变更 计算准时率预计到库时间(用于计算准时率的到库时间)的原因
     */
    @Lob
    public String reason = " ";

    /**
     * 收货人
     */
    public String receiver;

    /**
     * 收货人电话
     */
    public String receiverPhone;


    public String countryCode;

    public String city;

    public String postalCode;

    public String address;

    @ManyToOne
    public BtbCustom btbCustom;

    @Transient
    public Long customId;

    /**
     * Shipment 的检查
     */
    public void validate() {
        if(StringUtils.isNotBlank(this.trackNo) && !this.trackNo.equals("[]") && this.type.label().equals("快递"))
            Validation.required("shipment.internationExpress", this.internationExpress);

        // Whouse 不为 null 则需要检查 whouse 与其中的 item 数量是否一致
        if(this.whouse != null) {
            long samewhouseItems = new ShipmentQuery().shipemntItemCountWithSameWhouse(
                    this.id, this.whouse.id);
            if(samewhouseItems != this.items.size())
                Validation.addError("", "运输单中拥有与运输单去往仓库不一样的运输单项目");
        }
        // 避免 trackNo 为 '' 的时候进入唯一性判断错误
        if(StringUtils.isBlank(this.trackNo)) this.trackNo = null;
    }

    public boolean calcuPlanArriveDate() {
        if(this.dates.planBeginDate == null || this.type == null)
            throw new FastRuntimeException("必须拥有 预计发货时间 与 运输类型");
        int plusDay = shipDay();
        this.dates.planArrivDate = new DateTime(this.dates.planBeginDate).plusDays(plusDay).toDate();
        this.dates.planArrivDateForCountRate = this.dates.planArrivDate;
        return true;
    }

    /**
     * 查询运输相关的天数
     *
     * @return
     */
    public int shipDay() {
        String market = this.whouse.country.toLowerCase();
        String name = String.format("%s_%s", market, this.type.name().toLowerCase());
        int day = 7;
        String sql = "select sum(val) as day From ElcukConfig "
                + " where name like '%" + name + "%' ";
        Map<String, Object> row = DBUtils.row(sql);
        if(row != null && row.size() > 0) {
            Object obj = row.get("day");
            if(obj != null) {
                day = Float.valueOf(obj.toString()).intValue();
            }
        }
        return day;
    }


    public List<ProcureUnit> multipleUnitValidate(List<Long> units) {
        List<ProcureUnit> procureUnits = ProcureUnit.find(SqlSelect.whereIn("id", units)).fetch();
        if(procureUnits.size() != units.size())
            Validation.addError("", "提交的采购计划数量与系统存在的不一致!");
        return procureUnits;
    }

    /**
     * 创建B2B运输单
     *
     * @param units
     * @return
     */
    public Shipment buildB2BFromProcureUnits(List<Long> units) {
        List<ProcureUnit> procureUnits = ProcureUnit.find(SqlSelect.whereIn("id", units)).fetch();
        ProcureUnit firstProcureUnit = procureUnits.get(0);
        Date earlyPlanBeginDate = firstProcureUnit.attrs.planShipDate;
        if(StringUtils.isEmpty(this.id)) {
            this.id = Shipment.id();
        }
        this.dates.planBeginDate = earlyPlanBeginDate;
        this.creater = Login.current();
        this.projectName = User.COR.MengTop;
        this.save();
        procureUnits.forEach(unit -> {
            ShipItem shipitem = new ShipItem(unit);
            shipitem.shipment = this;
            this.items.add(shipitem.save());
        });
        return this;
    }

    /**
     * 从采购计划创建运输单
     *
     * @param units
     */
    public Shipment buildFromProcureUnits(List<Long> units) {
        /*
          1. 检查采购计划数量是否一致
          2. 检查运输方式是否一致
          3. 检查快递仓库是否一致或者 海运/空运 国家是否一致
         */
        List<ProcureUnit> procureUnits = multipleUnitValidate(units);
        ProcureUnit firstProcureUnit = procureUnits.get(0);
        Shipment.T firstShipType = this.type != null ? this.type : firstProcureUnit.shipType;
        Date earlyPlanBeginDate = firstProcureUnit.attrs.planShipDate;
        for(ProcureUnit unit : procureUnits) {
            if(unit.selling == null) {
                Validation.addError("", "采购单的selling为空");
                break;
            }
            if(firstShipType != unit.shipType) {
                Validation.addError("", "不同运输方式不可以创建到一个运输单.");
                break;
            }
            earlyPlanBeginDate = new Date(Math.min(earlyPlanBeginDate.getTime(), unit.attrs.planShipDate.getTime()));
        }
        this.type = firstShipType;
        if(this.whouse == null)
            this.whouse = firstProcureUnit.whouse;
        for(ProcureUnit unit : procureUnits) {
            if(unit.whouse == null) {
                Validation.addError("", "采购单仓库为空");
                break;
            }
            if(!this.whouse.id.equals(unit.whouse.id)) {
                Validation.addError("", "去往国家不一样不可以创建到一个运输单");
                break;
            }
        }
        if(Validation.hasErrors()) return this;
        if(StringUtils.isEmpty(this.id)) {
            this.id = Shipment.id();
        }
        this.dates.planBeginDate = earlyPlanBeginDate;
        this.calcuPlanArriveDate();
        this.creater = Login.current();
        this.save();
        procureUnits.forEach(this::addToShip);
        return this;
    }

    public void destroy() {
        /*
         * 0. 检查状态
         * 1. 取消掉关联的运输项目的运输单
         * 2. 删除运输单
         */
        if(this.state != S.PLAN)
            Validation.addError("", "运输单不可以在非 " + S.PLAN.label() + " 状态取消.");
        if(Validation.hasErrors()) return;
        List<ShipItem> shipItems = ShipItem.find("shipment.id=?", this.id).fetch();
        shipItems.forEach(GenericModel::delete);
        this.state = S.CANCEL;
        this.save();
    }

    public void updateShipment() {
        if(this.creater == null) this.creater = User.current();
        this.save();
    }

    public void setTrackNo(String trackNo) {
        if(StringUtils.isNotBlank(trackNo)) this.trackNo = trackNo.trim();
        else this.trackNo = null;
    }

    /**
     * 向运输单中添加一个采购计划
     *
     * @param unit
     */
    public synchronized void addToShip(ProcureUnit unit) {
        if(!Arrays.asList(S.PLAN, S.CONFIRM).contains(this.state))
            Validation.addError("", "只运输向" + S.PLAN.label() + "和" + S.CONFIRM.label() + "添加运输项目");
        if(!unit.whouse.market.equals(this.whouse.market))
            Validation.addError("", "运输目的地不一样, 无法添加");
        if(unit.shipType != this.type)
            Validation.addError("", "运输方式不一样, 无法添加.");
        if(unit.shipItems.size() > 0)
            Validation.addError("", "采购计划已经拥有运输项目, 不可以再重新创建.");
        if(Validation.hasErrors()) return;

        ShipItem shipitem = new ShipItem(unit);
        shipitem.shipment = this;
        this.items.add(shipitem.save());
    }


    public void comment(String cmt) {
        if(!StringUtils.isNotBlank(cmt)) return;
        this.memo = String.format("%s%n%s", cmt, this.memo).trim();
    }

    /**
     * 具体的开始运输
     * <p/>
     * ps: 不允许多个人, 对 Shipment 多次 beginShip
     *
     * @param datetime
     */
    public synchronized void beginShip(Date datetime, boolean sync) {
        /**
         * 0. 检查
         *  0.1 运输单状态 CONFIRM
         *  0.2 货物交货/抵达?
         *  0.3 基础信息
         * 1. 首先更新 FBA 信息
         * 2. 触发采购计划阶段, 时间
         * 3. 触发运输单状态, 时间
         */
        if(this.state != S.CONFIRM) {
            Validation.addError("", "运输单非 " + S.CONFIRM.label() + " 状态, 不可以运输");
        }
        if(this.items.size() <= 0) {
            Validation.addError("", "没有运输项目可以运输.");
        }
        for(ShipItem itm : this.items) {
            if(itm.unit.stage != ProcureUnit.STAGE.OUTBOUND) {
                Validation.addError("", "需要运输的采购计划 #" + itm.unit.id + " 还没有出仓.请联系仓库部门");
            }
        }
        if(this.type == T.EXPRESS && this.internationExpress == null) {
            Validation.addError("", "请填写运输单的国际快递商");
        }
        if(this.cooper == null) {
            Validation.addError("", "请填写运输单合作伙伴(货代)");
        }
        if(this.whouse == null) {
            Validation.addError("", "请填写运输单仓库信息");
        }
        this.arryParamSetUP(FLAG.STR_TO_ARRAY);
        if(this.tracknolist == null || this.tracknolist.size() == 0) {
            Validation.addError("", "请填写运输单的跟踪号");
        }

        if(Validation.hasErrors()) return;
        if(datetime == null) datetime = new Date();

        //只有页面勾选了"同步亚马逊"按钮，才进行亚马逊更新操作
        if(!sync) {
            // 在测试环境下也不能标记 SHIPPED
            this.items.stream().filter(shipItem -> shipItem.unit.fba != null).forEach(shipItem -> {
                if(!Arrays.asList(T.SEA, T.AIR, T.RAILWAY).contains(this.type)) {
                    //暂停提交空运和海运的物流跟踪号到 Amazon(Amazon 要求最长为 10, 而空运和海运的跟踪号一般都超过 10 位)
                    //详情: http://docs.developer.amazonservices.com/en_US/fba_inbound/FBAInbound_Datatypes.html#NonPartneredLtlDataInput
                    shipItem.unit.fba.putTransportContentRetry(3, this);
                }
                // 在测试环境下也不能标记 SHIPPED
                shipItem.unit.fba.updateFBAShipmentRetry(3,
                        Play.mode.isProd() ? FBAShipment.S.SHIPPED : FBAShipment.S.DELETED);
            });
        }

        for(ShipItem shipItem : this.items) {
            shipItem.shipDate = datetime;
            shipItem.qty = shipItem.unit.shipmentQty();
            shipItem.save();
        }
        this.changeRelateProcureUnitStage(ProcureUnit.STAGE.SHIPPING);

        this.dates.beginDate = datetime;
        this.state = S.SHIPPING;
        this.calculationRatio();
        this.save();
    }

    /**
     * 1、快递、专线、空运、铁路体积重与重量取大值 cm/5000
     * 2、海运取实际体积
     * 公式：单个sku的占比=取种值*数量/综合重
     * 注：综合重为所有sku的体积与重量对比取大值*数量的总和
     */
    public void calculationRatio() {
        float totalShipWeight = this.totalWeight();
        float totalShipVolume = this.totalVolume();
        float totalRealWeight = this.totalRealWeight();
        double totalRealVolume = this.totalRealVolume();
        int num = this.items.size();
        float totalWeightRatio = 0f;
        float totalVolumeRatio = 0f;
        float totalRealWeightRatio = 0f;
        for(int i = 0; i < num; i++) {
            ShipItem item = this.items.get(i);
            if(i + 1 == num) {
                item.weightRatio = BigDecimal.valueOf(1 - totalWeightRatio).setScale(
                        4, BigDecimal.ROUND_HALF_UP).floatValue();
                item.volumeRatio = BigDecimal.valueOf(1 - totalVolumeRatio).setScale(
                        4, BigDecimal.ROUND_HALF_UP).floatValue();
                item.finalRatio = BigDecimal.valueOf(1 - totalRealWeightRatio).setScale(
                        4, BigDecimal.ROUND_HALF_UP).floatValue();
            } else {
                double itemWeight = 0f;
                double weight = item.unit.mainBox.singleBoxWeight;
                double itemVolume = item.totalVolume();
                if(weight > 0) {
                    itemWeight = item.unit.mainBox.singleBoxWeight * item.unit.mainBox.boxNum;
                }
                if(totalShipWeight > 0)
                    item.weightRatio = BigDecimal.valueOf(itemWeight)
                            .divide(BigDecimal.valueOf(totalShipWeight), 4, BigDecimal.ROUND_HALF_UP).floatValue();
                if(totalShipVolume > 0)
                    item.volumeRatio = BigDecimal.valueOf(itemVolume)
                            .divide(BigDecimal.valueOf(totalRealVolume), 4, BigDecimal.ROUND_HALF_UP).floatValue();
                if(totalRealWeight > 0) {
                    if(Objects.equals(this.type, T.SEA)) {
                        item.finalRatio = item.volumeRatio;
                    } else {
                        double currentWeight = 0f;
                        double volume = item.unit.mainBox.length * item.unit.mainBox.width * item.unit.mainBox.height;
                        if((volume / 5000) > item.unit.mainBox.singleBoxWeight) {
                            currentWeight = (volume * item.unit.mainBox.boxNum) / 5000;
                        } else {
                            currentWeight = item.unit.mainBox.singleBoxWeight * item.unit.mainBox.boxNum;
                        }
                        item.finalRatio = BigDecimal.valueOf(currentWeight).divide(BigDecimal.valueOf(totalRealWeight),
                                4, BigDecimal.ROUND_HALF_UP).floatValue();
                    }
                }
                totalWeightRatio += item.weightRatio;
                totalVolumeRatio += item.volumeRatio;
                totalRealWeightRatio += item.finalRatio;
            }
            item.save();
        }
    }

    private void shouldSomeStateValidate(S state, String action) {
        if(this.state != state)
            Validation.addError("", "运输单应该在 " + state.label() + " 再进行" + action + "操作");
    }

    /**
     * 运输单状态进行中, 相关的采购计划的阶段也需要变化;
     * 1. beginDate 负责 SHIPPING
     * 2. landPort 负责 SHIP_OVER
     * 3. inbounding 负责 INBOUND
     * 4. endShip 负责 CLOSE
     *
     * @param stage
     */
    private void changeRelateProcureUnitStage(ProcureUnit.STAGE stage) {
        for(ShipItem shipItem : this.items) {
            shipItem.unitStage(stage);
        }
    }

    /**
     * 到港 (运输单抵达港口, 就开始进行清关状态
     */
    public void landPort(Date date) {
        if(this.type != T.EXPRESS) {
            shouldSomeStateValidate(S.SHIPPING, "到港");
            if(Validation.hasErrors()) return;
            if(date == null) date = new Date();
        }
        this.state = S.CLEARANCE;
        this.dates.atPortDate = date;
        this.changeRelateProcureUnitStage(ProcureUnit.STAGE.SHIP_OVER);
        this.save();
    }

    /**
     * 提货 (清关完成后, 就开始进行提货中状态)
     *
     * @param date
     */
    public void pickGoods(Date date) {
        shouldSomeStateValidate(S.CLEARANCE, "提货");

        if(Validation.hasErrors()) return;

        if(date == null) date = new Date();
        this.state = S.BOOKED;
        this.dates.pickGoodDate = date;
        this.save();
    }

    /**
     * 预约 (提货完成, 就开始与 amaozn 预约, 预约中状态)
     *
     * @param date
     */
    public void booking(Date date) {
        shouldSomeStateValidate(S.CLEARANCE, "预约");

        if(Validation.hasErrors()) return;

        if(date == null) date = new Date();
        this.state = S.BOOKED;
        this.dates.bookDate = date;
        this.save();
    }

    /**
     * 派送 (预约完成, 就开始进行派送, 派送中状态)
     *
     * @param beginDeliverDate
     */
    public void beginDeliver(Date beginDeliverDate) {
        if(this.type != T.EXPRESS) {
            shouldSomeStateValidate(S.BOOKED, "派送");
            if(Validation.hasErrors()) return;
            if(beginDeliverDate == null) beginDeliverDate = new Date();
        } else {
            // 快递, 需要将这两个时间补上
            this.dates.pickGoodDate = beginDeliverDate;
            this.dates.bookDate = beginDeliverDate;
        }
        this.state = S.DELIVERYING;
        this.dates.deliverDate = beginDeliverDate;
        this.save();
    }

    /**
     * 签收 (派送完成, 接下来签收, 已签收状态)
     *
     * @param reciptDate
     */
    public void receipt(Date reciptDate) {
        if(this.type != T.EXPRESS) {
            shouldSomeStateValidate(S.DELIVERYING, "签收");
            if(reciptDate == null) reciptDate = new Date();
            if(Validation.hasErrors()) return;
        }
        this.state = S.RECEIPTD;
        this.dates.receiptDate = reciptDate;
        this.save();
    }

    /**
     * 入库 (签收, 就开始进行入库, 入库中状态)
     *
     * @param date
     */
    public void inbounding(Date date) {
        shouldSomeStateValidate(S.RECEIPTD, "入库");
        if(Validation.hasErrors()) return;
        if(date == null) date = new Date();
        this.state = S.RECEIVING;
        this.dates.inbondDate = date;
        this.changeRelateProcureUnitStage(ProcureUnit.STAGE.INBOUND);
        this.save();
    }

    /**
     * 对海运运输单进行入库中状态的自动计算
     */
    public void inboundingByComputor() {
        if(this.state != S.RECEIPTD) return;
        List<Date> receivingDates = new ArrayList<>();
        for(FBAShipment fba : this.fbas()) {
            if(!Arrays.asList(FBAShipment.S.RECEIVING, FBAShipment.S.CLOSED).contains(fba.state)) continue;
            F.Option<Date> earliestDate = fba.getEarliestDate();
            if(earliestDate.isDefined())
                receivingDates.add(earliestDate.get());
        }
        if(receivingDates.size() > 0) {
            Collections.sort(receivingDates);
            this.inbounding(receivingDates.get(0));
        }
    }

    /**
     * 当前运输单中的所有运输项目的 recivedQty == qty 的时候, 运输单完成.
     */
    public void endShipByComputer() {
        if(this.state != S.RECEIVING) return;
        DateTime now = DateTime.now();
        if(now.isBefore(this.dates.inbondDate.getTime()))
            Validation.addError("", "结束时间不可能早于入库事件(自动检测运输单完成, 可忽略)");
        if(Validation.hasErrors()) return;
        int hundredSize = 0;
        for(ShipItem shipitem : this.items) {
            if(shipitem.qty.equals(shipitem.recivedQty))
                hundredSize++;
        }
        if(hundredSize == this.items.size()) {
            endShip(now.toDate());
        }
    }

    /**
     * 手动确认运输单完成
     *
     * @param date
     */
    public void endShipByHand(Date date) {
        if(date == null) date = new Date();
        shouldSomeStateValidate(S.RECEIVING, "完成运输");
        if(date.getTime() < this.dates.inbondDate.getTime())
            Validation.addError("", "结束时间不可能早于入库事件");
        if(Validation.hasErrors()) return;
        endShip(date);
        new ElcukRecord("shipment.endShipByHand", "手动完成", this.id).save();
    }

    private void endShip(Date date) {
        this.state = S.DONE;
        this.dates.arriveDate = date;
        this.changeRelateProcureUnitStage(ProcureUnit.STAGE.CLOSE);
        this.save();
    }

    /**
     * 返回 Shipment 的上一个状态 [PLAN 与 SHIPPING 除外]
     */
    public void revertState() {
        if(Arrays.asList(S.PLAN, S.SHIPPING).contains(this.state))
            Validation.addError("", "当前状态是是不允许撤销的.");
        if(Validation.hasErrors()) return;

        if(this.state == S.DONE) {
            this.state = S.RECEIVING;
            this.dates.arriveDate = null;
            this.changeRelateProcureUnitStage(ProcureUnit.STAGE.INBOUND);
        } else if(this.state == S.RECEIVING) {
            this.state = S.RECEIPTD;
            this.dates.inbondDate = null;
        } else if(this.state == S.RECEIPTD) {
            this.state = S.DELIVERYING;
            this.dates.receiptDate = null;
        } else if(this.state == S.DELIVERYING) {
            this.state = S.BOOKED;
            this.dates.deliverDate = null;
        } else if(this.state == S.BOOKED) {
            this.state = S.CLEARANCE;
            this.dates.bookDate = null;
        } else if(this.state == S.CLEARANCE) {
            this.state = S.SHIPPING;
            this.dates.atPortDate = null;
        } else if(this.state == S.CONFIRM)
            this.state = S.PLAN;
        this.save();
    }

    public void logEvent(String msg) {
        new ERecordBuilder("shipment.logEvent")
                .msgArgs(this.state.label(), msg)
                .fid(this.id)
                .save();
    }

    public List<ElcukRecord> logEvents() {
        return ElcukRecord.records(this.id, Messages.get("shipment.logEvent"));
    }

    /**
     * 对于运输单可以通过其特殊格式的 id 来获取所有 Records
     *
     * @return
     */
    public List<ElcukRecord> allRecords() {
        return ElcukRecord.records(this.id);
    }

    @Override
    public String toLog() {
        StringBuilder sbd = new StringBuilder("[id:").append(this.id).append("] ");
        sbd.append("[运输:").append(Dates.date2Date(this.dates.planBeginDate)).append("] ")
                .append("[到库:").append(Dates.date2Date(this.dates.planArrivDate)).append("] ");
        if(this.cooper != null) sbd.append("[货代:").append(this.cooper.name).append("] ");
        if(this.whouse != null) sbd.append("[仓库:").append(this.whouse.name()).append("]");
        return sbd.toString();
    }

    /**
     * 抓取 DHL, FEDEX, UPS 网站的运输信息, 更新系统中 SHIPMENT 的状态;
     * PS: 只有快递类型的运输单才可以自动跟踪
     * ([SHIPPING] -> CLEARANCE -> DELIVERYING -> RECEIPTD)
     *
     * @return
     */
    public S monitor() {
        // 仅仅是 快递 才有自动监控
        if(this.type != T.EXPRESS) return this.state;
        F.T2<Boolean, DateTime> result = null;
        if(this.state == S.SHIPPING) {
            result = this.internationExpress.isClearance(this.iExpressHTML);
            if(result._1)
                this.landPort(result._2.toDate());
        } else if(this.state == S.CLEARANCE) {
            result = this.internationExpress.isDelivered(this.iExpressHTML);
            if(result._1)
                this.beginDeliver(result._2.toDate());
        } else if(Arrays.asList(S.BOOKED, S.DELIVERYING).contains(this.state)) {
            result = this.internationExpress.isReceipt(this.iExpressHTML);
            if(result._1)
                this.receipt(result._2.toDate());
        } else if(this.state == S.RECEIPTD) {
            this.inboundingByComputor();
        }
        return this.state;
    }

    /**
     * 从外部网站获取需要的 html 代码信息
     *
     * @return
     */
    public String trackWebSite() {
        if(StringUtils.isBlank(this.trackNo)) {
            Logger.warn("Shipment %s do not have trackNo.", this.id);
            return "";
        }

        this.arryParamSetUP(Shipment.FLAG.STR_TO_ARRAY);
        if(this.tracknolist == null || this.tracknolist.size() <= 0) return "";
        String onetrackno = this.tracknolist.get(0);
        if(StringUtils.isBlank(onetrackno)) return "";

        Logger.info("Shipment sync from [%s]", this.internationExpress.trackUrl(onetrackno));
        String html = this.internationExpress.fetchStateHTML(onetrackno);
        try {
            this.iExpressHTML = this.internationExpress.parseExpress(html, onetrackno);
        } catch(Exception e) {
            FLog.fileLog(String.format("%s.%s.%s.html", this.id, onetrackno,
                    this.internationExpress.name()), html, FLog.T.HTTP_ERROR);
            throw new FastRuntimeException(Webs.s(e));
        }
        return this.iExpressHTML;
    }

    /**
     * 记录 Shipment 产生的费用条目
     *
     * @param fee
     */
    public void produceFee(PaymentUnit fee) {
        if(fee.currency == null) Validation.addError("", "币种必须存在");
        if(fee.feeType == null) Validation.addError("", "费用类型必须存在");
        if(fee.unitQty <= 0f) Validation.addError("", "数量必须大于等于 0");
        // 海运/空运的运输运费无法绑定运输项目, 只能平摊
        if(this.type == T.EXPRESS && FeeType.expressFee().equals(fee.feeType))
            Validation.addError("", "快递的运输费用需要通过运输项目记录");

        if(Validation.hasErrors()) return;
        fee.shipment = this;
        fee.payee = User.current();
        fee.amount = fee.unitQty * fee.unitPrice;
        this.fees.add(fee);
        this.save();

        //2018-02-05 要求请款操作修改请款单的 updateAt
        if(this.apply != null) {
            this.apply.updateAt = new Date();
            this.apply.save();
        }
        new ERecordBuilder("paymentunit.applynew").msgArgs(fee.currency, fee.amount(), fee.feeType.nickName)
                .fid(fee.shipment.id).save();
    }

    /**
     * 计算当前运输单所有项目的预计关税.
     * 当前的自动关税计算, 仅仅产生一项关税, 多次创建则被忽略
     */
    public void applyShipItemDuty() {
        FeeType transportDuty = FeeType.dutyAndVAT();
        for(ShipItem itm : this.items) {
            if(PaymentUnit.count("feeType=? AND shipItem=?", transportDuty, itm) > 0) continue;
            PaymentUnit fee = new PaymentUnit();
            //TODO 这里本应该为 HKD 但现在业务为 CNY 所以暂时以 CNY 存在
            fee.currency = Currency.CNY;
            fee.unitPrice = Webs.scalePointUp(4, (float) (itm.unit.product.declaredValue * 6.35 * 0.2));
            fee.unitQty = itm.qty;
            fee.cooperator = this.cooper;
            itm.produceFee(fee, transportDuty);
            if(Validation.hasErrors()) return;

            fee.memo = String.format("%s %s = %s(申报价) * 6.35 * 0.2 * %s(运输数量)",
                    fee.currency.symbol(), fee.amount(), itm.unit.product.declaredValue, itm.qty);
            fee.save();
        }
    }

    /**
     * 根据输入的最终关税金额, 计算还需支付的关税金额
     */
    public PaymentUnit calculateDuty(helper.Currency crcy, Float amount) {
        if(crcy == null) Validation.addError("", "币种必须确定.");
        /**
         * 1. 检查已经存在的关税币种是否一致, 不一致提示需要对关税进行处理
         * 2. 统计所有已经支付的关税金额
         * 3. 计算出关税差额
         */
        FeeType duty = FeeType.dutyAndVAT();
        if(duty == null) Validation.addError("", "关税费用类型不存在, 请在 transport 下添加 transportduty 关税类型.");
        this.fees.stream().filter(fee -> fee.feeType.equals(duty) && !fee.currency.equals(crcy))
                .forEach(fee -> Validation.addError("", "关税费用应该为统一币种, 请核实关税请款信息."));

        if(Validation.hasErrors()) return null;

        float paidAmount = 0;
        List<String> lines = new ArrayList<>();
        // TODO 把 Comment 更换为 record?
        lines.add(String.format("总关税 %s %s 减去 ", crcy, amount));
        for(PaymentUnit fee : this.fees) {
            if(!fee.feeType.equals(duty)) continue;
            lines.add(String.format("#%s %s %s %s", fee.id, fee.currency, fee.amount(), fee.feeType.nickName));
            paidAmount += fee.amount();
        }

        PaymentUnit leftDuty = new PaymentUnit();
        leftDuty.feeType = duty;
        leftDuty.amount = amount - paidAmount;
        leftDuty.unitPrice = leftDuty.amount;
        leftDuty.unitQty = 1;
        leftDuty.currency = crcy;
        leftDuty.payee = User.current();
        leftDuty.shipment = this;
        leftDuty.state = PaymentUnit.S.APPLY;
        leftDuty.memo = StringUtils.join(lines, ", ");
        this.fees.add(leftDuty);
        this.save();
        new ERecordBuilder("paymentunit.applynew")
                .msgArgs(leftDuty.currency, leftDuty.amount(), leftDuty.feeType.nickName)
                .fid(leftDuty.shipment.id)
                .save();
        return leftDuty.save();
    }

    /**
     * 加载运输相关的 Config
     *
     * @return
     */
    public ElcukConfig config(String dayType) {
        String market = this.whouse.country.toLowerCase();
        String name = String.format("%s_%s_%s", market, this.type.name().toLowerCase(), dayType);
        ElcukConfig config = ElcukConfig.findByName(name);
        if(!Optional.ofNullable(config).isPresent()) {
            ElcukConfig elcuk = new ElcukConfig();
            elcuk.fullName = String.format("%s %s %s", this.whouse.market.countryName(), this.type.label(), dayType);
            elcuk.name = name;
            elcuk.val = String.valueOf(1);
            return elcuk.save();
        }
        return ElcukConfig.findByName(name);
    }

    public String title() {
        if(StringUtils.isBlank(this.title))
            return String.format("Default Title: ShipmentId %s", this.id);
        return String.format("[%s] %s", this.id, this.title);
    }

    /**
     * 通过产品数据计算出来的这份运输单的总重量
     * 总重量, 需要根据体积/重量的运输算法来计算
     *
     * @return
     */
    public float totalWeight() {
        float totalWeight = 0f;
        for(ShipItem itm : this.items) {
            if(itm.unit == null) continue;
            Float weight = itm.unit.product.weight;
            if(weight != null) {
                totalWeight += itm.qty * weight;
            }
        }
        return totalWeight;
    }

    public float totalRealWeight() {
        float totalWeight = 0f;
        for(ShipItem itm : this.items) {
            if(itm.unit == null || itm.unit.mainBox == null) continue;
            Double volume = itm.unit.mainBox.length * itm.unit.mainBox.width * itm.unit.mainBox.height;
            Logger.info(itm.unit.product.sku);
            if((volume / 5000) > itm.unit.mainBox.singleBoxWeight) {
                totalWeight += (volume * itm.unit.mainBox.boxNum) / 5000;
            } else {
                totalWeight += itm.unit.mainBox.singleBoxWeight * itm.unit.mainBox.boxNum;
            }
        }
        return totalWeight;
    }

    public float totalVolume() {
        float totalVolume = 0f;
        for(ShipItem itm : this.items) {
            if(itm.unit == null) continue;
            Float volume = (itm.unit.product.lengths == null ? 0 : itm.unit.product.lengths)
                    * (itm.unit.product.width == null ? 0 : itm.unit.product.width)
                    * (itm.unit.product.heigh == null ? 0 : itm.unit.product.heigh);
            totalVolume += itm.qty * volume / 1000000000;
        }
        return totalVolume;
    }

    public double totalRealVolume() {
        return this.items.stream().filter(item -> item.unit != null && item.unit.mainBox != null)
                .mapToDouble(item -> (item.unit.mainBox.length * item.unit.mainBox.length
                        * item.unit.mainBox.length * item.unit.mainBox.boxNum) / 1000000).sum();
    }

    /**
     * 通过产品数据计算出来的这份运输单的质检总重量
     *
     * @return
     */
    public Double totalWeightQuaTest() {
        Double weight = 0d;
        for(ShipItem itm : this.items) {
            weight += itm.caluTotalWeightByCheckTask();
        }
        return weight;
    }

    /**
     * 通过产品数据计算出来的这份运输单的质检总体积
     *
     * @return
     */
    public Double totalVolumeQuaTest() {
        Double volume = 0d;
        for(ShipItem itm : this.items) {
            volume += itm.caluTotalVolumeByCheckTask();
        }
        return volume;
    }

    /**
     * 通过产品数据计算出来的这份运输单的质检总箱数
     *
     * @return
     */
    public Integer totalUnitQuaTest() {
        Integer totalUnit = 0;
        for(ShipItem itm : this.items) {
            totalUnit += itm.caluTotalUnitByCheckTask();
        }
        return totalUnit;
    }

    /**
     * 将运输单从其存在的运输请款单中剥离
     */
    public void departFromApply() {
        if(this.apply == null)
            Validation.addError("", "运输单没有添加进入请款单, 不需要剥离");
        for(PaymentUnit fee : this.fees) {
            if(Arrays.asList(PaymentUnit.S.PAID, PaymentUnit.S.APPROVAL).contains(fee.state)) {
                Validation.addError("", "运输单中已经有运输请款项目被批准或付款, 无法剥离");
                break;
            }
            if(fee.payment != null) {
                Validation.addError("", "运输单中已经有产生支付单, 无法剥离");
                break;
            }
        }
        if(Validation.hasErrors()) return;
        new ERecordBuilder("shipment.departFromApply")
                .msgArgs(this.id, this.apply.serialNumber)
                .fid(this.apply.id).save();
        this.apply = null;
        this.save();
    }

    @Override
    public String toString() {
        if(Objects.equals(this.projectName, User.COR.MengTop))
            return String.format("%s 开往 %s", this.id, this.target);
        else
            return String.format("%s 开往 %s", this.id, this.whouse.name);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Shipment shipment = (Shipment) o;

        if(id != null ? !id.equals(shipment.id) : shipment.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    public List<FBAShipment> fbas() {
        List<FBAShipment> fbas = new ArrayList<>();
        for(ShipItem item : this.items) {
            if(item.unit.fba == null) continue;
            if(fbas.contains(item.unit.fba)) continue;
            fbas.add(item.unit.fba);
        }
        return fbas;
    }

    /**
     * 当前运输单与运输项目的所有费用
     *
     * @return
     */
    public List<PaymentUnit> allFees() {
        return this.fees;
    }

    /**
     * 计算 Shipment 的 ID
     *
     * @return
     */
    public static String id() {
        DateTime dt = DateTime.now();
        DateTime nextMonth = dt.plusMonths(1);
        String count = Shipment.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear()))
                        .toDate(),
                DateTime.parse(
                        String.format("%s-%s-01", nextMonth.getYear(), nextMonth.getMonthOfYear()))
                        .toDate()) + "";
        return String.format("SP|%s|%s", dt.toString("yyyyMM"),
                count.length() == 1 ? "0" + count : count);
    }

    /**
     * 根据 仓库 与 运输类型查找运输单, 在查找的同时, 也会自动检查是否需要创建固定周期的运输单
     *
     * @param whouseId
     * @param shipType
     * @return
     */
    public static List<Shipment> findUnitRelateShipmentByWhouse(Long whouseId, T shipType, Date planDeliveryDate) {
        /**
         * 1. 判断是否有过期的周期型运输单, 有的话自动关闭
         * 2. 判断是否需要创建新的周期型运输单, 有的话自动创建
         * 3. 加载可使用的运输单
         */
        // 自动关闭.
        List<Shipment> overDueShipments = Shipment.find("state IN (?,?) AND planBeginDate<=?",
                S.PLAN, S.CONFIRM, DateTime.now().minusDays(3).toDate()).fetch();
        for(Shipment shipment : overDueShipments) {
            if(shipment.items.size() > 0)
                continue;
            shipment.state = S.CANCEL;
            shipment.comment("运输单过期, 自动 CANCEL");
            shipment.save();
        }

        // 自动创建
        List<Shipment> planedShipments = Shipment.find("state IN(?,?) AND planBeginDate>=? AND planBeginDate<=? "
                        + "AND projectName = ? ", S.PLAN, S.CONFIRM, new Date(), DateTime.now().plusDays(60).toDate(),
                User.COR.valueOf(OperatorConfig.getVal("brandname"))).fetch();
        //确定仓库接收的运输单
        List<Whouse> whs = Whouse.find("type=?", Whouse.T.FBA).fetch();
        for(Whouse whouse : whs) {
            whouse.checkWhouseNewShipment(planedShipments);
        }
        // 加载
        StringBuilder where = new StringBuilder("state IN (?,?)");
        List<Object> params = new ArrayList<>(Arrays.asList(S.PLAN, S.CONFIRM));
        if(whouseId != null) {
            Whouse whouse = Whouse.findById(whouseId);
            where.append("AND (whouse.market=? OR whouse.id IS NULL)");
            params.add(whouse.market);
        } else {
            where.append("AND whouse.id IS NULL");
        }
        where.append(" AND type =?");
        params.add(shipType);
        where.append(" AND dates.planBeginDate >= ?");
        params.add(Dates.morning(new Date()));
        where.append(" ORDER BY planBeginDate");
        return Shipment.find(where.toString(), params.toArray()).fetch();
    }

    /**
     * 由于 Play 无法将 models 目录下的 Enum 加载, 所以通过 model 提供一个暴露方法在 View 中使用
     *
     * @return
     */
    public static List<iExpress> express() {
        return Arrays.asList(iExpress.values());
    }

    public static List<Shipment> similarShipments(Date planBeginDate, Whouse whouse, T shipType) {
        if(whouse == null)
            return new ArrayList<>();
        else
            return Shipment.find("planBeginDate>=? AND whouse.market=? AND type=? ORDER BY planBeginDate",
                    planBeginDate, whouse.market, shipType).fetch();
    }

    public static List<Shipment> findByState(S... state) {
        return Shipment.find(SqlSelect.whereIn("state", state) + " ORDER BY createDate DESC").fetch();
    }

    public static List<Shipment> findByTypeAndStates(T type, S... state) {
        return Shipment.find(SqlSelect.whereIn("state", state) + " AND type=? ORDER BY createDate", type)
                .fetch();
    }

    /**
     * 检查并且创建运输单
     *
     * @param planBeginDate
     * @param type
     */
    public static void checkNotExistAndCreate(Date planBeginDate, Shipment.T type, Whouse whouse) {
        if(Shipment.count("planBeginDate=? AND whouse=? AND type=? AND state IN (?,?)",
                planBeginDate, whouse, type, S.PLAN, S.CONFIRM) > 0)
            return;
        new Shipment(planBeginDate, type, whouse).save();
    }

    public static final HashMap<String, Integer> MINIMUM_TRAFFICMAP = new HashMap<>();

    /**
     * 初始化不同运输方式的标准运输量对应关系
     */
    static {
        MINIMUM_TRAFFICMAP.put("AMAZON_DE_SEA", 500);
        MINIMUM_TRAFFICMAP.put("AMAZON_DE_AIR", 700);

        MINIMUM_TRAFFICMAP.put("AMAZON_UK_SEA", 500);
        MINIMUM_TRAFFICMAP.put("AMAZON_UK_AIR", 700);

        MINIMUM_TRAFFICMAP.put("AMAZON_US_SEA", 500);
        MINIMUM_TRAFFICMAP.put("AMAZON_US_AIR", 700);

        MINIMUM_TRAFFICMAP.put("AMAZON_IT_SEA", 500);
        MINIMUM_TRAFFICMAP.put("AMAZON_IT_AIR", 700);

        MINIMUM_TRAFFICMAP.put("AMAZON_FR_SEA", 500);
        MINIMUM_TRAFFICMAP.put("AMAZON_FR_AIR", 700);

        MINIMUM_TRAFFICMAP.put("AMAZON_JP_SEA", 500);
        MINIMUM_TRAFFICMAP.put("AMAZON_JP_AIR", 700);

        MINIMUM_TRAFFICMAP.put("AMAZON_CA_SEA", 500);
        MINIMUM_TRAFFICMAP.put("AMAZON_CA_AIR", 700);
    }

    public String minimumTrafficMapKey() {
        return String.format("%s_%s", this.whouse.account.type.name(), this.type.name());
    }

    /**
     * 获得不同运输方式的标准运输量
     *
     * @return
     */
    public float minimumTraffic() {
        //海运和空运暂时的最小运输量是500 而快递是不能超过500
        String key = this.minimumTrafficMapKey();
        if(MINIMUM_TRAFFICMAP.containsKey(key)) {
            return MINIMUM_TRAFFICMAP.get(key);
        } else {
            return 500;
        }
    }

    public void sendMsgMail(Date planArrivDate, String username) {
        if(planArrivDate != null) {
            String subject = "";
            String content = "";
            List<String> mailaddress = new ArrayList<>();

            if(this.dates.planArrivDate.compareTo(planArrivDate) != 0) {
                subject = String.format("更改运输单[%s]预计到库时间", this.id);
                content = String.format("运输单%s预计到库时间从:%s 更改为:%s,更改人:%s,请确认!运输单地址:%s/shipment/%s"
                        , this.id, Dates.date2Date(this.dates.planArrivDate), Dates.date2Date(planArrivDate),
                        username, System.getenv(Constant.ROOT_URL), this.id);
                List<ProcureUnit> punits = ProcureUnit
                        .find("SELECT DISTINCT p FROM ProcureUnit p LEFT JOIN p.shipItems si"
                                + " LEFT JOIN si.shipment sp where sp.id=?", this.id).fetch();
                for(ProcureUnit pu : punits) {
                    String email = pu.handler.email;
                    if(StringUtils.isNotBlank(email)) {
                        if(!mailaddress.contains(email)) {
                            mailaddress.add(email);
                            LogUtils.JOBLOG.info("Email:::" + email);
                        }
                    }
                }
                if(mailaddress.size() > 0) Webs.systemMail(subject, content, mailaddress);
            }
        }
    }

    /**
     * 将产品定位属性转换成 String 存入DB
     * 或者将 String 转换成 List
     *
     * @param flag
     */
    public void arryParamSetUP(FLAG flag) {
        if(flag.equals(FLAG.ARRAY_TO_STR)) {
            /*
             * 在转换成Json字符串之前需要对空字符串做一点处理
             */
            this.trackNo = this.listToStr(this.tracknolist);
            if(this.trackNo.equals("{}") || this.trackNo.equals("[\"\"]") || this.trackNo.equals("[]")) {
                this.trackNo = null;
            }
        } else {
            if(StringUtils.isNotBlank(this.trackNo)) {
                if(!trackNo.contains("[")) {
                    this.trackNo = "[\"" + this.trackNo + "\"]";
                }
                try {
                    List<String> trackingNumbers = JSON.parseArray(this.trackNo, String.class);
                    for(int i = 0; i < trackingNumbers.size(); i++) {
                        trackingNumbers.set(i, StringUtils.trim(trackingNumbers.get(i)));
                    }
                    this.tracknolist = trackingNumbers;
                } catch(Exception e) {
                    LogUtils.JOBLOG.info(this.trackNo + "--" + e.getMessage());
                }
            }

        }
    }

    /**
     * 对空字符进行处理
     *
     * @return
     */
    private List<String> fixNullStr(List<String> target) {
        target.removeIf(Objects::isNull);
        return target;
    }


    /**
     * 对空字符进行处理
     *
     * @return
     */
    private String listToStr(List<String> target) {
        Iterator<String> iterator = target.iterator();
        String str = "[";
        while(iterator.hasNext()) {
            String p = iterator.next();
            if(null == p || p.trim().equals("")) {
                iterator.remove();
            } else {
                String[] args = p.split(",");
                for(int i = 0; i < args.length; i++) {
                    str = str + J.json(args[i].replace(" ", "")) + ",";
                }
            }
        }
        if(!str.equals("[")) {
            str = str.substring(0, str.length() - 1);
        }
        str = str + "]";
        return str;
    }

    /**
     * 生成 InvoiceNO
     * <p/>
     * 发票编号Invoice NO.规则:
     * 1.快递: E + 当前日期年月日＋该运输单的CenterID+“-”+“两位数序号”(如01,02,03等依次变)
     * 2.空运: A + 当前日期年月日＋该运输单的CenterID+“-”+“两位数序号”(如01,02,03等依次变)
     * 3.海运: S + 当前日期年月日＋该运输单的CenterID+“-”+“两位数序号”(如01,02,03等依次变)
     *
     * @return
     */
    public String buildInvoiceNO() {
        if(StringUtils.isNotBlank(this.invoiceNo)) return this.invoiceNo;

        //特性: 相同运输方式 + 相同日期 + 相同 CenterID 共享两位数序号(才需要递增 01 02 03...，否则的话需要从 01 开始)
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String now = formatter.format(this.dates.planBeginDate);
        String maxInvoiceNo = this.fetchMaxInvoiceNoForDB();
        Integer invoice = 0;
        if(maxInvoiceNo != null) {
            invoice = NumberUtils.toInt(StringUtils.substring(maxInvoiceNo, maxInvoiceNo.length() - 2));
        }
        ++invoice;
        this.invoiceNo = String.format("%s%s%s-%s", this.invoiceNOTitle(), now, this.fetchCenterId(),
                invoice < 10 ? ("0" + invoice) : invoice);
        this.save();
        return this.invoiceNo;
    }

    public String fetchMaxInvoiceNoForDB() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        String now = formatter.format(this.dates.planBeginDate);
        List<Shipment> shipments = Shipment.find("invoiceNo like ?",
                String.format("%s%s%s%s", this.invoiceNOTitle(), now, this.fetchCenterId(), "%")).fetch();
        if(shipments == null || shipments.size() == 0) return null;

        //开始排序 从小到大
        Collections.sort(shipments, (o1, o2) -> {
            Integer invoiceNo1 = NumberUtils.toInt(StringUtils.substring(o1.invoiceNo, o1.invoiceNo.length() - 2));
            Integer invoiceNo2 = NumberUtils.toInt(StringUtils.substring(o2.invoiceNo, o2.invoiceNo.length() - 2));
            return invoiceNo1.compareTo(invoiceNo2);
        });
        return shipments.get(shipments.size() - 1).invoiceNo;
    }

    public String invoiceNOTitle() {
        switch(this.type) {
            case EXPRESS:
                return "E";
            case AIR:
                return "A";
            case SEA:
                return "S";
            default:
                return null;
        }
    }

    /**
     * 获取运输单的 CenterId
     * <p/>
     * 一个运输单里 CenterID 都是一样的
     *
     * @return
     */
    public String fetchCenterId() {
        for(ShipItem shipItem : this.items) {
            if(shipItem.unit != null) {
                if(shipItem.unit.fba != null) {
                    return shipItem.unit.fba.centerId.toUpperCase();
                }
            }
        }
        return null;
    }

    public String showTrackNo() {
        StringBuilder showTrackNo = new StringBuilder();
        this.arryParamSetUP(FLAG.STR_TO_ARRAY);
        for(String track : this.tracknolist) {
            showTrackNo.append(track).append(",");
        }
        return showTrackNo.substring(0, showTrackNo.length() - 1);
    }

    public static void handleQty1(List<Shipment> shipments, Shipment ship) {
        if(shipments != null && shipments.size() > 0) {
            for(Shipment shipment : shipments) {
                for(ShipItem shipItem : shipment.items) {
                    if(shipItem.recivedLogs().size() == 0) {
                        shipItem.adjustQty = shipItem.recivedQty;
                        shipItem.save();
                    }
                }
            }
        }
        if(ship != null) {
            for(ShipItem shipItem : ship.items) {
                if(shipItem.recivedLogs().size() == 0) {
                    shipItem.adjustQty = shipItem.recivedQty;
                    shipItem.save();
                }
            }
        }
    }

    /**
     * 修改运输单
     *
     * @param newShip
     */
    public void update(Shipment newShip) {
        if(Objects.equals(this.projectName, User.COR.MengTop)) {
            this.receiver = newShip.receiver;
            this.receiverPhone = newShip.receiverPhone;
            this.countryCode = newShip.countryCode;
            this.city = newShip.city;
            this.address = newShip.address;
            this.postalCode = newShip.postalCode;
            this.tradeMode = newShip.tradeMode;
            this.btbCustom = BtbCustom.findById(newShip.customId);
            this.type = newShip.type;
        }

        this.cooper = newShip.cooper;
        this.whouse = newShip.whouse;
        this.title = newShip.title;
        this.reason = newShip.reason;
        this.tracknolist = newShip.tracknolist;
        this.trackNo = newShip.trackNo;
        this.memo = newShip.memo;
        this.source = newShip.source;
        this.target = newShip.target;
        if(newShip.dates != null && newShip.dates.planBeginDate != null) {
            if(this.dates == null) {
                this.dates = new ShipmentDates();
            }
            this.dates.planBeginDate = newShip.dates.planBeginDate;
        }
        this.internationExpress = newShip.internationExpress;
        this.jobNumber = newShip.jobNumber;
        this.totalWeightShipment = newShip.totalWeightShipment;
        this.totalVolumeShipment = newShip.totalVolumeShipment;
        this.shipmentTpye = newShip.shipmentTpye;
        this.totalStockShipment = newShip.totalStockShipment;
        this.arryParamSetUP(Shipment.FLAG.ARRAY_TO_STR);

        //日期发生改变则记录旧的日期
        if(this.dates != null) {
            if(this.dates.planArrivDate != null && this.dates.planArrivDate.compareTo(newShip.dates.planArrivDate) != 0
                    && this.dates.oldPlanArrivDate == null)
                this.dates.oldPlanArrivDate = this.dates.planArrivDate;
            this.dates.planArrivDate = newShip.dates.planArrivDate;
        }

        //只有 PLAN 与 CONFIRM 状态下的运输单才能够修改计算准时率预计到库时间
        if(Arrays.asList(Shipment.S.PLAN, Shipment.S.CONFIRM).contains(this.state) && this.dates != null
                && this.dates.planArrivDateForCountRate != newShip.dates.planArrivDateForCountRate) {
            if(!StringUtils.isBlank(newShip.reason)) {
                this.reason = newShip.reason;
            }
            this.dates.planArrivDateForCountRate = newShip.dates.planArrivDateForCountRate;
            this.dates.beginDate = newShip.dates.beginDate;
        }
        this.validate();
        Validation.current().valid(this);
    }

    public String showRealDay() {
        if(this.dates != null && this.dates.beginDate != null && this.dates.arriveDate != null) {
            long day = (this.dates.arriveDate.getTime() - this.dates.beginDate.getTime()) / 1000 / 3600 / 24;
            return day + "";
        }
        return "";
    }

    public float totalCNYCost() {
        Float totalCost = 0f;
        for(PaymentUnit unit : fees) {
            totalCost += unit.currency.toCNY(unit.amount());
        }
        return totalCost;
    }

    public boolean isPaid() {
        return this.fees.size() > 0 && this.fees.get(0).payment.paymentDate != null;
    }

    public double totalPaidCNYCost() {
        return fees.stream().filter(unit -> Objects.equals(unit.state, PaymentUnit.S.PAID))
                .mapToDouble(unit -> unit.currency.toCNY(unit.amount())).sum();
    }

    public double totalNoPaidCNYCost() {
        return fees.stream().filter(unit -> !Objects.equals(unit.state, PaymentUnit.S.PAID))
                .mapToDouble(unit -> unit.currency.toCNY(unit.amount())).sum();
    }

    public Date getPaidDate() {
        if(this.fees.size() > 0) {
            return this.fees.get(0).payment.paymentDate;
        }
        return null;
    }

    public float calPrescription() {
        return (float) (Dates.morning(this.dates.receiptDate).getTime()
                - Dates.morning(this.dates.planBeginDate).getTime()) / (1000 * 60 * 60 * 24);
    }

    public static String showSplitFirst(String key) {
        return key.split(":")[0];
    }

    public static String showSplitSecond(String key) {
        return key.split(":")[1];
    }

}
