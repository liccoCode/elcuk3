package controllers;

import helper.Caches;
import models.Server;
import play.cache.Cache;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午1:58
 */
@With({Secure.class, GzipFilter.class})
public class Servers extends Controller {

    public static void index() {
        List<Server> sers = Server.all().fetch();
        render(sers);
    }

    public static void update(Server s) {
        if(!s.isPersistent()) renderJSON(new Error("Server.Id", "The Server is not persistent!", new String[]{}));
        validation.required(s.type);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        try {
            s.save();
            Cache.decr(String.format(Caches.SERVERS, s.type.toString()));
        } catch(Exception e) {
            renderJSON(new Error("Exception", e.getClass().getSimpleName() + "|" + e.getMessage(), new String[]{}));
        }
        renderJSON("{\"flag\":\"true\"}");
    }

}
