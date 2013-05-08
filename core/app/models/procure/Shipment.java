package models.procure;

import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.FLog;
import helper.Webs;
import models.ElcukRecord;
import models.User;
import models.finance.PaymentUnit;
import models.product.Whouse;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.helper.SqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.ShipmentQuery;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * 一张运输单
 * User: wyattpan
 * Date: 6/17/12
 * Time: 5:32 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Shipment extends GenericModel implements ElcukRecord.Log {


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
        this.beginDate = shipment.beginDate;
        this.planBeginDate = shipment.planBeginDate;
        this.planArrivDate = shipment.planArrivDate;
        // FBA 不做处理
        this.pype = shipment.pype;
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
        this.planBeginDate = planBeginDate;
        this.type = type;
        this.calcuPlanArriveDate();
        this.whouse = whouse;
        this.title = String.format("%s 去往 %s 在 %s", this.id, this.whouse.name(),
                Dates.date2Date(this.planBeginDate));
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
        },
        /**
         * 空运
         */
        AIR {
            @Override
            public String label() {
                return "空运";
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
        };

        public abstract String label();
    }

    public enum S {
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
        /**
         * 运输中
         */
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

        PACKAGE {
            @Override
            public String label() {
                return "提货中";
            }
        },

        BOOKING {
            @Override
            public String label() {
                return "预约中";
            }
        },

        DELIVERYING {
            @Override
            public String label() {
                return "派送中";
            }
        },

        RECEIVING {
            @Override
            public String label() {
                return "入库中";
            }
        },

        /**
         * 完成
         */
        DONE {
            @Override
            public String label() {
                return "完成";
            }
        },

        /**
         * 取消状态
         */
        CANCEL {
            @Override
            public String label() {
                return "取消";
            }
        };

        public abstract String label();
    }

    public enum P {
        /**
         * 重量计价
         */
        WEIGHT,
        /**
         * 体积计价
         */
        VOLUMN
    }

    /**
     * 此 Shipment 的运输项
     */
    @OneToMany(mappedBy = "shipment", cascade = {CascadeType.PERSIST})
    public List<ShipItem> items = new ArrayList<ShipItem>();

    @OneToMany(mappedBy = "shipment", orphanRemoval = true, fetch = FetchType.LAZY)
    public List<PaymentUnit> fees = new ArrayList<PaymentUnit>();

    /**
     * 运输合作商
     */
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public Cooperator cooper;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
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

    /**
     * 此货运单人工创建的时间
     */
    @Expose
    @Required
    public Date createDate = new Date();

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    @Expose
    @Required
    public S state = S.PLAN;

    /**
     * 预计发货日期
     */
    @Expose
    @Required
    public Date planBeginDate;

    /**
     * 货运开始日期(实际发货日期)
     */
    @Expose
    public Date beginDate;

    /**
     * 预计货运到达时间
     */
    @Expose
    @Required
    public Date planArrivDate;

    /**
     * 实际到达时间
     */
    @Expose
    public Date arriveDate;

    /**
     * 货运类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Required
    public T type;

    /**
     * 计价类型; 根据 体积 与 重量自动判断
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public P pype = P.WEIGHT;

    /**
     * 体积, 单位立方米
     */
    public Float volumn = 0f;

    /**
     * 重量, 单位 kg
     */
    public Float weight = 0f;

    /**
     * 申报价格, 单位为 USD
     */
    public Float declaredValue = 0f;

    /**
     * 押金, (申报价值的 20%) 单位为 RMB
     */
    public Float deposit = 0f;

    /**
     * 其他费用, 例如(手续费)
     */
    public Float otherFee = 0f;

    /**
     * 运费 (体积/重量 * 单价)
     */
    public Float shipFee = 0f;

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
    public String trackNo;

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
     * Shipment 的检查
     */
    public void validate() {
        if(this.declaredValue != null) Validation.min("ship.declaredValue", this.declaredValue, 0);
        if(this.deposit != null) Validation.min("ship.deposit", this.deposit, 0);
        if(this.otherFee != null) Validation.min("ship.otherfee", this.otherFee, 0);
        if(this.shipFee != null) Validation.min("ship.shipFee", this.shipFee, 0);
        if(this.volumn != null) Validation.min("ship.volumn", this.volumn, 0);
        if(this.weight != null) Validation.min("ship.weight", this.weight, 0);
        if(StringUtils.isNotBlank(this.trackNo))
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
        if(this.planBeginDate == null || this.type == null)
            throw new FastRuntimeException("必须拥有 预计发货时间 与 运输类型");
        int plusDay = 7;
        if(type == T.EXPRESS) plusDay = 7;
        else if(type == T.AIR) plusDay = 15;
        else if(type == T.SEA) plusDay = 60;
        this.planArrivDate = new DateTime(this.planBeginDate).plusDays(plusDay).toDate();
        return true;
    }

    public List<ProcureUnit> multipleUnitValidate(List<Long> units) {
        List<ProcureUnit> procureUnits = ProcureUnit.find(SqlSelect.whereIn("id", units)).fetch();
        if(procureUnits.size() != units.size())
            Validation.addError("", "提交的采购计划数量与系统存在的不一致!");

        return procureUnits;
    }

    /**
     * 从采购计划创建运输单
     *
     * @param units
     */
    public Shipment buildFromProcureUnits(List<Long> units) {
        /**
         * 1. 检查采购计划数量是否一致
         * 2. 检查运输方式是否一致
         * 3. 检查快递仓库是否一致或者 海运/空运 国家是否一致
         */

        List<ProcureUnit> procureUnits = multipleUnitValidate(units);

        ProcureUnit firstProcureUnit = procureUnits.get(0);
        Shipment.T firstShipType = firstProcureUnit.shipType;
        Date earlyPlanBeginDate = firstProcureUnit.attrs.planShipDate;

        for(ProcureUnit unit : procureUnits) {
            if(firstShipType != unit.shipType) {
                Validation.addError("", "不同运输方式不可以创建到一个运输单.");
                break;
            }
            earlyPlanBeginDate = new Date(
                    Math.min(earlyPlanBeginDate.getTime(), unit.attrs.planShipDate.getTime()));
        }

        this.type = firstShipType;

        for(ProcureUnit unit : procureUnits) {
            if(this.type == T.EXPRESS) {
                if(!firstProcureUnit.whouse.id.equals(unit.whouse.id)) {
                    Validation.addError("", "快递运输, 仓库不一样不可以创建到一个运输单");
                    break;
                }
            } else {
                if(!firstProcureUnit.whouse.country.equals(unit.whouse.country)) {
                    Validation.addError("", "海运/空运, 运往国家不一样不可以创建一个运输单");
                    break;
                }
            }
        }

        if(Validation.hasErrors()) return this;

        this.id = Shipment.id();
        this.planBeginDate = earlyPlanBeginDate;
        this.calcuPlanArriveDate();
        this.whouse = firstProcureUnit.whouse;
        this.creater = User.current();
        this.save();
        for(ProcureUnit unit : procureUnits) {
            this.addToShip(unit);
        }
        return this;
    }

    // TODO effect: 取消运输单需要调整
    public void cancel() {
        List<Integer> shipItemIds = new ArrayList<Integer>();
        for(ShipItem item : this.items) shipItemIds.add(item.id.intValue());
        this.cancelShip(shipItemIds, true);
        if(Validation.hasErrors()) return;
        this.state = S.CANCEL;
        this.trackNo = null;
        this.save();
    }

    public void updateShipment() {
        this.pype = this.pype();
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
        if(!unit.whouse.equals(this.whouse))
            Validation.addError("", "运输目的地不一样, 无法添加");
        if(unit.shipType != this.type)
            Validation.addError("", "运输方式不一样, 无法添加.");
        if(unit.shipItems.size() > 0)
            Validation.addError("", "采购计划已经拥有运输项目, 不可以再重新创建.");
        if(Validation.hasErrors()) return;

        ShipItem shipitem = new ShipItem(unit);
        shipitem.shipment = this;
        shipitem.save();
        //TODO c: 添加日志
    }


    /**
     * 从 Shipment 中删除不需要运输的 ShipItem,  数量返回到原有的 ProcureUnit 中
     * TODO: effect
     *
     * @param shipItemId
     */
    public void cancelShip(List<Integer> shipItemId, boolean log) {
        if(shipItemId == null) shipItemId = new ArrayList<Integer>();
        List<ShipItem> items = new ArrayList<ShipItem>();
        if(shipItemId.size() > 0)
            items = ShipItem.find("id IN " + JpqlSelect.inlineParam(shipItemId)).fetch();

        for(ShipItem itm : items) {
            if(!itm.shipment.equals(this))
                throw new FastRuntimeException("取消的运输单项不属于对应运输单");
        }

        List<String> unitsMerchantSKU = new ArrayList<String>();
        for(ShipItem itm : items) {
            F.T2<ShipItem, ProcureUnit> cancelT2 = itm.cancel();
            unitsMerchantSKU.add(cancelT2._2.selling.merchantSKU);
        }
        if(log && shipItemId.size() > 0) {
            new ElcukRecord(Messages.get("shipment.cancelShip2"),
                    Messages.get("shipment.cancelShip2.msg",
                            StringUtils.join(unitsMerchantSKU, Webs.SPLIT), this.id),
                    this.id
            ).save();
        }
    }

    public void comment(String cmt) {
        if(!StringUtils.isNotBlank(cmt)) return;
        this.memo = String.format("%s\r\n%s", cmt, this.memo).trim();
    }

    /**
     * 具体的开始运输
     * <p/>
     * ps: 不允许多个人, 对 Shipment 多次 beginShip
     * TODO: effect
     */
    public synchronized void beginShip() {
        if(this.state != S.CONFIRM)
            throw new FastRuntimeException("运输单没有 CONFIRM 无法运输");
        for(ShipItem itm : this.items) {
            if(itm.unit.stage == ProcureUnit.STAGE.PLAN ||
                    itm.unit.stage == ProcureUnit.STAGE.DELIVERY)
                throw new FastRuntimeException(String.format("采购计划 #%s 还没有交货, 无法运输.", itm.unit.id));
            if(!itm.unit.isPlaced)
                throw new FastRuntimeException(String.format("采购计划 %s 还没有抵达, 无法运输.", itm.unit.id));
        }

        for(ShipItem item : this.items) {
            item.shipDate = new Date();
            item.unit.stage = ProcureUnit.STAGE.SHIPPING;
        }
        // TODO 开始运输的时候, 需要更新采购计划的 FBA
//        for(FBAShipment fba : this.fbas) {
//            if(fba.state != FBAShipment.S.WORKING) continue;
//            fba.updateFBAShipment(FBAShipment.S.SHIPPED);
//        }
        for(ShipItem itm : this.items) itm.unit.save();
        this.pype = this.pype();
        this.beginDate = new Date();
        this.state = S.SHIPPING;
        this.save();
    }

    public P pype() {
        // 自动根据 体积与重量的值, 自动计算价格类型
        if(this.volumn == null || this.weight == null) return this.pype;
        if(this.volumn > this.weight) return P.VOLUMN;
        else return P.WEIGHT;
    }

    @Override
    public String to_log() {
        StringBuilder sbd = new StringBuilder("[id:").append(this.id).append("] ");
        sbd.append("[运输:").append(Dates.date2Date(this.planBeginDate)).append("] ")
                .append("[到库:").append(Dates.date2Date(this.planArrivDate)).append("] ");
        if(this.volumn != null && this.volumn != 0)
            sbd.append("[运输体积:").append(this.volumn).append("] ");
        if(this.weight != null && this.weight != 0)
            sbd.append("[运输重量:").append(this.weight).append("]");
        if(this.declaredValue != null && this.declaredValue != 0)
            sbd.append("[申报价(USD):").append(this.declaredValue).append("] ");
        if(this.deposit != null && this.deposit != 0)
            sbd.append("[押金:").append(this.deposit).append("] ");
        if(this.otherFee != null && this.otherFee != 0)
            sbd.append("[其他费用:").append(this.otherFee).append("] ");
        if(this.cooper != null) sbd.append("[货代:").append(this.cooper.name).append("] ");
        if(this.whouse != null) sbd.append("[仓库:").append(this.whouse.name()).append("]");
        return sbd.toString();
    }


    /**
     * 完成运输, 最终的确认
     */
    public void shipOver() {
        this.state = S.DONE;
        // 当运输单完成运输, ProcureUnit 进入 SHIP_OVER 阶段
        for(ShipItem item : this.items) {
            item.arriveDate = this.arriveDate;
            item.unitStage(ProcureUnit.STAGE.SHIP_OVER);
        }
        this.save();
    }

    /**
     * 抓取 DHL, FEDEX 网站的运输信息, 更新系统中 SHIPMENT 的状态;
     * <p/>
     * 如果此 Shipment 拥有 FBA 会根据具体状态, 更新 FBA 的签收时间
     *
     * @return
     */
    public S monitor() {
        if(StringUtils.isBlank(this.iExpressHTML)) {
            Logger.warn("Shipment %s do not have iExpressHTML.", this.id);
            return this.state;
        }
        if(this.state == S.SHIPPING && this.internationExpress.isClearance(this.iExpressHTML)) {
            // 正在运输,需要检查是否运输清关
            this.clearance();
        }
        if(this.state == S.SHIPPING || this.state == S.CLEARANCE) {
            // 清关, 检查是否送达; 因为有时候会跳过清关信息, 所以也需要将 SHIPPING 包括进来检查是否送达
            F.T2<Boolean, DateTime> isDelivered = this.internationExpress
                    .isDelivered(this.iExpressHTML);
            if(isDelivered._1)
                this.delivered(isDelivered._2.toDate());
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
        Logger.info("Shipment sync from [%s]", this.internationExpress.trackUrl(this.trackNo));
        String html = this.internationExpress.fetchStateHTML(this.trackNo);
        try {
            this.iExpressHTML = this.internationExpress.parseExpress(html, this.trackNo);
        } catch(Exception e) {
            FLog.fileLog(String.format("%s.%s.%s.html", this.id, this.trackNo,
                    this.internationExpress.name()), html, FLog.T.HTTP_ERROR);
            throw new FastRuntimeException(Webs.S(e));
        }
        return this.iExpressHTML;
    }

    /**
     * 完成清关
     */
    private void clearance() {
        this.state = S.CLEARANCE;
        this.save();
        Mails.shipment_clearance(this);
    }

    /**
     * 送达目的地
     */
    private void delivered(Date deliveredDate) {
        this.arriveDate = deliveredDate;
        this.shipOver();
        this.state = S.DONE;
        // 为避免 NPE, 对 fba 初始化了集合
        // TODO: effect 运输单抵达, 对 FBA 做响应; 需要响应吗?
//        for(FBAShipment fba : this.fbas) {
//            fba.receiptAt = deliveredDate;
//            fba.save();
//        }
        this.save();
        Mails.shipment_isdone(this);
    }

    /**
     * FBA 在运输单的状态跟踪抵达之前就已经签收了, 那么需要对运输单做状态改变;
     * 如果这个运输单的所有 FBA 都进入签收后状态了, 那么这个运输单才可以进行 DONE
     */
    public void fbaReceviedBeforeShipmentDelivered() {
        //TODO: effect FBA 与 Shipment 没有关系, 那么 FBA 不要影响 Shipment ?
        /*
        boolean makeDone = this.fbas.size() > 0;
        for(FBAShipment fba : this.fbas) {
            if(!fba.afterReceving()) {
                makeDone = false;
                break;
            }
        }
        if(makeDone) {
            this.state = S.DONE;
            this.arriveDate = fbas.get(0).receiptAt;
        }
        this.save();
        */
    }

    public String title() {
        if(StringUtils.isBlank(this.title))
            return String.format("Default Title: ShipmentId %s", this.id);
        return String.format("[%s] %s", this.id, this.title);
    }

    /**
     * 通过产品数据计算出来的这份运输单的总重量
     *
     * @return
     */
    public float totalWeight() {
        //TODO 总重量, 需要根据体积/重量的运输算法来计算
        float weight = 0f;
        for(ShipItem itm : this.items) {
            weight += itm.qty * itm.unit.product.weight;
        }
        return weight;
    }

    @Override
    public String toString() {
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
        List<FBAShipment> fbas = new ArrayList<FBAShipment>();
        for(ShipItem item : this.items) {
            if(item.unit.fba == null) continue;
            if(fbas.contains(item.unit.fba)) continue;
            fbas.add(item.unit.fba);
        }
        return fbas;
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

    public static List<Shipment> shipmentsByState(S state) {
        return Shipment.find("state=? ORDER BY createDate", state).fetch();
    }

    /**
     * 根据 仓库 与 运输类型查找运输单, 在查找的同时, 也会自动检查是否需要创建固定周期的运输单
     *
     * @param whouseId
     * @param shipType
     * @return
     */
    public static List<Shipment> findUnitRelateShipmentByWhouse(Long whouseId, T shipType) {
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
        List<Shipment> planedShipments = Shipment
                .find("state IN(?,?) AND planBeginDate>=? AND planBeginDate<=?",
                        S.PLAN, S.CONFIRM, new Date(), DateTime.now().plusDays(60).toDate())
                .fetch();
        //确定仓库接收的运输单
        List<Whouse> whs = Whouse.all().fetch();
        for(Whouse whouse : whs) {
            whouse.checkWhouseNewShipment(planedShipments);
        }


        // 加载
        StringBuilder where = new StringBuilder("state IN (?,?)");
        List<Object> params = new ArrayList<Object>(Arrays.asList(S.PLAN, S.CONFIRM));
        if(whouseId != null) {
            where.append("AND (whouse.id=? OR whouse.id IS NULL)");
            params.add(whouseId);
        } else {
            where.append("AND whouse.id IS NULL");
        }
        where.append(" AND type =?");
        params.add(shipType);

        where.append(" ORDER BY planBeginDate");

        return Shipment.find(where.toString(), params.toArray()).fetch();
    }

    /**
     * 由于 Play 无法将 models 目录下的 Enumer 加载, 所以通过 model 提供一个暴露方法在 View 中使用
     *
     * @return
     */
    public static List<iExpress> Express() {
        return Arrays.asList(iExpress.values());
    }

    public static List<Shipment> similarShipments(Date planBeginDate, Whouse whouse, T shipType) {
        return Shipment.find("planBeginDate>=? AND whouse=? AND type=? ORDER BY planBeginDate",
                planBeginDate, whouse, shipType).fetch();
    }

    public static List<Shipment> findByState(S state) {
        return Shipment.find("state=?", state).fetch();
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

    /**
     * 获得不同运输方式的标准运输量
     *
     * @return
     */
    public float minimumTraffic() {
        //海运和空运暂时的最小运输量是500 而快递是不能超过500
        return 500;
    }

}
