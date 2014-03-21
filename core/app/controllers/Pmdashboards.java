package controllers;

import helper.J;
import models.User;
import models.product.Category;
import models.product.Product;
import models.product.Team;
import models.view.Ret;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;
import services.MetricPmService;
import services.MetricProfitService;

import java.util.Date;
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
     * 异常信息
     */
    public static void abnormal() {
        User user = User.findByUserName(Secure.Security.connected());
        Set<Team> teams = user.teams;

        for(Team t : teams) {
            //获取每个Team的异常信息
            /**
             * 1 获取每个Team所拥有的全部的Product
             *
             * 2 SKU的当天销售额对比同期(过去四周当天平均值）突然下降，默认下降20%
             * 过去四周当天 将当前时间往后分别推四次每次一周得到同期当天的销售额除以4
             * 如果当天的销售额小于平均值 >=20% 则此 SKU 为销量异常 SKU
             *
             */
            for(Category c : t.categorys) {
                //获得所有的 SKU
                c.categorys(false);
                //查询出所有 SKU 当天的利润率
                for(Product p : c.products) {
                    MetricProfitService met = new MetricProfitService(new Date(), new Date(), null, p.sku, null);
                    Float nowSaleAmount = met.esSaleFee();

                }
            }
        }
    }
}
