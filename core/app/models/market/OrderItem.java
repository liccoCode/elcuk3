package models.market;

import helper.Caches;
import models.product.Product;
import play.cache.Cache;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 订单的具体订单项
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:42 AM
 */
@Entity
public class OrderItem extends GenericModel {

    /**
     * 为保持更新的时候的唯一性, 所以将起 Id 设置为 orderId_sku
     */
    @Id
    public String id;

    @ManyToOne(fetch = FetchType.LAZY)
    public Orderr order;

    @OneToOne
    public Selling selling;

    @OneToOne
    public Product product;

    /**
     * 冗余字段, 产品名称
     */
    public String productName;

    /**
     * 冗余字段, 订单项产生的时间
     */
    public Date createDate;

    /**
     * 这个商品的销售
     */
    public Float price;

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

    /**
     * <pre>
     * 通过 OrderItem 计算指定的 msku 在一个时间段内的销量情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     * yAxis,
     * xAxis:{
     *   type: 'datetime',
     *   dateTimeLabelFormats: {
     *     day: '%y.%m.%d'
     *   }
     * }
     * series:
     *     series: [{
     *       data: [29.9, 71.5, 106.4, 129.2, 144.0, 176.0, 135.6, 148.5, 216.4, 194.1, 95.6, 54.4],
     *       pointStart: Date.UTC(2010, 0, 1),
     *       pointInterval: 24 * 3600 * 1000 // one day
     *   }]
     *
     * HightChart 的使用 http://jsfiddle.net/kSkYN/6937/
     * </pre>
     *
     * @param msku
     * @param from
     * @param to
     * @return {series_size, days, series_n}
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> ajaxHighChartSelling(String msku, Date from, Date to) {
        Map<String, Object> cached = Cache.get(String.format(Caches.AJAX_SALE_LINE, msku), Map.class);
        if(cached != null && cached.size() > 0) return cached;
        /**
         * 加载出限定时间内的指定 Msku 的 OrderItem
         * 按照天过滤成销量数据
         * 组装成 HightChart 的格式
         */
        List<OrderItem> orderItems;
        if("all".equalsIgnoreCase(msku)) {
            orderItems = OrderItem.find(
                    "SELECT oi FROM OrderItem oi WHERE oi.createDate>=? AND oi.createDate<=?",
                    from, to).fetch();
        } else {
            orderItems = OrderItem.find(
                    "SELECT oi FROM OrderItem oi WHERE oi.selling.merchantSKU=? AND oi.createDate>=? AND oi.createDate<=?",
                    msku, from, to).fetch();
        }

        int days = (int) Math.ceil((to.getTime() - from.getTime()) / (24 * 3600 * 1000.0));
        // 按照每天进行分割
        List<Integer> allSales = new ArrayList<Integer>();
        List<Integer> amazonUk = new ArrayList<Integer>();
        List<Integer> amazonDe = new ArrayList<Integer>();
        List<Integer> amazonEs = new ArrayList<Integer>();
        List<Integer> amazonIt = new ArrayList<Integer>();
        List<Integer> amazonFr = new ArrayList<Integer>();
        List<Integer> amazonUs = new ArrayList<Integer>();
        List<Integer> ebayUk = new ArrayList<Integer>();


        // 从 from 时间开始, 按照每 24 小时进行一个时区进行划分, 将 OrderItem 划分到每个时间区间中去
        for(long begin = from.getTime(); begin <= to.getTime(); begin += TimeUnit.DAYS.toMillis(1)) {
            int all = 0;
            int auk = 0;
            int aus = 0;
            int ade = 0;
            int ait = 0;
            int afr = 0;
            int aes = 0;
            int euk = 0;

            for(OrderItem itm : orderItems) {
                if(itm.createDate.getTime() > begin && itm.createDate.getTime() <= (begin + TimeUnit.DAYS.toMillis(1))) {
                    all += itm.quantity;
                    switch(itm.selling.market) {
                        case AMAZON_UK:
                            auk += itm.quantity;
                            break;
                        case AMAZON_DE:
                            ade += itm.quantity;
                            break;
                        case AMAZON_FR:
                            afr += itm.quantity;
                            break;
                        case AMAZON_IT:
                            ait += itm.quantity;
                            break;
                        case AMAZON_US:
                            auk += itm.quantity;
                            break;
                        case AMAZON_ES:
                            aes += itm.quantity;
                            break;
                        case EBAY_UK:
                            euk += itm.quantity;
                    }
                }
            }
            // 每一个时间区间需要一组销量数据
            allSales.add(all);
            amazonUk.add(auk);
            amazonDe.add(ade);
            amazonIt.add(ait);
            amazonFr.add(afr);
            amazonEs.add(aes);
            amazonUs.add(aus);
            ebayUk.add(euk);
        }


        Map<String, Object> hightChartMap = new HashMap<String, Object>();
        hightChartMap.put("days", days);
        hightChartMap.put("series_all", allSales);
        hightChartMap.put("series_auk", amazonUk);
        hightChartMap.put("series_ade", amazonDe);
        hightChartMap.put("series_afr", amazonFr);
        hightChartMap.put("series_aes", amazonEs);
        hightChartMap.put("series_ait", amazonIt);
        hightChartMap.put("series_aus", amazonUs);
        hightChartMap.put("series_euk", ebayUk);

        if(hightChartMap.size() > 0) Cache.add(String.format(Caches.AJAX_SALE_LINE, msku), hightChartMap, "20mn");
        return hightChartMap;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof OrderItem)) return false;
        if(!super.equals(o)) return false;

        OrderItem orderItem = (OrderItem) o;

        if(!order.orderId.equals(orderItem.order.orderId)) return false;
        if(!product.sku.equals(orderItem.product.sku)) return false;
        if(!selling.sellingId.equals(orderItem.selling.sellingId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + order.orderId.hashCode();
        result = 31 * result + selling.sellingId.hashCode();
        result = 31 * result + product.sku.hashCode();
        return result;
    }
}
