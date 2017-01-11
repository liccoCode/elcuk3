package query;

import helper.Caches;
import helper.Webs;
import models.product.Team;
import org.joda.time.DateTime;
import play.Logger;
import play.cache.Cache;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 14-3-27
 * Time: 下午3:21
 */
public class PmDashboardCache {

    public static void doCache() {
        List<Team> teams = Team.findAll();
        int year = DateTime.now().getYear();
        for(Team teamobject : teams) {
            try {
                /**
                 * 销售额曲线
                 */
                deleteCache("salefeeline", year, teamobject);
                PmDashboardESQuery
                        .salefeeline("salefeeline", year, teamobject);
                /**
                 * 销量曲线
                 */
                deleteCache("saleqtyline", year, teamobject);
                PmDashboardESQuery.saleqtyline("saleqtyline", year, teamobject);
            } catch(Exception e) {
                Logger.error(Webs.S(e));
            }

        }
    }


    public static void doTargetCache(long id, int calyear) {
        Team teamobject = Team.findById(id);
        int year = DateTime.now().getYear();
        if(calyear != 0) {
            year = calyear;
        }
        try {
            /**
             * 月利润率
             */
            deleteCache("profitrateline", year, teamobject);
            PmDashboardESQuery
                    .profitrateline("profitrateline", year, teamobject);
            /**
             * 柱状
             */
            deleteCache("salecolumn", year, teamobject);
            PmDashboardESQuery
                    .categoryColumn("salecolumn", year, teamobject);
            /**
             * 饼状
             */
            deleteCache("sale", year, teamobject);
            PmDashboardESQuery
                    .categoryPie("sale", year, teamobject);
            /**
             * 饼状
             */
            deleteCache("profit", year, teamobject);
            PmDashboardESQuery
                    .categoryPie("profit", year, teamobject);
            /**
             * TEAM销售额所占比重
             */
            deleteCache("teamsale", year, teamobject);
            PmDashboardESQuery
                    .categoryPie("teamsale", year, teamobject);
            /**
             * TEAM利润所占比重
             */
            deleteCache("teamprofit", year, teamobject);
            PmDashboardESQuery
                    .categoryPie("teamprofit", year, teamobject);

            List<String> cates = teamobject.getStrCategorys();
            for(String cateid : cates) {
                /**产品线CATEGORY的销售额**/
                deleteCache("%s_%s_categoryinfo_salesamount", cateid, year);
                PmDashboardESQuery
                        .ajaxHighChartCategorySalesAmount(cateid, year);
                /**产品线CATEGORY的利润率**/
                deleteCache("%s_%s_categoryinfo_salesprofit", "", year);
                PmDashboardESQuery.ajaxHighChartCategorySalesProfit(cateid, year);
            }
        } catch(Exception e) {
            Logger.error(Webs.S(e));
        }
    }


    /**
     * 删除PM首页的cache
     *
     * @param type
     * @param year
     * @param team
     */
    public static void deleteCache(final String type, final int year, final Team team) {
        String key = Caches.Q.cacheKey(type, year, team.id);
        Cache.delete(key);
    }

    public static void deleteCache(String keyname, String categoryId, int year) {
        String cacked_key = String.format(keyname, year, categoryId);
        Cache.delete(cacked_key);
    }
}
