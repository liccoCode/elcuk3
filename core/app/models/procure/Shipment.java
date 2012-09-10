package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import helper.Dates;
import helper.FLog;
import helper.Webs;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.joda.time.DateTime;
import play.Logger;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.db.jpa.GenericModel;
import play.libs.F;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
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
public class Shipment extends GenericModel {

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

    /**
     * 运输合作商
     */
    @OneToOne
    public Cooperator cooper;

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
     * 计价类型
     */
    @Enumerated(EnumType.STRING)
    @Expose
    public P pype;

    /**
     * 计价单价
     */
    @Expose
    public Float price;
    /**
     * 单价单位
     */
    @Enumerated(EnumType.STRING)
    @Expose
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
     * //TODO 需要删除, 使用 cooper 代替
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
        try {
            this.iExpressHTML = this.internationExpress.parseExpress(html, this.trackNo);
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
            FLog.fileLog(String.format("%s.%s.%s.html", this.id, this.trackNo, this.internationExpress.name()), html, FLog.T.HTTP_ERROR);
            throw new FastRuntimeException(Webs.S(e));
        }
        return this.iExpressHTML;
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
