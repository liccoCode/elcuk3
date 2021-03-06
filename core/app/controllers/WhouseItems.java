package controllers;

import controllers.api.SystemOperation;
import models.view.post.WhouseItemPost;
import models.whouse.Whouse;
import models.whouse.WhouseItem;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: duan
 * Date: 4/12/16
 * Time: 5:34 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class WhouseItems extends Controller {
    @Before(only = {"index"})
    public static void setWhouses() {
        renderArgs.put("whouses", Whouse.selfWhouses());
    }

    @Check("whouseitems.index")
    public static void index(WhouseItemPost p) {
        if(p == null) p = new WhouseItemPost();
        List<WhouseItem> items = p.query();
        render(p, items);
    }
}
