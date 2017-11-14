package models.market;

import com.alibaba.fastjson.JSON;
import helper.Caches;
import helper.Dates;
import org.apache.commons.lang.StringUtils;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/14
 * Time: 上午11:43
 */
@Entity
public class MarketRecord extends Model {

    private static final long serialVersionUID = 4418184038845182213L;

    public Date createDate;

    @Enumerated(EnumType.STRING)
    public M market;

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


    public static List<MarketRecord> queryYesterdayRecords() {
        Date yesterday = Dates.yesterday();
        String cacheKey = "MarketRecords_" + Dates.date2Date(yesterday);
        String json = Caches.get(cacheKey);
        if(StringUtils.isNotBlank(json)) {
            List<MarketRecord> records = JSON.parseArray(json, MarketRecord.class);
            if(records != null && records.size() > 0) {
                return records;
            }
        }
        return MarketRecord.find("createDate =? ", yesterday).fetch();
    }

}
