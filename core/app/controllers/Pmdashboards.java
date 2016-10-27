package controllers;

import controllers.api.SystemOperation;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.User;
import models.market.AmazonListingReview;
import models.product.Category;
import models.product.Team;
import models.view.Ret;
import models.view.dto.AbnormalDTO;
import models.view.highchart.HighChart;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.jobs.Job;
import play.modules.excel.RenderExcel;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;
import query.PmDashboardESQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-3-17
 * Time: 下午2:44
 * @deprecated
 */

@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Pmdashboards extends Controller {
    @Before(only = {"index"})
    public static void beforIndex() {
        Date from = DateTime.now().minusMonths(6).toDate();
        Date to = DateTime.now().toDate();
        renderArgs.put("from", from);
        renderArgs.put("to", to);
    }

    @Check("pmdashboards.index")
    public static void index() {
        int year = DateTime.now().getYear();
        User user = User.findByUserName(Secure.Security.connected());
        Set<Team> teams = user.teams;
        List<Category> cates = new ArrayList<>();
        for(Team team : teams) {
            List<Category> teamcates = team.getObjCategorys();
            if(teamcates != null) {
                cates.addAll(teamcates);
            }
        }
        long abnormalSize = AbnormalDTO.queryAbnormalDTOListSize(user);
        render(year, teams, cates, abnormalSize);
    }


    public static void percent(String type, int year, String team) {
        User user = User.findByUserName(Secure.Security.connected());
        Team teamobject = Team.find("teamId=?", team).first();
        if (teamobject==null) teamobject = new Team();

        if(!teamobject.existUser(user)) {
            Validation.addError("", "没有TEAM" + teamobject.name + "权限");
        }

        if(Validation.hasErrors())
            renderJSON(new Ret(false));
        String json = "";
        /**
         * 曲线
         */
        if(type.equals("profitrateline")) {
            /**
             * 月利润率
             */
            json = J.json(PmDashboardESQuery
                    .profitrateline(type, year, teamobject));
        } else if(type.equals("salefeeline")) {
            /**
             * 销售额曲线
             */
            json = J.json(PmDashboardESQuery
                    .salefeeline(type, year, teamobject));

        } else if(type.equals("saleqtyline")) {
            /**
             * 销量曲线
             */
            json = J.json(PmDashboardESQuery
                    .saleqtyline(type, year, teamobject));
        }
        /**
         * 柱状
         */
        else if(type.equals("salecolumn")) {
            json = J.json(PmDashboardESQuery
                    .categoryColumn(type, year, teamobject));
        }
        /**
         * 饼状
         */
        else {
            json = J.json(PmDashboardESQuery
                    .categoryPie(type, year, teamobject));
        }
        renderJSON(json);
    }

    /**
     * 销量异常
     */
    public static void salesQty(AbnormalDTO p) {
        try {
            if(p == null) p = new AbnormalDTO(AbnormalDTO.T.SALESQTY, 20);
            List<AbnormalDTO> dtos = p.query(User.findByUserName(Secure.Security.connected()));
            render("Pmdashboards/_salesQty.html", dtos, dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * 销售额异常
     * <p/>
     * <p/>
     * 销售额周期指的是：
     * 上上周六 到 上周五 的销售额 对比 上个周期的销售额
     */
    public static void salesAmount(AbnormalDTO p) {
        try {
            if(p == null) p = new AbnormalDTO(AbnormalDTO.T.SALESAMOUNT, 5);
            List<AbnormalDTO> dtos = p.query(User.findByUserName(Secure.Security.connected()));
            render("Pmdashboards/_salesAmount.html", dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * 利润率异常
     * <p/>
     * <p/>
     * 周期为：
     * 上上周六 到 上周五 的利润率 对比 上个周期的利润率
     */
    public static void salesProfit(AbnormalDTO p) {
        try {
            if(p == null) p = new AbnormalDTO(AbnormalDTO.T.SALESPROFIT, 5);
            List<AbnormalDTO> dtos = p.query(User.findByUserName(Secure.Security.connected()));
            render("Pmdashboards/_salesProfit.html", dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * review异常信息
     */
    public static void review(AbnormalDTO p) {
        try {
            if(p == null) p = new AbnormalDTO(AbnormalDTO.T.REVIEW);
            List<AbnormalDTO> dtos = p.query(User.findByUserName(Secure.Security.connected()));
            if(dtos.size() > 10) dtos = dtos.subList(1, 10);
            render("Pmdashboards/_review.html", dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }


    /**
     * 用户所有的 sku 的负评 review
     */
    public static void skuReviews(AbnormalDTO p) {
        if(p == null) p = new AbnormalDTO(AbnormalDTO.T.REVIEW);
        List<AbnormalDTO> dtos = p.query(User.findByUserName(Secure.Security.connected()));
        render(dtos);
    }

    /**
     * 加载某一个 Category 的全年每个月的销售额和已经完成的销售额
     */
    public static void ajaxCategorySalesAmount(String cateid, int year) {
        try {
            HighChart chart = PmDashboardESQuery.ajaxHighChartCategorySalesAmount(cateid, year);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 加载某一个 Category 的全年每个月的利润率和已经完成的利润率
     */
    public static void ajaxCategorySalesProfit(String cateid, int year) {
        try {
            HighChart chart = PmDashboardESQuery.ajaxHighChartCategorySalesProfit(cateid, year);
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat fileNameFormatter = new SimpleDateFormat("yyyyMMdd");
        request.format = "xls";
        renderArgs.put(RenderExcel.RA_FILENAME,
                String.format("%s-%s品线Review星级与中差评.xls", fileNameFormatter.format(from), fileNameFormatter.format(to)));
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
    public static void reviewRatingLine(final Date from, final Date to, final String category) {
        try {
            HighChart chart = await(new Job<HighChart>() {
                @Override
                public HighChart doJobWithResult() throws Exception {
                    return AmazonListingReview.reviewRatingLine(from, to, category);
                }
            }.now());
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * Review 中差评趋势图
     */
    public static void poorRatingLine(final Date from, final Date to, final String category) {
        try {
            HighChart chart = await(new Job<HighChart>() {
                @Override
                public HighChart doJobWithResult() throws Exception {
                    return AmazonListingReview.poorRatingLine(from, to, category);
                }
            }.now());
            renderJSON(J.json(chart));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }
}
