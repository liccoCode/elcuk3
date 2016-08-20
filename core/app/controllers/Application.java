package controllers;

import controllers.api.SystemOperation;
import helper.Dates;
import helper.J;
import helper.Webs;
import models.User;
import models.market.*;
import models.view.Ret;
import models.view.dto.DashBoard;
import models.whouse.Whouse;
import org.joda.time.DateTime;
import play.Play;
import play.cache.Cache;
import play.data.validation.Validation;
import play.db.jpa.JPA;
import play.jobs.Job;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Application extends Controller {

    public static void index() {
        //如果是有PM首页权限则跳转到PM首页
        User user = Login.current();
        DashBoard dashborad = await(new Job<DashBoard>() {
            @Override
            public DashBoard doJobWithResult() throws Exception {
                return Orderr.frontPageOrderTable(11);
            }
        }.now());
        // Feedback 信息
        List<Whouse> fbaWhouse = Whouse.findByType(Whouse.T.FBA);
        render(dashborad, fbaWhouse);
    }

    public static void oldDashBoard() {
        DashBoard dashborad = await(new Job<DashBoard>() {
            @Override
            public DashBoard doJobWithResult() throws Exception {
                return Orderr.frontPageOrderTable(11);
            }
        }.now());
        List<Whouse> fbaWhouse = Whouse.findByType(Whouse.T.FBA);
        render("Application/index.html", dashborad, fbaWhouse);
    }

    public static void percent(String type, Date date, String m) {
        M market = M.val(m);
        if(market == null) Validation.addError("", "市场填写错误");
        if(Validation.hasErrors())
            renderJSON(new Ret(false));
        String json = J.json(OrderItem
                .categoryPie(type, Dates.morning(date),
                        Dates.morning(new DateTime(date).plusDays(1).toDate()), market));
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
        renderJSON(Account.cookieMap().get(Account.cookieKey(acc.uniqueName, acc.type)));
    }

    public static void o() {
        renderJSON(Orderr.frontPageOrderTable(9));
    }

}