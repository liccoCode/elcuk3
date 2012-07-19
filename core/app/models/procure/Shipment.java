package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.Dates;
import helper.FLog;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 一张运输单
 * User: wyattpan
 * Date: 6/17/12
 * Time: 5:32 PM
 */
@Entity
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Shipment extends GenericModel implements Payment.ClosePayment {

    public Shipment() {
    }

    public Shipment(String id) {
        this.createDate = new Date();

        // 计价方式
        this.pQty = 1f;
        this.price = 1f;
        this.currency = Currency.CNY;
        this.pype = P.VOLUMN;
        this.state = S.PLAN;

        // 暂时这么写
        this.source = "深圳";
        this.shipper = "周伟";
        this.type = T.AIR;

        this.id = id;
    }

    public enum T {
        /**
         * 海运
         */
        SEA,
        /**
         * 空运
         */
        AIR
    }

    public enum S {
        /**
         * 计划中
         */
        PLAN,
        /**
         * 运输中
         */
        SHIPPING,
        /**
         * 清关
         */
        CLEARANCE,
        /**
         * 完成
         */
        DONE,

        /**
         * 取消状态
         */
        CANCEL
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
     * 此 Shipment 的付款信息
     */
    @OneToMany(mappedBy = "shipment")
    @OrderBy("state DESC")
    public List<Payment> payments = new ArrayList<Payment>();

    /**
     * 此 Shipment 的运输项
     */
    @OneToMany(mappedBy = "shipment")
    public List<ShipItem> items = new ArrayList<ShipItem>();

    @Id
    @Column(length = 30)
    @Expose
    @Required(message = "v.require.shipment.id")
    public String id;

    /**
     * 此货运单人工创建的时间
     */
    @Expose
    @Required(message = "v.require.shipment.createDate")
    public Date createDate = new Date();

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    @Expose
    @Required(message = "v.require.shipment.state")
    public S state;

    /**
     * 货运开始日期
     */
    @Expose
    public Date beginDate;

    /**
     * 预计货运到达时间
     */
    @Expose
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
    @Required(message = "v.require.shipment.type")
    public T type;

    /**
     * 计价类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public P pype;

    /**
     * 计价单价
     */
    @Expose
    @Required(message = "v.require.shipment.price")
    public Float price;
    /**
     * 单价单位
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Required(message = "v.require.shipment.currency")
    public Currency currency;

    /**
     * 计价数量
     */
    @Expose
    public Float pQty;

    /**
     * 类似顺风发货单号的类似跟踪单号
     */
    @Expose
    public String trackNo;

    /**
     * 货运商
     */
    @Expose
    public String shipper;

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
    @Required(message = "v.require.shipment.source")
    public String source;

    /**
     * 目的地址
     */
    @Expose
    @Required(message = "v.require.shipment.target")
    public String target;

    /**
     * 国际运输的运输信息的记录
     */
    @Lob
    public String iExpressHTML = " ";

    @Lob
    public String memo = " ";

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

    public Shipment checkAndCreate() {
        return this.save();
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
     * 从 Plan 状态到 Ship 状态
     *
     * @param s 用来传递值的临时对象
     * @return
     */
    public Shipment fromPlanToShip(Shipment s) {
        if(this.state != S.PLAN) throw new FastRuntimeException("Shipment (" + this.id + ") 状态应该为 PLAN!");
        if(StringUtils.isBlank(s.trackNo)) throw new FastRuntimeException("Trac No 不允许为空!");
        if(s.internationExpress == null) throw new FastRuntimeException("国际快递商不允许为空!");
        if(s.beginDate == null) throw new FastRuntimeException("进行 Shipping 状态, 开始时间不能为空!");
        if(s.planArrivDate == null) throw new FastRuntimeException("必须指定预计到达时间!");
        this.trackNo = s.trackNo.trim();
        this.internationExpress = s.internationExpress;
        this.state = S.SHIPPING;
        this.beginDate = s.beginDate;
        this.planArrivDate = s.planArrivDate;
        return this.save();
    }

    public Payment payForShipment(Payment payment) {
        if(payment == null) throw new FastRuntimeException("付款信息为空!");
        if(StringUtils.isBlank(payment.memo)) throw new FastRuntimeException("必须填写付款原因.");
        payment.paymentCheckItSelf();

        payment.shipment = this; //由 Payment 添加关联
        payment.shipment.save();
        return payment.save();
    }

    @Override
    public void close(Payment thisPayment) {
        // empty check close in shipment.
    }

    /**
     * 返回总共付款的 RMB 的金额
     *
     * @return
     */
    public Float totalPayedCNY() {
        float totalPayed = 0;
        for(Payment pay : this.payments)
            if(pay.state == Payment.S.NORMAL) totalPayed += pay.currency.toCNY(pay.price);
        return totalPayed;
    }

    /**
     * 抓取 DHL, FEDEX 网站的运输信息, 更新系统中 SHIPMENT 的状态
     *
     * @return
     */
    public String refreshIExpressHTML() {
        String html = this.internationExpress.fetchStateHTML(this.trackNo);
        try {
            this.iExpressHTML = this.internationExpress.parseExpress(html);
            if(this.state == S.SHIPPING) { // 如果在 SHIPPING 状态则检查是否处于清关
                if(this.internationExpress.isContainsClearance(this.iExpressHTML)) {
                    this.state = S.CLEARANCE;
                    Mails.shipment_clearance(this);
                }
            } else if(this.state == S.CLEARANCE) { // 如果在 CLERANCE 检查是否有 Delivery 日期
                F.T2<Boolean, DateTime> isDeliveredAndTime = this.internationExpress.isDelivered(this.iExpressHTML);
                if(isDeliveredAndTime._1) {
                    this.arriveDate = isDeliveredAndTime._2.toDate();
                    Mails.shipment_isdone(this);
                }
            }
            this.save();
        } catch(Exception e) {
            FLog.fileLog(String.format("%s.%s.html", this.id, this.internationExpress.name()), html, FLog.T.HTTP_ERROR);
            throw new FastRuntimeException(e);
        }
        return this.iExpressHTML;
    }

    /**
     * 此运输单完成; 每一个 Shipment 关闭的时候, 都需要检查其中所有 ShipItem 所关联的 ProcureUnit 是否可以进行 SHIP_OVER 状态了.
     */
    public Shipment done() {
        if(this.state == S.PLAN) throw new FastRuntimeException("不允许从 PLAN 状态直接到 DONE");
        if(this.arriveDate == null) throw new FastRuntimeException("完成运输单, 必须拥有具体到达时间");
        if(this.arriveDate.getTime() < this.beginDate.getTime()) throw new FastRuntimeException("实际到达时间小于开始时间?");
        //TODO 运输单完成, 添加判断运输单项关联的 ProcureUnit 是否可以标记完成.
        for(ShipItem item : this.items) item.unit.beShipOver();
        this.state = S.DONE;
        return this.save();
    }

    /**
     * 取消一个 Shipment.
     */
    public void cancel() {
        if(this.state == S.CANCEL) throw new FastRuntimeException("Shipment " + this.id + " 已经为 CANCEL 状态");
        if(this.state != S.PLAN) throw new FastRuntimeException("已经在路上的 Shipment " + this.id + " 无法取消.");
        for(ShipItem item : this.items)
            item.cancel();
        this.state = S.CANCEL;
        this.memo = String.format("Shipment 于 %s 关闭, 共影响 %s 个 ShipItem.\r\n", Dates.date2DateTime(), this.items.size()) + this.memo;
        this.save();
    }

    /**
     * 此运输单总共的运输时长
     * 1. 如果运输单完成了, 则为 (实际到达时间 - 开始时间)
     * 2. 如果没有完成, 则为 (当前时间 - 开始时间)
     *
     * @return
     */
    public float shippingDays() {
        if(this.state == S.DONE) {
            if(this.arriveDate == null) throw new FastRuntimeException("运输单已经 Done, 但没有 [实际到达时间], 请联系 IT");
            return TimeUnit.MILLISECONDS.toDays(this.arriveDate.getTime() - this.beginDate.getTime());
        } else {
            return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - this.beginDate.getTime());
        }
    }

}
