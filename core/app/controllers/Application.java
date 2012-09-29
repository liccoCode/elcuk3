package controllers;

import com.alibaba.fastjson.JSON;
import helper.Dates;
import helper.Webs;
import models.market.Account;
import models.market.OrderItem;
import models.market.Orderr;
import models.product.Whouse;
import models.support.Ticket;
import models.view.Ret;
import org.joda.time.DateTime;
import play.Play;
import play.cache.Cache;
import play.cache.CacheFor;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
public class Application extends Controller {

    public static void index() {
        Map<String, Map<String, AtomicInteger>> odmaps = Orderr.frontPageOrderTable(9);
        Date now = new Date();
        Date yestorday = DateTime.now().minusDays(1).toDate();
        Date threeMonth = DateTime.now().minusMonths(3).toDate();
        Map<String, Map<String, Long>> ticketTable = Ticket.frontPageTable(now, now);
        Map<String, Map<String, Long>> yesterDayTicketTable = Ticket.frontPageTable(yestorday, yestorday);
        // 3 个月内的 Ticket 汇总
        Map<String, Long> ticketMap = Ticket.ticketTotalTable(threeMonth, new Date());
        List<Whouse> fbaWhouse = Whouse.findByType(Whouse.T.FBA);
        renderArgs.put("now", Dates.date2DateTime(now));
        render(odmaps, ticketTable, yesterDayTicketTable, ticketMap, fbaWhouse);
    }

    @CacheFor(value = "20mn")
    public static void categoryPercent(Date date, long aid) {
        renderJSON(JSON.toJSON(
                OrderItem.itemGroupByCategory(Dates.morning(date),
                        Dates.night(date),
                        Account.<Account>findById(aid)))
        );
    }

    public static void clearCache() {
        Cache.delete("home.page");
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

    public static void upload(File file) {

        System.out.println(file.length() / 1024 + " KB");
        renderJSON(new Ret());
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
    public static void amazonLogin(long id) throws IOException, ClassNotFoundException {
        if(Play.mode.isProd()) forbidden();

        Account acc = Account.findById(id);
        Webs.dev_login(acc);
        renderJSON(Account.cookieMap().get(Account.cookieKey(acc.id, acc.type)));
    }

}