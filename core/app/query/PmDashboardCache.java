package query;

import helper.J;
import models.product.Team;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-27
 * Time: 下午3:21
 */
public class PmDashboardCache {

    public void doCache() throws Exception {
        List<Team> teams = Team.findAll();
        int year = DateTime.now().getYear();
        for(Team teamobject : teams) {
            try {
                /**
                 * 月利润率
                 */
                J.json(PmDashboardESQuery
                        .profitrateline("profitrateline", year, teamobject));
                /**
                 * 销售额曲线
                 */
                J.json(PmDashboardESQuery
                        .salefeeline("salefeeline", year, teamobject));
                /**
                 * 销量曲线
                 */
                J.json(PmDashboardESQuery
                        .saleqtyline("saleqtyline", year, teamobject));
                /**
                 * 柱状
                 */J.json(PmDashboardESQuery
                        .categoryColumn("salecolumn", year, teamobject));
                /**
                 * 饼状
                 */J.json(PmDashboardESQuery
                        .categoryPie("sale", year, teamobject));
                /**
                 * 饼状
                 */J.json(PmDashboardESQuery
                        .categoryPie("profit", year, teamobject));
                /**
                 * 饼状
                 */J.json(PmDashboardESQuery
                        .categoryPie("teamsale", year, teamobject));
                /**
                 * 饼状
                 */J.json(PmDashboardESQuery
                        .categoryPie("teamprofit", year, teamobject));
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }
}
