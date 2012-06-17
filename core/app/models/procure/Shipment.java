package models.procure;

import helper.Currency;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.Date;

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

    @Id
    public String id;

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
        String count = Shipment.count() + "";
        return String.format("SP|%s|%s", dt.toString("yyyyMM"), count.length() == 1 ? "0" + count : count);
    }

}
