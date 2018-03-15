package controllers;

import controllers.api.SystemOperation;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.OperatorConfig;
import models.market.*;
import models.view.Ret;
import models.view.dto.AnalyzeDTO;
import models.view.dto.DashBoard;
import models.view.highchart.HighChart;
import models.view.post.AnalyzePost;
import models.view.post.StockPost;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.IOException;
import java.util.*;

@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Application extends Controller {

    public static void index() {
        String brandname = OperatorConfig.getVal("brandname");
        if(Objects.equals("MengTop", brandname)) {
            StockRecords.stockIndex(new StockPost());
        }
/*        DashBoard dashboard = await(new Job<DashBoard>() {
            @Override
            public DashBoard doJobWithResult() throws Exception {
                return Orderr.frontPageOrderTable(11);
            }
        }.now());*/
        DashBoard dashboard = new DashBoard();
        Map<String, List<MarketRecord>> map = MarketRecord.queryYesterdayRecords();
        render(dashboard, brandname, map);
    }

    public static void perDayOrderNum() {
        Date now = Dates.yesterday();
        HighChart chart = DashBoard.todayOrderNum("", "sid", now, now);
        renderJSON(J.json(chart));
    }

    public static void topTenSkuByMarket(String market) {
        AnalyzePost p = new AnalyzePost();
        p.market = market;
        List<AnalyzeDTO> dtos = p.queryOrderByDayOne();
        if(dtos.size() >= 5) {
            dtos = dtos.subList(0, 5);
        }
        render(dtos, market);
    }

    public static void mapJsonReturn() {
        Date date = Dates.yesterday();
        List<MarketRecord> records = MarketRecord.find("createDate =? ", Dates.date2JDate(date)).fetch();
        List<Map<String, Object>> list = new ArrayList<>();
        records.forEach(record -> {
            Map<String, Object> data = new HashMap<>();
            data.put("code", record.marketEnum.country());
            data.put("value", record.totalOrders);
            data.put("name", record.marketEnum.label());
            list.add(data);
        });
        renderJSON(J.json(list));
    }

    public static void ajaxUnit(String sid) {
        AnalyzePost p = new AnalyzePost();
        if(StringUtils.isNotBlank(sid)) {
            Selling selling = Selling.findById(sid);
            p.val = selling.merchantSKU;
            p.market = selling.market.toString();
            p.type = "sid";
            HighChart chart = await(new Job<HighChart>() {
                @Override
                public HighChart doJobWithResult() throws Exception {
                    return OrderItem.ajaxHighChartUnitOrder(p.val, p.type, p.from, p.to);
                }
            }.now());
            String countryName = selling.market.countryName();
            chart.series.forEach(se -> se.visible = se.name.contains(countryName));
            renderJSON(J.json(chart));
        } else {
            p.state = "Active";
            p.val = "all";
            HighChart chart = await(new Job<HighChart>() {
                @Override
                public HighChart doJobWithResult() throws Exception {
                    return OrderItem.ajaxHighChartUnitOrder(p.val, p.type, p.from, p.to);
                }
            }.now());
            renderJSON(J.json(chart));
        }
    }

    public static void oldDashBoard() {
        index();
    }

    public static void clearCache() {
        Cache.delete(Orderr.FRONT_TABLE);
        Cache.delete(Feedback.FRONT_TABLE);
        renderJSON(new Ret());
    }

    public static void cc() {
        Cache.clear();
        JPA.em().getEntityManagerFactory().getCache().evictAll();
        renderJSON(new Ret());
    }

    public static void jc() {
        JPA.em().getEntityManagerFactory().getCache().evictAll();
        renderJSON(new Ret());
    }

    /**
     * 清除指定 key 的缓存
     *
     * @param key
     */
    public static void c(String key) {
        Cache.delete(key);
        renderJSON(new Ret(true, String.format("[%s] clear success", key)));
    }

    public static void timeline() {
        render();
    }

    /**
     * 测试使用的登陆代码
     *
     * @param id
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void amazonLogin(long id) {
        if(Play.mode.isProd()) forbidden();

        Account acc = Account.findById(id);
        try {
            Webs.devLogin(acc);
        } catch(Exception e) {
            throw new FastRuntimeException(e);
        }
        renderJSON(Account.cookieMap().get(Account.cookieKey(acc.uniqueName, acc.type)));
    }

    public static void o() {
        renderJSON(Orderr.frontPageOrderTable(9));
    }

    public static void systemTimeline() {
        String url = OperatorConfig.getVal("elcuk2url");
        render(url);
    }

}
