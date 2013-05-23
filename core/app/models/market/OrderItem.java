package models.market;

import helper.*;
import models.product.Product;
import models.view.dto.HighChart;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import query.OrderItemQuery;
import query.vo.AnalyzeVO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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


    public void updateAttr(OrderItem noi) {
        if(noi.createDate != null) this.createDate = noi.createDate;
        if(noi.discountPrice != null) this.discountPrice = noi.discountPrice;
        if(noi.feesAmaount != null) this.feesAmaount = noi.feesAmaount;
        if(noi.memo != null) this.memo = noi.memo;
        if(noi.price != null) this.price = noi.price;
        if(noi.listingName != null) this.listingName = noi.listingName;
        if(noi.quantity != null) this.quantity = noi.quantity;
        if(noi.shippingPrice != null) this.shippingPrice = noi.shippingPrice;
        if(noi.product != null) this.product = noi.product;
        if(noi.selling != null) this.selling = noi.selling;
        if(noi.currency != null && this.currency != this.currency) this.currency = noi.currency;
        if(noi.usdCost != null) this.usdCost = noi.usdCost;
        this.save();
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

    /**
     * 根据 sku 或者 msku 从 OrderItem 中查询对应 Account 的 OrderItem
     * ps: 结果缓存 5mn (缓存是为了防止两次访问此方法, 此数据最终的缓存放置在了页面内容缓存)
     * 将时间语义转换为对应市场的, 例:
     * 查询 2013-01-01 这天的数据
     * ->
     * 查询 美国 2013-01-01 这天的数据; 系统中记录的是北京时间,所以会进行时间转换
     *
     * @param skuOrMskuOrCategory sku 或者 merchantSKU(msku) 还是 categoryId
     * @param type                sku/all/msku
     * @param acc
     * @param from
     * @param to
     * @return 返回需要用到 createDate, quantity, usdCost, market
     */
    @SuppressWarnings("unchecked")
    @Cached("5mn") //缓存是为了防止两次访问此方法, 此数据最终的缓存放置在了页面内容缓存
    public static List<AnalyzeVO> skuOrMskuAccountRelateOrderItem(
            final String skuOrMskuOrCategory, final String type, final Account acc, final Date from,
            final Date to) {
        String cacheKey = Caches.Q.cacheKey(skuOrMskuOrCategory, type, acc, from, to);
        List<AnalyzeVO> vos = Cache.get(cacheKey, List.class);
        if(vos != null) return vos;
        vos = new ArrayList<AnalyzeVO>();

        vos.addAll(Promises.forkJoin(new Promises.Callback<AnalyzeVO>() {
            @Override
            public List<AnalyzeVO> doJobWithResult(M m) {
                Date _from = m.withTimeZone(from).toDate();
                Date _to = m.withTimeZone(to).toDate();
                if("all".equalsIgnoreCase(skuOrMskuOrCategory))
                    return new OrderItemQuery().allNormalSaleOrderItem(_from, _to, m);
                else if(skuOrMskuOrCategory.matches("^\\d{2}$"))
                    return new OrderItemQuery().allNormalSaleOrderItem(_from, _to, m, skuOrMskuOrCategory);
                else if("sku".equalsIgnoreCase(type))
                    return new OrderItemQuery()
                            .skuNormalSaleOrderItem(skuOrMskuOrCategory, _from, _to, m);
                else if("sid".equalsIgnoreCase(type))
                    return new OrderItemQuery().mskuWithAccountNormalSaleOrderItem(
                            skuOrMskuOrCategory, acc == null ? null : acc.id, _from, _to, m);
                else
                    return new ArrayList<AnalyzeVO>();
            }

            @Override
            public String id() {
                return "OrderItem.skuOrMskuAccountRelateOrderItem";
            }
        }));
        if(vos.size() > 0)
            Cache.add(cacheKey, vos, "5mn");
        return vos;
    }

    /**
     * 销量的图形图表
     *
     * @param skuOrMsku
     * @param acc
     * @param type
     * @param from
     * @param to
     * @return
     */
    public static HighChart ajaxHighChartSales(String skuOrMsku,
                                               Account acc,
                                               String type,
                                               Date from, Date to) {
        // 做内部参数的容错
        DateTime _from = new DateTime(Dates.morning(from));
        DateTime _to = new DateTime(Dates.night(to)).plusDays(1); // "到" 的时间参数, 期望的是这一天的结束
        List<AnalyzeVO> vos = skuOrMskuAccountRelateOrderItem(skuOrMsku, type, acc,
                _from.toDate(), _to.toDate());

        HighChart lines = new HighChart().startAt(_from.getMillis());
        // 从开始时间起, 以 1 天的时间间隔去 group 数据, 没有的设置为 0
        DateTime travel = _from.plusDays(0); // copy 一个新的
        while(travel.getMillis() < _to.getMillis()) { // 开始计算每一天的数据
            String travelStr = travel.toString("yyyy-MM-dd");
            // 销售额
            float sale_all = 0;
            float sale_uk = 0;
            float sale_de = 0;
            float sale_fr = 0;
            float sale_us = 0;

            for(AnalyzeVO vo : vos) {
                DateTime marketLocalTime = new DateTime(vo.date, Dates.timeZone(vo.market));
                if(marketLocalTime.toString("yyyy-MM-dd").equals(travelStr)) {
                    try {
                        float usdCost = vo.usdCost == null ? 0 : vo.usdCost;
                        sale_all += usdCost;
                        if(vo.market == M.AMAZON_UK) sale_uk += usdCost;
                        else if(vo.market == M.AMAZON_DE) sale_de += usdCost;
                        else if(vo.market == M.AMAZON_FR) sale_fr += usdCost;
                        else if(vo.market == M.AMAZON_US) sale_us += usdCost;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    // 其他市场暂时先不统计
                }
            }
            // 当天所有市场的销售额数据
            lines.line("sale_all").add(sale_all);
            lines.line("sale_fr").add(sale_fr);
            lines.line("sale_uk").add(sale_uk);
            lines.line("sale_us").add(sale_us);
            lines.line("sale_de").add(sale_de);
            travel = travel.plusDays(1);
        }

        return lines;
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
    public static HighChart ajaxHighChartUnitOrder(String skuOrMsku,
                                                   Account acc,
                                                   String type,
                                                   Date from,
                                                   Date to) {
        // 做内部参数的容错
        DateTime _from = new DateTime(Dates.date2JDate(from));
        DateTime _to = new DateTime(Dates.date2JDate(to)).plusDays(1); // "到" 的时间参数, 期望的是这一天的结束
        /**
         * 加载出限定时间内的指定 Msku 的 OrderItem
         * 按照天过滤成销量数据
         * 组装成 HightChart 的格式
         */


        List<AnalyzeVO> vos = skuOrMskuAccountRelateOrderItem(skuOrMsku, type, acc,
                _from.toDate(), _to.toDate());

        HighChart lines = new HighChart().startAt(_from.getMillis());
        DateTime travel = _from.plusDays(0); // copy 一个新的
        while(travel.getMillis() < _to.getMillis()) { // 开始计算每一天的数据
            String travelStr = travel.toString("yyyy-MM-dd");
            // 销量
            float unit_all = 0;
            float unit_uk = 0;
            float unit_de = 0;
            float unit_fr = 0;
            float unit_us = 0;
            for(AnalyzeVO vo : vos) {
                /**
                 * 将北京时间换成对应市场的本地时间, 因为加载出了对应市场时间的数据后,
                 * 需要再将时间还原到对应市场, 然后进行市场当天数据的 group
                 */
                DateTime marketLocalTime = new DateTime(vo.date, Dates.timeZone(vo.market));
                if(marketLocalTime.toString("yyyy-MM-dd").equalsIgnoreCase(travelStr)) {
                    unit_all += vo.qty;
                    if(vo.market == M.AMAZON_UK) unit_uk += vo.qty;
                    else if(vo.market == M.AMAZON_DE) unit_de += vo.qty;
                    else if(vo.market == M.AMAZON_FR) unit_fr += vo.qty;
                    else if(vo.market == M.AMAZON_US) unit_us += vo.qty;
                    // 其他市场暂时先不统计
                }
            }
            // 当天所有市场的销售订单数据
            lines.line("unit_all").add(unit_all);
            lines.line("unit_fr").add(unit_fr);
            lines.line("unit_uk").add(unit_uk);
            lines.line("unit_us").add(unit_us);
            lines.line("unit_de").add(unit_de);
            travel = travel.plusDays(1);
        }
        return lines;
    }

    /**
     * 不同 Category 销量的百分比;
     *
     * @param type units/sales
     * @param from
     * @param to
     * @param acc  de/uk/us/all
     * @return
     */
    public static HighChart categoryPercent(String type, final Date from, final Date to,
                                            Account acc) {
        String key = Caches.Q.cacheKey(type, from, to, acc);
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;

        pieChart = new HighChart();
        List<AnalyzeVO> vos = new ArrayList<AnalyzeVO>();
        if(acc != null) {
            // 转换成为不同对应市场的时间
            vos = new OrderItemQuery().groupCategory(
                    acc.type.withTimeZone(from).toDate(),
                    acc.type.withTimeZone(to).toDate(),
                    acc.id);
        } else {
            vos.addAll(Promises.forkJoin(new Promises.Callback<AnalyzeVO>() {
                @Override
                public List<AnalyzeVO> doJobWithResult(M m) {
                    return new OrderItemQuery().groupCategory(
                            m.withTimeZone(from).toDate(),
                            m.withTimeZone(to).toDate(),
                            m);
                }

                @Override
                public String id() {
                    return "OrderItem.categoryPercent";
                }
            }));
        }
        for(AnalyzeVO vo : vos) {
            if(StringUtils.equals(type, "sales"))
                pieChart.pie(vo.sku, vo.usdCost);
            else
                pieChart.pie(vo.sku, vo.qty.floatValue());
        }
        Cache.add(key, pieChart, "40mn");
        return pieChart;
    }


    public static List<OrderItem> orderRelateItems(String orderId) {
        return OrderItem.find("order.orderId=?", orderId).fetch();
    }
}
