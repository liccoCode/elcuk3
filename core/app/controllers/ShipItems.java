package controllers;

import models.procure.ProcureUnit;
import models.view.post.ProcureUnitShipPost;
import play.mvc.Controller;
import play.mvc.With;

import java.util.List;

@With({GlobalExceptionHandler.class, Secure.class})
public class ShipItems extends Controller {

    public static void index(ProcureUnitShipPost p) {
        if(p == null)
            p = new ProcureUnitShipPost();
        List<ProcureUnit> units = p.query();
        render(p, units);
    }
}
