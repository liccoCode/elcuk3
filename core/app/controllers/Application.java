package controllers;

import com.alibaba.fastjson.JSON;
import helper.Dates;
import models.Ret;
import models.market.Account;
import models.market.OrderItem;
import models.market.Orderr;
import play.cache.Cache;
import play.cache.CacheFor;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@With({Secure.class, GzipFilter.class})
public class Application extends Controller {

    public static void index() {
        Map<String, Map<String, AtomicInteger>> odmaps = Orderr.frontPageOrderTable(9);
        render(odmaps);
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

    public static void index2() {
        render();
    }

    public static void upload(File file) {

        System.out.println(file.length() / 1024 + " KB");
        renderJSON(new Ret());
    }

}