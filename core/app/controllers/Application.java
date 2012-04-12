package controllers;

import models.Ret;
import models.market.Orderr;
import play.cache.Cache;
import play.cache.CacheFor;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@With({Secure.class, GzipFilter.class})
public class Application extends Controller {

    @CacheFor(value = "2h", id = "home.page")
    public static void index() {
        Map<String, Map<String, AtomicInteger>> odmaps = Orderr.frontPageOrderTable(7);
        render(odmaps);
    }

    public static void clearCache() {
        Cache.delete("home.page");
        renderJSON(new Ret());
    }

    public static void cc() {
        Cache.clear();
        renderJSON(new Ret());
    }

}