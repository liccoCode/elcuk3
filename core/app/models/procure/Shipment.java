package models.procure;

import com.google.gson.annotations.Expose;
import helper.Dates;
import helper.FBA;
import helper.FLog;
import helper.Webs;
import models.ElcukRecord;
import models.Notification;
import models.User;
import models.product.Whouse;
import notifiers.Mails;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.helper.JpqlSelect;
import play.db.jpa.GenericModel;
import play.i18n.Messages;
import play.libs.F;
import play.utils.FastRuntimeException;
import query.ShipmentQuery;

import javax.persistence.*;
import java.util.*;


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
        this.state = S.PLAN;
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
        this.pype = P.WEIGHT;
        this.state = S.PLAN;
        this.type = T.EXPRESS;
        this.id = id;
    }

    public enum T {
        /**
         * 海运
         */
        SEA {
            @Override
            public String toString() {
                return "海运";
            }
        },
        /**
         * 空运
         */
        AIR {
            @Override
            public String toString() {
                return "空运";
            }
        },
        /**
         * 快递
         */
        EXPRESS {
            @Override
            public String toString() {
                return "快递";
            }
        }
    }

    public enum S {
        /**
         * 计划中
         */
        PLAN {
            @Override
            public String toString() {
                return "计划中";
            }
        },
        /**
         * 确认运输单, 并创建了 FBA Shipment
         */
        CONFIRM {
            @Override
            public String toString() {
                return "准备运输,已创建 FBA";
            }
        },
        /**
         * 运输中
         */
        SHIPPING {
            @Override
            public String toString() {
                return "运输中";
            }
        },
        /**
         * 清关
         */
        CLEARANCE {
            @Override
            public String toString() {
                return "清关中";
            }
        },
        /**
         * 完成
         */
        DONE {
            @Override
            public String toString() {
                return "完成";
            }
        },

        /**
         * 取消状态
         */
        CANCEL {
            @Override
            public String toString() {
                return "取消";
            }
        }
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

    /**
     * 运输合作商
     */
    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public Cooperator cooper;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    public Whouse whouse;

    @OneToOne(fetch = FetchType.LAZY)
    public User creater;

    @OneToMany(mappedBy = "shipment")
    public List<FBAShipment> fbas = new ArrayList<FBAShipment>();

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
    public S state;

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
    public P pype;

    /**
     * 体积, 单位立方米
     */
    public Float volumn;

    /**
     * 重量, 单位 kg
     */
    public Float weight;

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
     * 是否为周期任务
     */
    public boolean cycle = false;

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
        if(this.creater == null) this.creater = User.findByUserName(ElcukRecord.username());
        this.save();
    }

    public void setTrackNo(String trackNo) {
        if(StringUtils.isNotBlank(trackNo)) this.trackNo = trackNo.trim();
        else this.trackNo = null;
    }

    /**
     * 向 Shipment 添加需要运输的 ProcureUnit
     * <p/>
     * ps: 这个方法不允许并发
     *
     * @param unitId
     */
    public synchronized void addToShip(List<Long> unitId) {
        /**
         * 运输单业务限制检查
         * 1. 检查运输单总重量只能在 300kg 以下
         * 2. 如果运输的是电池, 则需要单独运输
         */
        if(this.state != S.PLAN && this.state != S.CONFIRM)
            throw new FastRuntimeException("只有 PLAN 与 CONFIRM 状态可以添加运输项目");

        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(unitId))
                .fetch();
        List<String> unitsMerchantSKU = new ArrayList<String>();
        for(ProcureUnit unit : units) {
            // 如果是完全一个全新的 Shipment 那么则由第一个加入到其他的 ProcureUnit 决定去往仓库
            if(this.items.size() == 0 && this.whouse == null)
                this.whouse = unit.whouse;

            if(!unit.whouse.id.equals(this.whouse.id))
                throw new FastRuntimeException("运往仓库不一致");

            ShipItem shipItem = unit.ship(this);
            this.items.add(shipItem);
            unitsMerchantSKU.add(unit.selling.merchantSKU);
        }

        for(ShipItem itm : this.items) itm.save();
        this.save();
        new ElcukRecord(Messages.get("shipment.ship"),
                Messages.get("shipment.ship.msg", StringUtils.join(unitsMerchantSKU, Webs.SPLIT),
                        this.id),
                this.id
        ).save();
        if(this.cycle)
            Notification.notifies(String.format("周期型运输单 %s 有新货物(%s)", this.id, this.items.size()),
                    String.format("有新的货物添加进入了运输单 %s 记得处理哦.", this.id), Notification.SHIPPER);
    }


    /**
     * 从 Shipment 中删除不需要运输的 ShipItem,  数量返回到原有的 ProcureUnit 中
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
            if(itm.fba != null) {
                switch(itm.fba.state) {
                    case SHIPPED:
                    case CHECKED_IN:
                    case DELIVERED:
                    case IN_TRANSIT:
                    case RECEIVING:
                        throw new FastRuntimeException(
                                String.format("FBA(%s) 已经无法更改(%s), 所以不允许再修改运输项目.",
                                        itm.fba.shipmentId, itm.fba.state));
                }
            }
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

    /**
     * 返回还没有 FBA 的 ShipItem
     *
     * @return
     */
    public List<ShipItem> noFbaItems() {
        List<ShipItem> noFbaItems = new ArrayList<ShipItem>();
        for(ShipItem itm : this.items) {
            if(itm.fba == null)
                noFbaItems.add(itm);
        }
        return noFbaItems;
    }

    public void comment(String cmt) {
        if(!StringUtils.isNotBlank(cmt)) return;
        this.memo = String.format("%s\r\n%s", cmt, this.memo).trim();
    }

    /**
     * 确认运输单以后, 向 Amazon 创建 FBAShipment
     * <p/>
     */
    public F.Option<FBAShipment> deployFBA(List<String> shipitemIds) {
        /**
         * 1. 将本地的状态修改为 CONFIRM
         * 2. 向 Amazon 提交 FBA Shipment 的创建
         * 3. Amazon Shipment 创建成功后再本地更新, 否则不更新,包裹错误.
         */
        if(this.items.size() <= 0)
            Validation.addError("", "运输单为空, 不需要创建 FBA Shipment");

        // 检查提交的是否为当前运输单的 ShipItem, 是的挑出来, 找到一个不是则报告错误
        Map<String, ShipItem> shipItemsMap = new HashMap<String, ShipItem>();
        List<ShipItem> deployItems = new ArrayList<ShipItem>();
        for(ShipItem itm : this.items) {
            shipItemsMap.put(itm.id.toString(), itm);
        }
        for(String id : shipitemIds) {
            if(!shipItemsMap.containsKey(id)) {
                Validation.addError("", String.format("ShipItem Id %s 不存在运输单 %s 中", id, this.id));
            } else {
                deployItems.add(shipItemsMap.get(id));
            }
        }
        if(Validation.hasErrors()) return F.Option.None();

        F.Option<FBAShipment> fbaQpt = this.postFBAShipment(deployItems);
        if(Validation.hasErrors()) return fbaQpt;
        if(fbaQpt.isDefined()) {
            this.state = S.CONFIRM;
            this.target = fbaQpt.get().address();
            this.save();
        }
        return fbaQpt;
    }

    /**
     * 根据此 Shipment 创建一个 FBAShipment
     * <p/>
     * ps: 这个方法不允许并发
     *
     * @return
     */
    public synchronized F.Option<FBAShipment> postFBAShipment(List<ShipItem> shipItems) {
        F.Option<FBAShipment> fbaQpt = null;
        try {
            fbaQpt = FBA.plan(this.whouse.account, shipItems);
        } catch(Exception e) {
            Validation.addError("", "向 Amazon 创建 Shipment Plan 错误 " + Webs.E(e));
            return F.Option.None();
        }
        // 对 FBA 的仓库检查, 同一个运输单不可以运输两个不同的 FBA 地址
        if(fbaQpt.isDefined() && this.fbas.size() > 0) {
            if(!fbaQpt.get().centerId.equals(this.fbas.get(0).centerId) && !this.cycle) {
                Validation.addError("", "新获取的 FBA 仓库为 %s 与当前存在的去往 FBA 仓库不一样, 无法创建.",
                        fbaQpt.get().centerId);
                return F.Option.None();
            }
        }
        try {
            if(fbaQpt.isDefined()) {
                // Shipment 与 FBA 由 FBA 自行创建关系
                fbaQpt.get().shipment = this;
                fbaQpt.get().state = FBA.create(fbaQpt.get());
                fbaQpt.get().save();
                // 需要手动从 ShipItem 建立与 FBA 的关系 - -||
                for(ShipItem itm : fbaQpt.get().shipItems) {
                    itm.fba = fbaQpt.get();
                    itm.save();
                }
            }
        } catch(Exception e) {
            Validation.addError("", "向 Amazon 创建 Shipment 错误 " + Webs.E(e));
        }
        return fbaQpt;
    }

    /**
     * 具体的开始运输
     * <p/>
     * ps: 不允许多个人, 对 Shipment 多次 beginShip
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
        for(FBAShipment fba : this.fbas) {
            if(fba.state != FBAShipment.S.WORKING) continue;
            fba.updateFBAShipment(FBAShipment.S.SHIPPED);
        }
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
     * 对 Shipment 进行分拆, 主要解决的目的为运输单在 PM 处理好以后, 跟单人员需要将运输单根据实际情况分拆成为不同的运输单进行运输.
     * 每一票运输单对应一个唯一的 tracking number.
     */
    public F.Option<Shipment> splitShipment(List<String> shipItemIds) {
        if(this.state != S.PLAN && this.state != S.CONFIRM)
            Validation.addError("", "分拆运输单只运输在 \"计划\" 与 \"确认运输\" 状态");

        List<ShipItem> needSplitItems = ShipItem
                .find("id IN " + JpqlSelect.inlineParam(shipItemIds)).fetch();
        if(needSplitItems.size() != shipItemIds.size())
            Validation.addError("", "分拆运输单的运输项目数量与数据库中记录的不一致");
        for(ShipItem itm : needSplitItems) {
            if(itm.fba != null)
                Validation.addError("", "运输项目已经附属了 FBA, 请先请 FBA 中删除再进行拆分");
        }
        if(Validation.hasErrors()) return F.Option.None();

        Shipment newShipment = new Shipment(this);
        newShipment
                .comment(String.format("从运输单 %s 分拆 %s items 而来.", this.id, needSplitItems.size()));
        newShipment.save();
        for(ShipItem spitem : needSplitItems) {
            spitem.shipment = newShipment;
            spitem.save();
        }
        new ElcukRecord(Messages.get("shipment.splitShipment"),
                Messages.get("shipment.splitShipment.msg",
                        StringUtils.join(shipItemIds, Webs.SPLIT), newShipment.id),
                this.id).save();
        this.notifyWithMuchMoreShipmentCreate();
        return F.Option.Some(newShipment);
    }

    /**
     * 用来检查并且提醒运输者, 系统内运输单创建数量太多
     */
    public void notifyWithMuchMoreShipmentCreate() {
        long count = Shipment.count("createDate>=? AND createDate<=?", Dates.morning(new Date()),
                Dates.night(new Date()));
        //创建10 个以上的运输单才提醒
        if(count % 10 == 0 && count > 11) {
            long noitemShipments = Shipment.count("SIZE(items)=0 AND state!=?", Shipment.S.CANCEL);
            Notification.notifies(
                    String.format("今天已经创建了 %s 个运输单, 并且系统内拥有 %s 个无运输项目的运输单, 请记得处理.", count,
                            noitemShipments), Notification.SHIPPER);
        }
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
        for(FBAShipment fba : this.fbas) {
            fba.receiptAt = deliveredDate;
            fba.save();
        }
        this.save();
        Mails.shipment_isdone(this);
    }

    /**
     * FBA 在运输单的状态跟踪抵达之前就已经签收了, 那么需要对运输单做状态改变;
     * 如果这个运输单的所有 FBA 都进入签收后状态了, 那么这个运输单才可以进行 DONE
     */
    public void fbaReceviedBeforeShipmentDelivered() {
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
                .find("cycle=true AND state IN(?,?) AND planBeginDate>=? AND planBeginDate<=?",
                        S.PLAN, S.CONFIRM, new Date(), DateTime.now().plusDays(60).toDate())
                .fetch();
        //确定仓库接收的运输单
        List<Whouse> whs = Whouse.all().fetch();
        DateTime now = new DateTime(Dates.morning(new Date()));
        for(Whouse whouse :whs)
            whouse.checkWhouseNewShipment(planedShipments,now);


        // 加载
        StringBuilder where = new StringBuilder("cycle=? AND state IN (?,?)");
        List<Object> params = new ArrayList<Object>(Arrays.asList(true, S.PLAN, S.CONFIRM));
        if(whouseId != null) {
            where.append("AND (whouse.id=? OR whouse.id IS NULL)");
            params.add(whouseId);
        } else {
            where.append("AND whouse.id IS NULL");
        }
        //当运输方式是 air 或者 express的时候,统一一起查出来
        if(shipType != null) {
            if(shipType.equals(T.AIR)||shipType.equals(T.EXPRESS)){
                where.append(" AND type in (?,?)");
                params.add(T.AIR);
                params.add(T.EXPRESS);
            }else{
                where.append(" AND type =?");
                params.add(shipType);
            }


        }

        return Shipment.find(where.append(" ORDER BY planBeginDate").toString(), params.toArray())
                .fetch();
    }

    /**
     * 由于 Play 无法将 models 目录下的 Enumer 加载, 所以通过 model 提供一个暴露方法在 View 中使用
     *
     * @return
     */
    public static List<iExpress> Express() {
        return Arrays.asList(iExpress.values());
    }

    /**
     * 新建运输单
     * @param planBeginDate 计划开始时间
     * @param whouse    接受仓库
     * @param type      运输方式
     * @param arriveDate 预计到达时间
     */
    public static void create(Date planBeginDate,Whouse whouse,T type,Date arriveDate){
        Shipment shipment = new Shipment();
        shipment.id = Shipment.id();
        shipment.cycle = true;
        shipment.planBeginDate = planBeginDate;
        shipment.planArrivDate = arriveDate;
        shipment.whouse = whouse;
        shipment.type = type;
        shipment.title = String.format("%s 去往 %s 在 %s", shipment.id, shipment.whouse.name(),
                Dates.date2Date(shipment.planBeginDate));
        shipment.save();
    }

    /**
     * 获得最小运输量
     * @return
     */
    public  float minimumTraffic(){
        if(this.type.equals(T.AIR))
            return 500;
        else if (this.type.equals(T.SEA)){
            return 1000;
        }else
            return 400;
    }

}
