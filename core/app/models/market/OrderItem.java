package models.market;

import helper.Caches;
import models.product.Product;
import org.apache.commons.lang.StringUtils;
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

    @ManyToOne
    public Orderr order;

    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @OneToOne(fetch = FetchType.LAZY)
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
     * 在 OrderItem 根据保存的时候, 会减少其相对应的 SKU 的库存[成功保存了 OrderItem 以后处理库存]
     *
     * @see Orderr updateAttrs 更新 OrderItem + 库存部分
     */

    /**
     * <pre>
     * 通过 OrderItem 计算指定的 msku 在一个时间段内的销量情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     * HightChart 的使用 http://jsfiddle.net/kSkYN/6937/
     * </pre>
     *
     * @param msku
     * @param acc
     * @param from
     * @param to   @return {series_size, days, series_n}
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> ajaxHighChartSelling(String msku, Account acc, String type, Date from, Date to) {
        String cached_key = String.format(Caches.AJAX_SALE_LINE, msku + (StringUtils.isNotBlank(type) && "sku".equalsIgnoreCase(type) ? "_sku" : "") + "_" + (acc == null ? "0" : acc.id), from.getTime(), to.getTime());
        Map<String, Object> cached = Cache.get(cached_key, Map.class);
        if(cached != null && cached.size() > 0) return cached;
        /**
         * 举例: 2011-12-29 00:00:00 ~ 2012-1-28 00:00:00 ; 总共时间间隔为 30 天, 但实际上是需要
         * 2011-12-29 00:00:00 ~ 2012-1-28 23:59:59; 总共时间间隔应该为 31 天.
         */
        to.setTime(to.getTime() + (TimeUnit.DAYS.toMillis(1))); // 修正为 2012-1-30 00:00:00 , 不修改为 1-29 23:59:59 是后面进行时间分割需要
        int days = (int) Math.ceil((to.getTime() - from.getTime()) / (24 * 3600 * 1000.0));
        /**
         * 加载出限定时间内的指定 Msku 的 OrderItem
         * 按照天过滤成销量数据
         * 组装成 HightChart 的格式
         */
        List<OrderItem> orderItems;
        if("all".equalsIgnoreCase(msku)) {
            orderItems = OrderItem.find("createDate>=? AND createDate<=?", from, to).fetch();
        } else {
            if(StringUtils.isNotBlank(type) && "sku".equalsIgnoreCase(type))
                orderItems = OrderItem.find("product.sku=? AND createDate>=? AND createDate<=?", Product.merchantSKUtoSKU(msku), from, to).fetch();
            else {
                if(acc == null)
                    orderItems = OrderItem.find("selling.merchantSKU=? AND createDate>=? AND createDate<=?", msku, from, to).fetch();
                else
                    orderItems = OrderItem.find("selling.merchantSKU=? AND selling.account=? AND createDate>=? AND createDate<=?", msku, acc, from, to).fetch();
            }
        }

        // 按照每天进行分割 --- 销量
        List<Integer> allSales = new ArrayList<Integer>();
        List<Integer> amazonUk = new ArrayList<Integer>();
        List<Integer> amazonDe = new ArrayList<Integer>();
        List<Integer> amazonFr = new ArrayList<Integer>();

        // 按照每天进行分割 --- 销售额
        List<Float> allSalesM = new ArrayList<Float>();
        List<Float> amazonUkM = new ArrayList<Float>();
        List<Float> amazonDeM = new ArrayList<Float>();
        List<Float> amazonFrM = new ArrayList<Float>();


        // 从 from 时间开始, 按照每 24 小时进行一个时区进行划分, 将 OrderItem 划分到每个时间区间中去
        for(long begin = from.getTime(); begin <= to.getTime(); begin += TimeUnit.DAYS.toMillis(1)) {
            // 销量
            int all = 0;
            int auk = 0;
            int ade = 0;
            int afr = 0;

            // 销售额
            float allM = 0;
            float aukM = 0;
            float adeM = 0;
            float afrM = 0;

            for(OrderItem itm : orderItems) {
                // 由于使用 JPQL 查询的时候,添加了 State 的 where 限制不能够生效, 所以直接使用程序控制了
                if(itm.order.state == Orderr.S.CANCEL || itm.order.state == Orderr.S.REFUNDED || itm.order.state == Orderr.S.RETURNNEW)
                    continue;
                if(itm.createDate.getTime() > begin && itm.createDate.getTime() <= (begin + TimeUnit.DAYS.toMillis(1))) {
                    all += itm.quantity;
                    allM += itm.price;
                    switch(itm.selling.market) {
                        case AMAZON_UK:
                            auk += itm.quantity;
                            aukM += itm.price;
                            break;
                        case AMAZON_DE:
                            ade += itm.quantity;
                            adeM += itm.price;
                            break;
                        case AMAZON_FR:
                            afr += itm.quantity;
                            afrM += itm.price;
                            break;
                        case AMAZON_US:
                        case AMAZON_IT:
                        case AMAZON_ES:
                        case EBAY_UK:
                            break;
                    }
                }
            }
            // 每一个时间区间需要一组销量数据
            allSales.add(all);
            amazonUk.add(auk);
            amazonDe.add(ade);
            amazonFr.add(afr);

            // 每一个时间区间需要一组销售额数据
            allSalesM.add(allM);
            amazonUkM.add(aukM);
            amazonDeM.add(adeM);
            amazonFrM.add(afrM);
        }


        Map<String, Object> hightChartMap = new HashMap<String, Object>();
        hightChartMap.put("days", days);
        hightChartMap.put("series_all", allSales);
        hightChartMap.put("series_auk", amazonUk);
        hightChartMap.put("series_ade", amazonDe);
        hightChartMap.put("series_afr", amazonFr);

        hightChartMap.put("series_allM", allSalesM);
        hightChartMap.put("series_aukM", amazonUkM);
        hightChartMap.put("series_adeM", amazonDeM);
        hightChartMap.put("series_afrM", amazonFrM);

        hightChartMap.put("type", type);

        if(hightChartMap.size() > 0) Cache.add(cached_key, hightChartMap, "30mn");
        return hightChartMap;
    }

    /**
     * 判断这个 OrderItem 所属的产品是不是 Easyacc 的.
     * 不是判断产品的标题是否为 EasyAcc 而是判断这个产品是否为 EasyAcc 自己销售(不是跟的)
     *
     * @return
     */
    public boolean easyacc() {
        String title = this.productName.toLowerCase();
        if(StringUtils.startsWith(title, "easyacc")) return true;
        else if(StringUtils.startsWith(title, "nosson")) return true;
        else if(StringUtils.startsWith(title, "fencer")) return true;
        else if(StringUtils.startsWith(title, "saner")) return true;
        else return false;
    }

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
}
