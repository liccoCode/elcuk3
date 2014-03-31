package query;

import helper.Caches;
import helper.J;
import models.product.Team;
import models.view.highchart.HighChart;
import models.view.highchart.Series;
import org.joda.time.DateTime;
import play.cache.Cache;

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
                deleteCache("profitrateline", year, teamobject);
                J.json(PmDashboardESQuery
                        .profitrateline("profitrateline", year, teamobject));
                /**
                 * 销售额曲线
                 */
                deleteCache("salefeeline", year, teamobject);
                J.json(PmDashboardESQuery
                        .salefeeline("salefeeline", year, teamobject));
                /**
                 * 销量曲线
                 */
                deleteCache("saleqtyline", year, teamobject);
                J.json(PmDashboardESQuery
                        .saleqtyline("saleqtyline", year, teamobject));
                /**
                 * 柱状
                 */
                deleteCache("salecolumn", year, teamobject);
                J.json(PmDashboardESQuery
                        .categoryColumn("salecolumn", year, teamobject));
                /**
                 * 饼状
                 */
                deleteCache("sale", year, teamobject);
                J.json(PmDashboardESQuery
                        .categoryPie("sale", year, teamobject));
                /**
                 * 饼状
                 */
                deleteCache("profit", year, teamobject);
                J.json(PmDashboardESQuery
                        .categoryPie("profit", year, teamobject));
                /**
                 * 饼状
                 */
                deleteCache("teamsale", year, teamobject);
                J.json(PmDashboardESQuery
                        .categoryPie("teamsale", year, teamobject));
                /**
                 * 饼状
                 */
                deleteCache("teamprofit", year, teamobject);
                J.json(PmDashboardESQuery
                        .categoryPie("teamprofit", year, teamobject));
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }


    public static void deleteCache(final String type, final int year, final Team team) {
        String key = Caches.Q.cacheKey(type, year, team.name);
        Cache.delete(key);
    }
}
