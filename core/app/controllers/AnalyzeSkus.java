package controllers;

import helper.J;
import helper.Webs;
import models.market.*;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import models.view.dto.AnalyzeDTO;
import models.view.highchart.HighChart;
import models.view.post.AnalyzePost;
import models.view.post.TrafficRatePost;
import models.view.report.TrafficRate;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.Play;
import play.cache.CacheFor;
import play.jobs.Job;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.List;

/**
 * 数据分析页面的控制器
 * 备忘: 采购计划的 timeline 库 -> http://www.simile-widgets.org/timeline/
 * User: wyattpan
 * Date: 1/19/12
 * Time: 2:14 PM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class AnalyzeSkus extends Controller {

    @Check("analyzes.index")
    public static void index() {
        List<String> products = Product.skus(true);
        renderArgs.put("products", J.json(products));

        List<String> categoryIds = Category.categoryIds();
        AnalyzePost p = new AnalyzePost();
        DateTime now = DateTime.now();
        p.from = now.plusDays(-30).toDate();
        p.to = now.toDate();
        render(categoryIds, p);
    }


    /**
     * 加载指定 SKU 的时间段内的销量
     */
    public static void ajaxUnit(AnalyzePost p) {
        try {
            DateTime now = DateTime.now();
            p.from = now.plusDays(-30).toDate();
            p.to = now.toDate();
            HighChart chart = OrderItem.ajaxSkusUnitOrder(p.val, p.type, p.from, p.to);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }


    /**
     * 加载多个SKU 的market 的时间段内的销量
     */
    public static void ajaxUnitMarket(AnalyzePost p) {
        try {
            DateTime now = DateTime.now();
            p.from = now.plusDays(-30).toDate();
            p.to = now.toDate();

            HighChart chart = OrderItem.ajaxSkusMarketUnitOrder(p.val,p.market, p.type, p.from, p.to,p.ismoveing);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
