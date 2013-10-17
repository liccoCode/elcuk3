package models.market;

import helper.*;
import models.product.Product;
import models.view.highchart.AbstractSeries;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import query.OrderItemQuery;
import query.vo.AnalyzeVO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * 促销的名称
     */
    public String promotionIDs;

    /**
     * 这个商品所承担的运输费用
     */
    public Float shippingPrice;

    /**
     * GiftWrap 费用
     */
    public Float giftWrap;

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

    public void calUsdCose() {
        if(this.currency != null && this.price != null) {
            this.usdCost = this.currency
                    .toUSD(this.price - (this.discountPrice == null ? 0 : this.discountPrice));
        }
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
     * <pre>
     * 通过 OrderItem 计算指定的 skuOrMsku 在一个时间段内的销量情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     * HightChart 的使用 http://jsfiddle.net/kSkYN/6937/
     * </pre>
     *
     * @param val 需要查询的 all, categoryId, sku, sid
     * @param to  @return {series_size, days, series_n}
     */
    @Cached("8h")
    public static HighChart ajaxHighChartUnitOrder(final String val, final String type, Date from, Date to) {
        String cacked_key = Caches.Q.cacheKey("unit", val, type, from, to);
        HighChart lines = Cache.get(cacked_key, HighChart.class);
        if(lines != null) return lines;
        synchronized(cacked_key.intern()) { // 使用 cacked_key 在 String pool 中的对象
            lines = Cache.get(cacked_key, HighChart.class);
            if(lines != null) return lines;

            // 做内部参数的容错
            final Date _from = Dates.morning(from);
            final Date _to = Dates.night(to);

            final HighChart finalLines = new HighChart();
            Promises.forkJoin(new Promises.DBCallback<Map<M, List<AnalyzeVO>>>() {
                @Override
                public Map<M, List<AnalyzeVO>> doJobWithResult(M m) {
                    List<AnalyzeVO> lineVos = OrderItemQuery.getAnalyzeVOsFacade(m, val, type, _from, _to);
                    synchronized(finalLines) { // 避免 finalLines 内部因多线程并发修改数组的问题
                        for(AnalyzeVO vo : lineVos) {
                            finalLines.series("unit_all").add(vo.date, vo.qty.floatValue());
                            finalLines.series("unit_" + m.name().toLowerCase()).add(vo.date,
                                    vo.qty.floatValue());
                        }
                    }
                    return null;
                }

                @Override
                public String id() {
                    return "OrderItem.ajaxHighChartUnitOrder";
                }
            });

            for(AbstractSeries aline : finalLines.series) {
                ((Series.Line) aline).sort();
            }
            Cache.add(cacked_key, finalLines, "8h");
        }

        return Cache.get(cacked_key, HighChart.class);
    }

    /**
     * 不同 Category 销量的百分比;
     * TODO 取消销售额饼图
     *
     * @param type units/sales
     * @param from
     * @param to
     * @param acc  de/uk/us/all
     * @return
     */
    public static HighChart categoryPercent(String type, final Date from, final Date to, Account acc) {
        String key = Caches.Q.cacheKey(type, from, to, acc);
        HighChart pieChart = Cache.get(key, HighChart.class);
        if(pieChart != null) return pieChart;

        synchronized(key.intern()) {
            pieChart = Cache.get(key, HighChart.class);
            if(pieChart != null) return pieChart;

            pieChart = new HighChart(Series.PIE);
            List<AnalyzeVO> vos = new ArrayList<AnalyzeVO>();
            if(acc != null) {
                // 转换成为不同对应市场的时间
                vos = new OrderItemQuery().groupCategory(
                        acc.type.withTimeZone(from).toDate(),
                        acc.type.withTimeZone(to).toDate(),
                        acc.id);
            } else {
                Logger.info("OrderItem.categoryPercent begin...");
                long begin = System.currentTimeMillis();
                for(M m : Promises.MARKETS) {
                    vos.addAll(new OrderItemQuery()
                            .groupCategory(m.withTimeZone(from).toDate(), m.withTimeZone(to).toDate(), m));
                }
                Logger.info("OrderItem.categoryPercent passed %s ms...", System.currentTimeMillis() - begin);
            }
            for(AnalyzeVO vo : vos) {
                if(StringUtils.equals(type, "sales"))
                    pieChart.series("销售额").add(vo.sku, vo.usdCost);
                else
                    pieChart.series("销量").add(vo.sku, vo.qty.floatValue());
            }
            Cache.add(key, pieChart, "12h");
        }
        return pieChart;
    }
}
