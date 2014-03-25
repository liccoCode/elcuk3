package controllers;

import helper.J;
import models.view.Ret;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import models.product.Team;
import query.PmDashboardESQuery;

import java.util.Set;

import models.User;

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

}
