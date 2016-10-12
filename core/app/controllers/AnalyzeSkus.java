package controllers;

import controllers.api.SystemOperation;
import helper.Caches;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.market.M;
import models.market.OrderItem;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import models.view.dto.DailySalesReportsDTO;
import models.view.highchart.HighChart;
import models.view.post.AnalyzePost;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.jobs.Job;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
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
    @Before(only = {"index", "skuSalesReport", "skuMonthlyDailySalesReports"})
    public static void setupSkus() {
        renderArgs.put("products", J.json(Product.skus(true)));
    }

    @Check("analyzes.index")
    public static void index() {
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
     * @deprecated
     */
    @Check("analyzeskus.skusalesreport")
    public static void skuSalesReport() {
        Date from = Dates.startDayYear(DateTime.now().getYear());
        Date to = DateTime.now().toDate();
        render(from, to);
    }

    @Check("analyzeskus.skusalesreport")
    public static void processSkuSalesReport(Date from, Date to, String val) {
        try {
            List<F.T4<String, Long, Long, Double>> sales = OrderItem.querySalesBySkus(from, to, val);
            render(sales);
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * SKU 月度日均销量报表
     */
    @Check("analyzeskus.skumonthlydailysalesreports")
    public static void skuMonthlyDailySalesReports() {
        Date from = Dates.startDayYear(DateTime.now().getYear());
        Date to = DateTime.now().toDate();
        List<String> categories = Category.categoryIds();
        render(from, to, categories);
    }

    @Check("analyzeskus.skumonthlydailysalesreports")
    public static void processSkuMonthlyDailySalesReports(final Date from, final Date to, final M market,
                                                          final String category, final String val) {
        try {
            final int begin = new DateTime(from).getMonthOfYear();
            final int end = new DateTime(to).getMonthOfYear();
            if(from.getTime() > to.getTime() || begin > end) renderJSON(new Ret("开始时间必须小于结束时间且必须在同一年份内!"));

            String cacheKey = Caches.Q.cacheKey("SkuMonthlyDailySales", from, to, category, market, val);
            List<DailySalesReportsDTO> dtos = Cache.get(cacheKey, List.class);
            if(dtos == null || dtos.size() == 0) {
                new Job() {
                    @Override
                    public void doJob() throws Exception {
                        OrderItem.skuMonthlyDailySales(from, to, market, category, val);
                    }
                }.now();
                renderText("正在处理中...请稍后几分钟再来查看...");
            } else {
                List<Integer> months = new ArrayList<Integer>();
                for(int i = begin; i <= end; i++) months.add(i);
                render(months, dtos);
            }
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }
}
