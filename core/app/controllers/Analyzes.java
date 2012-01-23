package controllers;

import models.PageInfo;
import models.market.OrderItem;
import models.market.Selling;
import play.data.binding.As;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据分析页面的控制器
 * User: wyattpan
 * Date: 1/19/12
 * Time: 2:14 PM
 */
@With({GzipFilter.class})
public class Analyzes extends Controller {
    public static void index() {
        List<Selling> sells = Selling.salesRankWithTime();
        long p = 1l;
        long s = 10;
        long count = sells.size();
        render(sells, p, s, count);
    }

    /**
     * Analyze 页面下部分的 Selling 信息
     */
    public static void index_sell(PageInfo<Selling> p) {
        List<Selling> sells = Selling.salesRankWithTime();
        List<Selling> items = new ArrayList<Selling>();
        if(sells.size() == 0) {
            p.items = items;
            render(p);
        }
        p.count = (long) sells.size();
        for(int i = p.begin; i < p.end; i++) {
            items.add(sells.get(i));
        }
        p.items = items;
        render(p);
    }

    public static void ajaxSales(String msku,
                                 @As("MM/dd/yyyy") Date from,
                                 @As("MM/dd/yyyy") Date to) {
        validation.required(msku);
        validation.required(from);
        validation.required(to);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        renderJSON(OrderItem.ajaxHighChartSales(msku, from, to));
    }

    /**
     * 加载指定 Selling 的时间段内的销量数据
     *
     * @param msku
     * @param from
     * @param to
     */
    public static void ajaxSells(String msku,
                                 @As("MM/dd/yyyy") Date from,
                                 @As("MM/dd/yyyy") Date to) {
        validation.required(msku);
        validation.required(from);
        validation.required(to);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        renderJSON(OrderItem.ajaxHighChartSelling(msku, from, to));
    }
}
