package services;

import helper.Currency;
import helper.DBUtils;
import helper.Dates;
import helper.Promises;
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
 * Date: 10/15/13
 * Time: 10:17 AM
 */
public class MetricSalesService {
    private MetricAmazonFeeService amzService = new MetricAmazonFeeService();

    /**
     * Selling 的销量数据
     */
    public Map<String, Integer> sellingUnits(Date date) {
        Map<String, Integer> sellingUnits = new HashMap<>();
        for(M m : Promises.MARKETS) {
            sellingUnits.putAll(sellingUnits(date, m));
        }
        return sellingUnits;
    }

    public Map<String, Integer> sellingUnits(Date date, M market) {
        DateTime dt = new DateTime(date);
        F.T2<DateTime, DateTime> actualDatePair = market
                .withTimeZone(Dates.morning(date), Dates.morning(dt.plusDays(1).toDate()));
        SqlSelect sql = new SqlSelect()
                .select("oi.selling_sellingId as sellingId", "sum(oi.quantity) as qty")
                .from("OrderItem oi")
                .leftJoin("Orderr o ON o.orderId=oi.order_orderId")
                .where("oi.market=?").param(market.name())
                .where("oi.createDate>=?").param(actualDatePair._1.toDate())
                .where("oi.createDate<?").param(actualDatePair._2.toDate())
                .groupBy("sellingId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, Integer> sellingUnits = new HashMap<>();
        for(Map<String, Object> row : rows) {
            String sellingId = row.get("sellingId").toString();
            if(StringUtils.isBlank(sellingId)) continue;
            sellingUnits.put(sellingId, NumberUtils.toInt(row.get("qty").toString()));
        }
        return sellingUnits;
    }

    /**
     * @param date
     * @return
     */
    public Map<String, Integer> sellingOrders(Date date) {
        Map<String, Integer> sellingOrders = new HashMap<>();
        for(M m : Promises.MARKETS) {
            sellingOrders.putAll(sellingOrders(date, m));
        }
        return sellingOrders;
    }

    public Map<String, Integer> sellingOrders(Date date, M market) {
        DateTime dt = new DateTime(date).plusDays(1);
        F.T2<DateTime, DateTime> actualDatePair = market.withTimeZone(Dates.morning(date), Dates.morning(dt.toDate()));
        SqlSelect sql = new SqlSelect()
                .select("oi.selling_sellingId as sellingId", "count(o.orderId) as qty")
                .from("Orderr o")
                .leftJoin("OrderItem oi ON o.orderId=oi.order_orderId")
                .where("o.market=?").param(market.name())
                .where("o.createDate>=?").param(actualDatePair._1.toDate())
                .where("o.createDate<?").param(actualDatePair._2.toDate())
                .groupBy("sellingId");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());
        Map<String, Integer> sellingOrders = new HashMap<>();
        for(Map<String, Object> row : rows) {
            Object sellingId = row.get("sellingId");
            if(sellingId == null || StringUtils.isBlank(sellingId.toString())) continue;
            sellingOrders.put(sellingId.toString(), NumberUtils.toInt(row.get("qty").toString()));
        }
        return sellingOrders;
    }

    /**
     * Selling 的销售额数据;
     * <p/>
     * 因为 Amazon 收费的不及时, 所以对于离当天 10 天内的数据使用系统中的订单量进行计算
     */
    public Map<String, Float> sellingSales(Date date, List<Selling> sellings, Map<String, Integer> sellingUnits) {
        Map<String, Float> sellingSales = new HashMap<>();
        if((System.currentTimeMillis() - date.getTime()) <= TimeUnit.DAYS.toMillis(10)) {
            for(Selling sell : sellings) {
                Integer units = sellingUnits.get(sell.sellingId);
                if(units == null) units = 0;
                helper.Currency currency = Currency.USD;
                if(sell.account.type == M.AMAZON_DE) currency = Currency.EUR;
                else if(sell.account.type == M.AMAZON_UK) currency = Currency.GBP;
                else if(sell.account.type == M.AMAZON_US) currency = Currency.USD;
                else if(sell.account.type == M.AMAZON_JP) currency = Currency.JPY;
                else if(sell.account.type == M.AMAZON_CA) currency = Currency.CAD;
                sellingSales.put(sell.sellingId,
                        units * (sell.aps.salePrice == null ? 0 : currency.toUSD(sell.aps.salePrice)));
            }
        } else {
            /**
             * 1. 找到某天 OrderItem 中所有涉及的 Selling 与每个 Selling 涉及的 Order.id
             * 2. 根据每个 selling 所涉及的 id 与费用类型, 计算处每个 Selling 的销售额
             */
            sellingSales = amzService.sellingFeeTypesCost(date, Arrays.asList("productcharges", "shipping"));
        }
        return sellingSales;
    }
}
