package controllers;

import helper.J;
import helper.Webs;
import models.SaleTarget;
import models.User;
import models.product.Category;
import models.product.Team;
import models.view.Ret;
import models.view.dto.AbnormalDTO;
import models.view.highchart.HighChart;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;
import query.PmDashboardESQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: cary
 * Date: 14-3-17
 * Time: 下午2:44
 */

@With({GlobalExceptionHandler.class, Secure.class})
public class Pmdashboards extends Controller {

    @Check("pmdashboards.index")
    public static void index() {
        int year = DateTime.now().getYear();
        User user = User.findByUserName(Secure.Security.connected());
        Set<Team> teams = user.teams;
        List<Category> cates = new ArrayList<Category>();
        for(Team team : teams) {
            List<Category> teamcates = team.getObjCategorys();
            if(teamcates != null) {
                cates.addAll(teamcates);
            }
        }
        render(year, teams, cates);
    }


    public static void percent(String type, int year, String team) {
        User user = User.findByUserName(Secure.Security.connected());
        Team teamobject = Team.find("teamId=?", team).first();

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
            if(p == null) p = new AbnormalDTO(AbnormalDTO.T.SALESQTY);
            List<AbnormalDTO> dtos = p.query(User.findByUserName(Secure.Security.connected()));
            render("Pmdashboards/_salesQty.html", dtos, dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * 销售额异常
     *
     * <p/>
     * 销售额周期指的是：
     * 上上周六 到 上周五 的销售额 对比 上个周期的销售额
     */
    public static void salesAmount(AbnormalDTO p) {
        try {
            if(p == null) p = new AbnormalDTO(AbnormalDTO.T.SALESAMOUNT);
            List<AbnormalDTO> dtos = p.query(User.findByUserName(Secure.Security.connected()));
            render("Pmdashboards/_salesAmount.html", dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * 利润率异常
     *
     * <p/>
     * 周期为：
     * 上上周六 到 上周五 的利润率 对比 上个周期的利润率
     */
    public static void salesProfit(AbnormalDTO p) {
        try {
            if(p == null) p = new AbnormalDTO(AbnormalDTO.T.SALESPROFIT);
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
}
