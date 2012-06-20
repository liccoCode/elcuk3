package models.procure;

import helper.Currency;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 一张运输单
 * User: wyattpan
 * Date: 6/17/12
 * Time: 5:32 PM
 */
@Entity
public class Shipment extends GenericModel {

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
    public String id;

    /**
     * 此货运单人工创建的时间
     */
    public Date createDate = new Date();

    @Enumerated(EnumType.STRING)
    @Column(length = 12)
    public S state;

    /**
     * 货运开始日期
     */
    public Date beginDate;

    /**
     * 预计货运到达时间
     */
    public Date planArrivDate;

    /**
     * 实际到达时间
     */
    public Date arriveDate;

    /**
     * 货运类型
     */
    @Enumerated(EnumType.STRING)
    public T type;

    /**
     * 计价类型
     */
    @Enumerated(EnumType.STRING)
    public P pype;

    /**
     * 计价单价
     */
    public Float price;
    /**
     * 单价单位
     */
    @Enumerated(EnumType.STRING)
    public Currency currency;

    /**
     * 计价数量
     */
    public Integer pQty;

    /**
     * 类似顺风发货单号的类似跟踪单号
     */
    public String trackNo;

    /**
     * 货运商
     */
    public String shipper;

    /**
     * 起始地址
     */
    public String source;

    /**
     * 目的地址
     */
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
        return Shipment.find("state=?", state).fetch();
    }

}
