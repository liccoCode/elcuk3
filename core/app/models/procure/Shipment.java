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

            @Override
            public S nextState(Shipment ship) {
                // 如果在 SHIPPING 状态则检查是否处于清关
                if(ship.internationExpress.isContainsClearance(ship.iExpressHTML)) {
                    Mails.shipment_clearance(ship);
                    return CLEARANCE;
                }
                if(ship.internationExpress.isDelivered(ship.iExpressHTML)._1) {
                    // 有一些运输单还直接跳过清关状态, 对这个做兼容.
                    return S.CLEARANCE.nextState(ship);
                }
                return this;
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

            @Override
            public S nextState(Shipment ship) {
                // 如果在 CLERANCE 检查是否有 Delivery 日期
                F.T2<Boolean, DateTime> isDeliveredAndTime = ship.internationExpress.isDelivered(ship.iExpressHTML);
                if(isDeliveredAndTime._1) {
                    ship.arriveDate = isDeliveredAndTime._2.toDate();
                    for(ShipItem item : ship.items) {
                        item.arriveDate = ship.arriveDate;
                    }
                    // 为避免 NPE, 对 fba 初始化了集合
                    for(FBAShipment fba : ship.fbas)
                        fba.checkReceipt(ship);
                    Mails.shipment_isdone(ship);
                    return CLEARANCE;
                } else {
                    return this;
                }
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
        };

        public S nextState(Shipment ship) {
            return this;
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
     * 货运开始日期
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
            long samewhouseItems = ShipmentQuery.shipemntItemCountWithSameWhouse(this.id, this.whouse.id);
            if(samewhouseItems != this.items.size()) Validation.addError("", "运输单中拥有与运输单去往仓库不一样的运输单项目");
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
     * 与当前运输单运输地址, FBA 仓库相同的其他运输单;(非周期型)
     *
     * @return
     */
    public List<Shipment> similarShipments() {
        if(this.fbas.size() > 0)
            return Shipment.find("SELECT s FROM Shipment s LEFT JOIN s.fbas f WHERE s.id!=? AND s.whouse=? AND cycle=false AND (f.centerId=? OR SIZE(s.fbas)=0) AND s.state IN (?,?) ORDER BY planBeginDate",
                    this.id, this.whouse, this.fbas.get(0).centerId, S.PLAN, S.CONFIRM).fetch();
        else
            return Shipment.find("id!=? AND whouse=? AND cycle=false AND state IN (?,?) ORDER BY planBeginDate",
                    this.id, this.whouse, S.PLAN, S.CONFIRM).fetch();
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

        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(unitId)).fetch();
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
                Messages.get("shipment.ship.msg", StringUtils.join(unitsMerchantSKU, Webs.SPLIT), this.id),
                this.id
        ).save();
        if(this.cycle)
            Notification.notifies(String.format("周期型运输单 %s 有新货物(%s)", this.id, this.items.size()), String.format("有新的货物添加进入了运输单 %s 记得处理哦.", this.id), Notification.SHIPPER);
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
                        throw new FastRuntimeException(String.format("FBA(%s) 已经无法更改(%s), 所以不允许再修改运输项目.", itm.fba.shipmentId, itm.fba.state));
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
                    Messages.get("shipment.cancelShip2.msg", StringUtils.join(unitsMerchantSKU, Webs.SPLIT), this.id),
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
        if(this.cycle)
            Validation.addError("", "周期型运输单不允许再创建 FBA Shipment, 请运输人员制作运输计划.");
        if(this.items.size() <= 0)
            Validation.addError("", "运输单为空, 不需要创建 FBA Shipment");

        // 检查提交的是否为当前运输单的 ShipItem, 是的挑出来, 找到一个不是则报告错误
        Map<String, ShipItem> shipItemsMap = new HashMap<String, ShipItem>();
        List<ShipItem> deployItems = new ArrayList<ShipItem>();
        for(ShipItem itm : this.items)
            shipItemsMap.put(itm.id.toString(), itm);
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
            if(!fbaQpt.get().centerId.equals(this.fbas.get(0).centerId)) {
                Validation.addError("", "新获取的 FBA 仓库为 %s 与当前存在的去往 FBA 仓库不一样, 无法创建.", fbaQpt.get().centerId);
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
            if(itm.unit.stage != ProcureUnit.STAGE.DONE)
                throw new FastRuntimeException(String.format("采购计划 #%s 还没有交货, 无法运输.", itm.unit.id));
            if(!itm.unit.isPlaced)
                throw new FastRuntimeException(String.format("采购计划 %s 还没有抵达, 无法运输.", itm.unit.id));
        }

        for(ShipItem item : this.items) {
            item.shipDate = new Date();
            item.unit.stage = item.unit.nextStage();
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
        if(this.volumn != null && this.volumn != 0) sbd.append("[运输体积:").append(this.volumn).append("] ");
        if(this.weight != null && this.weight != 0) sbd.append("[运输重量:").append(this.weight).append("]");
        if(this.declaredValue != null && this.declaredValue != 0)
            sbd.append("[申报价(USD):").append(this.declaredValue).append("] ");
        if(this.deposit != null && this.deposit != 0) sbd.append("[押金:").append(this.deposit).append("] ");
        if(this.otherFee != null && this.otherFee != 0) sbd.append("[其他费用:").append(this.otherFee).append("] ");
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

        List<ShipItem> needSplitItems = ShipItem.find("id IN " + JpqlSelect.inlineParam(shipItemIds)).fetch();
        if(needSplitItems.size() != shipItemIds.size())
            Validation.addError("", "分拆运输单的运输项目数量与数据库中记录的不一致");
        for(ShipItem itm : needSplitItems) {
            if(itm.fba != null)
                Validation.addError("", "运输项目已经附属了 FBA, 请先请 FBA 中删除再进行拆分");
        }
        if(Validation.hasErrors()) return F.Option.None();

        Shipment newShipment = new Shipment(this);
        newShipment.comment(String.format("从运输单 %s 分拆 %s items 而来.", this.id, needSplitItems.size()));
        newShipment.save();
        for(ShipItem spitem : needSplitItems) {
            spitem.shipment = newShipment;
            spitem.save();
        }
        new ElcukRecord(Messages.get("shipment.splitShipment"),
                Messages.get("shipment.splitShipment.msg", StringUtils.join(shipItemIds, Webs.SPLIT), newShipment.id),
                this.id).save();
        this.notifyWithMuchMoreShipmentCreate();
        return F.Option.Some(newShipment);
    }

    /**
     * 用来检查并且提醒运输者, 系统内运输单创建数量太多
     */
    public void notifyWithMuchMoreShipmentCreate() {
        long count = Shipment.count("createDate>=? AND createDate<=?", Dates.morning(new Date()), Dates.night(new Date()));
        //创建10 个以上的运输单才提醒
        if(count % 10 == 0 && count > 11) {
            long noitemShipments = Shipment.count("SIZE(items)=0 AND state!=?", Shipment.S.CANCEL);
            Notification.notifies(String.format("今天已经创建了 %s 个运输单, 并且系统内拥有 %s 个无运输项目的运输单, 请记得处理.", count, noitemShipments), Notification.SHIPPER);
        }
    }

    /**
     * 完成运输, 最终的确认
     */
    public void ensureDone() {
        Validation.equals("shipments.ensureDone.state", this.state, "", Shipment.S.CLEARANCE);
        if(Validation.hasErrors()) return;
        this.state = S.DONE;
        for(ShipItem item : this.items) {
            item.unit.stage = item.unit.nextStage();
            item.unit.save();
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
    public String refreshIExpressHTML() {
        Logger.info("Shipment sync from [%s]", this.internationExpress.trackUrl(this.trackNo));
        String html = this.internationExpress.fetchStateHTML(this.trackNo);
        this.iExpressHTML = this.internationExpress.parseExpress(html, this.trackNo);
        this.state = this.state.nextState(this);
        try {
            this.save();
        } catch(Exception e) {
            FLog.fileLog(String.format("%s.%s.%s.html", this.id, this.trackNo, this.internationExpress.name()), html, FLog.T.HTTP_ERROR);
            throw new FastRuntimeException(Webs.S(e));
        }
        return this.iExpressHTML;
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
        for(ShipItem itm : this.items)
            weight += itm.qty * itm.unit.product.weight;
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
        String count = Shipment.count("createDate>=? AND createDate<?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear() + 1)).toDate()) + "";
        return String.format("SP|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public static List<Shipment> shipmentsByState(S state) {
        return Shipment.find("state=? ORDER BY createDate", state).fetch();
    }

    public static List<Shipment> findUnitRelateShipmentByWhouse(Long whouseId) {
        StringBuilder where = new StringBuilder("cycle=? AND state IN (?,?)");
        List<Object> params = new ArrayList<Object>(Arrays.asList(true, S.PLAN, S.CONFIRM));
        if(whouseId != null) {
            where.append("AND (whouse.id=? OR whouse.id IS NULL)");
            params.add(whouseId);
        } else {
            where.append("AND whouse.id IS NULL");
        }
        return Shipment.find(where.append(" ORDER BY planBeginDate").toString(), params.toArray()).fetch();
    }

    /**
     * 由于 Play 无法将 models 目录下的 Enumer 加载, 所以通过 model 提供一个暴露方法在 View 中使用
     *
     * @return
     */
    public static List<iExpress> Express() {
        return Arrays.asList(iExpress.values());
    }
}
