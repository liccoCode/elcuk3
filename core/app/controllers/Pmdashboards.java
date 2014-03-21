package controllers;

import helper.Dates;
import helper.J;
import models.market.M;
import models.market.OrderItem;
import models.market.Orderr;
import models.product.Whouse;
import models.view.Ret;
import models.view.dto.DashBoard;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import models.product.Team;
import services.MetricPmService;

import java.util.List;
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
        if(type.equals("profitrateline") ||
                type.equals("salefeeline")
                ) {
            json = J.json(MetricPmService
                    .categoryLine(type, year, teamobject));
        }
        /**
         * 柱状
         */
        else if(type.equals("salecolumn")) {
            json = J.json(MetricPmService
                    .categoryColumn(type, year, teamobject));
        }
        /**
         * 饼状
         */
        else {
            json = J.json(MetricPmService
                    .categoryPie(type, year, teamobject));
        }
        renderJSON(json);
    }

}
