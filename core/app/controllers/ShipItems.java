package controllers;

import models.procure.ProcureUnit;
import models.view.post.ProcureUnitShipPost;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With({GlobalExceptionHandler.class, Secure.class})
public class ShipItems extends Controller {

    public static void index(ProcureUnitShipPost post) {
        if(post == null)
            post = new ProcureUnitShipPost();
        List<ProcureUnit> items = post.query();
        render(post, items);
    }
}
