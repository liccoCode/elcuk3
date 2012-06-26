package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.db.jpa.GenericModel;
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
public class Shipment extends GenericModel implements Payment.ClosePayment {

    public Shipment() {
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

        this.id = Shipment.id();
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
        CLEARGATE,
        /**
         * 完成
         */
        DONE
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
     * @return
     */
    public Shipment fromPlanToShip(String trankNo, iExpress iExpress, Date beginDate, Date planArriveTime) {
        if(this.state != S.PLAN) throw new FastRuntimeException("Shipment (" + this.id + ") 状态应该为 PLAN!");
        if(StringUtils.isBlank(trankNo)) throw new FastRuntimeException("Trac No 不允许为空!");
        if(iExpress == null) throw new FastRuntimeException("国际快递商不允许为空!");
        if(beginDate == null) throw new FastRuntimeException("进行 Shipping 状态, 开始时间不能为空!");
        if(planArriveTime == null) throw new FastRuntimeException("必须指定预计到达时间!");
        this.trackNo = trankNo.trim();
        this.internationExpress = iExpress;
        this.state = S.SHIPPING;
        this.beginDate = beginDate;
        this.planArrivDate = planArriveTime;
        return this.save();
    }

    public Payment payForShipment(Payment payment) {
        if(payment == null) throw new FastRuntimeException("付款信息为空!");
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

    public String refreshIExpressHTML() {
        String html = this.internationExpress.fetchStateHTML(this.trackNo);
        this.iExpressHTML = this.internationExpress.parseState(html);
        this.save();
        return this.iExpressHTML;
    }
}
