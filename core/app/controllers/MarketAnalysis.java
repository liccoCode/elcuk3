package controllers;

import controllers.api.SystemOperation;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.market.AmazonListingReview;
import models.product.Category;
import models.view.Ret;
import models.view.highchart.HighChart;
import org.joda.time.DateTime;
import play.modules.excel.RenderExcel;
import play.mvc.Controller;
import play.mvc.With;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 市场分析控制器
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-1-5
 * Time: AM9:57
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class MarketAnalysis extends Controller {
    /**
     * 渠道(数据抓取任务)
     */
    public static void channels() {
        render();
    }

    /**
     * Listing 属性标签
     */
    public static void tags() {
        render();
    }

    /**
     * Review 星级与中差评率趋势
     */
    public static void reviewRecords() {
        List<String> categories = Category.categoryIds();
        Date from = DateTime.now().minusMonths(6).toDate();
        Date to = DateTime.now().toDate();
        render(from, to, categories);
    }

    /**
     * Review 星级与中差评率趋势导出
     */
    public static void exportReviewRecords(Date from, Date to, String category) {
        HighChart reviewRatingLine = AmazonListingReview.reviewRatingLine(from, to, category);
        HighChart poorRatingLine = AmazonListingReview.poorRatingLine(from, to, category);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("%s-%s品线Review星级与中差评.xls", formatter.format(from), formatter.format(to)));
        renderArgs.put(RenderExcel.RA_ASYNC, false);
        renderArgs.put("dateFormat", formatter);
        renderArgs.put("reviewRatingLine", reviewRatingLine);
        renderArgs.put("poorRatingLine", poorRatingLine);
        renderArgs.put("from", from);
        renderArgs.put("to", to);
        renderArgs.put("category", category);
        renderArgs.put("dates", Dates.getAllSunday(from, to));
        render();
    }

    /**
     * Review 星级趋势图
     */
    public static void reviewRatingLine(Date from, Date to, String category) {
        try {
            HighChart chart = AmazonListingReview.reviewRatingLine(from, to, category);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * Review 中差评趋势图
     */
    public static void poorRatingLine(Date from, Date to, String category) {
        try {
            HighChart chart = AmazonListingReview.poorRatingLine(from, to, category);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
