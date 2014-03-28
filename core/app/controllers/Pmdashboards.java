package controllers;

import helper.J;
import models.User;
import models.market.AmazonListingReview;
import models.market.Listing;
import models.product.Category;
import models.product.Team;
import models.view.Ret;
import models.view.dto.AbnormalDTO;
import models.view.post.AbnormalPost;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.db.helper.SqlSelect;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;
import services.MetricPmService;

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
        render(year, teams);
    }


    public static void percent(String type, int year, String team) {
        Team teamobject = Team.find("teamId=?", team).first();
        if(Validation.hasErrors())
            renderJSON(new Ret(false));
        String json = "";
        if(type.equals("profitratecolumn")) {
            json = J.json(MetricPmService
                    .categoryLine(type, year, teamobject));
        } else if(type.equals("salecolumn")) {
            json = J.json(MetricPmService
                    .categoryColumn(type, year, teamobject));
        } else {
            json = J.json(MetricPmService
                    .categoryPie(type, year, teamobject));
        }
        renderJSON(json);
    }

    /**
     * 昨天销售额异常信息
     */
    public static void day1SalesAmount(AbnormalPost p) {
        try {
            if(p == null) p = new AbnormalPost(AbnormalPost.T.DAY1);
            List<AbnormalDTO> dtos = p.abnormal(User.findByUserName(Secure.Security.connected()));
            render("Pmdashboards/_day1SalesAmount.html", dtos, dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * 历史销售额异常信息
     * <p/>
     * 历史销售额指的是：
     * 上上周六 到 上周五 的销售额 对比 上个周期的销售额
     */
    public static void beforeSalesAmount(AbnormalPost p) {
        try {
            if(p == null) p = new AbnormalPost(AbnormalPost.T.BEFOREAMOUNT);
            List<AbnormalDTO> dtos = p.abnormal(User.findByUserName(Secure.Security.connected()));
            render("Pmdashboards/_beforeSalesAmount.html", dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * 历史利润率异常信息
     */
    public static void beforeSalesProfit(AbnormalPost p) {
        try {
            if(p == null) p = new AbnormalPost(AbnormalPost.T.BEFOREPROFIT);
            List<AbnormalDTO> dtos = p.abnormal(User.findByUserName(Secure.Security.connected()));
            render("Pmdashboards/_beforeSalesProfit.html", dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * review异常信息
     */
    public static void review(AbnormalPost p) {
        try {
            if(p == null) p = new AbnormalPost(AbnormalPost.T.REVIEW);
            List<AbnormalDTO> dtos = p.abnormal(User.findByUserName(Secure.Security.connected()));
            if(dtos.size() > 10) dtos = dtos.subList(1, 10);
            render("Pmdashboards/_review.html", dtos, p);
        } catch(FastRuntimeException e) {
            renderHtml("<p>" + e.getMessage() + "</p>");
        }
    }


    /**
     * 用户所有的 sku 的负评 review
     */
    public static void skuReviews(AbnormalPost p) {
        if(p == null) p = new AbnormalPost(AbnormalPost.T.REVIEW);
        List<AbnormalDTO> dtos = p.abnormal(User.findByUserName(Secure.Security.connected()));
        render(dtos);
    }
}
