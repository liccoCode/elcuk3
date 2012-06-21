package models.procure;

import com.google.gson.annotations.Expose;
import helper.Currency;
import org.joda.time.DateTime;
import play.data.validation.Required;
import play.db.jpa.GenericModel;

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
public class Shipment extends GenericModel {

    public Shipment() {
        this.createDate = new Date();

        // 计价方式
        this.pQty = 1f;
        this.price = 1f;
        this.currency = Currency.CNY;
        this.pype = P.VOLUMN;
        this.state = S.PEDING;

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
        PEDING,
        /**
         * 运输中
         */
        SHIPPING,
        /**
         * 清关
         */
        CLEARGATE,
        /**
         * 入库中
         */
        RECIVING,
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
     * 此 Shipment 所运输的 ProcureUnits
     */
    @OneToMany(mappedBy = "shipment")
    public List<ProcureUnit> shipunits = new ArrayList<ProcureUnit>();

    @OneToMany(mappedBy = "shipment")
    public List<Payment> payments = new ArrayList<Payment>();

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
    @Required
    public Float price;
    /**
     * 单价单位
     */
    @Enumerated(EnumType.STRING)
    @Expose
    @Required
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

}
