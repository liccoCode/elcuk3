package models.procure;

import com.amazonservices.mws.FulfillmentInboundShipment._2010_10_01.FBAInboundServiceMWSException;
import com.google.gson.annotations.Expose;
import helper.FBA;
import helper.FLog;
import helper.Webs;
import models.ElcukRecord;
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
import java.util.concurrent.TimeUnit;

/**
 * 一张运输单
 * User: wyattpan
 * Date: 6/17/12
 * Time: 5:32 PM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class Shipment extends GenericModel {

    public Shipment() {
    }

    public Shipment(String id) {
        this.createDate = new Date();

        this.pype = P.WEIGHT;
        this.state = S.PLAN;

        // 暂时这么写
        this.source = "深圳";
        this.type = T.AIR;

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
         * 确认运输单, 不再允许修改
         */
        CONFIRM {
            @Override
            public String toString() {
                return "确认运输";
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
                    Mails.shipment_isdone(ship);
                }
                return CLEARANCE;
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
    @OneToOne(cascade = CascadeType.PERSIST)
    public Cooperator cooper;

    @OneToOne(cascade = CascadeType.PERSIST)
    public Whouse whouse;

    @OneToOne(mappedBy = "shipment", cascade = CascadeType.PERSIST)
    public FBAShipment fbaShipment;

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
     * 货运开始日期
     */
    @Expose
    @Required
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
     * 类似顺风发货单号的类似跟踪单号
     */
    @Expose
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
            long samewhouseItems = ShipmentQuery.shipemntItemCountWithSameWhouse(this.id, this.whouse.id);
            if(samewhouseItems != this.items.size()) Validation.addError("shipment.item.whouse", "%s");
        }
    }

    public void setTrackNo(String trackNo) {
        if(StringUtils.isNotBlank(trackNo)) this.trackNo = trackNo.trim();
        else this.trackNo = trackNo;
    }

    /**
     * 向 Shipment 添加需要运输的 ProcureUnit(+数量)
     * <p/>
     * ps: 这个方法不允许并发
     *
     * @param unitId
     * @param shipQty
     */
    public synchronized void addToShip(List<Long> unitId, List<Integer> shipQty) {
        if(this.state != S.PLAN && this.state != S.CONFIRM) Validation.addError("shipments.addShip", "%s");
        if(Validation.hasErrors()) return;
        List<ProcureUnit> units = ProcureUnit.find("id IN " + JpqlSelect.inlineParam(unitId)).fetch();
        if(units.size() != shipQty.size()) Validation.addError("shipments.ship.equal", "%s");
        List<String> unitsMerchantSKU = new ArrayList<String>();
        for(int i = 0; i < units.size(); i++) {
            ProcureUnit unit = units.get(i);
            int shipSize = shipQty.get(i);
            if(!unit.whouse.equals(this.whouse)) {
                Validation.addError("shipment.addToShip.whouse", "%s");
                return;
            }
            F.T3<Integer, Integer, List<String>> leftQty = unit.leftQty();
            if(leftQty._1 < shipSize) {
                Validation.addError("shipment.addToShip.shipQty", "%s");
                break;
            }

            this.items.add(unit.ship(this, shipSize));
            unit.save();
            unitsMerchantSKU.add(unit.selling.merchantSKU);
        }

        if(Validation.hasErrors()) return;
        this.save();
        new ElcukRecord(Messages.get("shipment.ship"),
                Messages.get("shipment.ship.msg", StringUtils.join(unitsMerchantSKU, Webs.SPLIT), this.id),
                this.id
        ).save();
    }


    /**
     * 从 Shipment 中删除不需要运输的 ShipItem,  数量返回到原有的 ProcureUnit 中
     *
     * @param shipItemId
     */
    public void cancelShip(List<Integer> shipItemId) {
        List<ShipItem> items = ShipItem.find("id IN " + JpqlSelect.inlineParam(shipItemId)).fetch();
        List<String> unitsMerchantSKU = new ArrayList<String>();
        for(ShipItem itm : items) {
            if(!itm.shipment.equals(this)) {
                Validation.addError("shipment.cancelShip", "%s");
                return;
            } else {
                F.T2<ShipItem, ProcureUnit> cancelT2 = itm.cancel();
                cancelT2._2.save();
                unitsMerchantSKU.add(cancelT2._2.selling.merchantSKU);
            }
        }
        new ElcukRecord(Messages.get("shipment.cancelShip2"),
                Messages.get("shipment.cancelShip2.msg", StringUtils.join(unitsMerchantSKU, Webs.SPLIT), this.id),
                this.id
        ).save();
    }

    public void comment(String cmt) {
        if(!StringUtils.isNotBlank(cmt)) return;
        this.memo = String.format("%s\r\n%s", cmt, this.memo).trim();
    }

    /**
     * 确认运输单以后, 向 Amazon 创建 FBAShipment
     * <p/>
     * ps: 这个方法不允许并发
     */
    public synchronized void confirmAndSyncTOAmazon() {
        /**
         * 1. 将本地的状态修改为 CONFIRM
         * 2. 向 Amazon 提交 FBA Shipment 的创建
         * 3. Amazon Shipment 创建成功后再本地更新, 否则不更新,包裹错误.
         */
        FBAShipment fba = this.postFBAShipment();
        if(Validation.hasErrors()) return;
        this.state = S.CONFIRM;
        this.target = fba.address();
        this.save();
    }

    /**
     * 将本地运输单的信息同步到 FBA Shipment<br/>
     * <p/>
     * 会通过 Record 去寻找从当前 FBAShipemnt 中删除的 ShipItem, 需要将其先从 FBA 中删除了再更新
     */
    public synchronized void updateFbaShipment() {
        try {
            List<ShipItem> dealItems = new ArrayList<ShipItem>();
            dealItems.addAll(this.items);
            dealItems.addAll(FBA.deleteShipItem(this.id));
            this.fbaShipment.state = FBA.update(this.fbaShipment, dealItems, this.fbaShipment.state);
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("shipment.beginShip.update", "%s " + Webs.E(e));
        }
        if(Validation.hasErrors()) return;
        this.fbaShipment.save();
    }

    /**
     * 具体的开始运输
     * <p/>
     * ps: 不允许多个人, 对 Shipment 多次 beginShip
     */
    public synchronized void beginShip() {
        Validation.equals("shipment.beginShip.state", this.state, "", S.CONFIRM);

        try {
            this.fbaShipment.state = FBA.update(this.fbaShipment, FBAShipment.S.SHIPPED);
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("shipment.beginShip.update", "%s " + Webs.E(e));
        }
        if(Validation.hasErrors()) return;
        for(ShipItem item : this.items) {
            item.shipDate = new Date();
            item.unit.stage = item.unit.nextStage();
            item.unit.save();
        }
        this.pype = this.pype();
        this.state = S.SHIPPING;
        this.save();
    }

    public P pype() {
        // 自动根据 体积与重量的值, 自动计算价格类型
        if(this.volumn == null || this.weight == null) return this.pype;
        if(this.volumn > this.weight) return P.VOLUMN;
        else return P.WEIGHT;
    }

    /**
     * 根据此 Shipment 创建一个 FBAShipment
     * <p/>
     * ps: 这个方法不允许并发
     *
     * @return
     */
    public synchronized FBAShipment postFBAShipment() {
        FBAShipment fba = null;
        try {
            fba = FBA.plan(this);
            fba.save();
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("shipment.postFBAShipment.plan", "%s " + Webs.E(e));
        }
        try {
            if(fba != null) {
                fba.state = FBA.create(fba);
                fba.save();
            }
        } catch(FBAInboundServiceMWSException e) {
            Validation.addError("shipment.postFBAShipment.create", "%s " + Webs.E(e));
        }
        return fba;
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
     * 创建的计划运输单超过 7 天则表示超时
     *
     * @return
     */
    public boolean overDue() {
        long t = System.currentTimeMillis() - this.createDate.getTime();
        return t - TimeUnit.DAYS.toMillis(7) > 0;
    }

    /**
     * 抓取 DHL, FEDEX 网站的运输信息, 更新系统中 SHIPMENT 的状态
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
        String count = Shipment.count("createDate>=? AND createDate<=?",
                DateTime.parse(String.format("%s-%s-01", dt.getYear(), dt.getMonthOfYear())).toDate(),
                DateTime.parse(String.format("%s-%s-30", dt.getYear(), dt.getMonthOfYear())).toDate()) + "";
        return String.format("SP|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

    public static List<Shipment> shipmentsByState(S state) {
        return Shipment.find("state=? ORDER BY createDate", state).fetch();
    }

    /**
     * 由于 Play 无法将 models 目录下的 Enumer 加载, 所以通过 model 提供一个暴露方法在 View 中使用
     *
     * @return
     */
    public static List<iExpress> iExpress() {
        return Arrays.asList(iExpress.values());
    }
}
