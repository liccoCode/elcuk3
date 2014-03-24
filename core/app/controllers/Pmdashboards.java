package controllers;

import helper.J;
import jobs.PmDashboard.AbnormalFetchJob;
import models.User;
import models.product.Category;
import models.product.Product;
import models.product.Team;
import models.view.Ret;
import models.view.dto.AbnormalDTO;
import org.joda.time.DateTime;
import play.cache.Cache;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;
import services.MetricPmService;
import services.MetricProfitService;

import java.util.Date;
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
    public static void day1SalesAmount() {
        try {
            List<AbnormalDTO> dtos = Cache.get(AbnormalFetchJob.AbnormalDTO_DAY1SALEASMOUNT_CACHE, List.class);
            if(dtos == null) {
                new AbnormalFetchJob().now();
                throw new FastRuntimeException("正在后台计算中, 请稍后在尝试");
            }
            render("Pmdashboards/day1.html", dtos);
        } catch(FastRuntimeException e) {
            renderHtml("<h3>" + e.getMessage() + "</h3>");
        }
    }
}
