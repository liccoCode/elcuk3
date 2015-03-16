package controllers;

import controllers.api.SystemOperation;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.market.OrderItem;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import models.view.highchart.HighChart;
import models.view.post.AnalyzePost;
import org.joda.time.DateTime;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.List;

/**
 * 数据分析页面的控制器
 * 备忘: 采购计划的 timeline 库 -> http://www.simile-widgets.org/timeline/
 * User: wyattpan
 * Date: 1/19/12
 * Time: 2:14 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
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

            HighChart chart = OrderItem.ajaxSkusMarketUnitOrder(p.val, p.market, p.type, p.from, p.to, p.ismoveing);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 自定义销售报表
     */
    @Check("analyzeskus.skusalesreport")
    public static void skuSalesReport() {
        Date from = Dates.startDayYear(DateTime.now().getYear());
        Date to = DateTime.now().toDate();
        List<String> products = Product.skus(true);
        renderArgs.put("products", J.json(products));
        render(from, to);
    }

    @Check("analyzeskus.skusalesreport")
    public static void processSkuSalesReport(Date from, Date to, String val) {
        try {
            List<F.T4<String, Long, Long, Float>> sales = OrderItem.querySalesBySkus(from, to, val);
            render(sales);
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
