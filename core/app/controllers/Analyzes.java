package controllers;

import models.market.OrderItem;
import models.market.Selling;
import play.data.binding.As;
import play.data.validation.Validation;
import play.mvc.Controller;

import java.util.Date;
import java.util.List;

/**
 * 数据分析页面的控制器
 * User: wyattpan
 * Date: 1/19/12
 * Time: 2:14 PM
 */
public class Analyzes extends Controller {
    public static void index() {
        List<Selling> sells = Selling.salesRankWithTime();
        long p = 1l;
        long s = 10;
        long count = sells.size();
        render(sells, p, s, count);
    }

    /**
     * 加载指定 Selling 的时间段内的销量数据
     *
     * @param msku
     * @param from
     * @param to
     */
    public static void ajaxLine(String msku,
                                @As("MM/dd/yyyy") Date from,
                                @As("MM/dd/yyyy") Date to) {
        validation.required(msku);
        validation.required(from);
        validation.required(to);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        renderJSON(OrderItem.ajaxHighChartSelling(msku, from, to));
    }
}
