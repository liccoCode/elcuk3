package controllers;

import com.alibaba.fastjson.JSON;
import helper.Dates;
import helper.Webs;
import models.market.Account;
import models.market.Feedback;
import models.market.OrderItem;
import models.market.Orderr;
import models.product.Whouse;
import models.view.Ret;
import play.Play;
import play.cache.Cache;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@With({GlobalExceptionHandler.class, Secure.class})
public class Application extends Controller {

    public static void index() {
        Map<String, Map<String, AtomicInteger>> odmaps = Orderr.frontPageOrderTable(9);
        // Feedback 信息
        Map<String, List<F.T3<Long, Long, Long>>> feedbacksOverView = Feedback.frontPageTable();
        List<Whouse> fbaWhouse = Whouse.findByType(Whouse.T.FBA);
        render(odmaps, fbaWhouse, feedbacksOverView);
    }

    //    @CacheFor(value = "40mn")
    public static void percent(String type, Date date, long aid) {
        renderJSON(JSON.toJSON(
                OrderItem.categoryPercent(
                        type,
                        Dates.morning(date),
                        Dates.night(date),
                        Account.<Account>findById(aid)))
        );
    }

    public static void clearCache() {
        Cache.delete(Orderr.FRONT_TABLE);
        Cache.delete(Feedback.FRONT_TABLE);
        renderJSON(new Ret());
    }

    public static void cc() {
        Cache.clear();
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

}