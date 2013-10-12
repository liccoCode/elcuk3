package services;

import helper.DBUtils;
import helper.Dates;
import models.finance.FeeType;
import models.market.M;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.db.helper.SqlSelect;
import play.libs.F;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 10/12/13
 * Time: 1:51 PM
 */
public class MetricAmazonFeeService {

    /**
     * Selling 的 Amazon 消耗的费用;
     * <p/>
     * 因为 Amazon 收费的不及时, 最近 10 天, 手动模拟计算一个典型:
     * 固定:
     * US: fbaweightbasedfee, fbaweighthandlingfee, fbapickpackfeeperunit => 2.46 USD
     * UK: fbaweighthandlingfee, fbapickpackfeeperunit => 2.49 USD
     * DE: fbaweighthandlingfee, fbapickpackfeeperunit => 有高有低, 取 3.1 USD
     * 统一起来取  2.8 USD
     * Commission: 按照销售价 13% 取
     */
    public Map<String, Float> sellingAmazonFee(Date date, List<Selling> sellings) {
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            Map<String, Float> sellingAmzFeeMap = new HashMap<String, Float>();
            for(Selling sell : sellings) {
                sellingAmzFeeMap.put(sell.sellingId, (2.8f + (sell.aps.salePrice == null ? 0 : sell.aps.salePrice)));
            }
            return sellingAmzFeeMap;
        } else {
            List<FeeType> fees = FeeType.amazon().children;
            List<String> feesTypeName = new ArrayList<String>();
            for(FeeType fee : fees) {
                if("shipping".equals(fee.name)) continue;
                feesTypeName.add(fee.name);
            }
            return sellingFeeTypesCost(date, feesTypeName);
        }

    }

    /**
     * Selling 的 FBA 销售的费用
     * <p/>
     * 因为 Amazon 收费的不及时, 最近 10 天, 手动模拟计算一个典型:
     * 固定:
     * US: fbaweightbasedfee, fbaweighthandlingfee, fbapickpackfeeperunit => 2.46 USD
     * UK: fbaweighthandlingfee, fbapickpackfeeperunit => 2.49 USD
     * DE: fbaweighthandlingfee, fbapickpackfeeperunit => 有高有低, 取 3.1 USD
     * 统一起来取  2.8 USD
     */
    public Map<String, Float> sellingAmazonFBAFee(Date date, List<Selling> sellings) {
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            Map<String, Float> sellingAmzFbaFeeMap = new HashMap<String, Float>();
            for(Selling sell : sellings) {
                sellingAmzFbaFeeMap.put(sell.sellingId, 2.8f);
            }
            return sellingAmzFbaFeeMap;
        } else {
            List<FeeType> fees = FeeType.fbaFees();
            List<String> feesTypeName = new ArrayList<String>();
            for(FeeType fee : fees) {
                feesTypeName.add(fee.name);
            }
            return sellingFeeTypesCost(date, feesTypeName);
        }
    }

    /**
     * 查询某天销售中, 每个 Selling 所涉及的 OrderId 是哪些;
     * 不同的市场需要拥有不同的时间段
     */
    public Map<String, List<String>> oneDaySellingOrderIds(Date date) {
        Map<String, List<String>> sellingOrders = new HashMap<String, List<String>>();
        for(M m : M.values()) {
            if(m.isEbay()) continue;
            sellingOrders.putAll(oneDaySellingOrderIds(date, m));
        }
        return sellingOrders;
    }

    public Map<String, List<String>> oneDaySellingOrderIds(Date date, M market) {
        // 设置 group_concat_max_len 最大为 20M
        F.T2<DateTime, DateTime> actualDatePair = market.withTimeZone(Dates.morning(date), Dates.night(date));
        DBUtils.execute("set group_concat_max_len=20971520");
        SqlSelect sellingOdsSql = new SqlSelect()
                .select("selling_sellingId as sellingId", "group_concat(order_orderId) as orderIds")
                .from("OrderItem")
                .where("market=?").param(market.name())
                .where("createDate>=?").param(actualDatePair._1.toDate())
                .where("createDate<=?").param(actualDatePair._2.toDate())
                .groupBy("sellingId");
        Map<String, List<String>> sellingOrders = new HashMap<String, List<String>>();
        List<Map<String, Object>> rows = DBUtils.rows(sellingOdsSql.toString(), sellingOdsSql.getParams().toArray());
        for(Map<String, Object> row : rows) {
            String sellingId = row.get("sellingId").toString();
            if(StringUtils.isBlank(sellingId)) continue;
            sellingOrders.put(sellingId, Arrays.asList(StringUtils.split(row.get("orderIds").toString(), ",")));
        }
        return sellingOrders;
    }

    /**
     * 指定 amazon 费用类型, 返回当天所有 Selling 这些费用类型的总费用
     */
    public Map<String, Float> sellingFeeTypesCost(Date date, List<String> feeTypes) {
        Map<String, List<String>> sellingOrders = oneDaySellingOrderIds(date);
        SqlSelect sellFeesTemplate = new SqlSelect()
                .select("sum(usdCost) as cost")
                .from("SaleFee")
                        // 需要统计 productcharges 销售价格, 和 shipping 加快快递(这个会在 amazon 中减去)
                .where(SqlSelect.whereIn("type_name", feeTypes));
        Map<String, Float> sellingSales = new HashMap<String, Float>();
        for(String sellingId : sellingOrders.keySet()) {
            SqlSelect sellFees = new SqlSelect(sellFeesTemplate)
                    .where(SqlSelect.whereIn("order_orderId", sellingOrders.get(sellingId)));
            Map<String, Object> row = DBUtils.row(sellFees.toString());
            Object costObj = row.get("cost");
            sellingSales.put(sellingId, costObj == null ? 0 : NumberUtils.toFloat(costObj.toString()));
        }
        return sellingSales;
    }
}
