package models.market;

import helper.Caches;
import helper.Dates;
import helper.J;
import org.joda.time.DateTime;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/11/14
 * Time: 上午11:43
 */
@Entity
public class MarketRecord extends GenericModel {

    private static final long serialVersionUID = 4418184038845182213L;

    @Id
    public String id;

    @Temporal(TemporalType.DATE)
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
     * 涨跌率
     */
    public float rose;

    /**
     * 总销售额
     */
    public BigDecimal totalSale;

    /**
     * 总FBA费用
     */
    public BigDecimal totalFbafee;


    public static Map<String, List<MarketRecord>> queryYesterdayRecords() {
        Map<String, List<MarketRecord>> map = new HashMap<>();
        int i = 1;
        while(i <= 7) {
            Date date = DateTime.now().minusDays(i).toDate();
            List<MarketRecord> records = MarketRecord.find("createDate =? ", Dates.date2JDate(date)).fetch();
            map.put(Dates.date2Date(date), records);
            Caches.set("MarketRecords_" + Dates.date2Date(date), J.json(records), 4);
            i++;
        }
        /* 倒序排序 */
        map = map.entrySet().stream().sorted((e1, e2) -> e2.getKey().compareTo(e1.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        return map;
    }

}
