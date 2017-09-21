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
import models.whouse.Whouse;
import play.Play;
import play.cache.Cache;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Application extends Controller {

    public static void index() {
        if(Objects.equals("MengTop", OperatorConfig.getVal("brandname"))) {
            StockRecords.stockIndex(new StockPost());
        }
        DashBoard dashboard = Orderr.frontPageOrderTable(11);
        List<Whouse> fbaWhouse = Whouse.findByType(Whouse.T.FBA);
        AnalyzePost p = new AnalyzePost();
        render("Application/index_v3.html", dashboard, p);
    }

    public static void indexV3() {
        if(Objects.equals("MengTop", OperatorConfig.getVal("brandname"))) {
            StockRecords.stockIndex(new StockPost());
        }
        DashBoard dashboard = Orderr.frontPageOrderTable(11);
        render("Application/index_v3.html", dashboard);
    }


    public static void perDayOrderNum() {
        DashBoard dashboard = Orderr.frontPageOrderTable(11);
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
