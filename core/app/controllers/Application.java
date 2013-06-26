package controllers;

import helper.Dates;
import helper.J;
import helper.Webs;
import models.market.Account;
import models.market.Feedback;
import models.market.OrderItem;
import models.market.Orderr;
import models.product.Whouse;
import models.view.Ret;
import models.view.dto.DashBoard;
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

@With({GlobalExceptionHandler.class, Secure.class})
public class Application extends Controller {

    public static void index() {
        DashBoard dashborad = Orderr.frontPageOrderTable(11);
        // Feedback 信息
        List<Whouse> fbaWhouse = Whouse.findByType(Whouse.T.FBA);
        render(dashborad, fbaWhouse);
    }

    public static void percent(final String type, final Date date, final long aid) {
        String json = await(new Job<String>() {
            @Override
            public String doJobWithResult() throws Exception {
                return J.json(
                        OrderItem.categoryPercent(
                                type, Dates.morning(date), Dates.night(date),
                                Account.<Account>findById(aid))
                );
            }
        }.now());
        renderJSON(json);
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
            Webs.dev_login(acc);
        } catch(Exception e) {
            throw new FastRuntimeException(e);
        }
        renderJSON(Account.cookieMap().get(Account.cookieKey(acc.id, acc.type)));
    }

    public static void o() {
        renderJSON(Orderr.frontPageOrderTable(9));
    }

}