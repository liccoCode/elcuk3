package controllers;

import models.procure.FBACenter;
import models.procure.FBAShipment;
import models.view.post.FBAPost;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/29/12
 * Time: 11:38 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
public class FBAs extends Controller {
    @Before
    public static void centerIds() {
        renderArgs.put("centerIds", FBACenter.centerIds());
    }

    public static void index(FBAPost p) {
        if(p == null) p = new FBAPost(Arrays.asList(FBAShipment.S.RECEIVING, FBAShipment.S.CANCELLED));
        List<FBAShipment> fbas = p.query();
        render(fbas, p);
    }
}
