package controllers;

import models.market.Orderr;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@With({Secure.class, GzipFilter.class})
public class Application extends Controller {

    public static void index() {
        Map<String, Map<String, AtomicInteger>> odmaps = Orderr.frontPageOrderTable(7);
        render(odmaps);
    }

}