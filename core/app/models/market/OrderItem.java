package models.market;

import helper.*;
import helper.Currency;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.*;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.libs.F;
import query.OrderItemQuery;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 订单的具体订单项
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:42 AM
 */
@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true)
public class OrderItem extends GenericModel {

    /**
     * 为保持更新的时候的唯一性, 所以将起 Id 设置为 orderId_sku
     */
    @Id
    public String id;

    @ManyToOne(fetch = FetchType.LAZY)
    public Orderr order;

    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    /**
     * 冗余字段, 产品名称
     */
    public String listingName;

    /**
     * 冗余字段, 订单项产生的时间
     */
    public Date createDate;

    /**
     * 这个商品的销售
     */
    public Float price;

    /**
     * 记录这个 OrderItem 记录的货币单位[price/discountPrice/..]
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10) DEFAULT ''")
    public Currency currency;

    /**
     * 统一为 USD 的金额
     */
    @Column(columnDefinition = "float DEFAULT 0")
    public Float usdCost;

    /**
     * 如果做促销的话, 具体折扣的价格
     */
    public Float discountPrice;

    /**
     * 这个商品所承担的运输费用
     */
    public Float shippingPrice;

    /**
     * 对不同网站不同情况的 Fee 所产生费用的一个总计(经过 -/+ 计算后的) TODO 暂时没有启用
     */
    public Float feesAmaount;


    /**
     * 商品的数量; 一般情况下这个数量就为 1, 一个订单有多少个 Item 则需要有多少个 Item 项; 但为了避免批发形式的销售, 所以需要拥有一个 quantity 将
     * 批发形式的销售作为一个拥有数量的 OrderItem 来对待;
     */
    public Integer quantity;

    /**
     * 一个中性的记录消息的地方
     */
    public String memo = "";

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) DEFAULT 'AMAZON_UK'")
    public M market;


    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        OrderItem orderItem = (OrderItem) o;

        if(!id.equals(orderItem.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    /**
     * 根据 sku 或者 msku 从 OrderItem 中查询对应 Account 的 OrderItem
     * ps: 结果缓存 5mn (缓存是为了防止两次访问此方法, 此数据最终的缓存放置在了页面内容缓存)
     *
     * @param skuOrMsku sku 或者 merchantSKU(msku)
     * @param type      sku/all/msku
     * @param acc
     * @param from
     * @param to
     * @return
     */
    @SuppressWarnings("unchecked")
    @Cached("5mn") //缓存是为了防止两次访问此方法, 此数据最终的缓存放置在了页面内容缓存
    public static List<OrderItem> skuOrMskuAccountRelateOrderItem(String skuOrMsku, String type, Account acc, Date from, Date to) {
        String cacheKey = Caches.Q.cacheKey(skuOrMsku, type, acc, from, to);
        List<OrderItem> orderItems = Cache.get(cacheKey, List.class);
        if(orderItems != null) return orderItems;
        synchronized(OrderItem.class) {
            orderItems = Cache.get(cacheKey, List.class);
            if(orderItems != null) return orderItems;

            if("all".equalsIgnoreCase(skuOrMsku)) {
                orderItems = OrderItem.find("createDate>=? AND createDate<=? AND order.state NOT IN (?,?,?)",
                        from, to, Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW).fetch();
            } else {
                if(StringUtils.isNotBlank(type) && "sku".equalsIgnoreCase(type))
                    orderItems = OrderItem.find("product.sku=? AND createDate>=? AND createDate<=? AND order.state NOT IN (?,?,?)",
                            Product.merchantSKUtoSKU(skuOrMsku), from, to, Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW).fetch();
                else {
                    if(acc == null)
                        orderItems = OrderItem.find("selling.merchantSKU=? AND createDate>=? AND createDate<=? AND order.state NOT IN (?,?,?)",
                                skuOrMsku, from, to, Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW).fetch();
                    else
                        orderItems = OrderItem.find("selling.merchantSKU=? AND selling.account=? AND createDate>=? AND createDate<=? AND order.state NOT IN (?,?,?)",
                                skuOrMsku, acc, from, to, Orderr.S.CANCEL, Orderr.S.REFUNDED, Orderr.S.RETURNNEW).fetch();
                }
            }
            Cache.add(cacheKey, orderItems, "5mn");
        }
        return Cache.get(cacheKey, List.class);
    }

    public static Map<String, ArrayList<F.T2<Long, Float>>> ajaxHighChartSales(String skuOrMsku, Account acc, String type, Date from, Date to) {
        // 做内部参数的容错
        DateTime inFrom = new DateTime(Dates.date2JDate(from));
        DateTime inTo = new DateTime(Dates.date2JDate(to)).plusDays(1); // "到" 的时间参数, 期望的是这一天的结束
        List<OrderItem> orderItems = skuOrMskuAccountRelateOrderItem(skuOrMsku, type, acc, inFrom.toDate(), inTo.toDate());
        Map<String, ArrayList<F.T2<Long, Float>>> hightChartLines = GTs.MapBuilder
                /*销售额*/
                .map("sale_all", new ArrayList<F.T2<Long, Float>>())
                .put("sale_uk", new ArrayList<F.T2<Long, Float>>())
                .put("sale_de", new ArrayList<F.T2<Long, Float>>())
                .put("sale_fr", new ArrayList<F.T2<Long, Float>>())
                .build();
        DateTime travel = inFrom.plusDays(0); // copy 一个新的
        while(travel.getMillis() <= inTo.getMillis()) { // 开始计算每一天的数据
            // 销售额
            float sale_all = 0;
            float sale_uk = 0;
            float sale_de = 0;
            float sale_fr = 0;

            for(OrderItem oi : orderItems) {
                if(Dates.date2JDate(oi.createDate).getTime() == travel.getMillis()) {
                    try {
                        float usdCost = oi.usdCost == null ? 0 : oi.usdCost;
                        sale_all += usdCost;
                        if(oi.market == M.AMAZON_UK) sale_uk += usdCost;
                        else if(oi.market == M.AMAZON_DE) sale_de += usdCost;
                        else if(oi.market == M.AMAZON_FR) sale_fr += usdCost;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    // 其他市场暂时先不统计
                }
            }
            // 当天所有市场的销售额数据
            hightChartLines.get("sale_all").add(new F.T2<Long, Float>(travel.getMillis(), sale_all));
            hightChartLines.get("sale_uk").add(new F.T2<Long, Float>(travel.getMillis(), sale_uk));
            hightChartLines.get("sale_de").add(new F.T2<Long, Float>(travel.getMillis(), sale_de));
            hightChartLines.get("sale_fr").add(new F.T2<Long, Float>(travel.getMillis(), sale_fr));
            travel = travel.plusDays(1);
        }

        return hightChartLines;
    }

    /**
     * <pre>
     * 通过 OrderItem 计算指定的 skuOrMsku 在一个时间段内的销量情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     * HightChart 的使用 http://jsfiddle.net/kSkYN/6937/
     * </pre>
     *
     * @param skuOrMsku 需要查询的 SKU 或者对应 Selling 的 sid
     * @param acc
     * @param from
     * @param to        @return {series_size, days, series_n}
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ArrayList<F.T2<Long, Float>>> ajaxHighChartUnitOrder(String skuOrMsku, Account acc, String type, Date from, Date to) {
        // 做内部参数的容错
        DateTime inFrom = new DateTime(Dates.date2JDate(from));
        DateTime inTo = new DateTime(Dates.date2JDate(to)).plusDays(1); // "到" 的时间参数, 期望的是这一天的结束
        /**
         * 加载出限定时间内的指定 Msku 的 OrderItem
         * 按照天过滤成销量数据
         * 组装成 HightChart 的格式
         */
        List<OrderItem> orderItems = skuOrMskuAccountRelateOrderItem(skuOrMsku, type, acc, inFrom.toDate(), inTo.toDate());
        Map<String, ArrayList<F.T2<Long, Float>>> hightChartLines = GTs.MapBuilder
                /*销量*/
                .map("unit_all", new ArrayList<F.T2<Long, Float>>())
                .put("unit_uk", new ArrayList<F.T2<Long, Float>>())
                .put("unit_de", new ArrayList<F.T2<Long, Float>>())
                .put("unit_fr", new ArrayList<F.T2<Long, Float>>())
                .build();
        DateTime travel = inFrom.plusDays(0); // copy 一个新的
        while(travel.getMillis() <= inTo.getMillis()) { // 开始计算每一天的数据
            // 销量
            float unit_all = 0;
            float unit_uk = 0;
            float unit_de = 0;
            float unit_fr = 0;
            for(OrderItem oi : orderItems) {
                if(Dates.date2JDate(oi.createDate).getTime() == travel.getMillis()) {
                    unit_all += oi.quantity;
                    if(oi.market == M.AMAZON_UK) unit_uk += oi.quantity;
                    else if(oi.market == M.AMAZON_DE) unit_de += oi.quantity;
                    else if(oi.market == M.AMAZON_FR) unit_fr += oi.quantity;
                    // 其他市场暂时先不统计
                }
            }
            // 当天所有市场的销售订单数据
            hightChartLines.get("unit_all").add(new F.T2<Long, Float>(travel.getMillis(), unit_all));
            hightChartLines.get("unit_uk").add(new F.T2<Long, Float>(travel.getMillis(), unit_uk));
            hightChartLines.get("unit_de").add(new F.T2<Long, Float>(travel.getMillis(), unit_de));
            hightChartLines.get("unit_fr").add(new F.T2<Long, Float>(travel.getMillis(), unit_fr));
            travel = travel.plusDays(1);
        }
        return hightChartLines;
    }

    public static List<F.T3<String, Integer, Float>> itemGroupByCategory(Date from, Date to, Account acc) {
        List<F.T3<String, Integer, Float>> rows = OrderItemQuery.sku_qty_usdCost(from, to, acc);

        Map<String, F.T2<AtomicInteger, AtomicReference<Float>>> categoryAndCounts = new HashMap<String, F.T2<AtomicInteger, AtomicReference<Float>>>();
        for(F.T3<String, Integer, Float> row : rows) {
            if(categoryAndCounts.containsKey(row._1)) {
                categoryAndCounts.get(row._1)._1.addAndGet(row._2);
                categoryAndCounts.get(row._1)._2.set(categoryAndCounts.get(row._1)._2.get() + row._3);
            } else
                categoryAndCounts.put(row._1, new F.T2<AtomicInteger, AtomicReference<Float>>(new AtomicInteger(row._2), new AtomicReference<Float>(row._3)));
        }

        List<F.T3<String, Integer, Float>> categoryAndQty = new ArrayList<F.T3<String, Integer, Float>>();
        for(String key : categoryAndCounts.keySet())
            categoryAndQty.add(new F.T3<String, Integer, Float>(key,
                    categoryAndCounts.get(key)._1.get(),
                    categoryAndCounts.get(key)._2.get()
            ));

        return categoryAndQty;
    }


    public static List<OrderItem> orderRelateItems(String orderId) {
        return OrderItem.find("order.orderId=?", orderId).fetch();
    }
}
