package models.market;

import play.db.jpa.Model;

import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/14
 * Time: 上午11:43
 */
@Entity
public class MarketRecord extends Model{

    private static final long serialVersionUID = 4418184038845182213L;

    public Date createDate;

    public int totalOrders;

    public int pendingOrders;

    public int paymentOrders;

    public int shippedOrders;

    public int cancelOrders;

    public int refundOrders;

    public int returnNewOrders;

    /**
     * 总销售额
     */
    public BigDecimal totalSale;

    /**
     * 总FBA费用
     */
    public BigDecimal totalFbafee;

}
