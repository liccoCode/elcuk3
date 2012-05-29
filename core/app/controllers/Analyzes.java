package controllers;

import com.alibaba.fastjson.JSON;
import helper.Webs;
import models.PageInfo;
import models.Ret;
import models.market.*;
import org.joda.time.DateTime;
import play.data.binding.As;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据分析页面的控制器
 * User: wyattpan
 * Date: 1/19/12
 * Time: 2:14 PM
 */
@With({Secure.class, GzipFilter.class})
public class Analyzes extends Controller {
    public static void index() {
        render();
    }

    /**
     * Analyze 页面下部分的 Selling 信息
     */
    public static void index_msku(PageInfo<Selling> p) {
        List<Selling> sells = Selling.salesRankWithTime(1);
        p.items = PageInfo.fixItemSize(sells, p);
        render(p);
    }

    public static void index_sku(PageInfo<Selling> p) {
        List<Selling> sells = Selling.salesRankWithTime(-1);
        p.items = PageInfo.fixItemSize(sells, p);
        render(p);
    }

    /**
     * 加载指定 Selling 的时间段内的销量与销售额数据
     *
     * @param msku
     * @param from
     * @param to
     */
    @Check("manager")
    public static void ajaxSells(String msku,
                                 String type,
                                 Account acc,
                                 @As("MM/dd/yyyy") Date from,
                                 @As("MM/dd/yyyy") Date to) {
        validation.required(msku);
        validation.required(from);
        validation.required(to);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        if(!acc.isPersistent()) acc = null;
        renderJSON(OrderItem.ajaxHighChartSelling(msku, acc, type, from, to));
    }

    /**
     * 查看某一个 Selling 在一段时间内的 PageView, Session 数量
     */
    public static void ajaxSellingRecord(String msku,
                                         @As("MM/dd/yyyy") Date from,
                                         @As("MM/dd/yyyy") Date to) {
        try {
            renderJSON(JSON.toJSONString(SellingRecord.ajaxHighChartPVAndSS(msku, from, to)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 给出某一天订单销量的时间区间饼图
     *
     * @param msku
     */
    public static void pie(String msku,
                           @As("MM/dd/yyyy") Date date) {
        Map<String, AtomicInteger> dataMap = Orderr.orderPieChart(msku, date);
        List<String> datax = new ArrayList<String>();
        for(String key : dataMap.keySet()) {
            datax.add("'" + new DateTime(Long.parseLong(key)).toString("HH:mm:ss") + "'");
        }
        List<AtomicInteger> datay = new ArrayList<AtomicInteger>(dataMap.values());
        render(datax, datay, date);
    }
}
