package controllers;

import models.Server;
import play.data.validation.Error;
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
        try {
            s.save();
        } catch(Exception e) {
            renderJSON(new Error("Exception", e.getClass().getSimpleName() + "|" + e.getMessage(), new String[]{}));
        }
        renderJSON("{\"flag\":\"true\"}");
    }

}
