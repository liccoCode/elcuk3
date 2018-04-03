package models.whouse;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * 每日库存
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2018/4/2
 * Time: 上午11:14
 */
@Entity
public class DailyStock extends Model {

    private static final long serialVersionUID = 7110535480291015947L;

    public int qty;

    @Temporal(TemporalType.DATE)
    public Date date;

    public double totalCNY;

    public double totalUSD;

    /**
     * 当日入库数量
     */
    public int inboundQty;

    /**
     * 当日出库数量
     */
    public int outboundQty;

    /**
     * 当日计划数量
     */
    public int planQty;

    /**
     * 当日下单数量
     */
    public int deliveryQty;

    /**
     * 当日收货数量
     */
    public int doneQty;

    public enum T {
        ProcureUnit,
        Whouse
    }

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public T type;
}
